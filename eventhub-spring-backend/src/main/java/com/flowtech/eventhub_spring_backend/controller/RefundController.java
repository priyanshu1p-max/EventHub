package com.flowtech.eventhub_spring_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flowtech.eventhub_spring_backend.dto.RefundCompleteRequest;
import com.flowtech.eventhub_spring_backend.dto.RefundInitiateRequest;
import com.flowtech.eventhub_spring_backend.dto.RefundReviewRequest;
import com.flowtech.eventhub_spring_backend.entity.RefundRequest;
import com.flowtech.eventhub_spring_backend.entity.RefundTimeline;
import com.flowtech.eventhub_spring_backend.service.RefundService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class RefundController {

	private final RefundService refundService;

	// Submit a refund request
	@PostMapping("/bookings/{bookingId}/refund")
	public ResponseEntity<RefundRequest> requestRefund(@PathVariable Integer bookingId,
			@RequestBody RefundRequestBody request, Authentication authentication) {

		RefundRequest refund = refundService.requestRefund(bookingId, request.getReason(), authentication);

		return ResponseEntity.status(HttpStatus.CREATED).body(refund);
	}

	// View the refund timeline
	@GetMapping("/refunds/{refundId}/timeline")
	public ResponseEntity<List<RefundTimeline>> getRefundTimeline(@PathVariable Integer refundId,
			Authentication authentication) {

		List<RefundTimeline> timeline = refundService.getRefundTimeline(refundId, authentication);

		return ResponseEntity.ok(timeline);
	}

	// Request body for refund submission
	public static class RefundRequestBody {
		private String reason;

		public String getReason() {
			return reason;
		}

		public void setReason(String reason) {
			this.reason = reason;
		}
	}

	@PutMapping("/refunds/{refundId}/review")
	@PreAuthorize("hasAnyRole('ADMIN', 'ORGANISER')")
	public ResponseEntity<RefundRequest> reviewRefund(@PathVariable Integer refundId,
			@Valid @RequestBody RefundReviewRequest request, Authentication authentication) {

		RefundRequest refund = refundService.reviewRefund(refundId, request.getApproved(), request.getRejectionReason(),
				authentication);

		return ResponseEntity.ok(refund);
	}

	@PutMapping("/refunds/{refundId}/initiate")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<RefundRequest> initiateRefund(@PathVariable Integer refundId,
			@Valid @RequestBody RefundInitiateRequest request, Authentication authentication) {

		RefundRequest refund = refundService.initiateRefund(refundId, request.getPaymentReference(), authentication);

		return ResponseEntity.ok(refund);
	}

	@PutMapping("/refunds/{refundId}/complete")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<RefundRequest> completeRefund(@PathVariable Integer refundId,
			@RequestBody RefundCompleteRequest request, Authentication authentication) {

		RefundRequest refund = refundService.completeRefund(refundId, request.isProviderConfirmed(), authentication);

		return ResponseEntity.ok(refund);
	}

}
