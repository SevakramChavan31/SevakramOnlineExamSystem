package com.sevak.sevakramonlineexamsystem.repository;

import com.sevak.sevakramonlineexamsystem.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {

    Optional<Result> findByAttemptId(Long attemptId);

    List<Result> findByPassed(Boolean passed);

    List<Result> findByAttemptUserId(Long userId);

    List<Result> findByAttemptExamId(Long examId);
}