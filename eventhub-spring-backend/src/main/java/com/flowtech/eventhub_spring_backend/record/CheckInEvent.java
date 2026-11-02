package com.flowtech.eventhub_spring_backend.record;

public record CheckInEvent(
		
		Integer eventId,
        CheckInActivityResponse activity
		) {

}
