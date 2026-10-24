package com.sevak.sevakramonlineexamsystem.repository;

import com.sevak.sevakramonlineexamsystem.entity.Option;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OptionRepository
        extends JpaRepository<Option, Long> {

    // Find all options for a question.
    List<Option> findByQuestionId(Long questionId);

    // Find active options for a question.
    List<Option> findByQuestionIdAndActiveTrue(Long questionId);

    // Find correct options for a question.
    List<Option> findByQuestionIdAndCorrectTrue(Long questionId);

    // Fetch multiple options in one query.
    // The associated Question is fetched with each Option.
    @Query("""
        SELECT o
        FROM Option o
        JOIN FETCH o.question
        WHERE o.id IN :optionIds
        ORDER BY o.question.id ASC, o.optionOrder ASC, o.id ASC
        """)
    List<Option> findOptionsByIds(
            @Param("optionIds") List<Long> optionIds
    );
}





//package com.sevak.sevakramonlineexamsystem.repository;
//
//import com.sevak.sevakramonlineexamsystem.entity.Option;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//
//public interface OptionRepository
//        extends JpaRepository<Option, Long> {
//
//    // Find all options for a question.
//    List<Option> findByQuestionId(Long questionId);
//
//    // Find active options for a question.
//    List<Option> findByQuestionIdAndActiveTrue(Long questionId);
//
//    // Find correct options for a question.
//    List<Option> findByQuestionIdAndCorrectTrue(Long questionId);
//
//    // Fetch multiple options in one query.
//    // The associated Question is fetched with each Option.
//    @Query("""
//        SELECT o
//        FROM Option o
//        JOIN FETCH o.question
//        WHERE o.id IN :optionIds
//        """)
//    List<Option> findOptionsByIds(
//            @Param("optionIds") List<Long> optionIds
//    );
//}