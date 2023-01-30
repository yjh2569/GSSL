package com.drdoc.BackEnd.api.service;

import java.util.List;

import com.drdoc.BackEnd.api.domain.dto.PetDetailDto;
import com.drdoc.BackEnd.api.domain.dto.PetKindListDto;
import com.drdoc.BackEnd.api.domain.dto.PetListDto;
import com.drdoc.BackEnd.api.domain.dto.PetModifyRequestDto;
import com.drdoc.BackEnd.api.domain.dto.PetRegisterRequestDto;

public interface PetService {
	void registerPet(String userId, PetRegisterRequestDto petRegisterRequestDto);
	void modifyPet(String userId, int petId, PetModifyRequestDto petModifyRequestDto);
	String getPetImage(int petId);
	void deletePet(String userId, int petId);
	List<PetListDto> getPetList(String userId);
	PetDetailDto getPetDetail(int petId);
	List<PetKindListDto> getPetKindList();
	PetKindListDto getPetKind(int kindId);



}
