package com.flowtech.eventhub_spring_backend.entity;

import java.time.LocalDateTime;

import com.flowtech.eventhub_spring_backend.enums.RefundStatus;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name="refund_requests")
@Entity
public class RefundRequest {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Integer refundId;
	
	// One booking can have only one refund request
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

    @Column(nullable = false)
    private Double refundAmount;

    @Column(length = 1000)
    private String reason;

    @Column(length = 1000)
    private String rejectionReason;

    private LocalDateTime requestReceivedAt;

    private LocalDateTime organiserNotifiedAt;

    private LocalDateTime reviewedAt;

    private LocalDateTime refundInitiatedAt;

    private LocalDateTime refundCompletedAt;

    private String paymentRefundReference;
	
}
