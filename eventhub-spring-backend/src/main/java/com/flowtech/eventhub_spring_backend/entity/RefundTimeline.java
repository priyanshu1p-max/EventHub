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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="refund_timeline")
public class RefundTimeline {

	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Integer timelineId;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "refund_id", nullable = false)
	    private RefundRequest refundRequest;

	    @Enumerated(EnumType.STRING)
	    @Column(nullable = false)
	    private RefundStatus status;

	    @Column(length = 1000)
	    private String message;

	    @Column(nullable = false)
	    private LocalDateTime createdAt;

	    @PrePersist
	    public void beforeInsert() {
	        if (createdAt == null) {
	            createdAt = LocalDateTime.now();
	        }
	    }
}
