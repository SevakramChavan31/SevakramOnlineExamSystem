package com.sevak.sevakramonlineexamsystem.controller;

import com.sevak.sevakramonlineexamsystem.entity.Exam;
import com.sevak.sevakramonlineexamsystem.entity.ExamAttempt;
import com.sevak.sevakramonlineexamsystem.repository.ExamAttemptRepository;
import com.sevak.sevakramonlineexamsystem.repository.ExamRepository;
import com.sevak.sevakramonlineexamsystem.service.AnswerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class PageController {
    private final AnswerService answerService;
    private final ExamAttemptRepository examAttemptRepository;
    private final ExamRepository examRepository;

    public PageController(
            ExamAttemptRepository examAttemptRepository,
            ExamRepository examRepository, AnswerService answerService) {

        this.examAttemptRepository = examAttemptRepository;
        this.examRepository = examRepository;
        this.answerService = answerService;
    }

    @GetMapping("/")
    public String loginPage() {
        return "login";
    }
    //    @GetMapping("/login")
//    public String login() {
//        return "login";
//    }
    @GetMapping("/about")
    public String aboutPage() {
        return "about";
    }

    @GetMapping("/features")
    public String featuresPage() {
        return "features";
    }
    @GetMapping("/help")
    public String helpPage() {
        return "help";
    }

    @GetMapping("/admin/results")
    public String adminResults(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String exam,
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(required = false, defaultValue = "") String date,
            Model model) {

        /*
         * =========================================================
         * ONLY COMPLETED / AUTO-SUBMITTED ATTEMPTS
         * =========================================================
         */

        List<ExamAttempt.AttemptStatus> completedStatuses =
                Arrays.asList(
                        ExamAttempt.AttemptStatus.COMPLETED,
                        ExamAttempt.AttemptStatus.AUTO_SUBMITTED
                );

        /*
         * Get all completed exam attempts
         */
        List<ExamAttempt> allResults =
                examAttemptRepository
                        .findResultsWithDetailsByStatuses(
                                completedStatuses
                        );

        /*
         * Get all exams for dropdown
         */
        List<Exam> exams = examRepository.findAll();

        /*
         * =========================================================
         * APPLY SEARCH / EXAM / STATUS / DATE FILTERS
         * =========================================================
         */

        List<ExamAttempt> filteredResults = new ArrayList<>();

        for (ExamAttempt attempt : allResults) {

            if (!matchesSearch(attempt, search)) {
                continue;
            }

            if (!matchesExam(attempt, exam)) {
                continue;
            }

            if (!matchesStatus(attempt, status)) {
                continue;
            }

            if (!matchesDate(attempt, date)) {
                continue;
            }

            filteredResults.add(attempt);
        }

        /*
         * =========================================================
         * FILTERED DASHBOARD STATISTICS
         * =========================================================
         *
         * These values now represent the filtered results.
         */

        long totalAttempts = filteredResults.size();

        long passedAttempts = 0;
        long failedAttempts = 0;

        double totalPercentage = 0.0;
        long percentageCount = 0;

        for (ExamAttempt attempt : filteredResults) {

            if (isPassed(attempt)) {
                passedAttempts++;
            } else {
                failedAttempts++;
            }

            if (attempt.getPercentage() != null) {

                totalPercentage +=
                        attempt.getPercentage();

                percentageCount++;
            }
        }

        /*
         * Calculate filtered average score
         */
        double averageScore = 0.0;

        if (percentageCount > 0) {

            averageScore =
                    totalPercentage / percentageCount;
        }

        /*
         * =========================================================
         * SEND DATA TO THYMELEAF
         * =========================================================
         */

        model.addAttribute(
                "results",
                filteredResults
        );

        model.addAttribute(
                "exams",
                exams
        );

        /*
         * Filtered statistics
         */
        model.addAttribute(
                "totalAttempts",
                totalAttempts
        );

        model.addAttribute(
                "passedAttempts",
                passedAttempts
        );

        model.addAttribute(
                "failedAttempts",
                failedAttempts
        );

        model.addAttribute(
                "averageScore",
                averageScore
        );

        /*
         * Overall completed attempts.
         *
         * This remains separate from filtered totalAttempts.
         */
        model.addAttribute(
                "totalCompletedAttempts",
                allResults.size()
        );

        /*
         * Preserve selected filter values
         */
        model.addAttribute(
                "search",
                search
        );

        model.addAttribute(
                "selectedExam",
                exam
        );

        model.addAttribute(
                "selectedStatus",
                status
        );

        model.addAttribute(
                "selectedDate",
                date
        );

        return "admin-results";
    }

    @GetMapping("/admin/results/export")
    public ResponseEntity<byte[]> exportResults(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String exam,
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(required = false, defaultValue = "") String date) {

        /*
         * =========================================================
         * ONLY COMPLETED / AUTO-SUBMITTED ATTEMPTS
         * =========================================================
         */

        List<ExamAttempt.AttemptStatus> completedStatuses =
                Arrays.asList(
                        ExamAttempt.AttemptStatus.COMPLETED,
                        ExamAttempt.AttemptStatus.AUTO_SUBMITTED
                );

        List<ExamAttempt> allResults =
                examAttemptRepository
                        .findResultsWithDetailsByStatuses(
                                completedStatuses
                        );

        /*
         * =========================================================
         * APPLY SAME FILTERS AS RESULTS PAGE
         * =========================================================
         */

        List<ExamAttempt> filteredResults =
                new ArrayList<>();

        for (ExamAttempt attempt : allResults) {

            if (!matchesSearch(attempt, search)) {
                continue;
            }

            if (!matchesExam(attempt, exam)) {
                continue;
            }

            if (!matchesStatus(attempt, status)) {
                continue;
            }

            if (!matchesDate(attempt, date)) {
                continue;
            }

            filteredResults.add(attempt);
        }

        /*
         * =========================================================
         * CREATE CSV
         * =========================================================
         */

        StringBuilder csv = new StringBuilder();

        /*
         * CSV Header
         */
        csv.append("Student Name,");
        csv.append("Email,");
        csv.append("Exam,");
        csv.append("Date,");
        csv.append("Score,");
        csv.append("Total Marks,");
        csv.append("Percentage,");
        csv.append("Status,");
        csv.append("Correct Answers,");
        csv.append("Wrong Answers,");
        csv.append("Unanswered");
        csv.append("\n");

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "dd MMM yyyy HH:mm"
                );

        /*
         * CSV Rows
         */
        for (ExamAttempt attempt : filteredResults) {

            String studentName = "";

            String email = "";

            String examTitle = "";

            String resultDate = "";

            String score = "";

            String totalMarks = "";

            String percentage = "";

            String resultStatus = "";

            String correctAnswers = "";

            String wrongAnswers = "";

            String unanswered = "";


            /*
             * Student
             */
            if (attempt.getUser() != null) {

                if (attempt.getUser().getFullName() != null) {

                    studentName =
                            attempt.getUser()
                                    .getFullName();
                }

                if (attempt.getUser().getEmail() != null) {

                    email =
                            attempt.getUser()
                                    .getEmail();
                }
            }


            /*
             * Exam
             */
            if (attempt.getExam() != null
                    && attempt.getExam().getTitle() != null) {

                examTitle =
                        attempt.getExam()
                                .getTitle();
            }


            /*
             * Date
             */
            LocalDateTime dateTime =
                    attempt.getSubmittedAt();

            if (dateTime == null) {

                dateTime =
                        attempt.getStartedAt();
            }

            if (dateTime != null) {

                resultDate =
                        dateTime.format(formatter);
            }


            /*
             * Score
             */
            if (attempt.getScore() != null) {

                score =
                        String.valueOf(
                                attempt.getScore()
                        );
            }


            /*
             * Total Marks
             */
            if (attempt.getTotalMarks() != null) {

                totalMarks =
                        String.valueOf(
                                attempt.getTotalMarks()
                        );
            }


            /*
             * Percentage
             */
            if (attempt.getPercentage() != null) {

                percentage =
                        String.format(
                                "%.2f%%",
                                attempt.getPercentage()
                        );
            }


            /*
             * Passed / Failed
             */
            resultStatus =
                    isPassed(attempt)
                            ? "Passed"
                            : "Failed";


            /*
             * Correct
             */
            if (attempt.getCorrectAnswers() != null) {

                correctAnswers =
                        String.valueOf(
                                attempt.getCorrectAnswers()
                        );
            }


            /*
             * Wrong
             */
            if (attempt.getWrongAnswers() != null) {

                wrongAnswers =
                        String.valueOf(
                                attempt.getWrongAnswers()
                        );
            }


            /*
             * Unanswered
             */
            if (attempt.getUnanswered() != null) {

                unanswered =
                        String.valueOf(
                                attempt.getUnanswered()
                        );
            }


            /*
             * Write CSV row
             */
            csv.append(csvValue(studentName)).append(",");
            csv.append(csvValue(email)).append(",");
            csv.append(csvValue(examTitle)).append(",");
            csv.append(csvValue(resultDate)).append(",");
            csv.append(csvValue(score)).append(",");
            csv.append(csvValue(totalMarks)).append(",");
            csv.append(csvValue(percentage)).append(",");
            csv.append(csvValue(resultStatus)).append(",");
            csv.append(csvValue(correctAnswers)).append(",");
            csv.append(csvValue(wrongAnswers)).append(",");
            csv.append(csvValue(unanswered));

            csv.append("\n");
        }


        /*
         * Convert CSV to bytes
         */
        byte[] csvBytes =
                csv.toString()
                        .getBytes(StandardCharsets.UTF_8);


        /*
         * Download filename
         */
        String filename =
                "exam-results.csv";


        /*
         * Response headers
         */
        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.parseMediaType(
                        "text/csv; charset=UTF-8"
                )
        );

        headers.setContentDispositionFormData(
                "attachment",
                filename
        );


        return ResponseEntity
                .ok()
                .headers(headers)
                .body(csvBytes);
    }
    @GetMapping("/admin/results/attempt/{attemptId}/answers")
    @ResponseBody
    public List<Map<String, Object>> getDetailedAnswers(
            @PathVariable Long attemptId,
            HttpSession session) {

        Object userId = session.getAttribute("userId");
        Object role = session.getAttribute("role");

        if (userId == null || !"ADMIN".equals(role)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Admin login required."
            );
        }

        try {
            return answerService.getDetailedReview(attemptId);

        } catch (IllegalArgumentException ex) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ex.getMessage()
            );
        }
    }

    /*
     * =========================================================
     * PASS / FAIL
     * =========================================================
     */

    private boolean isPassed(ExamAttempt attempt) {

        if (attempt == null) {
            return false;
        }

        if (attempt.getScore() == null) {
            return false;
        }

        if (attempt.getExam() == null) {
            return false;
        }

        if (attempt.getExam().getPassingMarks() == null) {
            return false;
        }

        return attempt.getScore()
                >= attempt.getExam().getPassingMarks();
    }


    /*
     * =========================================================
     * SEARCH FILTER
     * =========================================================
     */

    private boolean matchesSearch(
            ExamAttempt attempt,
            String search) {

        if (search == null
                || search.trim().isEmpty()) {

            return true;
        }

        String keyword =
                search.trim().toLowerCase();

        String studentName = "";
        String studentEmail = "";
        String examTitle = "";

        /*
         * Student name and email
         */
        if (attempt.getUser() != null) {

            if (attempt.getUser().getFullName() != null) {

                studentName =
                        attempt.getUser()
                                .getFullName()
                                .toLowerCase();
            }

            if (attempt.getUser().getEmail() != null) {

                studentEmail =
                        attempt.getUser()
                                .getEmail()
                                .toLowerCase();
            }
        }

        /*
         * Exam title
         */
        if (attempt.getExam() != null
                && attempt.getExam().getTitle() != null) {

            examTitle =
                    attempt.getExam()
                            .getTitle()
                            .toLowerCase();
        }

        return studentName.contains(keyword)
                || studentEmail.contains(keyword)
                || examTitle.contains(keyword);
    }


    /*
     * =========================================================
     * EXAM FILTER
     * =========================================================
     */

    private boolean matchesExam(
            ExamAttempt attempt,
            String exam) {

        if (exam == null
                || exam.trim().isEmpty()
                || "ALL".equalsIgnoreCase(exam)) {

            return true;
        }

        if (attempt.getExam() == null) {
            return false;
        }

        try {

            Long examId =
                    Long.parseLong(exam.trim());

            return attempt.getExam()
                    .getId()
                    .equals(examId);

        } catch (NumberFormatException ex) {

            return false;
        }
    }


    /*
     * =========================================================
     * STATUS FILTER
     * =========================================================
     */

    private boolean matchesStatus(
            ExamAttempt attempt,
            String status) {

        if (status == null
                || status.trim().isEmpty()
                || "ALL".equalsIgnoreCase(status)) {

            return true;
        }

        boolean passed =
                isPassed(attempt);

        if ("PASSED".equalsIgnoreCase(status)) {

            return passed;
        }

        if ("FAILED".equalsIgnoreCase(status)) {

            return !passed;
        }

        return true;
    }


    /*
     * =========================================================
     * DATE FILTER
     * =========================================================
     */

    private boolean matchesDate(
            ExamAttempt attempt,
            String date) {

        if (date == null
                || date.trim().isEmpty()
                || "ALL".equalsIgnoreCase(date)) {

            return true;
        }

        /*
         * Prefer submitted date
         */
        LocalDateTime resultDate =
                attempt.getSubmittedAt();

        /*
         * If submitted date is not available,
         * use started date.
         */
        if (resultDate == null) {

            resultDate =
                    attempt.getStartedAt();
        }

        if (resultDate == null) {
            return false;
        }

        LocalDateTime now =
                LocalDateTime.now();

        /*
         * =====================================================
         * TODAY
         * =====================================================
         */

        if ("TODAY".equalsIgnoreCase(date)
                || "today".equalsIgnoreCase(date)) {

            return resultDate
                    .toLocalDate()
                    .equals(now.toLocalDate());
        }

        /*
         * =====================================================
         * LAST 7 DAYS / THIS WEEK
         * =====================================================
         */

        if ("7".equals(date)
                || "7DAYS".equalsIgnoreCase(date)
                || "WEEK".equalsIgnoreCase(date)
                || "THIS_WEEK".equalsIgnoreCase(date)
                || "thisWeek".equalsIgnoreCase(date)
                || "week".equalsIgnoreCase(date)) {

            return !resultDate.isBefore(
                    now.minusDays(7)
            );
        }

        /*
         * =====================================================
         * LAST 30 DAYS / THIS MONTH
         * =====================================================
         */

        if ("30".equals(date)
                || "30DAYS".equalsIgnoreCase(date)
                || "MONTH".equalsIgnoreCase(date)
                || "THIS_MONTH".equalsIgnoreCase(date)
                || "thisMonth".equalsIgnoreCase(date)
                || "month".equalsIgnoreCase(date)) {

            return !resultDate.isBefore(
                    now.minusDays(30)
            );
        }

        return true;
    }

    private String csvValue(String value) {

        if (value == null) {
            return "";
        }

        String cleanValue =
                value.trim();

        /*
         * Prevent CSV/Excel formula injection
         */
        if (cleanValue.startsWith("=")
                || cleanValue.startsWith("+")
                || cleanValue.startsWith("-")
                || cleanValue.startsWith("@")) {

            cleanValue =
                    "'" + cleanValue;
        }

        /*
         * Escape double quotes
         */
        cleanValue =
                cleanValue.replace(
                        "\"",
                        "\"\""
                );

        /*
         * Always wrap CSV values in quotes
         */
        return "\"" + cleanValue + "\"";
    }
    /*
     * =========================================================
     * ADMIN REPORTS
     * =========================================================
     */

//    @GetMapping("/admin/reports")
//    public String adminReports() {
//        return "admin-reports";
//    }


    /*
     * =========================================================
     * ADMIN SETTINGS
     * =========================================================
     */

//    @GetMapping("/admin/settings")
//    public String adminSettings() {
//        return "admin-settings";
//    }
}
