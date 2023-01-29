package com.drdoc.BackEnd.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.drdoc.BackEnd.api.domain.Pet;

@Repository
public interface PetRepository extends JpaRepository<Pet, Integer> {
	 List<Pet> findAllByUserId(int userId);
}
