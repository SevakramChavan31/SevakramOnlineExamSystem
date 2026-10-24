package com.sevak.sevakramonlineexamsystem.repository;

import com.sevak.sevakramonlineexamsystem.entity.ExamAttempt;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamAttemptRepository
        extends JpaRepository<ExamAttempt, Long> {

    // =========================================================
    // BASIC FIND METHODS
    // =========================================================

    List<ExamAttempt> findByUserId(Long userId);

    List<ExamAttempt> findByExamId(Long examId);

    List<ExamAttempt> findByUserIdAndExamId(
            Long userId,
            Long examId
    );

    // Check whether an exam has any student attempts.
    boolean existsByExamId(Long examId);

    // =========================================================
    // FIND ATTEMPTS BY STATUS
    // =========================================================

    List<ExamAttempt> findByStatus(
            ExamAttempt.AttemptStatus status
    );

    List<ExamAttempt> findByStatusInOrderBySubmittedAtDesc(
            List<ExamAttempt.AttemptStatus> statuses
    );

    // Fetch user and exam for admin results and CSV export.
    @Query("""
        SELECT a
        FROM ExamAttempt a
        JOIN FETCH a.user
        JOIN FETCH a.exam
        WHERE a.status IN :statuses
        ORDER BY a.submittedAt DESC, a.startedAt DESC
        """)
    List<ExamAttempt> findResultsWithDetailsByStatuses(
            @Param("statuses")
            List<ExamAttempt.AttemptStatus> statuses
    );

    // =========================================================
    // STUDENT RESULTS
    // =========================================================

    @Query("""
        SELECT a
        FROM ExamAttempt a
        JOIN FETCH a.exam
        WHERE a.user.id = :userId
        ORDER BY a.startedAt DESC
        """)
    List<ExamAttempt> findResultsByUserId(
            @Param("userId") Long userId
    );

    // =========================================================
    // FETCH A SINGLE ATTEMPT WITH EXAM AND USER
    // =========================================================

    @Query("""
        SELECT a
        FROM ExamAttempt a
        JOIN FETCH a.exam
        JOIN FETCH a.user
        WHERE a.id = :attemptId
        """)
    Optional<ExamAttempt> findAttemptWithExamAndUser(
            @Param("attemptId") Long attemptId
    );

    // =========================================================
    // RECENT ATTEMPTS
    // =========================================================

    @Query("""
        SELECT a
        FROM ExamAttempt a
        JOIN FETCH a.user
        JOIN FETCH a.exam
        ORDER BY a.startedAt DESC
        """)
    List<ExamAttempt> findRecentAttemptsWithDetails(
            Pageable pageable
    );

    // =========================================================
    // DASHBOARD COUNTS
    // =========================================================

    long countByStartedAtGreaterThanEqual(
            LocalDateTime startDate
    );

    long countByUserIdAndStatus(
            Long userId,
            ExamAttempt.AttemptStatus status
    );

    long countByExamIdAndStatus(
            Long examId,
            ExamAttempt.AttemptStatus status
    );

    // =========================================================
    // COMPLETED ATTEMPTS BY STUDENT
    // =========================================================

    @Query("""
        SELECT COUNT(a)
        FROM ExamAttempt a
        WHERE a.user.id = :userId
          AND a.status IN :statuses
        """)
    long countCompletedAttemptsByUserId(
            @Param("userId") Long userId,
            @Param("statuses")
            List<ExamAttempt.AttemptStatus> statuses
    );

    // =========================================================
    // AVERAGE PERCENTAGE BY STUDENT
    // =========================================================

    @Query("""
        SELECT AVG(a.percentage)
        FROM ExamAttempt a
        WHERE a.user.id = :userId
          AND a.status IN :statuses
          AND a.percentage IS NOT NULL
        """)
    Double getAveragePercentageByUserId(
            @Param("userId") Long userId,
            @Param("statuses")
            List<ExamAttempt.AttemptStatus> statuses
    );

    // =========================================================
    // EXAM-WISE PERFORMANCE FOR ADMIN DASHBOARD
    // =========================================================

    @Query("""
        SELECT
            a.exam.id,
            a.exam.title,
            COUNT(a),
            AVG(a.percentage)
        FROM ExamAttempt a
        WHERE a.status IN (
            com.sevak.sevakramonlineexamsystem.entity.ExamAttempt.AttemptStatus.COMPLETED,
            com.sevak.sevakramonlineexamsystem.entity.ExamAttempt.AttemptStatus.AUTO_SUBMITTED
        )
        GROUP BY a.exam.id, a.exam.title
        ORDER BY COUNT(a) DESC
        """)
    List<Object[]> findExamPerformance();

    // =========================================================
    // REPORT ATTEMPTS
    // =========================================================

    @Query("""
        SELECT a
        FROM ExamAttempt a
        JOIN FETCH a.exam e
        JOIN FETCH a.user u
        WHERE (:examId IS NULL OR e.id = :examId)
          AND a.status IN :statuses
          AND (:fromDateTime IS NULL OR a.submittedAt >= :fromDateTime)
          AND (:toDateTime IS NULL OR a.submittedAt < :toDateTime)
        ORDER BY a.submittedAt DESC, a.startedAt DESC
        """)
    List<ExamAttempt> findReportAttempts(
            @Param("examId") Long examId,
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime,
            @Param("statuses")
            List<ExamAttempt.AttemptStatus> statuses
    );
}


















