package com.flowtech.eventhub_spring_backend.record;

public record TicketScanResponse(
		
		 	boolean success,
	        String message,
	        Integer ticketId,
	        String eventName,
	        String customerName,
	        String checkedInAt
		) {

}
