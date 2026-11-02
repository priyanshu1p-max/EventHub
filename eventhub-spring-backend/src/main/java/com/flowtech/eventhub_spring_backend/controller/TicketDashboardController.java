package com.flowtech.eventhub_spring_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flowtech.eventhub_spring_backend.record.TicketDashboardResponse;
import com.flowtech.eventhub_spring_backend.service.TicketDashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/organiser/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANISER')")
public class TicketDashboardController {
	
	private final TicketDashboardService ticketDashboardService;

    @GetMapping("/{eventId}/dashboard")
    public ResponseEntity<TicketDashboardResponse> getDashboard(
            @PathVariable Integer eventId,
            Authentication authentication
    ) {

        TicketDashboardResponse response =
                ticketDashboardService.getDashboard(
                        eventId,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    
    @GetMapping("/{eventId}/checkin-activities")
    public ResponseEntity<?> getCheckInActivities(
            @PathVariable Integer eventId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                ticketDashboardService.getCheckInActivities(
                        eventId,
                        authentication.getName()
                )
        );
    }
}
