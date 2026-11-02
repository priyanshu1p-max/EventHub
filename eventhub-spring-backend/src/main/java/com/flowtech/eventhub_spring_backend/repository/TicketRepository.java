package com.flowtech.eventhub_spring_backend.repository;

import com.flowtech.eventhub_spring_backend.entity.Ticket;
import com.flowtech.eventhub_spring_backend.enums.TicketStatus;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    Optional<Ticket> findByQrTokenHash(String qrTokenHash);

    List<Ticket> findByBooking_BookingId(Integer bookingId);

    List<Ticket> findByEvent_Id(Integer eventId);

    long countByEvent_Id(Integer eventId);

    long countByEvent_IdAndTicketStatus(
            Integer eventId,
            TicketStatus ticketStatus
    );

    // NEW: Locks the ticket row during check-in.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT t FROM Ticket t
           WHERE t.qrTokenHash = :hash
           """)
    Optional<Ticket> findByQrTokenHashForUpdate(
            @Param("hash") String hash
    );
    
    List<Ticket> findTop20ByEvent_IdAndTicketStatusOrderByCheckedInAtDesc(
            Integer eventId,
            TicketStatus ticketStatus
    );
    
}