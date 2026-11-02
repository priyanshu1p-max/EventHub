package com.flowtech.eventhub_spring_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.flowtech.eventhub_spring_backend.entity.Event;
import com.flowtech.eventhub_spring_backend.enums.EventVerification;

import jakarta.persistence.LockModeType;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
	List<Event> findByStatus(EventVerification status);
	
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)     // if two people try to book at same time so lock makes second transaction wait until first finishes
	@Query("SELECT e FROM Event e WHERE e.id= :eventId")
	Optional<Event> findByIdForUpdate(@Param ("eventId") Integer eventId);

	//Find events whose associated User.email matches the supplied email
	List<Event> findByUser_EmailOrderByCreatedAtDesc(String email);
	
	
	@Query("""
		    SELECT e FROM Event e
		    WHERE e.status = :status
		    AND (
		        :keyword IS NULL
		        OR :keyword = ''
		        OR LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
		        OR LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%'))
		    )
		    """)
		Page<Event> searchEvents(
		        @Param("status") EventVerification status,
		        @Param("keyword") String keyword,
		        Pageable pageable
		);
	
	// NEW: Find approved events within a date range and budget
	List<Event> findByStatusAndEventDateTimeBetweenAndTicketPriceLessThanEqual(
	        EventVerification status,
	        LocalDateTime startDate,
	        LocalDateTime endDate,
	        double budget
	);
	
	List<Event> findByStatusAndEventDateTimeBetweenAndTicketPriceLessThanEqualOrderByEventDateTimeAsc(
	        EventVerification status,
	        LocalDateTime startDateTime,
	        LocalDateTime endDateTime,
	        double budget
	);
	
}
