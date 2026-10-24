package com.sevak.sevakramonlineexamsystem.controller;

import com.sevak.sevakramonlineexamsystem.entity.Exam;
import com.sevak.sevakramonlineexamsystem.entity.ExamAttempt;
import com.sevak.sevakramonlineexamsystem.entity.User;
import com.sevak.sevakramonlineexamsystem.repository.ExamAttemptRepository;
import com.sevak.sevakramonlineexamsystem.repository.ExamRepository;
import com.sevak.sevakramonlineexamsystem.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class ReportsController {

    private final ExamAttemptRepository examAttemptRepository;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter REPORT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public ReportsController(
            ExamAttemptRepository examAttemptRepository,
            ExamRepository examRepository,
            UserRepository userRepository) {

        this.examAttemptRepository = examAttemptRepository;
        this.examRepository = examRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // REPORTS PAGE
    // =========================================================

    @GetMapping("/admin/reports")
    public String reports(
            @RequestParam(required = false) Long exam,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            Model model) {

        // ---------------------------------------------------------
        // Validate date range
        // ---------------------------------------------------------

        if (fromDate != null && toDate != null &&
                fromDate.isAfter(toDate)) {

            model.addAttribute(
                    "errorMessage",
                    "From Date cannot be after To Date."
            );

            fromDate = null;
            toDate = null;
        }

        // ---------------------------------------------------------
        // Get exams
        // ---------------------------------------------------------

        List<Exam> exams = examRepository.findAll();

        // ---------------------------------------------------------
        // Get filtered attempts
        // ---------------------------------------------------------

        List<ExamAttempt> filteredAttempts =
                getFilteredAttempts(
                        exam,
                        fromDate,
                        toDate
                );

        // =========================================================
        // BASIC STATISTICS
        // =========================================================

        long totalAttempts = filteredAttempts.size();

        long passedAttempts = 0;
        long failedAttempts = 0;

        double totalPercentage = 0.0;
        long percentageCount = 0;

        long totalCorrect = 0;
        long totalWrong = 0;
        long totalUnanswered = 0;

        for (ExamAttempt attempt : filteredAttempts) {

            if (isPassed(attempt)) {
                passedAttempts++;
            } else {
                failedAttempts++;
            }

            if (attempt.getPercentage() != null) {
                totalPercentage += attempt.getPercentage();
                percentageCount++;
            }

            if (attempt.getCorrectAnswers() != null) {
                totalCorrect += attempt.getCorrectAnswers();
            }

            if (attempt.getWrongAnswers() != null) {
                totalWrong += attempt.getWrongAnswers();
            }

            if (attempt.getUnanswered() != null) {
                totalUnanswered += attempt.getUnanswered();
            }
        }

        // =========================================================
        // AVERAGE SCORE
        // =========================================================

        double averageScore = 0.0;

        if (percentageCount > 0) {
            averageScore =
                    totalPercentage / percentageCount;
        }

        averageScore = roundToTwoDecimals(averageScore);

        // =========================================================
        // PASS RATE / FAIL RATE
        // =========================================================

        double passRate = 0.0;
        double failRate = 0.0;

        if (totalAttempts > 0) {

            passRate =
                    (passedAttempts * 100.0)
                            / totalAttempts;

            failRate =
                    (failedAttempts * 100.0)
                            / totalAttempts;
        }

        passRate = roundToTwoDecimals(passRate);
        failRate = roundToTwoDecimals(failRate);

        // =========================================================
        // EXAM-WISE PERFORMANCE
        // =========================================================

        Map<Long, ExamPerformance> performanceMap =
                new LinkedHashMap<>();

        for (ExamAttempt attempt : filteredAttempts) {

            if (attempt == null ||
                    attempt.getExam() == null ||
                    attempt.getExam().getId() == null) {

                continue;
            }

            Long examId =
                    attempt.getExam().getId();

            ExamPerformance performance =
                    performanceMap.get(examId);

            if (performance == null) {

                performance =
                        new ExamPerformance(
                                examId,
                                attempt.getExam().getTitle()
                        );

                performanceMap.put(
                        examId,
                        performance
                );
            }

            performance.addAttempt(
                    attempt.getPercentage(),
                    isPassed(attempt)
            );
        }

        List<ExamPerformance> examPerformance =
                new ArrayList<>(
                        performanceMap.values()
                );

        examPerformance.sort(
                Comparator.comparingDouble(
                        ExamPerformance::getAveragePercentage
                ).reversed()
        );

        // =========================================================
        // RECENT PERFORMANCE
        // =========================================================

        List<ExamAttempt> recentAttempts =
                new ArrayList<>(filteredAttempts);

        recentAttempts.sort(
                Comparator.comparing(
                        this::getResultDate,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        // Show only latest 10 on dashboard
        if (recentAttempts.size() > 10) {

            recentAttempts =
                    new ArrayList<>(
                            recentAttempts.subList(0, 10)
                    );
        }

        // =========================================================
        // SUMMARY COUNTS
        // =========================================================

        long totalExams =
                exam != null
                        ? 1
                        : exams.size();

        long totalStudents =
                userRepository.countByRole(
                        User.Role.STUDENT
                );

        // =========================================================
        // MODEL ATTRIBUTES
        // =========================================================

        model.addAttribute(
                "exams",
                exams
        );

        model.addAttribute(
                "selectedExam",
                exam
        );

        model.addAttribute(
                "fromDate",
                fromDate
        );

        model.addAttribute(
                "toDate",
                toDate
        );

        // Summary
        model.addAttribute(
                "totalExams",
                totalExams
        );

        model.addAttribute(
                "totalStudents",
                totalStudents
        );

        model.addAttribute(
                "totalAttempts",
                totalAttempts
        );

        model.addAttribute(
                "averageScore",
                averageScore
        );

        // Pass / Fail
        model.addAttribute(
                "passedAttempts",
                passedAttempts
        );

        model.addAttribute(
                "failedAttempts",
                failedAttempts
        );

        model.addAttribute(
                "passRate",
                passRate
        );

        model.addAttribute(
                "failRate",
                failRate
        );

        // Answer Analytics
        model.addAttribute(
                "totalCorrect",
                totalCorrect
        );

        model.addAttribute(
                "totalWrong",
                totalWrong
        );

        model.addAttribute(
                "totalUnanswered",
                totalUnanswered
        );

        // Exam Performance
        model.addAttribute(
                "examPerformance",
                examPerformance
        );

        // Recent Attempts
        model.addAttribute(
                "recentAttempts",
                recentAttempts
        );

        return "admin-reports";
    }

    // =========================================================
    // EXPORT FULL FILTERED REPORT
    // =========================================================

    @GetMapping(
            value = "/admin/reports/export",
            produces = "text/csv"
    )
    public ResponseEntity<byte[]> exportReport(
            @RequestParam(required = false) Long exam,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {

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
        // Get all filtered attempts
        // ---------------------------------------------------------

        List<ExamAttempt> attempts =
                getFilteredAttempts(
                        exam,
                        fromDate,
                        toDate
                );

        // ---------------------------------------------------------
        // Sort latest first
        // ---------------------------------------------------------

        attempts.sort(
                Comparator.comparing(
                        this::getResultDate,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        // =========================================================
        // BUILD CSV
        // =========================================================

        StringBuilder csv =
                new StringBuilder();

        // Header
        csv.append(
                "Student,Email,Exam,Date,Score,Total Marks,"
                        + "Percentage,Correct,Wrong,Unanswered,Status\n"
        );

        // ---------------------------------------------------------
        // Data
        // ---------------------------------------------------------

        for (ExamAttempt attempt : attempts) {

            String studentName = "";
            String email = "";
            String examTitle = "";
            String date = "";
            String score = "";
            String totalMarks = "";
            String percentage = "";
            String correct = "";
            String wrong = "";
            String unanswered = "";
            String status = "";

            // -----------------------------------------------------
            // Student
            // -----------------------------------------------------

            if (attempt.getUser() != null) {

                studentName =
                        nullToEmpty(
                                attempt.getUser().getFullName()
                        );

                email =
                        nullToEmpty(
                                attempt.getUser().getEmail()
                        );
            }

            // -----------------------------------------------------
            // Exam
            // -----------------------------------------------------

            if (attempt.getExam() != null) {

                examTitle =
                        nullToEmpty(
                                attempt.getExam().getTitle()
                        );

                if (attempt.getTotalMarks() != null) {

                    totalMarks =
                            String.valueOf(
                                    attempt.getTotalMarks()
                            );

                } else if (
                        attempt.getExam().getTotalMarks()
                                != null) {

                    totalMarks =
                            String.valueOf(
                                    attempt.getExam()
                                            .getTotalMarks()
                            );
                }
            }

            // -----------------------------------------------------
            // Date
            // -----------------------------------------------------

            LocalDateTime resultDate =
                    getResultDate(attempt);

            if (resultDate != null) {

                date =
                        REPORT_DATE_FORMAT.format(
                                resultDate
                        );
            }

            // -----------------------------------------------------
            // Score
            // -----------------------------------------------------

            if (attempt.getScore() != null) {

                score =
                        String.valueOf(
                                attempt.getScore()
                        );
            }

            // -----------------------------------------------------
            // Percentage
            // -----------------------------------------------------

            if (attempt.getPercentage() != null) {

                percentage =
                        String.valueOf(
                                roundToTwoDecimals(
                                        attempt.getPercentage()
                                )
                        );
            }

            // -----------------------------------------------------
            // Correct
            // -----------------------------------------------------

            if (attempt.getCorrectAnswers() != null) {

                correct =
                        String.valueOf(
                                attempt.getCorrectAnswers()
                        );
            }

            // -----------------------------------------------------
            // Wrong
            // -----------------------------------------------------

            if (attempt.getWrongAnswers() != null) {

                wrong =
                        String.valueOf(
                                attempt.getWrongAnswers()
                        );
            }

            // -----------------------------------------------------
            // Unanswered
            // -----------------------------------------------------

            if (attempt.getUnanswered() != null) {

                unanswered =
                        String.valueOf(
                                attempt.getUnanswered()
                        );
            }

            // -----------------------------------------------------
            // Status
            // -----------------------------------------------------

            status =
                    isPassed(attempt)
                            ? "Passed"
                            : "Failed";

            // -----------------------------------------------------
            // CSV Row
            // -----------------------------------------------------

            csv.append(
                    csvValue(studentName)
            ).append(",");

            csv.append(
                    csvValue(email)
            ).append(",");

            csv.append(
                    csvValue(examTitle)
            ).append(",");

            csv.append(
                    csvValue(date)
            ).append(",");

            csv.append(
                    csvValue(score)
            ).append(",");

            csv.append(
                    csvValue(totalMarks)
            ).append(",");

            csv.append(
                    csvValue(percentage)
            ).append(",");

            csv.append(
                    csvValue(correct)
            ).append(",");

            csv.append(
                    csvValue(wrong)
            ).append(",");

            csv.append(
                    csvValue(unanswered)
            ).append(",");

            csv.append(
                    csvValue(status)
            ).append("\n");
        }

        // =========================================================
        // RESPONSE
        // =========================================================

        byte[] data =
                csv.toString()
                        .getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.parseMediaType(
                        "text/csv; charset=UTF-8"
                )
        );

        headers.setContentDispositionFormData(
                "attachment",
                "exam-report.csv"
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(data);
    }

    // =========================================================
// GET FILTERED ATTEMPTS
// =========================================================

    private List<ExamAttempt> getFilteredAttempts(
            Long exam,
            LocalDate fromDate,
            LocalDate toDate) {

        // ---------------------------------------------------------
        // Validate date range
        // ---------------------------------------------------------

        if (fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate)) {

            return new ArrayList<>();
        }

        // ---------------------------------------------------------
        // Convert LocalDate to LocalDateTime
        // ---------------------------------------------------------

        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (fromDate != null) {
            fromDateTime = fromDate.atStartOfDay();
        }

        if (toDate != null) {
            // Exclusive boundary includes the complete selected day.
            toDateTime = toDate.plusDays(1).atStartOfDay();
        }

        // ---------------------------------------------------------
        // Allowed attempt statuses
        // ---------------------------------------------------------

        List<ExamAttempt.AttemptStatus> completedStatuses =
                Arrays.asList(
                        ExamAttempt.AttemptStatus.COMPLETED,
                        ExamAttempt.AttemptStatus.AUTO_SUBMITTED
                );

        // ---------------------------------------------------------
        // Fetch attempts with Exam and User
        // JOIN FETCH prevents lazy-loading failure here.
        // ---------------------------------------------------------

        List<ExamAttempt> attempts =
                examAttemptRepository.findReportAttempts(
                        exam,
                        fromDateTime,
                        toDateTime,
                        completedStatuses
                );

        // ---------------------------------------------------------
        // Return a mutable list
        // ---------------------------------------------------------

        if (attempts == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(attempts);
    }

    // =========================================================
    // EXAM FILTER
    // =========================================================

    private boolean matchesExam(
            ExamAttempt attempt,
            Long examId) {

        // No exam selected
        if (examId == null) {
            return true;
        }

        if (attempt == null ||
                attempt.getExam() == null ||
                attempt.getExam().getId() == null) {

            return false;
        }

        return attempt.getExam()
                .getId()
                .equals(examId);
    }

    // =========================================================
    // DATE FILTER
    // =========================================================

    private boolean matchesDateRange(
            ExamAttempt attempt,
            LocalDate fromDate,
            LocalDate toDate) {

        LocalDateTime resultDate =
                getResultDate(attempt);

        if (resultDate == null) {
            return false;
        }

        // From date
        if (fromDate != null) {

            LocalDateTime fromDateTime =
                    fromDate.atStartOfDay();

            if (resultDate.isBefore(
                    fromDateTime
            )) {
                return false;
            }
        }

        // To date
        if (toDate != null) {

            LocalDateTime toDateTime =
                    LocalDateTime.of(
                            toDate,
                            LocalTime.MAX
                    );

            if (resultDate.isAfter(
                    toDateTime
            )) {
                return false;
            }
        }

        return true;
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
    // PASS / FAIL
    // =========================================================

    private boolean isPassed(
            ExamAttempt attempt) {

        if (attempt == null ||
                attempt.getScore() == null ||
                attempt.getExam() == null ||
                attempt.getExam().getPassingMarks() == null) {

            return false;
        }

        return attempt.getScore()
                >= attempt.getExam()
                .getPassingMarks();
    }

    // =========================================================
    // ROUND DECIMAL
    // =========================================================

    private double roundToTwoDecimals(
            double value) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }

    // =========================================================
    // CSV VALUE
    // =========================================================

    private String csvValue(
            String value) {

        if (value == null) {
            value = "";
        }

        // Escape double quotes
        String escaped =
                value.replace(
                        "\"",
                        "\"\""
                );

        return "\"" +
                escaped +
                "\"";
    }

    // =========================================================
    // NULL TO EMPTY
    // =========================================================

    private String nullToEmpty(
            String value) {

        return value == null
                ? ""
                : value;
    }

    // =========================================================
    // EXAM PERFORMANCE DTO
    // =========================================================

    public static class ExamPerformance {

        private final Long examId;

        private final String examTitle;

        private long attempts;

        private long passedAttempts;

        private long failedAttempts;

        private double totalPercentage;

        private long percentageCount;

        public ExamPerformance(
                Long examId,
                String examTitle) {

            this.examId = examId;
            this.examTitle = examTitle;
        }

        // ---------------------------------------------------------
        // Add attempt
        // ---------------------------------------------------------

        public void addAttempt(
                Double percentage,
                boolean passed) {

            attempts++;

            if (passed) {
                passedAttempts++;
            } else {
                failedAttempts++;
            }

            if (percentage != null) {

                totalPercentage +=
                        percentage;

                percentageCount++;
            }
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

        public long getAttempts() {
            return attempts;
        }

        public long getPassedAttempts() {
            return passedAttempts;
        }

        public long getFailedAttempts() {
            return failedAttempts;
        }

        // ---------------------------------------------------------
        // Average percentage
        // ---------------------------------------------------------

        public double getAveragePercentage() {

            if (percentageCount == 0) {
                return 0.0;
            }

            return Math.round(
                    (
                            totalPercentage
                                    / percentageCount
                    ) * 100.0
            ) / 100.0;
        }

        // ---------------------------------------------------------
        // Pass rate
        // ---------------------------------------------------------

        public double getPassRate() {

            if (attempts == 0) {
                return 0.0;
            }

            return Math.round(
                    (
                            passedAttempts
                                    * 100.0
                                    / attempts
                    ) * 100.0
            ) / 100.0;
        }

        // ---------------------------------------------------------
        // Fail rate
        // ---------------------------------------------------------

        public double getFailRate() {

            if (attempts == 0) {
                return 0.0;
            }

            return Math.round(
                    (
                            failedAttempts
                                    * 100.0
                                    / attempts
                    ) * 100.0
            ) / 100.0;
        }
    }
}