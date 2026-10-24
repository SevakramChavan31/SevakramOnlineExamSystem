package com.sevak.sevakramonlineexamsystem.repository;

import com.sevak.sevakramonlineexamsystem.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupportTicketRepository
        extends JpaRepository<SupportTicket, Long> {

    // Student tickets
    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Admin: all tickets with student details loaded
    @Query("""
        SELECT t
        FROM SupportTicket t
        LEFT JOIN FETCH t.user
        ORDER BY t.createdAt DESC
        """)
    List<SupportTicket> findAllTicketsWithStudentDetails();

    // Admin: filtered tickets with student details loaded
    @Query("""
        SELECT t
        FROM SupportTicket t
        LEFT JOIN FETCH t.user
        WHERE t.status = :status
        ORDER BY t.createdAt DESC
        """)
    List<SupportTicket> findTicketsWithStudentDetailsByStatus(
            @Param("status") SupportTicket.TicketStatus status
    );
    @Query("""
    SELECT t
    FROM SupportTicket t
    JOIN FETCH t.user
    WHERE t.id = :ticketId
    """)
    Optional<SupportTicket> findTicketWithStudentDetailsById(
            @Param("ticketId") Long ticketId
    );

    // Existing compatibility methods
    List<SupportTicket> findByStatusOrderByCreatedAtDesc(
            SupportTicket.TicketStatus status
    );

    List<SupportTicket> findAllByOrderByCreatedAtDesc();

    long countByStatus(SupportTicket.TicketStatus status);

    long countByUserId(Long userId);
}