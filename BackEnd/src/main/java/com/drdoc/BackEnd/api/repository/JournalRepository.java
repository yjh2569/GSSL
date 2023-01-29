package com.drdoc.BackEnd.api.repository;


import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.drdoc.BackEnd.api.domain.Journal;


@Repository
public interface JournalRepository extends JpaRepository<Journal, Integer> {
    List<Journal> findByUserId(int userId, Sort sort);
    void deleteByPetId(int petId);
}