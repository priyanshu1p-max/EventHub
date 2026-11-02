package com.flowtech.eventhub_spring_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.flowtech.eventhub_spring_backend.entity.Booking;

import jakarta.persistence.LockModeType;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

	// Find a booking using its booking ID
	Optional<Booking> findByBookingId(int bookingId);
	
	// find all bookings belonging to a particular customer,s email
	List<Booking> findByUser_EmailOrderByBookingDateTimeDesc(String email);
	
	// Find a booking by ID that belongs to a particular customer
	Optional<Booking> findByBookingIdAndUser_Email(int bookingId, String email);
					// This is useful for cancellation: it finds a booking only when both the
					// booking ID and customer email match. That helps prevent one customer from
					// canceling another customer's booking
	
	
	// lock the booking while canceling it
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT b FROM Booking b WHERE b.bookingId =:bookingId AND b.user.email =:email")   
					//query is used so that one customer cannot cancel another customer booking as it checks both bookingId and email
	Optional<Booking> findBookingIdForCancellation(@Param("bookingId") int bookingId,
											@Param("email") String email);
	
	
	// NEW: Find all bookings for events created by a particular organiser
	List<Booking> findByEvent_User_EmailOrderByBookingDateTimeDesc(String email);

	// Find booking using Razorpay order ID
	Optional<Booking> findByRazorpayOrderId(String razorpayOrderId);

	// Find booking using Razorpay payment ID
	Optional<Booking> findByRazorpayPaymentId(String razorpayPaymentId);

	// Lock booking while processing payment
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
	       SELECT b FROM Booking b
	       WHERE b.bookingId = :bookingId
	       """)
	Optional<Booking> findBookingForPayment(
	        @Param("bookingId") Integer bookingId);
	
}
