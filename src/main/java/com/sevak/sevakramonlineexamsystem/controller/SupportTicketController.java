package com.sevak.sevakramonlineexamsystem.controller;

import com.sevak.sevakramonlineexamsystem.entity.SupportTicket;
import com.sevak.sevakramonlineexamsystem.service.SupportTicketService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/student/support")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    public SupportTicketController(
            SupportTicketService supportTicketService) {

        this.supportTicketService = supportTicketService;
    }


    // =========================================================
    // HELP & SUPPORT PAGE
    // =========================================================

    @GetMapping
    public String supportPage(
            HttpSession session,
            Model model) {

        Long userId = getUserId(session);

        model.addAttribute(
                "tickets",
                supportTicketService.getStudentTickets(userId)
        );

        return "student-support";
    }


    // =========================================================
    // CREATE SUPPORT TICKET
    // =========================================================

    @PostMapping("/create")
    public String createTicket(
            @RequestParam String subject,
            @RequestParam String message,
            HttpSession session,
            Model model) {

        Long userId = getUserId(session);

        try {

            supportTicketService.createTicket(
                    userId,
                    subject,
                    message
            );

            return "redirect:/student/support?success=ticket";

        } catch (RuntimeException ex) {

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );

            model.addAttribute(
                    "tickets",
                    supportTicketService.getStudentTickets(userId)
            );

            model.addAttribute(
                    "subject",
                    subject
            );

            model.addAttribute(
                    "message",
                    message
            );

            return "student-support";
        }
    }


    // =========================================================
    // VIEW SINGLE TICKET
    // =========================================================

    @GetMapping("/ticket/{id}")
    public String viewTicket(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        Long userId = getUserId(session);

        try {

            SupportTicket ticket =
                    supportTicketService.getStudentTicket(
                            userId,
                            id
                    );

            model.addAttribute(
                    "ticket",
                    ticket
            );

            return "student-support-details";

        } catch (RuntimeException ex) {

            return "redirect:/student/support?error="
                    + ex.getMessage();
        }
    }


    // =========================================================
    // GET USER ID FROM SESSION
    // =========================================================

    private Long getUserId(HttpSession session) {

        Object userIdObject =
                session.getAttribute("userId");

        if (userIdObject == null) {

            throw new RuntimeException(
                    "Please login to continue"
            );
        }

        if (userIdObject instanceof Long) {

            return (Long) userIdObject;
        }

        if (userIdObject instanceof Integer) {

            return ((Integer) userIdObject).longValue();
        }

        if (userIdObject instanceof String) {

            try {

                return Long.parseLong(
                        (String) userIdObject
                );

            } catch (NumberFormatException ex) {

                throw new RuntimeException(
                        "Invalid user session"
                );
            }
        }

        throw new RuntimeException(
                "Invalid user session"
        );
    }
}