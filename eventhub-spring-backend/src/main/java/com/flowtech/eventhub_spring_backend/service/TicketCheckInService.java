package com.flowtech.eventhub_spring_backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flowtech.eventhub_spring_backend.entity.Booking;
import com.flowtech.eventhub_spring_backend.entity.Event;
import com.flowtech.eventhub_spring_backend.entity.Ticket;
import com.flowtech.eventhub_spring_backend.entity.User;
import com.flowtech.eventhub_spring_backend.enums.TicketStatus;
import com.flowtech.eventhub_spring_backend.record.CheckInActivityResponse;
import com.flowtech.eventhub_spring_backend.record.CheckInEvent;
import com.flowtech.eventhub_spring_backend.record.TicketScanResponse;
import com.flowtech.eventhub_spring_backend.repository.TicketRepository;
import com.flowtech.eventhub_spring_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class TicketCheckInService {

	
	private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final QrTokenService qrTokenService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TicketScanResponse scanTicket(
            String qrToken,
            String organiserEmail
    ) {

        if (qrToken == null || qrToken.isBlank()) {
            return failure("QR code is empty.");
        }

        // 1. Find the logged-in organiser.
        User organiser = userRepository.findByEmail(organiserEmail)
                .orElseThrow(() -> new RuntimeException("Organiser not found."));

        // 2. Hash the scanned token. The database stores only its hash.
        String tokenHash = qrTokenService.hashToken(qrToken);

        // 3. Lock the ticket row to prevent duplicate simultaneous scans.
        Ticket ticket = ticketRepository.findByQrTokenHashForUpdate(tokenHash)
                .orElse(null);

        if (ticket == null) {
            return failure("Invalid QR code. Ticket not found.");
        }

        // 4. Check whether this organiser owns the event.
        Event event = ticket.getEvent();

        if (event.getUser() == null ||
                !event.getUser().getEmail().equals(organiserEmail)) {
            return failure("You are not authorised to scan this event's tickets.");
        }

        // 5. Check ticket status.
        if (ticket.getTicketStatus() != TicketStatus.ACTIVE) {
            if (ticket.getTicketStatus() == TicketStatus.CHECKED_IN) {
                return failure("This ticket has already been used.");
            }

            return failure("This ticket is not active.");
        }

        // 6. Check booking status and payment.
        Booking booking = ticket.getBooking();

        if ("CANCELLED".equalsIgnoreCase(booking.getBookingStatus())) {
            return failure("This booking has been cancelled.");
        }

        // Update these accepted values if your payment system uses another value.
        String paymentStatus = booking.getPaymentStatus();

        if (paymentStatus == null ||
                !(paymentStatus.equalsIgnoreCase("PAID")
                || paymentStatus.equalsIgnoreCase("COMPLETED")
                || paymentStatus.equalsIgnoreCase("SUCCESS"))) {
            return failure("Payment is not confirmed. Entry is not allowed.");
        }

        // 7. Check the allowed event entry window.
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventStart = event.getEventDateTime();

        LocalDateTime allowedStart = eventStart.minusHours(1);
        LocalDateTime allowedEnd = eventStart.plusMinutes(30);

        if (now.isBefore(allowedStart)) {
            return failure("Entry is not open yet. Scanning starts 1 hour before the event.");
        }

        if (now.isAfter(allowedEnd)) {
            return failure("The ticket entry window has expired.");
        }

        // 8. Mark the ticket as checked in.
        ticket.setTicketStatus(TicketStatus.CHECKED_IN);
        ticket.setCheckedInAt(now);
        ticket.setCheckedInBy(organiser.getId());

        ticketRepository.save(ticket);
        
        
     // Create the activity message.
        CheckInActivityResponse activity =
                new CheckInActivityResponse(
                        ticket.getTicketId(),
                        booking.getUser().getName(),
                        event.getName(),
                        now.toString(),
                        "Ticket checked in successfully"
                );

        // Publish the event. WebSocket sends it after transaction commit.
        eventPublisher.publishEvent(
                new CheckInEvent(event.getId(), activity)
        );
        

        return new TicketScanResponse(
                true,
                "Ticket verified. Entry allowed!",
                ticket.getTicketId(),
                event.getName(),
                booking.getUser().getName(),
                now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
        );
    }

    private TicketScanResponse failure(String message) {
        return new TicketScanResponse(
                false,
                message,
                null,
                null,
                null,
                null
        );
    }
}
