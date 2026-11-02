package com.flowtech.eventhub_spring_backend.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.flowtech.eventhub_spring_backend.dto.EventRequest;
import com.flowtech.eventhub_spring_backend.dto.EventResponse;
import com.flowtech.eventhub_spring_backend.dto.OrganiserBookingResponse;
import com.flowtech.eventhub_spring_backend.entity.Event;
import com.flowtech.eventhub_spring_backend.service.EventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value="/organiser")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANISER')")
public class OrganiserController {
	
	private final EventService eventService;
	

	@PostMapping(
		    value = "/registerEvent",
		    consumes = MediaType.MULTIPART_FORM_DATA_VALUE
		)
		public ResponseEntity<?> registerEvent(
		        @Valid @RequestPart("event") EventRequest request,
		        @RequestPart(value = "images", required = false)
		        List<MultipartFile> images,
		        Authentication authentication) {

		    return eventService.registerEvent(request, images, authentication);
		}

	
	 // View events owned by the logged-in organiser
    @GetMapping("/myEvents")
    public List<Event> getMyEvents(Authentication authentication) {

        return eventService.getMyEvents(authentication);
    }

    // Update an event owned by the logged-in organiser
    @PutMapping("/updateEvent/{eventId}")
    public ResponseEntity<?> updateMyEvent(
            @PathVariable Integer eventId,
            @RequestBody Event event,
            Authentication authentication) {

        return eventService.updateMyEvent(
                eventId, event, authentication);
    }
	
	
	@GetMapping("/test")
	public ResponseEntity<String> test(){
		return ResponseEntity.ok("Jwt authentication succesful for organiser");
	}
	
	@PostMapping(
		    value = "/events/{eventId}/image",
		    consumes = MediaType.MULTIPART_FORM_DATA_VALUE
		)
		public ResponseEntity<EventResponse> uploadEventImage(
		        @PathVariable Integer eventId,
		        @RequestPart("image") MultipartFile image,
		        Authentication authentication) {

		    EventResponse response = eventService.uploadEventImage(
		            eventId, image, authentication);

		    return ResponseEntity.ok(response);
		}
	
	
	// NEW: View bookings for the logged-in organiser's events
	@GetMapping("/bookings")
	public ResponseEntity<List<OrganiserBookingResponse>> getOrganiserBookings(
	        Authentication authentication) {

	    List<OrganiserBookingResponse> bookings =
	            eventService.getOrganiserBookings(authentication);

	    return ResponseEntity.ok(bookings);
	}
	
}
