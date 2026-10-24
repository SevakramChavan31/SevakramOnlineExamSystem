package com.sevak.sevakramonlineexamsystem.service;

import com.sevak.sevakramonlineexamsystem.entity.ExamAttempt;
import com.sevak.sevakramonlineexamsystem.entity.User;
import com.sevak.sevakramonlineexamsystem.repository.ExamAttemptRepository;
import com.sevak.sevakramonlineexamsystem.repository.ExamRepository;
import com.sevak.sevakramonlineexamsystem.repository.QuestionRepository;
import com.sevak.sevakramonlineexamsystem.repository.UserRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository examAttemptRepository;

    public AdminDashboardService(
            UserRepository userRepository,
            ExamRepository examRepository,
            QuestionRepository questionRepository,
            ExamAttemptRepository examAttemptRepository) {

        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examAttemptRepository = examAttemptRepository;
    }

    // =========================
    // TOTAL COUNTS
    // =========================

    public long getTotalStudents() {
        return userRepository.countByRole(User.Role.STUDENT);
    }

    public long getTotalExams() {
        return examRepository.count();
    }

    public long getTotalQuestions() {
        return questionRepository.count();
    }

    public long getTotalAttempts() {
        return examAttemptRepository.count();
    }

    // =========================
    // RECENT EXAM ATTEMPTS
    // =========================
    public List<ExamAttempt> getRecentAttempts() {
        return examAttemptRepository.findRecentAttemptsWithDetails(
                PageRequest.of(0, 5)
        );
    }

    // =========================
    // EXAM PERFORMANCE
    // =========================

    public List<Object[]> getExamPerformance() {
        return examAttemptRepository.findExamPerformance();
    }

    // =========================
    // THIS MONTH COUNTS
    // =========================

    public long getStudentsThisMonth() {

        LocalDateTime startOfMonth =
                LocalDate.now()
                        .withDayOfMonth(1)
                        .atStartOfDay();

        return userRepository.countByRoleAndCreatedAtGreaterThanEqual(
                User.Role.STUDENT,
                startOfMonth
        );
    }

    public long getExamsThisMonth() {

        LocalDateTime startOfMonth =
                LocalDate.now()
                        .withDayOfMonth(1)
                        .atStartOfDay();

        return examRepository.countByCreatedAtGreaterThanEqual(
                startOfMonth
        );
    }

    public long getQuestionsThisMonth() {

        LocalDateTime startOfMonth =
                LocalDate.now()
                        .withDayOfMonth(1)
                        .atStartOfDay();

        return questionRepository.countByCreatedAtGreaterThanEqual(
                startOfMonth
        );
    }

    public long getAttemptsThisMonth() {

        LocalDateTime startOfMonth =
                LocalDate.now()
                        .withDayOfMonth(1)
                        .atStartOfDay();

        return examAttemptRepository.countByStartedAtGreaterThanEqual(
                startOfMonth
        );
    }
}