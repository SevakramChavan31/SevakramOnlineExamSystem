package com.sevak.sevakramonlineexamsystem.service;

import com.sevak.sevakramonlineexamsystem.entity.AdminNotification;
import com.sevak.sevakramonlineexamsystem.entity.ExamAttempt;
import com.sevak.sevakramonlineexamsystem.entity.SupportTicket;
import com.sevak.sevakramonlineexamsystem.entity.User;
import com.sevak.sevakramonlineexamsystem.repository.AdminNotificationRepository;
import com.sevak.sevakramonlineexamsystem.repository.ExamAttemptRepository;
import com.sevak.sevakramonlineexamsystem.repository.SupportTicketRepository;
import com.sevak.sevakramonlineexamsystem.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminNotificationService {

    private final AdminNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final SupportTicketRepository supportTicketRepository;

    public AdminNotificationService(
            AdminNotificationRepository notificationRepository,
            UserRepository userRepository,
            ExamAttemptRepository examAttemptRepository,
            SupportTicketRepository supportTicketRepository) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.supportTicketRepository = supportTicketRepository;
    }

    @Transactional
    public Map<String, Object> getNotifications() {

        syncFromExistingRecords();

        List<AdminNotification> items =
                notificationRepository.findAllByOrderByCreatedAtDesc();

        long unreadCount = notificationRepository.countByReadFalse();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", items);
        response.put("unreadCount", unreadCount);

        return response;
    }

    @Transactional
    public boolean markRead(Long id) {

        if (id == null || id <= 0) {
            return false;
        }

        AdminNotification notification =
                notificationRepository.findById(id).orElse(null);

        if (notification == null) {
            return false;
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }

        return true;
    }

    @Transactional
    public int markAllAsRead() {
        return notificationRepository.markAllAsRead();
    }

    private void syncFromExistingRecords() {

        // Existing students
        for (User student : userRepository.findByRole(User.Role.STUDENT)) {

            if (student.getId() == null || student.getCreatedAt() == null) {
                continue;
            }

            addIfMissing(
                    "STUDENT:" + student.getId(),
                    "STUDENT",
                    "New student registered",
                    safe(student.getFullName(), "A new student registered."),
                    student.getCreatedAt(),
                    "/admin/students"
            );
        }

        // Completed and automatically submitted exams
        for (ExamAttempt attempt :
                examAttemptRepository.findByStatusInOrderBySubmittedAtDesc(
                        List.of(
                                ExamAttempt.AttemptStatus.COMPLETED,
                                ExamAttempt.AttemptStatus.AUTO_SUBMITTED
                        ))) {

            if (attempt.getId() == null) {
                continue;
            }

            LocalDateTime eventTime = attempt.getSubmittedAt() != null
                    ? attempt.getSubmittedAt()
                    : attempt.getStartedAt();

            if (eventTime == null) {
                continue;
            }

            String studentName = attempt.getUser() == null
                    ? "A student"
                    : safe(attempt.getUser().getFullName(), "A student");

            String examTitle = attempt.getExam() == null
                    ? "an exam"
                    : safe(attempt.getExam().getTitle(), "an exam");

            addIfMissing(
                    "EXAM_ATTEMPT:" + attempt.getId(),
                    "EXAM",
                    "Exam submitted",
                    studentName + " submitted " + examTitle + ".",
                    eventTime,
                    "/admin/results"
            );
        }

        // Existing support tickets
        for (SupportTicket ticket :
                supportTicketRepository.findAllByOrderByCreatedAtDesc()) {

            if (ticket.getId() == null || ticket.getCreatedAt() == null) {
                continue;
            }

            addIfMissing(
                    "SUPPORT_TICKET:" + ticket.getId(),
                    "SUPPORT",
                    "New support ticket",
                    safe(
                            ticket.getSubject(),
                            "A student submitted a support ticket."
                    ),
                    ticket.getCreatedAt(),
                    "/admin/support"
            );
        }
    }

    private void addIfMissing(
            String sourceKey,
            String type,
            String title,
            String message,
            LocalDateTime createdAt,
            String targetUrl) {

        if (notificationRepository.existsBySourceKey(sourceKey)) {
            return;
        }

        AdminNotification notification = new AdminNotification();

        notification.setSourceKey(sourceKey);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setCreatedAt(createdAt);
        notification.setTargetUrl(targetUrl);
        notification.setRead(false);

        notificationRepository.save(notification);
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}










