package com.sevak.sevakramonlineexamsystem.controller;

import com.sevak.sevakramonlineexamsystem.entity.SupportTicket;
import com.sevak.sevakramonlineexamsystem.repository.SupportTicketRepository;
import com.sevak.sevakramonlineexamsystem.service.EmailService;
import com.sevak.sevakramonlineexamsystem.service.SupportTicketService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/support")
public class AdminSupportController {

    private final SupportTicketService supportTicketService;
    private final SupportTicketRepository supportTicketRepository;
    private final EmailService emailService;

    public AdminSupportController(
            SupportTicketService supportTicketService,
            SupportTicketRepository supportTicketRepository,
            EmailService emailService) {

        this.supportTicketService = supportTicketService;
        this.supportTicketRepository = supportTicketRepository;
        this.emailService = emailService;
    }

    // =========================================================
    // SUPPORT TICKET LIST
    // GET /admin/support
    // =========================================================

    @GetMapping({"", "/"})
    public String supportTickets(
            @RequestParam(required = false) String status,
            Model model) {

        String selectedStatus = "ALL";

        if (status != null && !status.trim().isEmpty()) {
            selectedStatus = status.trim().toUpperCase();

            if (!"ALL".equals(selectedStatus)) {
                try {
                    SupportTicket.TicketStatus ticketStatus =
                            SupportTicket.TicketStatus.valueOf(selectedStatus);

                    model.addAttribute(
                            "tickets",
                            supportTicketService.getTicketsByStatus(ticketStatus)
                    );

                } catch (IllegalArgumentException ex) {
                    selectedStatus = "ALL";
                    model.addAttribute("tickets", supportTicketService.getAllTickets());
                }
            } else {
                model.addAttribute("tickets", supportTicketService.getAllTickets());
            }
        } else {
            model.addAttribute("tickets", supportTicketService.getAllTickets());
        }

        // Ticket statistics
        model.addAttribute("openTickets", supportTicketService.countOpenTickets());
        model.addAttribute("inProgressTickets", supportTicketService.countInProgressTickets());
        model.addAttribute("resolvedTickets", supportTicketService.countResolvedTickets());
        model.addAttribute("closedTickets", supportTicketService.countClosedTickets());
        model.addAttribute("selectedStatus", selectedStatus);

        return "admin-support";
    }

    // =========================================================
    // TICKET DETAILS
    // GET /admin/support/ticket/{id}
    // =========================================================

    @GetMapping("/ticket/{id}")
    public String ticketDetails(
            @PathVariable("id") Long id,
            Model model) {

        SupportTicket ticket = supportTicketService.getTicket(id);
        model.addAttribute("ticket", ticket);

        model.addAttribute(
                "studentName",
                ticket.getUser() != null
                        ? ticket.getUser().getFullName()
                        : "Unknown Student"
        );

        model.addAttribute(
                "studentEmail",
                ticket.getUser() != null
                        ? ticket.getUser().getEmail()
                        : "No email available"
        );

        model.addAttribute(
                "studentMobile",
                ticket.getUser() != null
                        && ticket.getUser().getMobile() != null
                        ? ticket.getUser().getMobile()
                        : "Not provided"
        );

        return "admin-support-details";
    }

    // =========================================================
    // ADMIN REPLY TO TICKET
    // POST /admin/support/ticket/{id}/reply
    // =========================================================

    @PostMapping("/ticket/{id}/reply")
    public String replyToTicket(
            @PathVariable("id") Long id,
            @RequestParam("reply") String reply) {

        if (reply == null || reply.trim().isEmpty()) {
            return "redirect:/admin/support/ticket/" + id
                    + "?error=Reply+cannot+be+empty";
        }

        String cleanReply = reply.trim();

        try {
            SupportTicket ticket = supportTicketRepository
                    .findById(id)
                    .orElse(null);

            if (ticket == null) {
                return "redirect:/admin/support/ticket/" + id
                        + "?error=Ticket+not+found";
            }

            if (ticket.getUser() == null) {
                return "redirect:/admin/support/ticket/" + id
                        + "?error=Student+information+not+found";
            }

            String studentEmail = ticket.getUser().getEmail();
            String studentName = ticket.getUser().getFullName();
            String ticketSubject = ticket.getSubject();

            if (studentEmail == null || studentEmail.trim().isEmpty()) {
                return "redirect:/admin/support/ticket/" + id
                        + "?error=Student+email+not+found";
            }

            // Save the reply before attempting email delivery.
            supportTicketService.replyToTicket(id, cleanReply);

            String emailSubject =
                    "Sevakram Online Examination System - Support Ticket #"
                            + id + " Reply";

            // Pass the student's name so the email greeting is personalized.
            // Pass only the actual admin reply to avoid duplicate email content.
            try {
                emailService.sendSupportTicketReplyEmail(
                        studentEmail.trim(),
                        studentName,
                        emailSubject,
                        ticketSubject,
                        cleanReply
                );

            } catch (Exception emailEx) {
                // The reply remains saved even if email delivery fails.
                emailEx.printStackTrace();
                return "redirect:/admin/support/ticket/" + id
                        + "?success=reply&email=failed";
            }

            return "redirect:/admin/support/ticket/" + id + "?success=reply";

        } catch (Exception ex) {
            ex.printStackTrace();
            return "redirect:/admin/support/ticket/" + id
                    + "?error=Unable+to+send+reply";
        }
    }

    // =========================================================
    // UPDATE TICKET STATUS
    // POST /admin/support/ticket/{id}/status
    // =========================================================

    @PostMapping("/ticket/{id}/status")
    public String updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status) {

        if (status == null || status.trim().isEmpty()) {
            return "redirect:/admin/support/ticket/" + id
                    + "?error=Invalid+status";
        }

        try {
            SupportTicket.TicketStatus ticketStatus =
                    SupportTicket.TicketStatus.valueOf(status.trim().toUpperCase());

            supportTicketService.updateStatus(id, ticketStatus);
            return "redirect:/admin/support/ticket/" + id + "?success=status";

        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return "redirect:/admin/support/ticket/" + id
                    + "?error=Invalid+status";

        } catch (Exception ex) {
            ex.printStackTrace();
            return "redirect:/admin/support/ticket/" + id
                    + "?error=Unable+to+update+status";
        }
    }
}
