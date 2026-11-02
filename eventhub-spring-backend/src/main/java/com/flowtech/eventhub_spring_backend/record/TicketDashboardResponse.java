package com.flowtech.eventhub_spring_backend.record;

public record TicketDashboardResponse(
		
		Integer eventId,
        String eventName,
        int totalTicketsSold,
        int ticketsRemaining,
        long attendeesCheckedIn,
        long attendeesNotCheckedIn,
        double checkInPercentage
		
		) {

}
