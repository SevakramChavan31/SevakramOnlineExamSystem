package com.sevak.sevakramonlineexamsystem.service;

import com.sevak.sevakramonlineexamsystem.entity.Exam;
import com.sevak.sevakramonlineexamsystem.entity.ExamAttempt;
import com.sevak.sevakramonlineexamsystem.entity.User;
import com.sevak.sevakramonlineexamsystem.repository.ExamAttemptRepository;
import com.sevak.sevakramonlineexamsystem.repository.ExamRepository;
import com.sevak.sevakramonlineexamsystem.repository.UserRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class ExamAttemptService {

    private final ExamAttemptRepository examAttemptRepository;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;

    public ExamAttemptService(
            ExamAttemptRepository examAttemptRepository,
            ExamRepository examRepository,
            UserRepository userRepository) {
        this.examAttemptRepository = examAttemptRepository;
        this.examRepository = examRepository;
        this.userRepository = userRepository;
    }

    // =====================================================
    // START EXAM
    // =====================================================

    @Transactional
    public ExamAttempt startExam(Long userId, Long examId) {
        validatePositiveId(userId, "User ID");
        validatePositiveId(examId, "Exam ID");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + userId));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Exam not found: " + examId));

        if (!Boolean.TRUE.equals(exam.getActive())) {
            throw new IllegalStateException("Exam is not active.");
        }

        ExamAttempt attempt = new ExamAttempt();
        attempt.setUser(user);
        attempt.setExam(exam);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setStatus(ExamAttempt.AttemptStatus.IN_PROGRESS);

        return examAttemptRepository.save(attempt);
    }

    // =====================================================
    // GET ATTEMPT BY ID WITH USER AND EXAM
    // =====================================================

    @Transactional(readOnly = true)
    public ExamAttempt getAttemptById(Long attemptId) {
        validatePositiveId(attemptId, "Exam attempt ID");

        return examAttemptRepository.findAttemptWithExamAndUser(attemptId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Exam attempt not found: " + attemptId));
    }

    // =====================================================
    // GET ALL ATTEMPTS BY USER ID
    // =====================================================

    @Transactional(readOnly = true)
    public List<ExamAttempt> getAttemptsByUserId(Long userId) {
        validatePositiveId(userId, "User ID");

        return examAttemptRepository.findResultsByUserId(userId)
                .stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        ExamAttempt::getStartedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    // =====================================================
    // GET ATTEMPTS BY EXAM ID
    // =====================================================

    @Transactional(readOnly = true)
    public List<ExamAttempt> getAttemptsByExamId(Long examId) {
        validatePositiveId(examId, "Exam ID");
        return examAttemptRepository.findByExamId(examId);
    }

    // =====================================================
    // GET ATTEMPTS BY USER AND EXAM
    // =====================================================

    @Transactional(readOnly = true)
    public List<ExamAttempt> getAttemptsByUserAndExam(Long userId, Long examId) {
        validatePositiveId(userId, "User ID");
        validatePositiveId(examId, "Exam ID");
        return examAttemptRepository.findByUserIdAndExamId(userId, examId);
    }

    // =====================================================
    // GET ATTEMPTS BY STATUS
    // =====================================================

    @Transactional(readOnly = true)
    public List<ExamAttempt> getAttemptsByStatus(ExamAttempt.AttemptStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Attempt status is required.");
        }
        return examAttemptRepository.findByStatus(status);
    }

    // =====================================================
    // GET COMPLETED STUDENT RESULTS
    // =====================================================

    @Transactional(readOnly = true)
    public List<ExamAttempt> getCompletedAttemptsByUserId(Long userId) {
        validatePositiveId(userId, "User ID");

        return examAttemptRepository.findResultsByUserId(userId)
                .stream()
                .filter(Objects::nonNull)
                .filter(attempt -> attempt.getStatus() == ExamAttempt.AttemptStatus.COMPLETED
                        || attempt.getStatus() == ExamAttempt.AttemptStatus.AUTO_SUBMITTED)
                .sorted(Comparator.comparing(
                        ExamAttempt::getStartedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    // =====================================================
    // GET RECENT ATTEMPTS
    // =====================================================

    @Transactional(readOnly = true)
    public List<ExamAttempt> getRecentAttempts(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable information is required.");
        }
        return examAttemptRepository.findRecentAttemptsWithDetails(pageable);
    }

    private void validatePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be a positive number.");
        }
    }
}



















