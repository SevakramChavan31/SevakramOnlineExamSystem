package com.sevak.sevakramonlineexamsystem.service;

import com.sevak.sevakramonlineexamsystem.entity.SupportTicket;
import com.sevak.sevakramonlineexamsystem.entity.User;
import com.sevak.sevakramonlineexamsystem.repository.SupportTicketRepository;
import com.sevak.sevakramonlineexamsystem.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;

    public SupportTicketService(
            SupportTicketRepository supportTicketRepository,
            UserRepository userRepository) {

        this.supportTicketRepository = supportTicketRepository;
        this.userRepository = userRepository;
    }


    // =========================================================
    // CREATE SUPPORT TICKET
    // =========================================================

    @Transactional
    public SupportTicket createTicket(
            Long userId,
            String subject,
            String message) {

        if (userId == null) {
            throw new RuntimeException("User session is required");
        }

        if (subject == null || subject.trim().isEmpty()) {
            throw new RuntimeException("Subject is required");
        }

        if (message == null || message.trim().isEmpty()) {
            throw new RuntimeException("Message is required");
        }

        String cleanSubject = subject.trim();
        String cleanMessage = message.trim();

        if (cleanSubject.length() > 150) {
            throw new RuntimeException(
                    "Subject cannot exceed 150 characters"
            );
        }

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Student account not found"
                        )
                );

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new RuntimeException(
                    "Your account is inactive"
            );
        }

        SupportTicket ticket = new SupportTicket();

        ticket.setUser(user);

        ticket.setSubject(cleanSubject);

        ticket.setMessage(cleanMessage);

        ticket.setStatus(
                SupportTicket.TicketStatus.OPEN
        );

        return supportTicketRepository.save(ticket);
    }


    // =========================================================
    // GET STUDENT TICKETS
    // =========================================================

    @Transactional(readOnly = true)
    public List<SupportTicket> getStudentTickets(
            Long userId) {

        if (userId == null) {
            throw new RuntimeException(
                    "User session is required"
            );
        }

        return supportTicketRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }


    // =========================================================
    // GET SINGLE STUDENT TICKET
    // =========================================================

    @Transactional(readOnly = true)
    public SupportTicket getStudentTicket(
            Long userId,
            Long ticketId) {

        if (userId == null) {
            throw new RuntimeException(
                    "User session is required"
            );
        }

        if (ticketId == null) {
            throw new RuntimeException(
                    "Ticket ID is required"
            );
        }

        SupportTicket ticket =
                supportTicketRepository
                        .findById(ticketId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Support ticket not found"
                                )
                        );

        // Security check:
        // Student can only see their own ticket.

        if (ticket.getUser() == null
                || !ticket.getUser()
                .getId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You are not authorized to view this ticket"
            );
        }

        return ticket;
    }


    // =========================================================
    // ADMIN - GET ALL TICKETS
    // =========================================================

    @Transactional(readOnly = true)
    public List<SupportTicket> getAllTickets() {
        return supportTicketRepository.findAllTicketsWithStudentDetails();
    }


    // =========================================================
    // ADMIN - GET TICKETS BY STATUS
    // =========================================================

    @Transactional(readOnly = true)
    public List<SupportTicket> getTicketsByStatus(
            SupportTicket.TicketStatus status) {

        if (status == null) {
            throw new RuntimeException("Ticket status is required");
        }

        return supportTicketRepository
                .findTicketsWithStudentDetailsByStatus(status);
    }


    // =========================================================


    // ADMIN - GET SINGLE TICKET
    // =========================================================

    @Transactional(readOnly = true)
    public SupportTicket getTicket(Long ticketId) {

        if (ticketId == null) {
            throw new RuntimeException("Ticket ID is required");
        }

        return supportTicketRepository
                .findTicketWithStudentDetailsById(ticketId)
                .orElseThrow(() ->
                        new RuntimeException("Support ticket not found")
                );
    }


    // =========================================================
    // ADMIN - REPLY TO TICKET
    // =========================================================

    @Transactional
    public SupportTicket replyToTicket(
            Long ticketId,
            String adminReply) {

        if (ticketId == null) {
            throw new RuntimeException(
                    "Ticket ID is required"
            );
        }

        if (adminReply == null
                || adminReply.trim().isEmpty()) {

            throw new RuntimeException(
                    "Admin reply is required"
            );
        }

        SupportTicket ticket =
                supportTicketRepository
                        .findById(ticketId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Support ticket not found"
                                )
                        );

        ticket.setAdminReply(
                adminReply.trim()
        );

        ticket.setStatus(
                SupportTicket.TicketStatus.RESOLVED
        );

        return supportTicketRepository.save(ticket);
    }


    // =========================================================
    // ADMIN - UPDATE STATUS
    // =========================================================

    @Transactional
    public SupportTicket updateStatus(
            Long ticketId,
            SupportTicket.TicketStatus status) {

        if (ticketId == null) {
            throw new RuntimeException(
                    "Ticket ID is required"
            );
        }

        if (status == null) {
            throw new RuntimeException(
                    "Ticket status is required"
            );
        }

        SupportTicket ticket =
                supportTicketRepository
                        .findById(ticketId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Support ticket not found"
                                )
                        );

        ticket.setStatus(status);

        return supportTicketRepository.save(ticket);
    }


    // =========================================================
    // ADMIN DASHBOARD COUNTS
    // =========================================================

    @Transactional(readOnly = true)
    public long countOpenTickets() {

        return supportTicketRepository.countByStatus(
                SupportTicket.TicketStatus.OPEN
        );
    }


    @Transactional(readOnly = true)
    public long countInProgressTickets() {

        return supportTicketRepository.countByStatus(
                SupportTicket.TicketStatus.IN_PROGRESS
        );
    }


    @Transactional(readOnly = true)
    public long countResolvedTickets() {

        return supportTicketRepository.countByStatus(
                SupportTicket.TicketStatus.RESOLVED
        );
    }


    @Transactional(readOnly = true)
    public long countClosedTickets() {

        return supportTicketRepository.countByStatus(
                SupportTicket.TicketStatus.CLOSED
        );
    }
}