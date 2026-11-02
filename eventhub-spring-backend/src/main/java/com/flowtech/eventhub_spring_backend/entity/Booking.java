
package com.flowtech.eventhub_spring_backend.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int bookingId;

	private int quantity;

	private double totalPrice;

	private String paymentStatus;

	private LocalDateTime paymentDateTime;

	private LocalDateTime eventDateTime;

	private String bookingStatus;

	@CreationTimestamp
	private LocalDateTime bookingDateTime;

	// Existing relationships
	@ManyToOne
	@JoinColumn(name = "event_id")
	private Event event;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	// NEW: Razorpay payment details
	@Column(unique = true)
	private String razorpayOrderId;

	@Column(unique = true)
	private String razorpayPaymentId;

	// Store only if needed for audit; never expose the signature in API responses.
	@Column(length = 500)
	private String razorpayPaymentSignature;

	// NEW: Payment expiry for releasing unpaid ticket reservations
	private LocalDateTime paymentExpiresAt;
}
