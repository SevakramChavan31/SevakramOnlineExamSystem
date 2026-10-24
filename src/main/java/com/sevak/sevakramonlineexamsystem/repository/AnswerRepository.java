package com.sevak.sevakramonlineexamsystem.repository;

import com.sevak.sevakramonlineexamsystem.entity.Answer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository
        extends JpaRepository<Answer, Long> {

    // Find all answers for an attempt.
    List<Answer> findByAttemptId(Long attemptId);

    // Find answers for a question.
    List<Answer> findByQuestionId(Long questionId);

    // Find an existing answer for an attempt and question.
    Optional<Answer> findByAttemptIdAndQuestionId(
            Long attemptId,
            Long questionId
    );

    // Fetch existing answers in bulk for optimized submission.
    @Query("""
        SELECT a
        FROM Answer a
        JOIN FETCH a.question
        WHERE a.attempt.id = :attemptId
        """)
    List<Answer> findExistingAnswersByAttemptId(
            @Param("attemptId") Long attemptId
    );

    // Fetch detailed answers for result review.
    @Query("""
        SELECT a
        FROM Answer a
        JOIN FETCH a.question q
        LEFT JOIN FETCH a.selectedOption
        WHERE a.attempt.id = :attemptId
        ORDER BY q.questionOrder ASC
        """)
    List<Answer> findDetailedAnswersByAttemptId(
            @Param("attemptId") Long attemptId
    );

    // Count answers for an attempt.
    long countByAttemptId(Long attemptId);

    // Count correct answers for an attempt.
    long countByAttemptIdAndCorrectTrue(Long attemptId);

    // Check whether a question has answers.
    boolean existsByQuestionId(Long questionId);

    // Check whether an option belonging to a question is selected.
    boolean existsBySelectedOptionQuestionId(Long questionId);
}










//package com.sevak.sevakramonlineexamsystem.repository;
//
//import com.sevak.sevakramonlineexamsystem.entity.Answer;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface AnswerRepository
//        extends JpaRepository<Answer, Long> {
//
//    // Find all answers for an attempt.
//    List<Answer> findByAttemptId(Long attemptId);
//
//    // Find answers for a question.
//    List<Answer> findByQuestionId(Long questionId);
//
//    // Find an existing answer for an attempt and question.
//    Optional<Answer> findByAttemptIdAndQuestionId(
//            Long attemptId,
//            Long questionId
//    );
//
//    // Fetch existing answers in bulk for optimized submission.
//    @Query("""
//        SELECT a
//        FROM Answer a
//        JOIN FETCH a.question
//        WHERE a.attempt.id = :attemptId
//        """)
//    List<Answer> findExistingAnswersByAttemptId(
//            @Param("attemptId") Long attemptId
//    );
//
//    // Fetch detailed answers for result review.
//    @Query("""
//        SELECT a
//        FROM Answer a
//        JOIN FETCH a.question q
//        LEFT JOIN FETCH a.selectedOption
//        WHERE a.attempt.id = :attemptId
//        ORDER BY q.questionOrder ASC
//        """)
//    List<Answer> findDetailedAnswersByAttemptId(
//            @Param("attemptId") Long attemptId
//    );
//
//    // Count answers for an attempt.
//    long countByAttemptId(Long attemptId);
//
//    // Count correct answers for an attempt.
//    long countByAttemptIdAndCorrectTrue(Long attemptId);
//
//    // Check whether a question has answers.
//    boolean existsByQuestionId(Long questionId);
//
//    // Check whether an option belonging to a question is selected.
//    boolean existsBySelectedOptionQuestionId(Long questionId);
//}