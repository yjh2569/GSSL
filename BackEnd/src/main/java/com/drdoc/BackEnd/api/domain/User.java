package com.drdoc.BackEnd.api.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;

import com.drdoc.BackEnd.api.domain.dto.UserModifyRequestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Entity
@Builder
public class User {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "memberId", nullable = false, unique = true, length = 20)
    private String memberId;
    
    @Column(name = "password", nullable = false, length = 256)
    private String password;
    
    @Column(name = "nickname", nullable = false, unique = true, length = 10)
    private String nickname;
    
    @Column(name = "gender", nullable = false, length = 1)
    private String gender;
    
    @Column(name = "phone", nullable = false, length = 11)
    private String phone;
    
    @Email
    @Column(name = "email", nullable = false, length = 50)
    private String email;
    
    @Column(name = "profilePic", nullable = true, length = 256)
    private String profilePic;
    
    @Column(name = "introduce", nullable = true, length = 50)
    private String introduce;
    
    @Column(name = "isLeft", nullable = false, columnDefinition = "boolean default false")
    private boolean isLeft;
    
    @Column(name = "pet_id", nullable = true, columnDefinition = "int default 0")
    private int petId;
    
    public void modify(UserModifyRequestDto requestDto) {
		this.memberId = requestDto.getMember_id();
		this.password = requestDto.getPassword();
		this.email = requestDto.getEmail();
		this.gender = requestDto.getGender();
		this.introduce = requestDto.getIntroduce();
		this.nickname = requestDto.getNickname();
		this.petId = requestDto.getPet_id();
		this.phone = requestDto.getPhone();
		this.profilePic = requestDto.getProfile_pic();
	}
    
    public void modifyPetId(int petId) {
    	this.petId = petId;
    }
    
    public void quit() {
    	this.isLeft = true;
    }
}