package com.sevak.sevakramonlineexamsystem.controller;

import com.sevak.sevakramonlineexamsystem.dto.ExamSubmissionRequest;
import com.sevak.sevakramonlineexamsystem.entity.*;
import com.sevak.sevakramonlineexamsystem.repository.ExamAttemptRepository;
import com.sevak.sevakramonlineexamsystem.repository.OptionRepository;
import com.sevak.sevakramonlineexamsystem.repository.QuestionRepository;
import com.sevak.sevakramonlineexamsystem.service.AnswerService;
import com.sevak.sevakramonlineexamsystem.service.ExamAttemptService;
import com.sevak.sevakramonlineexamsystem.service.ExamService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/student")
public class StudentExamController {

    private static final Logger logger =
            LoggerFactory.getLogger(StudentExamController.class);

    private final ExamService examService;
    private final ExamAttemptService examAttemptService;
    private final AnswerService answerService;

    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final ExamAttemptRepository examAttemptRepository;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public StudentExamController(
            ExamService examService,
            ExamAttemptService examAttemptService,
            AnswerService answerService,
            QuestionRepository questionRepository,
            OptionRepository optionRepository,
            ExamAttemptRepository examAttemptRepository) {

        this.examService = examService;
        this.examAttemptService = examAttemptService;
        this.answerService = answerService;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.examAttemptRepository = examAttemptRepository;
    }

    // =========================================================
    // ACTIVE EXAMS
    // GET /student/exams
    // =========================================================

    @GetMapping("/exams")
    public String exams(
            HttpSession session,
            Model model) {

        Long userId =
                (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "exams",
                examService.getActiveExams()
        );

        return "student-exams";
    }

    // =========================================================
    // EXAM INSTRUCTIONS
    // GET /student/exam-instructions?examId=1
    // =========================================================

