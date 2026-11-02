package com.flowtech.eventhub_spring_backend.dto;

import java.time.LocalDateTime;

import com.flowtech.eventhub_spring_backend.entity.Booking;

import lombok.Data;

@Data
public class OrganiserBookingResponse {

	private int bookingId;
    private String customerName;
    private String customerEmail;
    private String eventName;
    private int quantity;
    private double totalPrice;
    private String bookingStatus;
    private String paymentStatus;
    private LocalDateTime bookingDateTime;
    private LocalDateTime eventDateTime;
    private Long customerPhone;

    // NEW: Convert Booking entity into a safe response DTO
    public static OrganiserBookingResponse from(Booking booking) {

        OrganiserBookingResponse response = new OrganiserBookingResponse();

        response.setBookingId(booking.getBookingId());
        response.setCustomerName(booking.getUser().getName());
        response.setCustomerEmail(booking.getUser().getEmail());
        response.setEventName(booking.getEvent().getName());
        response.setQuantity(booking.getQuantity());
        response.setTotalPrice(booking.getTotalPrice());
        response.setBookingStatus(booking.getBookingStatus());
        response.setPaymentStatus(booking.getPaymentStatus());
        response.setBookingDateTime(booking.getBookingDateTime());
        response.setEventDateTime(booking.getEventDateTime());
        response.setCustomerPhone(booking.getUser().getPhone());
        
        
        return response;
    }
}
