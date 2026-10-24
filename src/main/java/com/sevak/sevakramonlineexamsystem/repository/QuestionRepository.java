package com.sevak.sevakramonlineexamsystem.repository;

import com.sevak.sevakramonlineexamsystem.entity.Question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QuestionRepository
        extends JpaRepository<Question, Long> {

    // Find all questions for an exam.
    List<Question> findByExamId(Long examId);

    // Find active questions for an exam.
    List<Question> findByExamIdAndActiveTrue(Long examId);

    // Count questions belonging to an exam.
    long countByExamId(Long examId);

    // Count questions created after a given date.
    long countByCreatedAtGreaterThanEqual(
            LocalDateTime startDate
    );

    // Fetch active questions in a consistent display order.
    @Query("""
        SELECT q
        FROM Question q
        WHERE q.exam.id = :examId
          AND q.active = true
        ORDER BY q.questionOrder ASC, q.id ASC
        """)
    List<Question> findActiveQuestionsForSubmission(
            @Param("examId") Long examId
    );
}











//package com.sevak.sevakramonlineexamsystem.repository;
//
//import com.sevak.sevakramonlineexamsystem.entity.Question;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//public interface QuestionRepository
//        extends JpaRepository<Question, Long> {
//
//    // Find all questions for an exam.
//    List<Question> findByExamId(Long examId);
//
//    // Find active questions for an exam.
//    List<Question> findByExamIdAndActiveTrue(Long examId);
//
//    // Count questions belonging to an exam.
//    long countByExamId(Long examId);
//
//    // Count questions created after a given date.
//    long countByCreatedAtGreaterThanEqual(
//            LocalDateTime startDate
//    );
//
//    // Fetch active questions in display order for exam submission.
//    @Query("""
//        SELECT q
//        FROM Question q
//        WHERE q.exam.id = :examId
//          AND q.active = true
//        ORDER BY q.questionOrder ASC
//        """)
//    List<Question> findActiveQuestionsForSubmission(
//            @Param("examId") Long examId
//    );
//}