//package com.sevak.sevakramonlineexamsystem.repository;
//
//import com.sevak.sevakramonlineexamsystem.entity.ExamAttempt;
//
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface ExamAttemptRepository
//        extends JpaRepository<ExamAttempt, Long> {
//
//    // =========================================================
//    // BASIC FIND METHODS
//    // =========================================================
//
//    List<ExamAttempt> findByUserId(Long userId);
//
//    List<ExamAttempt> findByExamId(Long examId);
//
//    List<ExamAttempt> findByUserIdAndExamId(
//            Long userId,
//            Long examId
//    );
//
//    // Check whether an exam has any student attempts.
//    boolean existsByExamId(Long examId);
//
//    // =========================================================
//    // FIND ATTEMPTS BY STATUS
//    // =========================================================
//
//    List<ExamAttempt> findByStatus(
//            ExamAttempt.AttemptStatus status
//    );
//
//    List<ExamAttempt> findByStatusInOrderBySubmittedAtDesc(
//            List<ExamAttempt.AttemptStatus> statuses
//    );
//
//    // Fetch user and exam for admin results and CSV export.
//    @Query("""
//        SELECT a
//        FROM ExamAttempt a
//        JOIN FETCH a.user
//        JOIN FETCH a.exam
//        WHERE a.status IN :statuses
//        ORDER BY a.submittedAt DESC
//        """)
//    List<ExamAttempt> findResultsWithDetailsByStatuses(
//            @Param("statuses")
//            List<ExamAttempt.AttemptStatus> statuses
//    );
//
//    // =========================================================
//    // STUDENT RESULTS
//    // =========================================================
//
//    @Query("""
//        SELECT a
//        FROM ExamAttempt a
//        JOIN FETCH a.exam
//        WHERE a.user.id = :userId
//        ORDER BY a.startedAt DESC
//        """)
//    List<ExamAttempt> findResultsByUserId(
//            @Param("userId") Long userId
//    );
//
//    // =========================================================
//    // FETCH A SINGLE ATTEMPT WITH EXAM AND USER
//    // =========================================================
//
//    @Query("""
//        SELECT a
//        FROM ExamAttempt a
//        JOIN FETCH a.exam
//        JOIN FETCH a.user
//        WHERE a.id = :attemptId
//        """)
//    Optional<ExamAttempt> findAttemptWithExamAndUser(
//            @Param("attemptId") Long attemptId
//    );
//
//    // =========================================================
//    // RECENT ATTEMPTS
//    // =========================================================
//
//    @Query("""
//        SELECT a
//        FROM ExamAttempt a
//        JOIN FETCH a.user
//        JOIN FETCH a.exam
//        ORDER BY a.startedAt DESC
//        """)
//    List<ExamAttempt> findRecentAttemptsWithDetails(
//            Pageable pageable
//    );
//
//    // =========================================================
//    // DASHBOARD COUNTS
//    // =========================================================
//
//    long countByStartedAtGreaterThanEqual(
//            LocalDateTime startDate
//    );
//
//    long countByUserIdAndStatus(
//            Long userId,
//            ExamAttempt.AttemptStatus status
//    );
//
//    long countByExamIdAndStatus(
//            Long examId,
//            ExamAttempt.AttemptStatus status
//    );
//
//    // =========================================================
//    // COMPLETED ATTEMPTS BY STUDENT
//    // =========================================================
//
//    @Query("""
//        SELECT COUNT(a)
//        FROM ExamAttempt a
//        WHERE a.user.id = :userId
//          AND a.status IN :statuses
//        """)
//    long countCompletedAttemptsByUserId(
//            @Param("userId") Long userId,
//            @Param("statuses")
//            List<ExamAttempt.AttemptStatus> statuses
//    );
//
//    // =========================================================
//    // AVERAGE PERCENTAGE BY STUDENT
//    // =========================================================
//
//    @Query("""
//        SELECT AVG(a.percentage)
//        FROM ExamAttempt a
//        WHERE a.user.id = :userId
//          AND a.status IN :statuses
//          AND a.percentage IS NOT NULL
//        """)
//    Double getAveragePercentageByUserId(
//            @Param("userId") Long userId,
//            @Param("statuses")
//            List<ExamAttempt.AttemptStatus> statuses
//    );
//
//    // =========================================================
//    // EXAM-WISE PERFORMANCE FOR ADMIN DASHBOARD
//    // =========================================================
//
//    @Query("""
//        SELECT
//            a.exam.id,
//            a.exam.title,
//            COUNT(a),
//            AVG(a.percentage)
//        FROM ExamAttempt a
//        WHERE a.status IN (
//            com.sevak.sevakramonlineexamsystem.entity.ExamAttempt.AttemptStatus.COMPLETED,
//            com.sevak.sevakramonlineexamsystem.entity.ExamAttempt.AttemptStatus.AUTO_SUBMITTED
//        )
//        GROUP BY a.exam.id, a.exam.title
//        ORDER BY COUNT(a) DESC
//        """)
//    List<Object[]> findExamPerformance();
//
//    // =========================================================
//    // REPORT ATTEMPTS
//    // =========================================================
//
//    @Query("""
//        SELECT a
//        FROM ExamAttempt a
//        JOIN FETCH a.exam e
//        JOIN FETCH a.user u
//        WHERE (:examId IS NULL OR e.id = :examId)
//          AND a.status IN :statuses
//          AND (:fromDateTime IS NULL OR a.submittedAt >= :fromDateTime)
//          AND (:toDateTime IS NULL OR a.submittedAt < :toDateTime)
//        ORDER BY a.submittedAt DESC
//        """)
//    List<ExamAttempt> findReportAttempts(
//            @Param("examId") Long examId,
//            @Param("fromDateTime") LocalDateTime fromDateTime,
//            @Param("toDateTime") LocalDateTime toDateTime,
//            @Param("statuses")
//            List<ExamAttempt.AttemptStatus> statuses
//    );
//}