package com.flowtech.eventhub_spring_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flowtech.eventhub_spring_backend.entity.RefundRequest;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Integer> {

	// Find a refund using its booking ID
    Optional<RefundRequest> findByBooking_BookingId(Integer bookingId);
}
