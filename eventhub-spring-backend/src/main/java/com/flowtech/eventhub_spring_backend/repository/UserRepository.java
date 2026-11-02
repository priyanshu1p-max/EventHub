package com.flowtech.eventhub_spring_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.flowtech.eventhub_spring_backend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{

	Optional<User> findByEmail(String email);
	
	boolean existsByEmail(String email);
	
	boolean existsByPhone(Long phone);
	
	// NEW: Fetch all users having the ORGANISER role
	List<User> findByRole_Name(String roleName);
}
