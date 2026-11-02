package com.flowtech.eventhub_spring_backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flowtech.eventhub_spring_backend.entity.Booking;
import com.flowtech.eventhub_spring_backend.entity.Event;
import com.flowtech.eventhub_spring_backend.entity.Ticket;
import com.flowtech.eventhub_spring_backend.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {

	private final TicketRepository ticketRepository;
    private final QrTokenService qrTokenService;

    public record GeneratedTicket(
        Integer ticketId,
        String qrToken
    ) {}

    @Transactional
    public List<GeneratedTicket> generateTickets(
            Booking booking,
            Event event,
            int quantity) {

        if (quantity < 1) {
            throw new IllegalArgumentException(
                "Ticket quantity must be at least 1"
            );
        }

        List<Ticket> tickets = new ArrayList<>();
        List<String> rawTokens = new ArrayList<>();

        for (int i = 0; i < quantity; i++) {

            String token = qrTokenService.generateToken();

            Ticket ticket = new Ticket();

            ticket.setBooking(booking);
            ticket.setEvent(event);
            ticket.setQrTokenHash(
                qrTokenService.hashToken(token)
            );

            tickets.add(ticket);
            rawTokens.add(token);
        }

        List<Ticket> savedTickets =
            ticketRepository.saveAll(tickets);

        List<GeneratedTicket> result = new ArrayList<>();

        for (int i = 0; i < savedTickets.size(); i++) {

            result.add(
                new GeneratedTicket(
                    savedTickets.get(i).getTicketId(),
                    rawTokens.get(i)
                )
            );
        }

        return result;
    }
}
