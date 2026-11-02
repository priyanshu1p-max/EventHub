package com.flowtech.eventhub_spring_backend.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flowtech.eventhub_spring_backend.entity.Event;
import com.flowtech.eventhub_spring_backend.entity.Ticket;
import com.flowtech.eventhub_spring_backend.enums.TicketStatus;
import com.flowtech.eventhub_spring_backend.record.CheckInActivityResponse;
import com.flowtech.eventhub_spring_backend.record.TicketDashboardResponse;
import com.flowtech.eventhub_spring_backend.repository.EventRepository;
import com.flowtech.eventhub_spring_backend.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketDashboardService {

	private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public TicketDashboardResponse getDashboard(
            Integer eventId,
            String organiserEmail
    ) {

        // 1. Find the event.
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found."));

        // 2. Check whether the logged-in organiser owns this event.
        if (event.getUser() == null ||
                !event.getUser().getEmail().equals(organiserEmail)) {
            throw new RuntimeException(
                    "You are not authorised to view this event dashboard."
            );
        }

        // 3. Count tickets by status.
        long checkedIn = ticketRepository.countByEvent_IdAndTicketStatus(
                eventId,
                TicketStatus.CHECKED_IN
        );

        long active = ticketRepository.countByEvent_IdAndTicketStatus(
                eventId,
                TicketStatus.ACTIVE
        );

        // Cancelled tickets are excluded from sold ticket count.
        long totalSold = active + checkedIn;

        // 4. Calculate attendees who have not checked in.
        long notCheckedIn = active;

        // 5. Calculate check-in percentage.
        double percentage = totalSold == 0
                ? 0.0
                : (checkedIn * 100.0) / totalSold;

        return new TicketDashboardResponse(
                event.getId(),
                event.getName(),
                (int) totalSold,
                event.getAvailableTickets(),
                checkedIn,
                notCheckedIn,
                Math.round(percentage * 100.0) / 100.0
        );
    }
    
    
    @Transactional(readOnly = true)
    public List<CheckInActivityResponse> getCheckInActivities(
            Integer eventId,
            String organiserEmail
    ) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found."));

        if (event.getUser() == null ||
                !event.getUser().getEmail().equals(organiserEmail)) {
            throw new RuntimeException("You are not authorised to view this event.");
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        List<Ticket> tickets =
                ticketRepository
                        .findTop20ByEvent_IdAndTicketStatusOrderByCheckedInAtDesc(
                                eventId,
                                TicketStatus.CHECKED_IN
                        );

        return tickets.stream()
                .map(ticket -> new CheckInActivityResponse(
                        ticket.getTicketId(),
                        ticket.getBooking().getUser().getName(),
                        event.getName(),
                        ticket.getCheckedInAt() == null
                                ? "N/A"
                                : ticket.getCheckedInAt().format(formatter),
                        "Ticket checked in successfully"
                ))
                .toList();
    }
}
