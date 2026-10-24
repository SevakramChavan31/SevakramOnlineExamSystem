package com.sevak.sevakramonlineexamsystem.repository;

import com.sevak.sevakramonlineexamsystem.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByActiveTrue();

    List<Exam> findByActive(Boolean active);

    long countByActiveTrue();

    long countByCreatedAtGreaterThanEqual(
            LocalDateTime startDate
    );

    long countByActiveTrueAndCreatedAtGreaterThanEqual(
            LocalDateTime startDate
    );
}