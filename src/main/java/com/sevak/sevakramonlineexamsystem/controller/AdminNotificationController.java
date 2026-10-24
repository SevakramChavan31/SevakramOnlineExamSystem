package com.sevak.sevakramonlineexamsystem.controller;

import com.sevak.sevakramonlineexamsystem.service.AdminNotificationService;import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/admin/notifications")
public class AdminNotificationController {
    private final AdminNotificationService notificationService;

    public AdminNotificationController(AdminNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Map<String, Object> getNotifications(HttpSession session) {
        requireAdmin(session);
        return notificationService.getNotifications();
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id, HttpSession session) {
        requireAdmin(session);
        return notificationService.markRead(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private void requireAdmin(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null || !"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in as an administrator.");
        }
    }
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead(HttpSession session) {
        requireAdmin(session);
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }
}
