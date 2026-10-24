package com.sevak.sevakramonlineexamsystem.controller;

import com.sevak.sevakramonlineexamsystem.entity.ExamAttempt;
import com.sevak.sevakramonlineexamsystem.entity.SupportTicket;
import com.sevak.sevakramonlineexamsystem.entity.User;
import com.sevak.sevakramonlineexamsystem.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.*;

@Controller
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final SupportTicketRepository supportTicketRepository;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AdminDashboardController(
            UserRepository userRepository,
            ExamRepository examRepository,
            QuestionRepository questionRepository,
            ExamAttemptRepository examAttemptRepository,
            SupportTicketRepository supportTicketRepository) {

        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.supportTicketRepository = supportTicketRepository;
    }

    // =========================================================
    // ADMIN DASHBOARD
    // =========================================================

    @GetMapping("/admin/dashboard")
    public String dashboard(
            HttpSession session,
            Model model) {

        // =====================================================
        // GET LOGGED-IN USER
        // =====================================================

        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        // =====================================================
        // LOGIN CHECK
        // =====================================================

        if (userId == null) {
            return "redirect:/login";
        }

        // =====================================================
        // ADMIN ROLE CHECK
        // =====================================================

        if (!"ADMIN".equals(role)) {
            return "redirect:/student/dashboard";
        }

        // =====================================================
        // GET ADMIN
        // =====================================================

        User admin = userRepository.findById(userId).orElse(null);

        if (admin == null) {
            session.invalidate();
            return "redirect:/login";
        }

        // =====================================================
        // CURRENT MONTH START
        // =====================================================

        LocalDateTime startOfMonth =
                LocalDateTime.now()
                        .withDayOfMonth(1)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

        // =====================================================
        // STUDENTS
        // =====================================================

        long totalStudents =
                userRepository.countByRole(User.Role.STUDENT);

        long studentsThisMonth =
                userRepository.countByRoleAndCreatedAtGreaterThanEqual(
                        User.Role.STUDENT,
                        startOfMonth
                );

        // =====================================================
        // EXAMS
        // =====================================================

        long totalExams = examRepository.count();

        long examsThisMonth =
                examRepository.countByCreatedAtGreaterThanEqual(
                        startOfMonth
                );

        // =====================================================
        // QUESTIONS
        // =====================================================

        long totalQuestions = questionRepository.count();

        long questionsThisMonth =
                questionRepository.countByCreatedAtGreaterThanEqual(
                        startOfMonth
                );

        // =====================================================
        // EXAM ATTEMPTS
        // =====================================================

        long totalAttempts = examAttemptRepository.count();

        long attemptsThisMonth =
                examAttemptRepository.countByStartedAtGreaterThanEqual(
                        startOfMonth
                );

        // =====================================================
        // EXAM PERFORMANCE
        // =====================================================

        List<Object[]> examPerformance =
                examAttemptRepository.findExamPerformance();

        // =====================================================
        // RECENT STUDENTS
        // =====================================================

        List<User> recentStudents =
                new ArrayList<>(
                        userRepository.findByRole(User.Role.STUDENT)
                );

        recentStudents.sort(
                Comparator.comparing(
                        User::getCreatedAt,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        if (recentStudents.size() > 5) {
            recentStudents =
                    new ArrayList<>(recentStudents.subList(0, 5));
        }

        // =====================================================
        // RECENT EXAM ATTEMPTS
        // =====================================================

        List<ExamAttempt> recentAttempts =
                examAttemptRepository.findRecentAttemptsWithDetails(
                        PageRequest.of(0, 5)
                );

        // =====================================================
        // SUPPORT TICKETS
        // =====================================================

        long openTickets =
                supportTicketRepository.countByStatus(
                        SupportTicket.TicketStatus.OPEN
                );

        long inProgressTickets =
                supportTicketRepository.countByStatus(
                        SupportTicket.TicketStatus.IN_PROGRESS
                );

        long resolvedTickets =
                supportTicketRepository.countByStatus(
                        SupportTicket.TicketStatus.RESOLVED
                );

        // =====================================================
        // RECENT SUPPORT TICKETS
        // =====================================================

        List<SupportTicket> recentTickets =
                new ArrayList<>(
                        supportTicketRepository.findAllByOrderByCreatedAtDesc()
                );

        if (recentTickets.size() > 5) {
            recentTickets =
                    new ArrayList<>(recentTickets.subList(0, 5));
        }

        // =====================================================
        // ADMIN NAME
        // =====================================================

        String adminName = admin.getFullName();

        if (adminName == null || adminName.trim().isEmpty()) {
            adminName = "Administrator";
        }

        session.setAttribute("userName", adminName);

        // =====================================================
        // ADMIN INFORMATION
        // =====================================================

        model.addAttribute("admin", admin);
        model.addAttribute("adminName", adminName);

        // =====================================================
        // STUDENT STATISTICS
        // =====================================================

        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("studentsThisMonth", studentsThisMonth);

        // =====================================================
        // EXAM STATISTICS
        // =====================================================

        model.addAttribute("totalExams", totalExams);
        model.addAttribute("examsThisMonth", examsThisMonth);

        // =====================================================
        // QUESTION STATISTICS
        // =====================================================

        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("questionsThisMonth", questionsThisMonth);

        // =====================================================
        // ATTEMPT STATISTICS
        // =====================================================

        model.addAttribute("totalAttempts", totalAttempts);
        model.addAttribute("attemptsThisMonth", attemptsThisMonth);

        // =====================================================
        // EXAM PERFORMANCE
        // =====================================================

        model.addAttribute("examPerformance", examPerformance);

        // =====================================================
        // RECENT STUDENTS
        // =====================================================

        model.addAttribute("recentStudents", recentStudents);

        // =====================================================
        // RECENT ATTEMPTS
        // =====================================================

        model.addAttribute("recentAttempts", recentAttempts);

        // =====================================================
        // SUPPORT STATISTICS
        // =====================================================

        model.addAttribute("openTickets", openTickets);
        model.addAttribute("inProgressTickets", inProgressTickets);
        model.addAttribute("resolvedTickets", resolvedTickets);

        // =====================================================
        // RECENT SUPPORT TICKETS
        // =====================================================

        model.addAttribute("recentTickets", recentTickets);

        // =====================================================
        // RETURN ADMIN DASHBOARD
        // =====================================================

        return "admin-dashboard";
    }

    // =========================================================
    // ADMIN NOTIFICATIONS - RECENT ACTIVITY
    // =========================================================

    @GetMapping("/admin/notifications/recent")
    @ResponseBody
    public List<Map<String, Object>> recentNotifications(
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (userId == null || !"ADMIN".equals(role)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Please log in as an administrator."
            );
        }

        List<Map<String, Object>> notifications = new ArrayList<>();

        // =====================================================
        // RECENT STUDENT REGISTRATIONS
        // =====================================================

        List<User> students =
                userRepository.findByRole(User.Role.STUDENT);

        for (User student : students) {

            if (student.getCreatedAt() == null) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();

            item.put("type", "STUDENT");
            item.put("title", "New student registered");

            item.put(
                    "message",
                    student.getFullName() == null
                            ? "A new student registered."
                            : student.getFullName() + " registered."
            );

            item.put("createdAt", student.getCreatedAt());
            item.put("targetUrl", "/admin/students");
            item.put("sortTime", student.getCreatedAt());

            notifications.add(item);
        }

        // =====================================================
        // RECENT SUBMITTED EXAM ATTEMPTS
        // =====================================================

        List<ExamAttempt> attempts =
                examAttemptRepository.findByStatusInOrderBySubmittedAtDesc(
                        List.of(
                                ExamAttempt.AttemptStatus.COMPLETED,
                                ExamAttempt.AttemptStatus.AUTO_SUBMITTED
                        )
                );

        for (ExamAttempt attempt : attempts) {

            LocalDateTime eventTime =
                    attempt.getSubmittedAt() != null
                            ? attempt.getSubmittedAt()
                            : attempt.getStartedAt();

            if (eventTime == null) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();

            item.put("type", "EXAM");
            item.put("title", "Exam submitted");

            String studentName =
                    attempt.getUser() != null
                            && attempt.getUser().getFullName() != null
                            ? attempt.getUser().getFullName()
                            : "A student";

            String examTitle =
                    attempt.getExam() != null
                            && attempt.getExam().getTitle() != null
                            ? attempt.getExam().getTitle()
                            : "an exam";

            item.put(
                    "message",
                    studentName + " submitted " + examTitle + "."
            );

            item.put("createdAt", eventTime);
            item.put("targetUrl", "/admin/results");
            item.put("sortTime", eventTime);

            notifications.add(item);
        }

        // =====================================================
        // RECENT SUPPORT TICKETS
        // =====================================================

        List<SupportTicket> tickets =
                supportTicketRepository.findAllByOrderByCreatedAtDesc();

        for (SupportTicket ticket : tickets) {

            if (ticket.getCreatedAt() == null) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();

            item.put("type", "SUPPORT");
            item.put("title", "New support ticket");

            item.put(
                    "message",
                    ticket.getSubject() == null
                            ? "A student submitted a support ticket."
                            : ticket.getSubject()
            );

            item.put("createdAt", ticket.getCreatedAt());
            item.put("targetUrl", "/admin/support");
            item.put("sortTime", ticket.getCreatedAt());

            notifications.add(item);
        }

        // =====================================================
        // SORT NOTIFICATIONS BY LATEST ACTIVITY
        // =====================================================

        notifications.sort((first, second) -> {

            LocalDateTime firstTime =
                    (LocalDateTime) first.get("sortTime");

            LocalDateTime secondTime =
                    (LocalDateTime) second.get("sortTime");

            return secondTime.compareTo(firstTime);
        });

        // =====================================================
        // RETURN TOP 10 NOTIFICATIONS
        // =====================================================

        List<Map<String, Object>> recent = new ArrayList<>();

        for (int i = 0; i < notifications.size() && i < 10; i++) {

            Map<String, Object> item = notifications.get(i);

            item.remove("sortTime");

            recent.add(item);
        }

        return recent;
    }
}