//package com.sevak.sevakramonlineexamsystem.service;
//
//import com.sevak.sevakramonlineexamsystem.entity.Exam;
//import com.sevak.sevakramonlineexamsystem.entity.ExamAttempt;
//import com.sevak.sevakramonlineexamsystem.entity.User;
//import com.sevak.sevakramonlineexamsystem.repository.ExamAttemptRepository;
//import com.sevak.sevakramonlineexamsystem.repository.ExamRepository;
//import com.sevak.sevakramonlineexamsystem.repository.UserRepository;
//
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.Comparator;
//import java.util.List;
//
//@Service
//public class ExamAttemptService {
//
//    private final ExamAttemptRepository examAttemptRepository;
//    private final ExamRepository examRepository;
//    private final UserRepository userRepository;
//
//    public ExamAttemptService(
//            ExamAttemptRepository examAttemptRepository,
//            ExamRepository examRepository,
//            UserRepository userRepository) {
//
//        this.examAttemptRepository = examAttemptRepository;
//        this.examRepository = examRepository;
//        this.userRepository = userRepository;
//    }
//
//    // =====================================================
//    // START EXAM
//    // =====================================================
//
//    @Transactional
//    public ExamAttempt startExam(Long userId, Long examId) {
//
//        if (userId == null || examId == null) {
//            throw new IllegalArgumentException(
//                    "User ID and Exam ID are required."
//            );
//        }
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() ->
//                        new IllegalArgumentException(
//                                "User not found: " + userId
//                        )
//                );
//
//        Exam exam = examRepository.findById(examId)
//                .orElseThrow(() ->
//                        new IllegalArgumentException(
//                                "Exam not found: " + examId
//                        )
//                );
//
//        if (!Boolean.TRUE.equals(exam.getActive())) {
//            throw new IllegalStateException(
//                    "Exam is not active."
//            );
//        }
//
//        ExamAttempt attempt = new ExamAttempt();
//
//        attempt.setUser(user);
//        attempt.setExam(exam);
//        attempt.setStartedAt(LocalDateTime.now());
//        attempt.setStatus(
//                ExamAttempt.AttemptStatus.IN_PROGRESS
//        );
//
//        return examAttemptRepository.save(attempt);
//    }
//
//    // =====================================================
//    // GET ATTEMPT BY ID WITH USER AND EXAM
//    // =====================================================
//
//    @Transactional(readOnly = true)
//    public ExamAttempt getAttemptById(Long attemptId) {
//
//        if (attemptId == null) {
//            throw new IllegalArgumentException(
//                    "Exam attempt ID is required."
//            );
//        }
//
//        return examAttemptRepository
//                .findAttemptWithExamAndUser(attemptId)
//                .orElseThrow(() ->
//                        new IllegalArgumentException(
//                                "Exam attempt not found: " + attemptId
//                        )
//                );
//    }
//
//    // =====================================================
//    // GET ALL ATTEMPTS BY USER ID
//    // =====================================================
//
//    @Transactional(readOnly = true)
//    public List<ExamAttempt> getAttemptsByUserId(Long userId) {
//
//        if (userId == null) {
//            throw new IllegalArgumentException(
//                    "User ID is required."
//            );
//        }
//
//        List<ExamAttempt> attempts =
//                examAttemptRepository.findResultsByUserId(userId);
//
//        // Sort latest attempt first.
//        attempts.sort(
//                Comparator.comparing(
//                        ExamAttempt::getStartedAt,
//                        Comparator.nullsLast(Comparator.reverseOrder())
//                )
//        );
//
//        return attempts;
//    }
//
//    // =====================================================
//    // GET ATTEMPTS BY EXAM ID
//    // =====================================================
//
//    @Transactional(readOnly = true)
//    public List<ExamAttempt> getAttemptsByExamId(Long examId) {
//
//        if (examId == null) {
//            throw new IllegalArgumentException(
//                    "Exam ID is required."
//            );
//        }
//
//        return examAttemptRepository.findByExamId(examId);
//    }
//
//    // =====================================================
//    // GET ATTEMPTS BY USER AND EXAM
//    // =====================================================
//
//    @Transactional(readOnly = true)
//    public List<ExamAttempt> getAttemptsByUserAndExam(
//            Long userId,
//            Long examId) {
//
//        if (userId == null || examId == null) {
//            throw new IllegalArgumentException(
//                    "User ID and Exam ID are required."
//            );
//        }
//
//        return examAttemptRepository
//                .findByUserIdAndExamId(userId, examId);
//    }
//
//    // =====================================================
//    // GET ATTEMPTS BY STATUS
//    // =====================================================
//
//    @Transactional(readOnly = true)
//    public List<ExamAttempt> getAttemptsByStatus(
//            ExamAttempt.AttemptStatus status) {
//
//        if (status == null) {
//            throw new IllegalArgumentException(
//                    "Attempt status is required."
//            );
//        }
//
//        return examAttemptRepository.findByStatus(status);
//    }
//
//    // =====================================================
//    // GET COMPLETED STUDENT RESULTS
//    // =====================================================
//
//    @Transactional(readOnly = true)
//    public List<ExamAttempt> getCompletedAttemptsByUserId(
//            Long userId) {
//
//        if (userId == null) {
//            throw new IllegalArgumentException(
//                    "User ID is required."
//            );
//        }
//
//        return examAttemptRepository.findResultsByUserId(userId)
//                .stream()
//                .filter(attempt ->
//                        attempt.getStatus()
//                                == ExamAttempt.AttemptStatus.COMPLETED
//                                || attempt.getStatus()
//                                == ExamAttempt.AttemptStatus.AUTO_SUBMITTED
//                )
//                .toList();
//    }
//
//    // =====================================================
//    // GET RECENT ATTEMPTS
//    // =====================================================
//
//    @Transactional(readOnly = true)
//    public List<ExamAttempt> getRecentAttempts(
//            org.springframework.data.domain.Pageable pageable) {
//
//        if (pageable == null) {
//            throw new IllegalArgumentException(
//                    "Pageable information is required."
//            );
//        }
//
//        return examAttemptRepository
//                .findRecentAttemptsWithDetails(pageable);
//    }
//}