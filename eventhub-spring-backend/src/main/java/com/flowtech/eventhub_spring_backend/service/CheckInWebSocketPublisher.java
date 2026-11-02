package com.flowtech.eventhub_spring_backend.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.flowtech.eventhub_spring_backend.record.CheckInEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CheckInWebSocketPublisher {

	 private final SimpMessagingTemplate messagingTemplate;

	    // Send the update only after the database transaction commits.
	    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	    public void sendCheckInUpdate(CheckInEvent event) {

	        String destination =
	                "/topic/events/" + event.eventId() + "/checkins";

	        messagingTemplate.convertAndSend(
	                destination,
	                event.activity()
	        );
	    }
}
