package com.flowtech.eventhub_spring_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.flowtech.eventhub_spring_backend.entity.CustomerPreference;

@Repository
public interface CustomerPreferenceRepository extends JpaRepository<CustomerPreference, Integer>{

	// Find preferences of the logged-in customer
    Optional<CustomerPreference> findByUser_Email(String email);
    
}
