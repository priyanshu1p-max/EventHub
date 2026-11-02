package com.flowtech.eventhub_spring_backend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EventRequest {

    @NotBlank(message = "Event name is required")
    @Size(max = 100, message = "Event name cannot exceed 100 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotBlank(message = "Event location is required")
    private String location;

    // NEW: Event category for recommendations
    @NotBlank(message = "Event category is required")
    private String category;

    // NEW: Coordinates for distance calculation
    private Double latitude;
    private Double longitude;

    @NotNull(message = "Event date and time are required")
    @Future(message = "Event date and time must be in the future")
    private LocalDateTime eventDateTime;

    @PositiveOrZero(message = "Ticket price cannot be negative")
    private double ticketPrice;

    @Positive(message = "Available tickets must be greater than zero")
    private int availableTickets;
}