//package com.sevak.sevakramonlineexamsystem.service;
//
//import com.sevak.sevakramonlineexamsystem.entity.AdminNotification;
//import com.sevak.sevakramonlineexamsystem.entity.ExamAttempt;
//import com.sevak.sevakramonlineexamsystem.entity.SupportTicket;
//import com.sevak.sevakramonlineexamsystem.entity.User;
//import com.sevak.sevakramonlineexamsystem.repository.AdminNotificationRepository;
//import com.sevak.sevakramonlineexamsystem.repository.ExamAttemptRepository;
//import com.sevak.sevakramonlineexamsystem.repository.SupportTicketRepository;
//import com.sevak.sevakramonlineexamsystem.repository.UserRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class AdminNotificationService {
//    private final AdminNotificationRepository notificationRepository;
//    private final UserRepository userRepository;
//    private final ExamAttemptRepository examAttemptRepository;
//    private final SupportTicketRepository supportTicketRepository;
//
//    public AdminNotificationService(AdminNotificationRepository notificationRepository,
//                                    UserRepository userRepository,
//                                    ExamAttemptRepository examAttemptRepository,
//                                    SupportTicketRepository supportTicketRepository) {
//        this.notificationRepository = notificationRepository;
//        this.userRepository = userRepository;
//        this.examAttemptRepository = examAttemptRepository;
//        this.supportTicketRepository = supportTicketRepository;
//    }
//
//    @Transactional
//    public Map<String, Object> getNotifications() {
//        syncFromExistingRecords();
//        List<AdminNotification> items = notificationRepository.findAllByOrderByCreatedAtDesc();;
//        long unreadCount = notificationRepository.countByReadFalse();
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("items", items);
//        response.put("unreadCount", unreadCount);
//        return response;
//    }
//
//    @Transactional
//    public boolean markRead(Long id) {
//        AdminNotification notification = notificationRepository.findById(id).orElse(null);
//        if (notification == null) return false;
//        notification.setRead(true);
//        notificationRepository.save(notification);
//        return true;
//    }
//
//    private void syncFromExistingRecords() {
//        // Generate persistent notifications from the project's existing records only once per source record.
//        for (User student : userRepository.findByRole(User.Role.STUDENT)) {
//            if (student.getId() == null || student.getCreatedAt() == null) continue;
//            addIfMissing("STUDENT:" + student.getId(), "STUDENT", "New student registered",
//                    safe(student.getFullName(), "A new student registered."), student.getCreatedAt(), "/admin/students");
//        }
//
//        for (ExamAttempt attempt : examAttemptRepository.findByStatusInOrderBySubmittedAtDesc(List.of(
//                ExamAttempt.AttemptStatus.COMPLETED, ExamAttempt.AttemptStatus.AUTO_SUBMITTED))) {
//            if (attempt.getId() == null) continue;
//            LocalDateTime eventTime = attempt.getSubmittedAt() != null ? attempt.getSubmittedAt() : attempt.getStartedAt();
//            if (eventTime == null) continue;
//            String studentName = attempt.getUser() == null ? "A student" : safe(attempt.getUser().getFullName(), "A student");
//            String examTitle = attempt.getExam() == null ? "an exam" : safe(attempt.getExam().getTitle(), "an exam");
//            addIfMissing("EXAM_ATTEMPT:" + attempt.getId(), "EXAM", "Exam submitted",
//                    studentName + " submitted " + examTitle + ".", eventTime, "/admin/results");
//        }
//
//        for (SupportTicket ticket : supportTicketRepository.findAllByOrderByCreatedAtDesc()) {
//            if (ticket.getId() == null || ticket.getCreatedAt() == null) continue;
//            addIfMissing("SUPPORT_TICKET:" + ticket.getId(), "SUPPORT", "New support ticket",
//                    safe(ticket.getSubject(), "A student submitted a support ticket."), ticket.getCreatedAt(), "/admin/support");
//        }
//    }
//
//    private void addIfMissing(String sourceKey, String type, String title, String message,
//                              LocalDateTime createdAt, String targetUrl) {
//        if (notificationRepository.existsBySourceKey(sourceKey)) return;
//        AdminNotification item = new AdminNotification();
//        item.setSourceKey(sourceKey);
//        item.setType(type);
//        item.setTitle(title);
//        item.setMessage(message);
//        item.setCreatedAt(createdAt);
//        item.setTargetUrl(targetUrl);
//        item.setRead(false);
//        notificationRepository.save(item);
//    }
//
//    private String safe(String value, String fallback) {
//        return value == null || value.isBlank() ? fallback : value;
//    }
//    @Transactional
//    public int markAllAsRead() {
//        return notificationRepository.markAllAsRead();
//    }
//}
