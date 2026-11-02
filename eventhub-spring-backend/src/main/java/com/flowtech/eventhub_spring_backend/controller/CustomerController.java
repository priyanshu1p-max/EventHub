package com.flowtech.eventhub_spring_backend.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flowtech.eventhub_spring_backend.dto.CustomerPreferenceRequest;
import com.flowtech.eventhub_spring_backend.dto.EventResponse;
import com.flowtech.eventhub_spring_backend.dto.NearbyEventResponse;
import com.flowtech.eventhub_spring_backend.dto.RecommendationResponse;
import com.flowtech.eventhub_spring_backend.entity.Booking;
import com.flowtech.eventhub_spring_backend.entity.CustomerPreference;
import com.flowtech.eventhub_spring_backend.entity.Event;
import com.flowtech.eventhub_spring_backend.entity.SavedEvent;
import com.flowtech.eventhub_spring_backend.record.PaymentOrderResponse;
import com.flowtech.eventhub_spring_backend.record.PaymentVerificationRequest;
import com.flowtech.eventhub_spring_backend.service.CustomerPreferenceService;
import com.flowtech.eventhub_spring_backend.service.EventService;
import com.flowtech.eventhub_spring_backend.service.NearbyEventService;
import com.flowtech.eventhub_spring_backend.service.RecommendationService;
import com.flowtech.eventhub_spring_backend.service.SavedEventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(value="/customer")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {
	
	private final SavedEventService savedEventService;
	
	private final EventService eventService;
	
	private final CustomerPreferenceService preferenceService;
	
	private final RecommendationService recommendationService;
	
	private final NearbyEventService nearbyEventService;
	
		@GetMapping(value="/getAllApprovedEvents")
		public List<Event> getAllApprovedEvents(){
			
			return eventService.getAllApprovedEvents();
	}
		
		@PostMapping(value="/bookEvent")
		public ResponseEntity<PaymentOrderResponse> bookEvent(
		        @RequestParam int eventId,
		        @RequestParam int ticketQuantity,
		        Authentication authentication) {

		    PaymentOrderResponse response =
		            eventService.createBookingOrder(
		                    eventId,
		                    ticketQuantity,
		                    authentication);

		    return ResponseEntity.ok(response);
		}
		
		
		@GetMapping("/events")
		public ResponseEntity<Page<EventResponse>> searchEvents(
		        @RequestParam(defaultValue = "") String keyword,
		        @RequestParam(defaultValue = "0") int page,
		        @RequestParam(defaultValue = "10") int size,
		        @RequestParam(defaultValue = "eventDateTime") String sortBy,
		        @RequestParam(defaultValue = "asc") String direction) {

		    if (page < 0) {
		        throw new IllegalArgumentException(
		                "Page number cannot be negative");
		    }

		    if (size < 1 || size > 50) {
		        throw new IllegalArgumentException(
		                "Page size must be between 1 and 50");
		    }

		    List<String> allowedSortFields =
		            List.of("name", "eventDateTime", "ticketPrice", "createdAt");

		    if (!allowedSortFields.contains(sortBy)) {
		        throw new IllegalArgumentException(
		                "Invalid sort field");
		    }

		    Sort sort;

		    if ("asc".equalsIgnoreCase(direction)) {
		        sort = Sort.by(sortBy).ascending();
		    } else if ("desc".equalsIgnoreCase(direction)) {
		        sort = Sort.by(sortBy).descending();
		    } else {
		        throw new IllegalArgumentException(
		                "Direction must be asc or desc");
		    }

		    Pageable pageable = PageRequest.of(page, size, sort);

		    Page<EventResponse> events =
		            eventService.searchApprovedEvents(keyword, pageable);

		    return ResponseEntity.ok(events);
		}
		
		
		@GetMapping("/bookings")
		public List<Booking> getBookings(Authentication authentication){
			return eventService.getMyBookings(authentication);
		}
		
		@DeleteMapping("/bookings/{bookingId}")
		public ResponseEntity<Booking> cancelBooking(@PathVariable int bookingId,Authentication authentication){
			
			Booking booking=eventService.cancelBooking(bookingId, authentication);
			
			return ResponseEntity.ok(booking);
		}
		
		
		// NEW: Save or update customer preferences
		@PostMapping("/preferences")
		public ResponseEntity<CustomerPreference> savePreferences(
		        @Valid @org.springframework.web.bind.annotation.RequestBody
		        CustomerPreferenceRequest request,
		        Authentication authentication) {

		    CustomerPreference preference =
		            preferenceService.savePreferences(
		                    request, authentication.getName());

		    return ResponseEntity.ok(preference);
		}


		// NEW: Get customer preferences
		@GetMapping("/preferences")
		public ResponseEntity<CustomerPreference> getPreferences(
		        Authentication authentication) {

		    CustomerPreference preference =
		            preferenceService.getPreferences(authentication.getName());

		    return ResponseEntity.ok(preference);
		}
		
		
		// NEW: Save an event
		@PostMapping("/saved-events/{eventId}")
		public ResponseEntity<SavedEvent> saveEvent(
		        @PathVariable Integer eventId,
		        Authentication authentication) {

		    SavedEvent savedEvent = savedEventService.saveEvent(
		            eventId, authentication.getName());

		    return ResponseEntity.ok(savedEvent);
		}


		// NEW: Get all saved events
		@GetMapping("/saved-events")
		public ResponseEntity<List<SavedEvent>> getSavedEvents(
		        Authentication authentication) {

		    List<SavedEvent> savedEvents =
		            savedEventService.getSavedEvents(authentication.getName());

		    return ResponseEntity.ok(savedEvents);
		}


		// NEW: Remove a saved event
		@DeleteMapping("/saved-events/{eventId}")
		public ResponseEntity<String> removeSavedEvent(
		        @PathVariable Integer eventId,
		        Authentication authentication) {

		    savedEventService.removeSavedEvent(
		            eventId, authentication.getName());

		    return ResponseEntity.ok("Event removed from saved events");
		}
		
		// NEW: Get personalized event recommendations
		@GetMapping("/recommendations")
		public ResponseEntity<List<RecommendationResponse>> getRecommendations(
		        Authentication authentication) {

		    List<RecommendationResponse> recommendations =
		            recommendationService.getRecommendations(
		                    authentication.getName());

		    return ResponseEntity.ok(recommendations);
		}
		
		// NEW: Discover weekend events within a budget
		@GetMapping("/discover/weekend")
		public ResponseEntity<List<EventResponse>> discoverWeekendEvents(
		        @RequestParam(defaultValue = "500") double budget,
		        @RequestParam(defaultValue = "1") int groupSize) {

		    List<EventResponse> events =
		            eventService.discoverWeekendEvents(budget, groupSize);

		    return ResponseEntity.ok(events);
		}
		
		
		// NEW: Find nearby approved events
		@GetMapping("/nearby-events")
		public ResponseEntity<List<NearbyEventResponse>> getNearbyEvents(
		        Authentication authentication,
		        @RequestParam(defaultValue = "25") double radiusKm) {

		    List<NearbyEventResponse> events =
		            nearbyEventService.findNearbyEvents(
		                    authentication.getName(),
		                    radiusKm
		            );

		    return ResponseEntity.ok(events);
		}
		
		
		@PostMapping("/verify-payment")
		public ResponseEntity<Booking> verifyPayment(
		        @Valid @RequestBody PaymentVerificationRequest request,
		        Authentication authentication) {

		    Booking booking = eventService.verifyBookingPayment(
		            request,
		            authentication
		    );

		    return ResponseEntity.ok(booking);
		}
		
		
		
}
