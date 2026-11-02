package com.flowtech.eventhub_spring_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flowtech.eventhub_spring_backend.record.TicketScanRequest;
import com.flowtech.eventhub_spring_backend.record.TicketScanResponse;
import com.flowtech.eventhub_spring_backend.service.TicketCheckInService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/organiser/tickets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANISER')")
public class TicketScanController {

	private final TicketCheckInService ticketCheckInService;

    @PostMapping("/scan")
    public ResponseEntity<TicketScanResponse> scanTicket(
            @RequestBody TicketScanRequest request,
            Authentication authentication
    ) {

        TicketScanResponse response =
                ticketCheckInService.scanTicket(
                        request.qrToken(),
                        authentication.getName()
                );

        if (!response.success()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