    @GetMapping("/exam-instructions")
    public String examInstructions(
            @RequestParam Long examId,
            HttpSession session,
            Model model) {

        Long userId =
                (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        Exam exam = examService.getExamById(examId);

        if (exam == null
                || !Boolean.TRUE.equals(exam.getActive())) {

            return "redirect:/student/exams";
        }

        model.addAttribute("exam", exam);

        return "exam-instructions";
    }

    // =========================================================
    // START EXAM
    // POST /student/start-exam
    // =========================================================

    @PostMapping("/start-exam")
    public String startExam(
            @RequestParam Long examId,
            HttpSession session) {

        Long userId =
                (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        ExamAttempt attempt =
                examAttemptService.startExam(userId, examId);

        return "redirect:/student/exam?attemptId="
                + attempt.getId();
    }

    // =========================================================
    // OPEN EXAM
    // GET /student/exam?attemptId=1
    // =========================================================

    @GetMapping("/exam")
    public String exam(
            @RequestParam Long attemptId,
            HttpSession session,
            Model model) {

        Long userId =
                (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        ExamAttempt attempt =
                examAttemptService.getAttemptById(attemptId);

        if (attempt == null
                || attempt.getUser() == null
                || !userId.equals(attempt.getUser().getId())) {

            return "redirect:/student/dashboard";
        }

        // Already submitted? Open result instead.
        if (attempt.getStatus()
                != ExamAttempt.AttemptStatus.IN_PROGRESS) {

            return "redirect:/student/result?attemptId="
                    + attemptId;
        }

        Exam exam = attempt.getExam();

        if (exam == null
                || !Boolean.TRUE.equals(exam.getActive())) {

            return "redirect:/student/dashboard";
        }

        List<Question> questions =
                questionRepository.findByExamIdAndActiveTrue(
                        exam.getId()
                );

        Map<Long, List<Option>> questionOptions =
                new HashMap<>();

        for (Question question : questions) {

            List<Option> options =
                    optionRepository.findByQuestionIdAndActiveTrue(
                            question.getId()
                    );

            questionOptions.put(
                    question.getId(),
                    options
            );
        }

        model.addAttribute("attempt", attempt);
        model.addAttribute("exam", exam);
        model.addAttribute("questions", questions);
        model.addAttribute("questionOptions", questionOptions);

        return "exam";
    }

    // =========================================================
    // SUBMIT EXAM
    // POST /student/submit-exam
    // =========================================================

    @PostMapping("/submit-exam")
    @ResponseBody
    public ResponseEntity<?> submitExam(
            @RequestBody ExamSubmissionRequest request,
            HttpSession session) {

        Long attemptId = null;

        try {

            // 1. Validate login

            Long userId =
                    (Long) session.getAttribute("userId");

            if (userId == null) {

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(errorResponse(
                                "Please login first."
                        ));
            }

            // 2. Validate request

            if (request == null
                    || request.getAttemptId() == null) {

                return ResponseEntity
                        .badRequest()
                        .body(errorResponse(
                                "A valid exam attempt ID is required."
                        ));
            }

            attemptId = request.getAttemptId();

            // 3. Load attempt

            ExamAttempt attempt =
                    examAttemptService.getAttemptById(attemptId);

            if (attempt == null) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(errorResponse(
                                "Exam attempt not found."
                        ));
            }

            // 4. Verify ownership

            if (attempt.getUser() == null
                    || !userId.equals(attempt.getUser().getId())) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(errorResponse(
                                "You are not allowed to submit this exam."
                        ));
            }

            // 5. Prevent duplicate submission

            if (attempt.getStatus()
                    != ExamAttempt.AttemptStatus.IN_PROGRESS) {

                return ResponseEntity
                        .badRequest()
                        .body(errorResponse(
                                "This exam attempt has already been submitted."
                        ));
            }

            // 6. Save answers and calculate result

            ExamAttempt completedAttempt =
                    answerService.submitExam(
                            attemptId,
                            request.getAnswers()
                    );

            // 7. Return success response

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", true);
            response.put("attemptId", completedAttempt.getId());

            response.put(
                    "message",
                    "Exam submitted successfully."
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException
                 | IllegalStateException ex) {

            logger.warn(
                    "Invalid exam submission. Attempt ID: {}. Reason: {}",
                    attemptId,
                    ex.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(errorResponse(
                            ex.getMessage() == null
                                    ? "Invalid exam submission."
                                    : ex.getMessage()
                    ));

        } catch (Exception ex) {

            logger.error(
                    "Unexpected error while submitting exam. Attempt ID: {}",
                    attemptId,
                    ex
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Unable to submit exam. Please try again."
                    ));
        }
    }

    // =========================================================
    // ERROR RESPONSE
    // =========================================================

    private Map<String, Object> errorResponse(
            String message) {

        Map<String, Object> response =
                new HashMap<>();

        response.put("success", false);
        response.put("message", message);

        return response;
    }

    // =========================================================
    // STUDENT RESULTS
    // GET /student/result
    // GET /student/result?attemptId=1
    // =========================================================

    @GetMapping("/result")
    public String result(
            @RequestParam(required = false) Long attemptId,
            HttpSession session,
            Model model) {

        Long userId =
                (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        // -----------------------------------------------------
        // CASE 1: Show all completed results
        // -----------------------------------------------------

        if (attemptId == null) {

            List<ExamAttempt> attempts =
                    examAttemptRepository.findResultsByUserId(userId);

            List<ExamAttempt> completedAttempts =
                    new ArrayList<>();

            for (ExamAttempt attempt : attempts) {

                if (attempt.getStatus()
                        == ExamAttempt.AttemptStatus.COMPLETED
                        || attempt.getStatus()
                        == ExamAttempt.AttemptStatus.AUTO_SUBMITTED) {

                    completedAttempts.add(attempt);
                }
            }

            model.addAttribute(
                    "results",
                    completedAttempts
            );

            return "student-results";
        }

        // -----------------------------------------------------
        // CASE 2: Show an individual result
        // -----------------------------------------------------

        ExamAttempt attempt =
                examAttemptService.getAttemptById(attemptId);

        if (attempt == null
                || attempt.getUser() == null
                || !userId.equals(attempt.getUser().getId())) {

            return "redirect:/student/dashboard";
        }

        if (attempt.getStatus()
                == ExamAttempt.AttemptStatus.IN_PROGRESS) {

            return "redirect:/student/exam?attemptId="
                    + attemptId;
        }

        Exam exam = attempt.getExam();

        if (exam == null) {
            return "redirect:/student/dashboard";
        }

        boolean passed = false;

        if (attempt.getScore() != null
                && exam.getPassingMarks() != null) {

            passed = attempt.getScore()
                    >= exam.getPassingMarks();
        }

        model.addAttribute("attempt", attempt);
        model.addAttribute("exam", exam);

        model.addAttribute(
                "passingMarks",
                exam.getPassingMarks()
        );

        model.addAttribute("passed", passed);

        return "result";
    }

    // =========================================================
    // DETAILED ANSWER REVIEW
    // GET /student/result-details?attemptId=1
    // =========================================================

    @GetMapping("/result-details")
    public String resultDetails(
            @RequestParam Long attemptId,
            HttpSession session,
            Model model) {

        Long userId =
                (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        // Load attempt

        ExamAttempt attempt =
                examAttemptService.getAttemptById(attemptId);

        // Verify ownership

        if (attempt == null
                || attempt.getUser() == null
                || !userId.equals(attempt.getUser().getId())) {

            return "redirect:/student/dashboard";
        }

        // Do not allow review before submission

        if (attempt.getStatus()
                == ExamAttempt.AttemptStatus.IN_PROGRESS) {

            return "redirect:/student/exam?attemptId="
                    + attemptId;
        }

        Exam exam = attempt.getExam();

        if (exam == null) {
            return "redirect:/student/dashboard";
        }

        // Fetch questions

        List<Question> questions =
                questionRepository.findByExamIdAndActiveTrue(
                        exam.getId()
                );

        // Fetch submitted answers

        List<Answer> answers =
                answerService.getAnswersByAttemptId(attemptId);

        // Map question ID to answer

        Map<Long, Answer> answerMap =
                new HashMap<>();

        for (Answer answer : answers) {

            if (answer.getQuestion() != null) {

                answerMap.put(
                        answer.getQuestion().getId(),
                        answer
                );
            }
        }

        // Find the correct option for each question

        Map<Long, Option> correctOptions =
                new HashMap<>();

        for (Question question : questions) {

            List<Option> options =
                    optionRepository.findByQuestionIdAndActiveTrue(
                            question.getId()
                    );

            for (Option option : options) {

                if (Boolean.TRUE.equals(option.getCorrect())) {

                    correctOptions.put(
                            question.getId(),
                            option
                    );

                    break;
                }
            }
        }

        // Send data to Thymeleaf

        model.addAttribute("attempt", attempt);
        model.addAttribute("exam", exam);
        model.addAttribute("questions", questions);
        model.addAttribute("answerMap", answerMap);
        model.addAttribute("correctOptions", correctOptions);

        return "result-details";
    }
}




//package com.sevak.sevakramonlineexamsystem.controller;
//
//import com.sevak.sevakramonlineexamsystem.dto.ExamSubmissionRequest;
//import com.sevak.sevakramonlineexamsystem.entity.*;
//import com.sevak.sevakramonlineexamsystem.repository.ExamAttemptRepository;
//import com.sevak.sevakramonlineexamsystem.repository.OptionRepository;
//import com.sevak.sevakramonlineexamsystem.repository.QuestionRepository;
//import com.sevak.sevakramonlineexamsystem.service.AnswerService;
//import  com.sevak.sevakramonlineexamsystem.service.ExamAttemptService;
//import  com.sevak.sevakramonlineexamsystem.service.ExamService;
//import jakarta.servlet.http.HttpSession;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Controller
//@RequestMapping("/student")
//public class StudentExamController {
//
//    private static final Logger logger =
//            LoggerFactory.getLogger(StudentExamController.class);
//
//    private final ExamService examService;
//    private final ExamAttemptService examAttemptService;
//    private final AnswerService answerService;
//
//    private final QuestionRepository questionRepository;
//    private final OptionRepository optionRepository;
//    private final ExamAttemptRepository examAttemptRepository;
//
//    // =========================================================
//    // CONSTRUCTOR
//    // =========================================================
//
//    public StudentExamController(
//            ExamService examService,
//            ExamAttemptService examAttemptService,
//            AnswerService answerService,
//            QuestionRepository questionRepository,
//            OptionRepository optionRepository,
//            ExamAttemptRepository examAttemptRepository) {
//
//        this.examService = examService;
//        this.examAttemptService = examAttemptService;
//        this.answerService = answerService;
//        this.questionRepository = questionRepository;
//        this.optionRepository = optionRepository;
//        this.examAttemptRepository = examAttemptRepository;
//    }
//
//    // =========================================================
//    // ACTIVE EXAMS
//    // GET /student/exams
//    // =========================================================
//
//    @GetMapping("/exams")
//    public String exams(
//            HttpSession session,
//            Model model) {
//
//        Long userId =
//                (Long) session.getAttribute("userId");
//
//        if (userId == null) {
//            return "redirect:/login";
//        }
//
//        model.addAttribute(
//                "exams",
//                examService.getActiveExams()
//        );
//
//        return "student-exams";
//    }
//
//    // =========================================================
//    // EXAM INSTRUCTIONS
//    // GET /student/exam-instructions?examId=1
//    // =========================================================
//
//    @GetMapping("/exam-instructions")
//    public String examInstructions(
//            @RequestParam Long examId,
//            HttpSession session,
//            Model model) {
//
//        Long userId =
//                (Long) session.getAttribute("userId");
//
//        if (userId == null) {
//            return "redirect:/login";
//        }
//
//        Exam exam = examService.getExamById(examId);
//
//        if (exam == null
//                || !Boolean.TRUE.equals(exam.getActive())) {
//
//            return "redirect:/student/exams";
//        }
//
//        model.addAttribute("exam", exam);
//
//        return "exam-instructions";
//    }
//
//    // =========================================================
//    // START EXAM
//    // POST /student/start-exam
//    // =========================================================
//
//    @PostMapping("/start-exam")
//    public String startExam(
//            @RequestParam Long examId,
//            HttpSession session) {
//
//        Long userId =
//                (Long) session.getAttribute("userId");
//
//        if (userId == null) {
//            return "redirect:/login";
//        }
//
//        ExamAttempt attempt =
//                examAttemptService.startExam(userId, examId);
//
//        return "redirect:/student/exam?attemptId="
//                + attempt.getId();
//    }
//
//    // =========================================================
//    // OPEN EXAM
//    // GET /student/exam?attemptId=1
//    // =========================================================
//
//    @GetMapping("/exam")
//    public String exam(
//            @RequestParam Long attemptId,
//            HttpSession session,
//            Model model) {
//
//        Long userId =
//                (Long) session.getAttribute("userId");
//
//        if (userId == null) {
//            return "redirect:/login";
//        }
//
//        ExamAttempt attempt =
//                examAttemptService.getAttemptById(attemptId);
//
//        if (attempt == null
//                || attempt.getUser() == null
//                || !userId.equals(attempt.getUser().getId())) {
//
//            return "redirect:/student/dashboard";
//        }
//
//        // Already submitted? Open result instead.
//        if (attempt.getStatus()
//                != ExamAttempt.AttemptStatus.IN_PROGRESS) {
//
//            return "redirect:/student/result?attemptId="
//                    + attemptId;
//        }
//
//        Exam exam = attempt.getExam();
//
//        if (exam == null
//                || !Boolean.TRUE.equals(exam.getActive())) {
//
//            return "redirect:/student/dashboard";
//        }
//
//        List<Question> questions =
//                questionRepository.findByExamIdAndActiveTrue(
//                        exam.getId()
//                );
//
//        Map<Long, List<Option>> questionOptions =
//                new HashMap<>();
//
//        for (Question question : questions) {
//
//            List<Option> options =
//                    optionRepository.findByQuestionIdAndActiveTrue(
//                            question.getId()
//                    );
//
//            questionOptions.put(
//                    question.getId(),
//                    options
//            );
//        }
//
//        model.addAttribute("attempt", attempt);
//        model.addAttribute("exam", exam);
//        model.addAttribute("questions", questions);
//        model.addAttribute("questionOptions", questionOptions);
//
//        return "exam";
//    }
//
//    // =========================================================
//    // SUBMIT EXAM
//    // POST /student/submit-exam
//    // =========================================================
//
//    @PostMapping("/submit-exam")
//    @ResponseBody
//    public ResponseEntity<?> submitExam(
//            @RequestBody ExamSubmissionRequest request,
//            HttpSession session) {
//
//        Long attemptId = null;
//
//        try {
//
//            // 1. Validate login
//
//            Long userId =
//                    (Long) session.getAttribute("userId");
//
//            if (userId == null) {
//
//                return ResponseEntity
//                        .status(HttpStatus.UNAUTHORIZED)
//                        .body(errorResponse(
//                                "Please login first."
//                        ));
//            }
//
//            // 2. Validate request
//
//            if (request == null
//                    || request.getAttemptId() == null) {
//
//                return ResponseEntity
//                        .badRequest()
//                        .body(errorResponse(
//                                "A valid exam attempt ID is required."
//                        ));
//            }
//
//            attemptId = request.getAttemptId();
//
//            // 3. Load attempt
//
//            ExamAttempt attempt =
//                    examAttemptService.getAttemptById(attemptId);
//
//            if (attempt == null) {
//
//                return ResponseEntity
//                        .status(HttpStatus.NOT_FOUND)
//                        .body(errorResponse(
//                                "Exam attempt not found."
//                        ));
//            }
//
//            // 4. Verify ownership
//
//            if (attempt.getUser() == null
//                    || !userId.equals(attempt.getUser().getId())) {
//
//                return ResponseEntity
//                        .status(HttpStatus.FORBIDDEN)
//                        .body(errorResponse(
//                                "You are not allowed to submit this exam."
//                        ));
//            }
//
//            // 5. Prevent duplicate submission
//
//            if (attempt.getStatus()
//                    != ExamAttempt.AttemptStatus.IN_PROGRESS) {
//
//                return ResponseEntity
//                        .badRequest()
//                        .body(errorResponse(
//                                "This exam attempt has already been submitted."
//                        ));
//            }
//
//            // 6. Save answers and calculate result
//
//            ExamAttempt completedAttempt =
//                    answerService.submitExam(
//                            attemptId,
//                            request.getAnswers()
//                    );
//
//            // 7. Return success response
//
//            Map<String, Object> response =
//                    new HashMap<>();
//
//            response.put("success", true);
//            response.put("attemptId", completedAttempt.getId());
//
//            response.put(
//                    "message",
//                    "Exam submitted successfully."
//            );
//
//            return ResponseEntity.ok(response);
//
//        } catch (IllegalArgumentException
//                 | IllegalStateException ex) {
//
//            logger.warn(
//                    "Invalid exam submission. Attempt ID: {}. Reason: {}",
//                    attemptId,
//                    ex.getMessage()
//            );
//
//            return ResponseEntity
//                    .badRequest()
//                    .body(errorResponse(
//                            ex.getMessage() == null
//                                    ? "Invalid exam submission."
//                                    : ex.getMessage()
//                    ));
//
//        } catch (Exception ex) {
//
//            logger.error(
//                    "Unexpected error while submitting exam. Attempt ID: {}",
//                    attemptId,
//                    ex
//            );
//
//            return ResponseEntity
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(errorResponse(
//                            "Unable to submit exam. Please try again."
//                    ));
//        }
//    }
//
//    // =========================================================
//    // ERROR RESPONSE
//    // =========================================================
//
//    private Map<String, Object> errorResponse(
//            String message) {
//
//        Map<String, Object> response =
//                new HashMap<>();
//
//        response.put("success", false);
//        response.put("message", message);
//
//        return response;
//    }
//
//    // =========================================================
//    // STUDENT RESULTS
//    // GET /student/result
//    // GET /student/result?attemptId=1
//    // =========================================================
//
//    @GetMapping("/result")
//    public String result(
//            @RequestParam(required = false) Long attemptId,
//            HttpSession session,
//            Model model) {
//
//        Long userId =
//                (Long) session.getAttribute("userId");
//
//        if (userId == null) {
//            return "redirect:/login";
//        }
//
//        // -----------------------------------------------------
//        // CASE 1: Show all completed results
//        // -----------------------------------------------------
//
//        if (attemptId == null) {
//
//            List<ExamAttempt> attempts =
//                    examAttemptRepository.findResultsByUserId(userId);
//
//            List<ExamAttempt> completedAttempts =
//                    new ArrayList<>();
//
//            for (ExamAttempt attempt : attempts) {
//
//                if (attempt.getStatus()
//                        == ExamAttempt.AttemptStatus.COMPLETED
//                        || attempt.getStatus()
//                        == ExamAttempt.AttemptStatus.AUTO_SUBMITTED) {
//
//                    completedAttempts.add(attempt);
//                }
//            }
//
//            model.addAttribute(
//                    "results",
//                    completedAttempts
//            );
//
//            return "student-results";
//        }
//
//        // -----------------------------------------------------
//        // CASE 2: Show an individual result
//        // -----------------------------------------------------
//
//        ExamAttempt attempt =
//                examAttemptService.getAttemptById(attemptId);
//
//        if (attempt == null
//                || attempt.getUser() == null
//                || !userId.equals(attempt.getUser().getId())) {
//
//            return "redirect:/student/dashboard";
//        }
//
//        if (attempt.getStatus()
//                == ExamAttempt.AttemptStatus.IN_PROGRESS) {
//
//            return "redirect:/student/exam?attemptId="
//                    + attemptId;
//        }
//
//        Exam exam = attempt.getExam();
//
//        if (exam == null) {
//            return "redirect:/student/dashboard";
//        }
//
//       // boolean passed = false;
//        boolean passed = false;
//
//        if (attempt.getScore() != null
//                && exam.getPassingMarks() != null) {
//
//            passed = attempt.getScore()
//                    >= exam.getPassingMarks();
//        }
//
//        model.addAttribute("attempt", attempt);
//        model.addAttribute("exam", exam);
//
//        model.addAttribute(
//                "passingMarks",
//                exam.getPassingMarks()
//        );
//
//        model.addAttribute("passed", passed);
//
//        return "result";
//    }
//
//    // =========================================================
//    // DETAILED ANSWER REVIEW
//    // GET /student/result-details?attemptId=1
//    // =========================================================
//
//    @GetMapping("/result-details")
//    public String resultDetails(
//            @RequestParam Long attemptId,
//            HttpSession session,
//            Model model) {
//
//        Long userId =
//                (Long) session.getAttribute("userId");
//
//        if (userId == null) {
//            return "redirect:/login";
//        }
//
//        // Load attempt
//
//        ExamAttempt attempt =
//                examAttemptService.getAttemptById(attemptId);
//
//        // Verify ownership
//
//        if (attempt == null
//                || attempt.getUser() == null
//                || !userId.equals(attempt.getUser().getId())) {
//
//            return "redirect:/student/dashboard";
//        }
//
//        // Do not allow review before submission
//
//        if (attempt.getStatus()
//                == ExamAttempt.AttemptStatus.IN_PROGRESS) {
//
//            return "redirect:/student/exam?attemptId="
//                    + attemptId;
//        }
//
//        Exam exam = attempt.getExam();
//
//        if (exam == null) {
//            return "redirect:/student/dashboard";
//        }
//
//        // Fetch questions
//
//        List<Question> questions =
//                questionRepository.findByExamIdAndActiveTrue(
//                        exam.getId()
//                );
//
//        // Fetch submitted answers
//
//        List<Answer> answers =
//                answerService.getAnswersByAttemptId(attemptId);
//
//        // Map question ID to answer
//
//        Map<Long, Answer> answerMap =
//                new HashMap<>();
//
//        for (Answer answer : answers) {
//
//            if (answer.getQuestion() != null) {
//
//                answerMap.put(
//                        answer.getQuestion().getId(),
//                        answer
//                );
//            }
//        }
//
//        // Find the correct option for each question
//
//        Map<Long, Option> correctOptions =
//                new HashMap<>();
//
//        for (Question question : questions) {
//
//            List<Option> options =
//                    optionRepository.findByQuestionIdAndActiveTrue(
//                            question.getId()
//                    );
//
//            for (Option option : options) {
//
//                if (Boolean.TRUE.equals(option.getCorrect())) {
//
//                    correctOptions.put(
//                            question.getId(),
//                            option
//                    );
//
//                    break;
//                }
//            }
//        }
//
//        // Send data to Thymeleaf
//
//        model.addAttribute("attempt", attempt);
//        model.addAttribute("exam", exam);
//        model.addAttribute("questions", questions);
//        model.addAttribute("answerMap", answerMap);
//        model.addAttribute("correctOptions", correctOptions);
//
//        return "result-details";
//    }
//}