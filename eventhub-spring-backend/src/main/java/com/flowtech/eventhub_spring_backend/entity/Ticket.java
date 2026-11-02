package com.flowtech.eventhub_spring_backend.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.flowtech.eventhub_spring_backend.enums.TicketStatus;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(name="tickets",uniqueConstraints = 
{@UniqueConstraint(name="uk_ticket_qr_token_hash", columnNames="qr_token_hash")})
public class Ticket {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ticketId;

    // Every ticket belongs to a booking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Every ticket belongs to an event
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // Store only the hash, never the original QR token
    @Column(name = "qr_token_hash", nullable = false, unique = true, length = 64)
    private String qrTokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus ticketStatus = TicketStatus.ACTIVE;

    private LocalDateTime checkedInAt;

    // User ID of the organiser/staff member who checked in the ticket
    private Integer checkedInBy;

    @CreationTimestamp
    private LocalDateTime createdAt;
}


//The QR code will contain a randomly generated secret token.
//We store only its SHA-256 hash in MySQL. When an organiser scans the QR code,
//the backend hashes the scanned token and searches for that hash.
//This way, someone who only gets database access cannot directly copy valid QR tokens from the ticket table.
