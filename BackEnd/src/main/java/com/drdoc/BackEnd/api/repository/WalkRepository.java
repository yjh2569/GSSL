package com.drdoc.BackEnd.api.repository;


import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.drdoc.BackEnd.api.domain.User;
import com.drdoc.BackEnd.api.domain.Walk;


@Repository
public interface WalkRepository extends JpaRepository<Walk, Integer> {
	List<Walk> findByUser(User user, Sort sort);
}