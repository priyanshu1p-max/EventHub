package com.flowtech.eventhub_spring_backend.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.flowtech.eventhub_spring_backend.dto.EventRequest;
import com.flowtech.eventhub_spring_backend.dto.EventResponse;
import com.flowtech.eventhub_spring_backend.dto.OrganiserBookingResponse;
import com.flowtech.eventhub_spring_backend.entity.Booking;
import com.flowtech.eventhub_spring_backend.entity.Event;
import com.flowtech.eventhub_spring_backend.entity.User;
import com.flowtech.eventhub_spring_backend.enums.EventVerification;
import com.flowtech.eventhub_spring_backend.exception.TicketNotAvailableException;
import com.flowtech.eventhub_spring_backend.mail.EventHandlingEmailService;
import com.flowtech.eventhub_spring_backend.record.PaymentOrderResponse;
import com.flowtech.eventhub_spring_backend.record.PaymentVerificationRequest;
import com.flowtech.eventhub_spring_backend.repository.BookingRepository;
import com.flowtech.eventhub_spring_backend.repository.EventRepository;
import com.flowtech.eventhub_spring_backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

	private final EventRepository eventRepository;

	private final UserRepository userRepository;

	private final BookingRepository bookingRepository;

	private final EventHandlingEmailService emailService;

	private final EventImageStorageService imageStorageService;

	private final TicketService ticketService;

	private final RazorpayService razorpayService;

	public ResponseEntity<?> registerEvent(EventRequest request, List<MultipartFile> images,
			Authentication authentication) {

		String email = authentication.getName();

		User organiser = userRepository.findByEmail(email)
				.orElseThrow(() -> new NoSuchElementException("Organiser account not found"));

		Event event = new Event();
		event.setName(request.getName());
		event.setDescription(request.getDescription());
		event.setLocation(request.getLocation());
		// NEW: Save smart discovery fields
		event.setCategory(request.getCategory());
		event.setLatitude(request.getLatitude());
		event.setLongitude(request.getLongitude());

		event.setEventDateTime(request.getEventDateTime());
		event.setTicketPrice(request.getTicketPrice());
		event.setAvailableTickets(request.getAvailableTickets());
		event.setUser(organiser);
		event.setStatus(EventVerification.PENDING);

		try {
			if (images != null) {
				for (MultipartFile image : images) {
					if (image != null && !image.isEmpty()) {
						String imageUrl = imageStorageService.storeImage(image);

						event.getImageUrls().add(imageUrl);
					}
				}
			}

			// Preserve compatibility with existing code that uses imageUrl.
			if (!event.getImageUrls().isEmpty()) {
				event.setImageUrl(event.getImageUrls().get(0));
			}

			Event savedEvent = eventRepository.save(event);

			return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(savedEvent));

		} catch (IOException e) {
			throw new RuntimeException("Failed to upload event images", e);
		}
	}

	// get events belonging to a logged in organiser
	public List<Event> getMyEvents(Authentication authentication) {

		String email = authentication.getName();

		return eventRepository.findByUser_EmailOrderByCreatedAtDesc(email);
	}

	// Update an event only if it belongs to the logged-in organiser
	@Transactional
	public ResponseEntity<?> updateMyEvent(Integer eventId, Event updatedEvent, Authentication authentication) {

		if (updatedEvent == null || authentication == null) {
			return ResponseEntity.badRequest().body("Event details and authentication are required");
		}

		Event existingEvent = eventRepository.findById(eventId).orElse(null);

		if (existingEvent == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
		}

		String email = authentication.getName();

		if (existingEvent.getUser() == null || !existingEvent.getUser().getEmail().equals(email)) {

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You cannot update another organiser's event");
		}

		if (updatedEvent.getName() == null || updatedEvent.getName().isBlank() || updatedEvent.getLocation() == null
				|| updatedEvent.getLocation().isBlank() || updatedEvent.getEventDateTime() == null) {

			return ResponseEntity.badRequest().body("Event name, location and date/time are required");
		}

		if (!updatedEvent.getEventDateTime().isAfter(LocalDateTime.now())) {
			return ResponseEntity.badRequest().body("Event date and time must be in the future");
		}

		if (updatedEvent.getTicketPrice() < 0) {
			return ResponseEntity.badRequest().body("Ticket price cannot be negative");
		}

		// Update only fields the organiser is allowed to change.
		existingEvent.setName(updatedEvent.getName());
		existingEvent.setDescription(updatedEvent.getDescription());
		existingEvent.setLocation(updatedEvent.getLocation());

		// NEW: Update smart discovery fields
		existingEvent.setCategory(updatedEvent.getCategory());
		existingEvent.setLatitude(updatedEvent.getLatitude());
		existingEvent.setLongitude(updatedEvent.getLongitude());

		existingEvent.setEventDateTime(updatedEvent.getEventDateTime());
		existingEvent.setTicketPrice(updatedEvent.getTicketPrice());

		// Send the modified event back for admin review.
		existingEvent.setStatus(EventVerification.PENDING);

		Event savedEvent = eventRepository.save(existingEvent);

		return ResponseEntity.ok(savedEvent);
	}

	@Transactional
	public PaymentOrderResponse createBookingOrder(Integer eventId, int ticketQuantity, Authentication authentication) {

		// 1. Validate ticket quantity
		if (ticketQuantity <= 0) {
			throw new IllegalArgumentException("Ticket quantity must be greater than zero");
		}

		// 2. Find logged-in customer
		String customerEmail = authentication.getName();

		User user = userRepository.findByEmail(customerEmail)
				.orElseThrow(() -> new NoSuchElementException("User not found"));

		// 3. Lock event to prevent overselling
		Event event = eventRepository.findByIdForUpdate(eventId)
				.orElseThrow(() -> new NoSuchElementException("Event not found with id: " + eventId));

		// 4. Check event approval
		if (event.getStatus() != EventVerification.APPROVED) {
			throw new IllegalStateException("This event is not approved for booking");
		}
		
		// Prevent booking when the event has started or is in the past
		if (event.getEventDateTime() == null ||
		        !event.getEventDateTime().isAfter(LocalDateTime.now())) {

		    throw new RuntimeException(
		        "Ticket booking is closed. This event has already started or is completed."
		    );
		}

		// 5. Check ticket availability
		if (ticketQuantity > event.getAvailableTickets()) {
			throw new TicketNotAvailableException("Not enough tickets available");
		}

		// 6. Calculate amount on backend
		double totalAmount = event.getTicketPrice() * ticketQuantity;

		if (!Double.isFinite(totalAmount) || totalAmount <= 0) {
			throw new IllegalArgumentException("Invalid ticket price. Razorpay requires a positive amount.");
		}

		// 7. Create pending booking
		Booking booking = new Booking();

		booking.setUser(user);
		booking.setEvent(event);
		booking.setQuantity(ticketQuantity);
		booking.setTotalPrice(totalAmount);

		booking.setPaymentStatus("PENDING");
		booking.setBookingStatus("PENDING_PAYMENT");
		booking.setEventDateTime(event.getEventDateTime());

		// 8. Set payment expiry (15 minutes)
		booking.setPaymentExpiresAt(LocalDateTime.now().plusMinutes(15));

		// 9. Reserve tickets
		event.setAvailableTickets(event.getAvailableTickets() - ticketQuantity);

		eventRepository.save(event);

		// 10. Save booking to generate booking ID
		Booking savedBooking = bookingRepository.save(booking);

		// 11. Create Razorpay order
		JSONObject order = razorpayService.createOrder(totalAmount, "booking_" + savedBooking.getBookingId());

		String razorpayOrderId = order.getString("orderId");
		int amountInPaise = order.getInt("amount");
		String currency = order.getString("currency");
		String keyId = order.getString("keyId");

		// 12. Store Razorpay order ID
		savedBooking.setRazorpayOrderId(razorpayOrderId);

		bookingRepository.save(savedBooking);

		// 13. Return checkout details to frontend
		return new PaymentOrderResponse(savedBooking.getBookingId(), razorpayOrderId, amountInPaise, currency, keyId,
				event.getName());
	}

	@Transactional
	public Page<EventResponse> searchApprovedEvents(String keyword, Pageable pageable) {

		Page<Event> events = eventRepository.searchEvents(EventVerification.APPROVED, keyword, pageable);

		return events.map(EventResponse::from);
	}

	@Transactional
	public List<Booking> getMyBookings(Authentication authentication) {

		String email = authentication.getName();

		return bookingRepository.findByUser_EmailOrderByBookingDateTimeDesc(email);
	}

	@Transactional
	public Booking cancelBooking(int bookingId, Authentication authentication) {

		String email = authentication.getName();

		// find and lock this customer booking
		Booking booking = bookingRepository.findBookingIdForCancellation(bookingId, email)
				.orElseThrow(() -> new IllegalArgumentException("booking not found for this customer"));

		// prevent cancellation more than once
		if ("CANCELLED".equals(booking.getBookingStatus())) {
			throw new IllegalArgumentException("This booking has already been cancelled");
		}

		// find and lock the associated event
		Integer eventId = booking.getEvent().getId();

		Event event = eventRepository.findByIdForUpdate(eventId)
				.orElseThrow(() -> new IllegalArgumentException("Associated event not found"));

		// restore available tickets
		event.setAvailableTickets(event.getAvailableTickets() + booking.getQuantity());

		// mark the booking as cancelled
		booking.setBookingStatus("CANCELLED");

		// save both changes in one transaction
		eventRepository.save(event);

		Booking cancelledBooking = bookingRepository.save(booking);

		emailService.sendBookingCancellation(cancelledBooking);

		return cancelledBooking;

	}

	public List<Event> findEventByStatusPending() {
		return eventRepository.findByStatus(EventVerification.PENDING);
	}

	public Event find(Integer eventId) {
		return eventRepository.findById(eventId).orElse(null);
	}

	public List<Event> getAllApprovedEvents() {
		return eventRepository.findByStatus(EventVerification.APPROVED);
	}

	public List<Event> getAllRejectedEvents() {
		return eventRepository.findByStatus(EventVerification.REJECTED);
	}

	@Transactional
	public EventResponse uploadEventImage(Integer eventId, MultipartFile image, Authentication authentication) {

		String email = authentication.getName();

		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new IllegalArgumentException("Event not found"));

		// Check event ownership
		if (event.getUser() == null || !event.getUser().getEmail().equalsIgnoreCase(email)) {

			throw new org.springframework.security.access.AccessDeniedException(
					"You can upload images only for your own events");
		}

		try {
			// Store the image in the uploads/events folder
			String imageUrl = imageStorageService.storeImage(image);

			// Save image URL in the database
			event.setImageUrl(imageUrl);

			Event savedEvent = eventRepository.save(event);

			return EventResponse.from(savedEvent);

		} catch (IOException e) {
			throw new RuntimeException("Failed to upload event image: " + e.getMessage(), e);
		}
	}

	// NEW: Get bookings for events belonging to the logged-in organiser
	@Transactional
	public List<OrganiserBookingResponse> getOrganiserBookings(Authentication authentication) {

		String email = authentication.getName();

		List<Booking> bookings = bookingRepository.findByEvent_User_EmailOrderByBookingDateTimeDesc(email);

		return bookings.stream().map(OrganiserBookingResponse::from).toList();
	}

	// NEW: Discover events happening this weekend within budget
	public List<EventResponse> discoverWeekendEvents(double budget, int groupSize) {

		// Validate budget
		if (budget < 0) {
			throw new IllegalArgumentException("Budget cannot be negative.");
		}

		// Validate group size
		if (groupSize < 1) {
			throw new IllegalArgumentException("Group size must be at least 1.");
		}

		LocalDateTime now = LocalDateTime.now();

		// Find Saturday of this or next weekend
		LocalDate today = now.toLocalDate();

		LocalDate saturday = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SATURDAY));

		// If today is Sunday, use yesterday as Saturday
		if (today.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
			saturday = today.minusDays(1);
		}

		LocalDate sunday = saturday.plusDays(1);

		// Start from now if the weekend has already started
		LocalDateTime startDateTime = saturday.atStartOfDay();

		if (now.isAfter(startDateTime)) {
			startDateTime = now;
		}

		LocalDateTime endDateTime = sunday.atTime(java.time.LocalTime.MAX);

		// Fetch approved events within the budget and weekend dates
		List<Event> events = eventRepository
				.findByStatusAndEventDateTimeBetweenAndTicketPriceLessThanEqualOrderByEventDateTimeAsc(
						EventVerification.APPROVED, startDateTime, endDateTime, budget);

		// Check that enough tickets are available for the group
		return events.stream().filter(event -> event.getAvailableTickets() >= groupSize).map(EventResponse::from)
				.toList();
	}

	
	@Transactional
	public Booking verifyBookingPayment(
	        PaymentVerificationRequest request,
	        Authentication authentication) {

	    // 1. Get logged-in customer email
	    String email = authentication.getName();

	    // 2. Find and lock booking
	    Booking booking = bookingRepository
	            .findBookingForPayment(request.bookingId())
	            .orElseThrow(() ->
	                    new NoSuchElementException("Booking not found"));

	    // 3. Check booking ownership
	    if (!booking.getUser().getEmail().equalsIgnoreCase(email)) {
	        throw new AccessDeniedException(
	                "You cannot verify another customer's booking");
	    }

	    // 4. Prevent duplicate payment processing
	    if ("PAID".equalsIgnoreCase(booking.getPaymentStatus())) {

	        if (request.razorpay_payment_id()
	                .equals(booking.getRazorpayPaymentId())) {
	            return booking;
	        }

	        throw new IllegalStateException(
	                "This booking has already been paid");
	    }

	    // 5. Check Razorpay order ID
	    if (!request.razorpay_order_id()
	            .equals(booking.getRazorpayOrderId())) {

	        throw new IllegalArgumentException(
	                "Invalid Razorpay order ID");
	    }

	    // 6. Check booking payment status
	    if (!"PENDING".equalsIgnoreCase(booking.getPaymentStatus())
	            || !"PENDING_PAYMENT".equalsIgnoreCase(
	                    booking.getBookingStatus())) {

	        throw new IllegalStateException(
	                "Booking is not awaiting payment");
	    }

	    // 7. Check payment expiry
	    if (booking.getPaymentExpiresAt() == null
	            || !booking.getPaymentExpiresAt()
	                    .isAfter(LocalDateTime.now())) {

	        throw new IllegalStateException(
	                "Payment session has expired");
	    }

	    // 8. Verify Razorpay signature
	    boolean signatureValid =
	            razorpayService.verifyPaymentSignature(
	                    request.razorpay_order_id(),
	                    request.razorpay_payment_id(),
	                    request.razorpay_signature());

	    if (!signatureValid) {
	        throw new IllegalArgumentException(
	                "Invalid payment signature");
	    }

	    // 9. Confirm payment is captured by Razorpay
	    boolean paymentCaptured =
	            razorpayService.isPaymentCaptured(
	                    request.razorpay_payment_id(),
	                    booking.getRazorpayOrderId());

	    if (!paymentCaptured) {
	        throw new IllegalStateException(
	                "Payment has not been captured");
	    }

	    // 10. Update booking payment details
	    booking.setRazorpayPaymentId(
	            request.razorpay_payment_id());

	    booking.setRazorpayPaymentSignature(
	            request.razorpay_signature());

	    booking.setPaymentStatus("PAID");
	    booking.setBookingStatus("CONFIRMED");
	    booking.setPaymentDateTime(LocalDateTime.now());

	    Booking paidBooking = bookingRepository.save(booking);

	    // 11. Generate QR tickets after successful payment
	    List<TicketService.GeneratedTicket> generatedTickets =
	            ticketService.generateTickets(
	                    paidBooking,
	                    paidBooking.getEvent(),
	                    paidBooking.getQuantity());

	    // 12. Send booking confirmation email
	    emailService.sendBookingConfirmation(
	            paidBooking,
	            generatedTickets);

	    return paidBooking;
	}
}
