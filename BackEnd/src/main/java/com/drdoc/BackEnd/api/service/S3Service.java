package com.drdoc.BackEnd.api.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.NoArgsConstructor;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@NoArgsConstructor
public class S3Service implements FileUploadService {
    private AmazonS3 s3Client;
    public static final String CLOUD_FRONT_DOMAIN_NAME ="a204drdoc.s3.ap-northeast-2.amazonaws.com";
    private static long FILE_SIZE_LIMIT = 10*1024*1024; // 10MB 
    
    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(this.region)
                .build();
    }
    
    @Override
    public String uploadFile(MultipartFile file) throws FileUploadException {
    	return modifyFile("", file);
    }
    
    @Override
    public String modifyFile(String currentFilePath, MultipartFile file) throws FileUploadException {
    	if (file == null) return "";
    	if (file.getSize() >= FILE_SIZE_LIMIT) throw new FileSizeLimitExceededException("이미지 크기 제한은 10MB 입니다.", file.getSize(), FILE_SIZE_LIMIT);
    	String originFile = file.getOriginalFilename();
		String originFileExtension = originFile.substring(originFile.lastIndexOf("."));
		if (!originFileExtension.equalsIgnoreCase(".jpg") && !originFileExtension.equalsIgnoreCase(".png")
				&& !originFileExtension.equalsIgnoreCase(".jpeg")) {
			throw new FileUploadException("jpg, jpeg, png의 이미지 파일만 업로드해주세요.");
		}
		try {
			return upload(currentFilePath, file);
		} catch (IOException e) {
			throw new FileUploadException("파일 업로드에 실패했습니다.");
		}
    }
    
    public String upload(String currentFilePath, MultipartFile file) throws IOException {
        // 고유한 key 값을 갖기위해 현재 시간을 prefix로 붙여줌
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = date.format(new Date())+"-"+file.getOriginalFilename();

        // key가 존재하면 기존 파일은 삭제
        deleteFile(currentFilePath);

        // 파일 업로드
        s3Client.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), null)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        return fileName;
    }
    
    @Override
    public void deleteFile(String currentFilePath) {
        // key가 존재하면 기존 파일은 삭제
        if ("".equals(currentFilePath) == false && currentFilePath != null) {
            boolean isExistObject = s3Client.doesObjectExist(bucket, currentFilePath);

            if (isExistObject == true) {
                s3Client.deleteObject(bucket, currentFilePath);
            }
        }
    }
}
