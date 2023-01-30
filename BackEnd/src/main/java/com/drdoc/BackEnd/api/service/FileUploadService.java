package com.drdoc.BackEnd.api.service;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
	String uploadFile(MultipartFile file) throws FileUploadException;
	String modifyFile(String currentFilePath, MultipartFile file) throws FileUploadException;
	void deleteFile(String currentFilePath);
}
