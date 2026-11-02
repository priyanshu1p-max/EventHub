package com.flowtech.eventhub_spring_backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flowtech.eventhub_spring_backend.entity.Booking;
import com.flowtech.eventhub_spring_backend.entity.RefundRequest;
import com.flowtech.eventhub_spring_backend.entity.RefundTimeline;
import com.flowtech.eventhub_spring_backend.entity.User;
import com.flowtech.eventhub_spring_backend.enums.RefundStatus;
import com.flowtech.eventhub_spring_backend.mail.EventHandlingEmailService;
import com.flowtech.eventhub_spring_backend.repository.BookingRepository;
import com.flowtech.eventhub_spring_backend.repository.RefundRequestRepository;
import com.flowtech.eventhub_spring_backend.repository.RefundTimelineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefundService {

	private final BookingRepository bookingRepository;
	private final RefundRequestRepository refundRequestRepository;
	private final RefundTimelineRepository refundTimelineRepository;
	private final EventHandlingEmailService emailService;

	@Transactional
	public RefundRequest requestRefund(Integer bookingId, String reason, Authentication authentication) {

		String email = authentication.getName();

		// Find the booking
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new IllegalArgumentException("Booking not found"));

		// Verify that the booking belongs to this customer
		if (booking.getUser() == null || !booking.getUser().getEmail().equalsIgnoreCase(email)) {
			throw new AccessDeniedException("You can request a refund only for your own booking");
		}

		// Prevent duplicate refund requests
		if (refundRequestRepository.findByBooking_BookingId(bookingId).isPresent()) {
			throw new IllegalArgumentException("A refund request already exists for this booking");
		}

		// Only eligible bookings can request a refund
		if (!"CANCELLED".equalsIgnoreCase(booking.getBookingStatus())) {
			throw new IllegalArgumentException("Cancel your booking before requesting a refund");
		}

		if (!"PAID".equalsIgnoreCase(booking.getPaymentStatus())) {
			throw new IllegalArgumentException("Only paid bookings can request a monetary refund");
		}

		if (reason == null || reason.isBlank()) {
			throw new IllegalArgumentException("Please provide a refund reason");
		}

		// Create refund request
		RefundRequest refund = new RefundRequest();
		refund.setBooking(booking);
		refund.setRefundAmount(booking.getTotalPrice());
		refund.setReason(reason.trim());
		refund.setStatus(RefundStatus.REQUEST_RECIEVED);
		refund.setRequestReceivedAt(LocalDateTime.now());

		RefundRequest savedRefund = refundRequestRepository.saveAndFlush(refund);

		// Record first timeline entry
		addTimeline(savedRefund, RefundStatus.REQUEST_RECIEVED, "Your refund request has been received.");

		// Record organiser notification
		savedRefund.setStatus(RefundStatus.ORGANISER_NOTIFIED);
		savedRefund.setOrganiserNotifiedAt(LocalDateTime.now());

		refundRequestRepository.save(savedRefund);

		addTimeline(savedRefund, RefundStatus.ORGANISER_NOTIFIED,
				"The organiser has been notified of the refund request.");

		// Send notification using the existing email service
		try {
			emailService.sendRefundRequestNotification(savedRefund);
		} catch (Exception e) {
			// Do not hide refund request persistence behind email delivery.
			// The notification can be retried separately.
		}

		return savedRefund;
	}

	@Transactional(readOnly = true)
	public List<RefundTimeline> getRefundTimeline(Integer refundId, Authentication authentication) {

		String email = authentication.getName();

		RefundRequest refund = refundRequestRepository.findById(refundId)
				.orElseThrow(() -> new IllegalArgumentException("Refund request not found"));

		if (refund.getBooking().getUser() == null
				|| !refund.getBooking().getUser().getEmail().equalsIgnoreCase(email)) {
			throw new AccessDeniedException("You cannot view another customer's refund timeline");
		}

		return refundTimelineRepository.findByRefundRequest_RefundIdOrderByCreatedAtAsc(refundId);
	}

	private void addTimeline(RefundRequest refund, RefundStatus status, String message) {

		RefundTimeline timeline = new RefundTimeline();
		timeline.setRefundRequest(refund);
		timeline.setStatus(status);
		timeline.setMessage(message);
		timeline.setCreatedAt(LocalDateTime.now());

		refundTimelineRepository.save(timeline);
	}

	@Transactional
	public RefundRequest reviewRefund(Integer refundId, boolean approved, String rejectionReason,
			Authentication authentication) {

		String email = authentication.getName();

		RefundRequest refund = refundRequestRepository.findById(refundId)
				.orElseThrow(() -> new IllegalArgumentException("Refund request not found"));

		// Only the event organiser or an admin can review this request.
		boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

		boolean isOrganiser = authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ORGANISER"));

		if (!isAdmin && !isOrganiser) {
			throw new AccessDeniedException("Only an organiser or admin can review refunds");
		}

		// Organisers may review only refunds for their own events.
		if (!isAdmin) {
			User organiser = refund.getBooking().getEvent().getUser();

			if (organiser == null || !organiser.getEmail().equalsIgnoreCase(email)) {
				throw new AccessDeniedException("You cannot review refunds for another organiser's event");
			}
		}

		// Only requests awaiting a decision can be reviewed.
		if (refund.getStatus() != RefundStatus.ORGANISER_NOTIFIED
				&& refund.getStatus() != RefundStatus.REQUEST_RECIEVED) {
			throw new IllegalArgumentException("This refund request has already been reviewed");
		}

		if (!approved && (rejectionReason == null || rejectionReason.isBlank())) {
			throw new IllegalArgumentException("A reason is required when rejecting a refund");
		}

		RefundStatus newStatus = approved ? RefundStatus.APPROVED : RefundStatus.REJECTED;

		refund.setStatus(newStatus);
		refund.setRejectionReason(approved ? null : rejectionReason.trim());
		refund.setReviewedAt(LocalDateTime.now());

		RefundRequest savedRefund = refundRequestRepository.save(refund);

		String message = approved ? "Your refund request has been approved."
				: "Your refund request was rejected. Reason: " + rejectionReason.trim();

		addTimeline(savedRefund, newStatus, message);

		return savedRefund;
	}

	@Transactional
	public RefundRequest initiateRefund(Integer refundId, String paymentReference, Authentication authentication) {

		requireAdmin(authentication);

		RefundRequest refund = refundRequestRepository.findById(refundId)
				.orElseThrow(() -> new IllegalArgumentException("Refund request not found"));

		if (refund.getStatus() != RefundStatus.APPROVED) {
			throw new IllegalArgumentException("Only approved refunds can be initiated");
		}

		if (paymentReference == null || paymentReference.isBlank()) {
			throw new IllegalArgumentException("Payment refund reference is required");
		}

		refund.setPaymentRefundReference(paymentReference.trim());
		refund.setStatus(RefundStatus.REFUND_INITIATED);
		refund.setRefundInitiatedAt(LocalDateTime.now());

		RefundRequest saved = refundRequestRepository.save(refund);

		addTimeline(saved, RefundStatus.REFUND_INITIATED,
				"The refund has been initiated. Reference: " + paymentReference.trim());

		return saved;
	}

	@Transactional
	public RefundRequest completeRefund(Integer refundId, boolean providerConfirmed, Authentication authentication) {

		requireAdmin(authentication);

		RefundRequest refund = refundRequestRepository.findById(refundId)
				.orElseThrow(() -> new IllegalArgumentException("Refund request not found"));

		if (refund.getStatus() != RefundStatus.REFUND_INITIATED) {
			throw new IllegalArgumentException("Only initiated refunds can be completed");
		}

		if (!providerConfirmed) {
			throw new IllegalArgumentException("Refund completion must be verified first");
		}

		if (refund.getPaymentRefundReference() == null || refund.getPaymentRefundReference().isBlank()) {
			throw new IllegalArgumentException("A payment refund reference is required");
		}

		refund.setStatus(RefundStatus.REFUND_COMPLETED);
		refund.setRefundCompletedAt(LocalDateTime.now());

		RefundRequest saved = refundRequestRepository.save(refund);

		addTimeline(saved, RefundStatus.REFUND_COMPLETED, "The refund was verified as completed.");

		return saved;
	}

	private void requireAdmin(Authentication authentication) {
		if (authentication == null || authentication.getAuthorities().stream()
				.noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
			throw new AccessDeniedException("Only an admin can perform this refund action");
		}
	}

}
