package com.sevak.sevakramonlineexamsystem.service;

import com.sevak.sevakramonlineexamsystem.entity.ExamAttempt;
import com.sevak.sevakramonlineexamsystem.entity.User;
import com.sevak.sevakramonlineexamsystem.repository.ExamAttemptRepository;
import com.sevak.sevakramonlineexamsystem.repository.ExamRepository;
import com.sevak.sevakramonlineexamsystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final UserRepository userRepository;

    public ReportService(
            ExamRepository examRepository,
            ExamAttemptRepository examAttemptRepository,
            UserRepository userRepository) {

        this.examRepository = examRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // MAIN REPORT
    // =========================================================

    public Map<String, Object> getReport(
            Long examId,
            LocalDate fromDate,
            LocalDate toDate) {

        Map<String, Object> report =
                new HashMap<>();

        // ---------------------------------------------------------
        // Validate date range
        // ---------------------------------------------------------

        if (fromDate != null &&
                toDate != null &&
                fromDate.isAfter(toDate)) {

            fromDate = null;
            toDate = null;
        }

        // ---------------------------------------------------------
        // Get ALL filtered attempts
        // ---------------------------------------------------------

        List<ExamAttempt> attempts =
                getFilteredAttempts(
                        examId,
                        fromDate,
                        toDate
                );

        // =========================================================
        // TOTAL EXAMS
        // =========================================================

        long totalExams;

        if (examId != null) {
            totalExams = 1;
        } else {
            totalExams =
                    examRepository.count();
        }

        report.put(
                "totalExams",
                totalExams
        );

        // =========================================================
        // TOTAL STUDENTS
        // =========================================================

        long totalStudents =
                userRepository.countByRole(
                        User.Role.STUDENT
                );

        report.put(
                "totalStudents",
                totalStudents
        );

        // =========================================================
        // TOTAL ATTEMPTS
        // =========================================================

        long totalAttempts =
                attempts.size();

        report.put(
                "totalAttempts",
                totalAttempts
        );

        // =========================================================
        // AVERAGE SCORE
        // =========================================================

        double averageScore =
                calculateAveragePercentage(
                        attempts
                );

        report.put(
                "averageScore",
                averageScore
        );

        // =========================================================
        // PASS / FAIL
        // =========================================================

        long passedCount = 0;
        long failedCount = 0;

        for (ExamAttempt attempt : attempts) {

            if (isPassed(attempt)) {

                passedCount++;

            } else {

                failedCount++;
            }
        }

        report.put(
                "passedCount",
                passedCount
        );

        report.put(
                "failedCount",
                failedCount
        );

        // =========================================================
        // PASS RATE
        // =========================================================

        double passRate = 0.0;

        if (!attempts.isEmpty()) {

            passRate =
                    (passedCount * 100.0)
                            / attempts.size();
        }

        report.put(
                "passRate",
                round(passRate)
        );

        // =========================================================
        // FAIL RATE
        // =========================================================

        double failRate = 0.0;

        if (!attempts.isEmpty()) {

            failRate =
                    (failedCount * 100.0)
                            / attempts.size();
        }

        report.put(
                "failRate",
                round(failRate)
        );

        // =========================================================
        // EXAM-WISE PERFORMANCE
        // =========================================================

        List<ExamPerformance> examPerformance =
                buildExamPerformance(
                        attempts
                );

        report.put(
                "examPerformance",
                examPerformance
        );

        // =========================================================
        // RECENT ATTEMPTS
        // =========================================================

        List<ExamAttempt> recentAttempts =
                getRecentAttempts(
                        attempts
                );

        report.put(
                "recentAttempts",
                recentAttempts
        );

        // =========================================================
        // ALL FILTERED ATTEMPTS
        // =========================================================

        /*
         * Used by CSV export.
         *
         * Do NOT limit this list to 10.
         */
        report.put(
                "allAttempts",
                attempts
        );

        // =========================================================
        // COMPLETED ATTEMPTS
        // =========================================================

        report.put(
                "completedAttempts",
                attempts.size()
        );

        // =========================================================
        // ANSWER ANALYTICS
        // =========================================================

        long correctAnswers =
                attempts.stream()
                        .filter(
                                a ->
                                        a.getCorrectAnswers()
                                                != null
                        )
                        .mapToLong(
                                ExamAttempt::getCorrectAnswers
                        )
                        .sum();

        long wrongAnswers =
                attempts.stream()
                        .filter(
                                a ->
                                        a.getWrongAnswers()
                                                != null
                        )
                        .mapToLong(
                                ExamAttempt::getWrongAnswers
                        )
                        .sum();

        long unanswered =
                attempts.stream()
                        .filter(
                                a ->
                                        a.getUnanswered()
                                                != null
                        )
                        .mapToLong(
                                ExamAttempt::getUnanswered
                        )
                        .sum();

        report.put(
                "correctAnswers",
                correctAnswers
        );

        report.put(
                "wrongAnswers",
                wrongAnswers
        );

        report.put(
                "unanswered",
                unanswered
        );

        return report;
    }

    // =========================================================
    // GET ALL FILTERED ATTEMPTS
    // =========================================================

    public List<ExamAttempt> getFilteredAttempts(
            Long examId,
            LocalDate fromDate,
            LocalDate toDate) {

        // ---------------------------------------------------------
        // Validate date range
        // ---------------------------------------------------------

        if (fromDate != null &&
                toDate != null &&
                fromDate.isAfter(toDate)) {

            return new ArrayList<>();
        }

        // ---------------------------------------------------------
        // Convert LocalDate to LocalDateTime
        // ---------------------------------------------------------

        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (fromDate != null) {

            fromDateTime =
                    fromDate.atStartOfDay();
        }

        if (toDate != null) {

            /*
             * Exclusive next-day boundary.
             *
             * Example:
             *
             * To Date = 08-Oct
             *
             * Query:
             *
             * submittedAt < 09-Oct 00:00
             *
             * This includes the complete 08-Oct.
             */
            toDateTime =
                    toDate
                            .plusDays(1)
                            .atStartOfDay();
        }

        // ---------------------------------------------------------
        // Allowed statuses
        // ---------------------------------------------------------

        List<ExamAttempt.AttemptStatus> statuses =
                List.of(
                        ExamAttempt.AttemptStatus.COMPLETED,
                        ExamAttempt.AttemptStatus.AUTO_SUBMITTED
                );

        // ---------------------------------------------------------
        // Fetch filtered attempts from DB
        // ---------------------------------------------------------

        List<ExamAttempt> attempts =
                examAttemptRepository.findReportAttempts(
                        examId,
                        fromDateTime,
                        toDateTime,
                        statuses
                );

        if (attempts == null) {

            return new ArrayList<>();
        }

        // ---------------------------------------------------------
        // Make mutable copy
        // ---------------------------------------------------------

        List<ExamAttempt> result =
                new ArrayList<>(
                        attempts
                );

        // ---------------------------------------------------------
        // Sort latest first
        // ---------------------------------------------------------

        result.sort(
                Comparator.comparing(
                        this::getResultDate,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        return result;
    }

    // =========================================================
    // GET RECENT 10 ATTEMPTS
    // =========================================================

    private List<ExamAttempt> getRecentAttempts(
            List<ExamAttempt> attempts) {

        if (attempts == null ||
                attempts.isEmpty()) {

            return new ArrayList<>();
        }

        List<ExamAttempt> recentAttempts =
                new ArrayList<>(
                        attempts
                );

        // Latest first
        recentAttempts.sort(
                Comparator.comparing(
                        this::getResultDate,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        // Only latest 10 for UI
        if (recentAttempts.size() > 10) {

            return new ArrayList<>(
                    recentAttempts.subList(
                            0,
                            10
                    )
            );
        }

        return recentAttempts;
    }

    // =========================================================
    // AVERAGE PERCENTAGE
    // =========================================================

    private double calculateAveragePercentage(
            List<ExamAttempt> attempts) {

        if (attempts == null ||
                attempts.isEmpty()) {

            return 0.0;
        }

        double total = 0.0;

        int count = 0;

        for (ExamAttempt attempt : attempts) {

            if (attempt == null) {
                continue;
            }

            if (attempt.getPercentage() != null) {

                total +=
                        attempt.getPercentage();

                count++;
            }
        }

        if (count == 0) {
            return 0.0;
        }

        return round(
                total / count
        );
    }

    // =========================================================
    // PASS / FAIL
    // =========================================================

    private boolean isPassed(
            ExamAttempt attempt) {

        if (attempt == null) {
            return false;
        }

        if (attempt.getScore() == null) {
            return false;
        }

        if (attempt.getExam() == null) {
            return false;
        }

        Integer passingMarks =
                attempt.getExam()
                        .getPassingMarks();

        if (passingMarks == null) {
            return false;
        }

        return attempt.getScore()
                >= passingMarks;
    }

    // =========================================================
    // RESULT DATE
    // =========================================================

    private LocalDateTime getResultDate(
            ExamAttempt attempt) {

        if (attempt == null) {
            return null;
        }

        // Prefer submitted date
        if (attempt.getSubmittedAt() != null) {

            return attempt.getSubmittedAt();
        }

        // Fallback to started date
        return attempt.getStartedAt();
    }

    // =========================================================
    // EXAM PERFORMANCE
    // =========================================================

    private List<ExamPerformance> buildExamPerformance(
            List<ExamAttempt> attempts) {

        if (attempts == null ||
                attempts.isEmpty()) {

            return new ArrayList<>();
        }

        // ---------------------------------------------------------
        // Group by exam
        // ---------------------------------------------------------

        Map<Long, List<ExamAttempt>> grouped =
                attempts.stream()
                        .filter(
                                a ->
                                        a != null &&
                                                a.getExam() != null &&
                                                a.getExam().getId() != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        a ->
                                                a.getExam()
                                                        .getId()
                                )
                        );

        List<ExamPerformance> result =
                new ArrayList<>();

        // ---------------------------------------------------------
        // Calculate each exam
        // ---------------------------------------------------------

        for (Map.Entry<Long, List<ExamAttempt>> entry
                : grouped.entrySet()) {

            List<ExamAttempt> examAttempts =
                    entry.getValue();

            if (examAttempts == null ||
                    examAttempts.isEmpty()) {

                continue;
            }

            ExamAttempt firstAttempt =
                    examAttempts.get(0);

            String examTitle = "";

            if (firstAttempt.getExam() != null) {

                examTitle =
                        firstAttempt.getExam()
                                .getTitle();
            }

            double totalPercentage = 0.0;

            int percentageCount = 0;

            long passed = 0;

            long failed = 0;

            // -----------------------------------------------------
            // Calculate exam statistics
            // -----------------------------------------------------

            for (ExamAttempt attempt :
                    examAttempts) {

                if (attempt.getPercentage() != null) {

                    totalPercentage +=
                            attempt.getPercentage();

                    percentageCount++;
                }

                if (isPassed(attempt)) {

                    passed++;

                } else {

                    failed++;
                }
            }

            // -----------------------------------------------------
            // Average
            // -----------------------------------------------------

            double average = 0.0;

            if (percentageCount > 0) {

                average =
                        totalPercentage
                                / percentageCount;
            }

            // -----------------------------------------------------
            // Add result
            // -----------------------------------------------------

            result.add(
                    new ExamPerformance(
                            entry.getKey(),
                            examTitle,
                            examAttempts.size(),
                            round(average),
                            passed,
                            failed
                    )
            );
        }

        // ---------------------------------------------------------
        // Highest average first
        // ---------------------------------------------------------

        result.sort(
                Comparator.comparing(
                        ExamPerformance
                                ::getAveragePercentage
                ).reversed()
        );

        return result;
    }

    // =========================================================
    // ROUND
    // =========================================================

    private double round(
            double value) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }

    // =========================================================
    // EXAM PERFORMANCE DTO
    // =========================================================

    public static class ExamPerformance {

        private final Long examId;

        private final String examTitle;

        private final int attemptCount;

        private final double averagePercentage;

        private final long passed;

        private final long failed;

        public ExamPerformance(
                Long examId,
                String examTitle,
                int attemptCount,
                double averagePercentage,
                long passed,
                long failed) {

            this.examId = examId;

            this.examTitle = examTitle;

            this.attemptCount =
                    attemptCount;

            this.averagePercentage =
                    averagePercentage;

            this.passed = passed;

            this.failed = failed;
        }

        // ---------------------------------------------------------
        // Getters
        // ---------------------------------------------------------

        public Long getExamId() {
            return examId;
        }

        public String getExamTitle() {
            return examTitle;
        }

        public int getAttemptCount() {
            return attemptCount;
        }

        public double getAveragePercentage() {
            return averagePercentage;
        }

        public long getPassed() {
            return passed;
        }

        public long getFailed() {
            return failed;
        }

        // ---------------------------------------------------------
        // Pass Rate
        // ---------------------------------------------------------

        public double getPassRate() {

            if (attemptCount == 0) {
                return 0.0;
            }

            return Math.round(
                    (
                            passed * 100.0
                                    / attemptCount
                    ) * 100.0
            ) / 100.0;
        }

        // ---------------------------------------------------------
        // Fail Rate
        // ---------------------------------------------------------

        public double getFailRate() {

            if (attemptCount == 0) {
                return 0.0;
            }

            return Math.round(
                    (
                            failed * 100.0
                                    / attemptCount
                    ) * 100.0
            ) / 100.0;
        }
    }
}