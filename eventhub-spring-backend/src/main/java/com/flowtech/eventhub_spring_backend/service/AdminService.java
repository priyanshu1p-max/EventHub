package com.flowtech.eventhub_spring_backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.flowtech.eventhub_spring_backend.dto.OrganiserDetailsResponse;
import com.flowtech.eventhub_spring_backend.entity.Event;
import com.flowtech.eventhub_spring_backend.entity.User;
import com.flowtech.eventhub_spring_backend.enums.EventVerification;
import com.flowtech.eventhub_spring_backend.mail.EventHandlingEmailService;
import com.flowtech.eventhub_spring_backend.repository.EventRepository;
import com.flowtech.eventhub_spring_backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

	private final EventHandlingEmailService emailService;

	private final EventService eventService;
	private final EventRepository eventRepository;
	
	private final UserRepository userRepository;
	
	@Transactional
	public ResponseEntity<?> approveEvents(Authentication authentication){
		
		List<Event> events=eventService.findEventByStatusPending();
		
		if(events.isEmpty()) {
			throw new RuntimeException("No events found with status PENDING");
		}
		
		for(Event event:events) {
			event.setStatus(EventVerification.APPROVED);
			
			Event savedEvent = eventRepository.save(event);

		    emailService.sendEventStatusUpdate(savedEvent);
		}
		return ResponseEntity.ok(events.size()+"All pending events have been approved");
	}
	
	//change status of one event
	public ResponseEntity<?> changeEventStatus(Integer eventId,String status,Authentication authentication){
		Event event=eventRepository.findById(eventId)
				.orElseThrow(()-> new IllegalArgumentException("Event not found with id: "+eventId));
		
		
		EventVerification newStatus;
		
		try {
			newStatus=EventVerification.valueOf(status.trim().toUpperCase());
		}catch(IllegalArgumentException e) {
			throw new RuntimeException("Invalid Status value: "+status);
		}
		
		event.setStatus(newStatus);
		
		Event savedEvent = eventRepository.save(event);

	    emailService.sendEventStatusUpdate(savedEvent);
		
		return ResponseEntity.ok("Event status updated successfully");
			
	}
	

	// NEW: Get all registered organisers
	public List<OrganiserDetailsResponse> getAllOrganisers() {

	    List<User> organisers =
	            userRepository.findByRole_Name("ROLE_ORGANISER");

	    return organisers.stream().map(user -> {

	        OrganiserDetailsResponse response =
	                new OrganiserDetailsResponse();

	        response.setId(user.getId());
	        response.setName(user.getName());
	        response.setEmail(user.getEmail());
	        response.setPhone(user.getPhone());

	        return response;

	    }).collect(Collectors.toList());
	}

	
	
}
