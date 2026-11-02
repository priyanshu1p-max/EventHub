package com.flowtech.eventhub_spring_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.flowtech.eventhub_spring_backend.entity.SavedEvent;

@Repository
public interface SavedEventRepository extends JpaRepository<SavedEvent, Integer>{

	 // Get all saved events of a customer
    List<SavedEvent> findByUser_EmailOrderBySavedAtDesc(String email);

    // Check whether customer already saved an event
    boolean existsByUser_EmailAndEvent_Id(String email, Integer eventId);

    // Find a saved event belonging to a particular customer
    Optional<SavedEvent> findByUser_EmailAndEvent_Id(String email, Integer eventId);
}
