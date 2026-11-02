package com.flowtech.eventhub_spring_backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.flowtech.eventhub_spring_backend.entity.Event;
import com.flowtech.eventhub_spring_backend.enums.EventVerification;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EventResponse {

    private Integer id;
    private String name;
    private String description;
    private String location;

    // NEW: Smart discovery fields
    private String category;
    private Double latitude;
    private Double longitude;

    private LocalDateTime eventDateTime;
    private LocalDate createdAt;
    private double ticketPrice;
    private int availableTickets;
    private EventVerification status;
    private String imageUrl;
    private List<String> imageUrls;

    public static EventResponse from(Event event) {

        EventResponse response = new EventResponse();

        response.setId(event.getId());
        response.setName(event.getName());
        response.setDescription(event.getDescription());
        response.setLocation(event.getLocation());

        // NEW: Copy smart discovery fields
        response.setCategory(event.getCategory());
        response.setLatitude(event.getLatitude());
        response.setLongitude(event.getLongitude());

        response.setEventDateTime(event.getEventDateTime());
        response.setCreatedAt(event.getCreatedAt());
        response.setTicketPrice(event.getTicketPrice());
        response.setAvailableTickets(event.getAvailableTickets());
        response.setStatus(event.getStatus());

        response.setImageUrl(event.getImageUrl());
        response.setImageUrls(event.getImageUrls());

        return response;
    }
}