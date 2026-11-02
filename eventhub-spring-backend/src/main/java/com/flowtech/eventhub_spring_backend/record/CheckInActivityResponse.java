package com.flowtech.eventhub_spring_backend.record;

public record CheckInActivityResponse(
		
		Integer ticketId,
        String customerName,
        String eventName,
        String checkedInAt,
        String message
		) {

}
