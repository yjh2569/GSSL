package com.drdoc.BackEnd.api.service;

import java.util.List;

import com.drdoc.BackEnd.api.domain.dto.PetDetailDto;
import com.drdoc.BackEnd.api.domain.dto.PetKindListDto;
import com.drdoc.BackEnd.api.domain.dto.PetListDto;
import com.drdoc.BackEnd.api.domain.dto.PetModifyRequestDto;
import com.drdoc.BackEnd.api.domain.dto.PetRegisterRequestDto;

public interface PetService {
	void registerPet(PetRegisterRequestDto petRegisterRequestDto);
	void modifyPet(int petId, PetModifyRequestDto petModifyRequestDto);
	String getPetImage(int petId);
	void deletePet(int petId);
	List<PetListDto> getPetList();
	PetDetailDto getPetDetail(int petId);
	List<PetKindListDto> getPetKindList();
	PetKindListDto getPetKind(int kindId);



}
