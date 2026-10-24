package com.sevak.sevakramonlineexamsystem.controller;

import com.sevak.sevakramonlineexamsystem.entity.ExamAttempt;
import com.sevak.sevakramonlineexamsystem.entity.User;
import com.sevak.sevakramonlineexamsystem.repository.UserRepository;
import  com.sevak.sevakramonlineexamsystem.service.ExamAttemptService;
import  com.sevak.sevakramonlineexamsystem.service.ExamService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class StudentDashboardController {

    private final ExamService examService;
    private final ExamAttemptService examAttemptService;
    private final UserRepository userRepository;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public StudentDashboardController(
            ExamService examService,
            ExamAttemptService examAttemptService,
            UserRepository userRepository) {

        this.examService = examService;
        this.examAttemptService = examAttemptService;
        this.userRepository = userRepository;
    }


    // =========================================================
    // STUDENT DASHBOARD
    // =========================================================

    @GetMapping("/student/dashboard")
    public String dashboard(
            HttpSession session,
            Model model) {

        // -----------------------------------------------------
        // Get logged-in user ID
        // -----------------------------------------------------

        Long userId =
                (Long) session.getAttribute("userId");


        // -----------------------------------------------------
        // Login check
        // -----------------------------------------------------

        if (userId == null) {

            return "redirect:/login";
        }


        // =====================================================
        // GET LOGGED-IN USER
        // =====================================================

        User user =
                userRepository
                        .findById(userId)
                        .orElse(null);


        // -----------------------------------------------------
        // User not found
        // -----------------------------------------------------

        if (user == null) {

            session.invalidate();

            return "redirect:/login";
        }


        // =====================================================
        // STUDENT NAME
        // =====================================================

        String userName = user.getFullName();

        if (userName == null
                || userName.trim().isEmpty()) {

            userName = "Student";
        }


        // -----------------------------------------------------
        // Keep user name in session also
        // -----------------------------------------------------

        session.setAttribute(
                "userName",
                userName
        );


        // -----------------------------------------------------
        // Send user name to Thymeleaf
        // -----------------------------------------------------

        model.addAttribute(
                "userName",
                userName
        );


        // =====================================================
        // AVAILABLE EXAMS
        // =====================================================

        model.addAttribute(
                "exams",
                examService.getActiveExams()
        );


        // =====================================================
        // STUDENT ATTEMPTS
        // =====================================================

        List<ExamAttempt> attempts =
                examAttemptService
                        .getAttemptsByUserId(userId);


        // =====================================================
        // STATISTICS
        // =====================================================

        int totalAttempts = 0;

        int passedAttempts = 0;

        int failedAttempts = 0;

        double totalPercentage = 0.0;

        int percentageCount = 0;


        // =====================================================
        // CALCULATE STATISTICS
        // =====================================================

        for (ExamAttempt attempt : attempts) {

            // -------------------------------------------------
            // Only completed attempts are counted
            // -------------------------------------------------

            boolean completed =
                    attempt.getStatus()
                            == ExamAttempt.AttemptStatus.COMPLETED;

            boolean autoSubmitted =
                    attempt.getStatus()
                            == ExamAttempt.AttemptStatus.AUTO_SUBMITTED;


            if (!completed && !autoSubmitted) {

                continue;
            }


            // -------------------------------------------------
            // Total Attempts
            // -------------------------------------------------

            totalAttempts++;


            // -------------------------------------------------
            // Percentage
            // -------------------------------------------------

            if (attempt.getPercentage() != null) {

                totalPercentage +=
                        attempt.getPercentage();

                percentageCount++;
            }


            // -------------------------------------------------
            // Pass / Fail
            // -------------------------------------------------

            if (attempt.getScore() != null
                    && attempt.getExam() != null
                    && attempt.getExam().getPassingMarks() != null) {

                int score =
                        attempt.getScore();

                int passingMarks =
                        attempt.getExam()
                                .getPassingMarks();


                if (score >= passingMarks) {

                    passedAttempts++;

                } else {

                    failedAttempts++;
                }
            }
        }


        // =====================================================
        // AVERAGE SCORE
        // =====================================================

        double averagePercentage = 0.0;


        if (percentageCount > 0) {

            averagePercentage =
                    totalPercentage / percentageCount;
        }


        // =====================================================
        // RECENT ATTEMPTS
        // =====================================================

        List<ExamAttempt> recentAttempts;


        if (attempts.size() > 5) {

            recentAttempts =
                    attempts.subList(0, 5);

        } else {

            recentAttempts =
                    attempts;
        }


        // =====================================================
        // SEND DATA TO THYMELEAF
        // =====================================================

        model.addAttribute(
                "user",
                user
        );

        model.addAttribute(
                "userName",
                userName
        );

        model.addAttribute(
                "attempts",
                attempts
        );

        model.addAttribute(
                "recentAttempts",
                recentAttempts
        );

        model.addAttribute(
                "totalAttempts",
                totalAttempts
        );

        model.addAttribute(
                "passedAttempts",
                passedAttempts
        );

        model.addAttribute(
                "failedAttempts",
                failedAttempts
        );

        model.addAttribute(
                "averagePercentage",
                averagePercentage
        );


        // =====================================================
        // RETURN DASHBOARD
        // =====================================================

        return "student-dashboard";
    }


    // =========================================================
    // STUDENT EXAM HISTORY
    // =========================================================

    @GetMapping("/student/exam-history")
    public String examHistory(
            HttpSession session,
            Model model) {

        Long userId =
                (Long) session.getAttribute("userId");


        // -----------------------------------------------------
        // Login check
        // -----------------------------------------------------

        if (userId == null) {

            return "redirect:/login";
        }


        // -----------------------------------------------------
        // Get user
        // -----------------------------------------------------

        User user =
                userRepository
                        .findById(userId)
                        .orElse(null);


        if (user == null) {

            session.invalidate();

            return "redirect:/login";
        }


        // -----------------------------------------------------
        // Set user name
        // -----------------------------------------------------

        String userName =
                user.getFullName();

        if (userName == null
                || userName.trim().isEmpty()) {

            userName = "Student";
        }


        session.setAttribute(
                "userName",
                userName
        );

        model.addAttribute(
                "userName",
                userName
        );

        model.addAttribute(
                "user",
                user
        );


        // -----------------------------------------------------
        // Get attempts
        // -----------------------------------------------------

        List<ExamAttempt> attempts =
                examAttemptService
                        .getAttemptsByUserId(userId);


        // =====================================================
        // STATISTICS
        // =====================================================

        int totalAttempts = 0;

        int passedAttempts = 0;

        int failedAttempts = 0;

        double totalPercentage = 0.0;

        int completedAttempts = 0;


        // =====================================================
        // CALCULATE STATISTICS
        // =====================================================

        for (ExamAttempt attempt : attempts) {

            boolean completed =
                    attempt.getStatus()
                            == ExamAttempt.AttemptStatus.COMPLETED;

            boolean autoSubmitted =
                    attempt.getStatus()
                            == ExamAttempt.AttemptStatus.AUTO_SUBMITTED;


            if (!completed && !autoSubmitted) {

                continue;
            }


            totalAttempts++;


            if (attempt.getPercentage() != null) {

                totalPercentage +=
                        attempt.getPercentage();

                completedAttempts++;
            }


            if (attempt.getScore() != null
                    && attempt.getExam() != null
                    && attempt.getExam().getPassingMarks() != null) {

                if (attempt.getScore()
                        >= attempt.getExam()
                        .getPassingMarks()) {

                    passedAttempts++;

                } else {

                    failedAttempts++;
                }
            }
        }


        // =====================================================
        // AVERAGE
        // =====================================================

        double averagePercentage = 0.0;


        if (completedAttempts > 0) {

            averagePercentage =
                    totalPercentage
                            / completedAttempts;
        }


        // =====================================================
        // SEND DATA
        // =====================================================

        model.addAttribute(
                "attempts",
                attempts
        );

        model.addAttribute(
                "totalAttempts",
                totalAttempts
        );

        model.addAttribute(
                "passedAttempts",
                passedAttempts
        );

        model.addAttribute(
                "failedAttempts",
                failedAttempts
        );

        model.addAttribute(
                "averagePercentage",
                averagePercentage
        );


        return "exam-history";
    }
}