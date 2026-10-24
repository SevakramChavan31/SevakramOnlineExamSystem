package com.sevak.sevakramonlineexamsystem.service;

import com.sevak.sevakramonlineexamsystem.entity.*;
import com.sevak.sevakramonlineexamsystem.repository.AnswerRepository;
import com.sevak.sevakramonlineexamsystem.repository.ExamAttemptRepository;
import com.sevak.sevakramonlineexamsystem.repository.OptionRepository;
import com.sevak.sevakramonlineexamsystem.repository.QuestionRepository;
import com.sevak.sevakramonlineexamsystem.repository.ResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final ResultRepository resultRepository;

    public AnswerService(
            AnswerRepository answerRepository,
            ExamAttemptRepository examAttemptRepository,
            QuestionRepository questionRepository,
            OptionRepository optionRepository,
            ResultRepository resultRepository) {

        this.answerRepository = answerRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.resultRepository = resultRepository;
    }

    // =========================================================
    // SUBMIT EXAM
    // =========================================================

    @Transactional
    public ExamAttempt submitExam(
            Long attemptId,
            Map<Long, Long> answers) {

        // 1. Validate attempt ID
        if (attemptId == null) {
            throw new IllegalArgumentException(
                    "Exam attempt ID is required."
            );
        }

        // 2. Find exam attempt
        ExamAttempt attempt = examAttemptRepository
                .findById(attemptId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Exam attempt not found: " + attemptId
                        )
                );

        // 3. Validate attempt status
        if (attempt.getStatus()
                != ExamAttempt.AttemptStatus.IN_PROGRESS) {

            throw new IllegalStateException(
                    "This exam attempt has already been submitted."
            );
        }

        // 4. Get exam
        Exam exam = attempt.getExam();

        if (exam == null) {
            throw new IllegalStateException(
                    "Exam is not associated with this attempt."
            );
        }

        Long examId = exam.getId();

        // 5. Get active questions for this exam
        List<Question> questions =
                questionRepository.findByExamIdAndActiveTrue(examId);

        if (questions == null || questions.isEmpty()) {
            throw new IllegalStateException(
                    "No active questions found for this exam."
            );
        }

        int score = 0;
        int correctAnswers = 0;
        int wrongAnswers = 0;
        int unanswered = 0;

        Set<Long> processedQuestionIds = new HashSet<>();

        // 6. Process each question
        for (Question question : questions) {

            Long questionId = question.getId();

            if (questionId == null
                    || !processedQuestionIds.add(questionId)) {
                continue;
            }

            Long selectedOptionId = null;

            if (answers != null) {
                selectedOptionId = answers.get(questionId);
            }

            // Question not answered
            if (selectedOptionId == null) {
                unanswered++;
                continue;
            }

            // 7. Find selected option
            final Long optionId = selectedOptionId;

            Option selectedOption = optionRepository
                    .findById(optionId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Selected option not found: " + optionId
                            )
                    );

            // 8. Validate option belongs to this question
            if (selectedOption.getQuestion() == null
                    || !questionId.equals(
                    selectedOption.getQuestion().getId())) {

                throw new IllegalArgumentException(
                        "Selected option does not belong to question "
                                + questionId
                );
            }

            // 9. Find existing answer or create new answer
            Answer answer = answerRepository
                    .findByAttemptIdAndQuestionId(
                            attemptId,
                            questionId
                    )
                    .orElseGet(Answer::new);

            answer.setAttempt(attempt);
            answer.setQuestion(question);
            answer.setSelectedOption(selectedOption);

            boolean isCorrect =
                    Boolean.TRUE.equals(selectedOption.getCorrect());

            answer.setCorrect(isCorrect);

            int questionMarks = question.getMarks() == null
                    ? 0
                    : question.getMarks();

            int marksObtained = isCorrect
                    ? questionMarks
                    : 0;

            answer.setMarksObtained(marksObtained);
            answer.setAnsweredAt(LocalDateTime.now());

            // 10. Save answer
            answerRepository.save(answer);

            // 11. Calculate result
            if (isCorrect) {
                correctAnswers++;
                score += marksObtained;
            } else {
                wrongAnswers++;
            }
        }

        // 12. Calculate total marks
        Integer configuredTotalMarks = exam.getTotalMarks();

        int totalMarks = configuredTotalMarks == null
                ? 0
                : configuredTotalMarks;

        // Use question marks if exam total marks are not configured
        if (totalMarks <= 0) {
            totalMarks = 0;

            for (Question question : questions) {
                if (question.getMarks() != null) {
                    totalMarks += question.getMarks();
                }
            }
        }

        // 13. Calculate percentage
        double percentage = 0.0;

        if (totalMarks > 0) {
            percentage = ((double) score / totalMarks) * 100.0;
        }

        // 14. Update exam attempt
        attempt.setScore(score);
        attempt.setTotalMarks(totalMarks);
        attempt.setCorrectAnswers(correctAnswers);
        attempt.setWrongAnswers(wrongAnswers);
        attempt.setUnanswered(unanswered);
        attempt.setPercentage(percentage);
        attempt.setSubmittedAt(LocalDateTime.now());

        attempt.setStatus(
                ExamAttempt.AttemptStatus.COMPLETED
        );

        // 15. Save completed attempt
        ExamAttempt savedAttempt = examAttemptRepository.save(attempt);

        // 16. Save the corresponding result record.
        // Reuse an existing record if this attempt already has one.
        Result result = resultRepository.findByAttemptId(savedAttempt.getId())
                .orElseGet(Result::new);

        result.setAttempt(savedAttempt);
        result.setScore(savedAttempt.getScore());
        result.setTotalMarks(savedAttempt.getTotalMarks());
        result.setPercentage(savedAttempt.getPercentage());
        result.setCorrectAnswers(savedAttempt.getCorrectAnswers());
        result.setWrongAnswers(savedAttempt.getWrongAnswers());
        result.setUnanswered(savedAttempt.getUnanswered());

        int passingMarks = exam.getPassingMarks() == null
                ? 0
                : exam.getPassingMarks();
        result.setPassingMarks(passingMarks);

        int finalScore = savedAttempt.getScore() == null
                ? 0
                : savedAttempt.getScore();
        result.setPassed(finalScore >= passingMarks);
        result.setResultDate(savedAttempt.getSubmittedAt() != null
                ? savedAttempt.getSubmittedAt()
                : LocalDateTime.now());

        resultRepository.save(result);

        return savedAttempt;
    }

    // =========================================================
    // GET ANSWERS BY ATTEMPT ID
    // Initialize lazy relationships before returning answers
    // =========================================================

    @Transactional(readOnly = true)
    public List<Answer> getAnswersByAttemptId(Long attemptId) {

        if (attemptId == null) {
            throw new IllegalArgumentException(
                    "Exam attempt ID is required."
            );
        }

        List<Answer> answers =
                answerRepository.findByAttemptId(attemptId);

        for (Answer answer : answers) {

            // Initialize Question relationship
            if (answer.getQuestion() != null) {
                answer.getQuestion().getId();
                answer.getQuestion().getQuestionText();
                answer.getQuestion().getMarks();
            }

            // Initialize selected Option relationship
            if (answer.getSelectedOption() != null) {
                answer.getSelectedOption().getId();
                answer.getSelectedOption().getOptionLabel();
                answer.getSelectedOption().getOptionText();
                answer.getSelectedOption().getCorrect();
            }

            // Initialize ExamAttempt relationship
            if (answer.getAttempt() != null) {
                answer.getAttempt().getId();
            }
        }

        return answers;
    }
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDetailedReview(Long attemptId) {

        if (attemptId == null || attemptId <= 0) {
            throw new IllegalArgumentException("A valid exam attempt ID is required.");
        }

        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Exam attempt not found: " + attemptId
                        )
                );

        if (attempt.getStatus() != ExamAttempt.AttemptStatus.COMPLETED
                && attempt.getStatus() != ExamAttempt.AttemptStatus.AUTO_SUBMITTED) {

            throw new IllegalArgumentException(
                    "Detailed review is available only for completed exams."
            );
        }

        if (attempt.getExam() == null || attempt.getExam().getId() == null) {
            throw new IllegalStateException("Exam is not associated with this attempt.");
        }

        Long examId = attempt.getExam().getId();

        // Retrieve all questions, including unanswered questions.
        List<Question> questions =
                questionRepository.findByExamId(examId);

        questions.sort(
                Comparator.comparing(
                        Question::getQuestionOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())
                )
        );

        // Retrieve answers saved for this attempt.
        List<Answer> savedAnswers =
                answerRepository.findDetailedAnswersByAttemptId(attemptId);

        Map<Long, Answer> answerMap = new LinkedHashMap<>();

        for (Answer answer : savedAnswers) {
            answerMap.put(
                    answer.getQuestion().getId(),
                    answer
            );
        }

        List<Map<String, Object>> result = new ArrayList<>();

        int questionNumber = 1;

        for (Question question : questions) {

            Long questionId = question.getId();

            Answer savedAnswer = answerMap.get(questionId);

            Option selectedOption = savedAnswer != null
                    ? savedAnswer.getSelectedOption()
                    : null;

            List<Option> options =
                    optionRepository.findByQuestionId(questionId);

            String selectedAnswerText = null;
            String correctAnswerText = null;

            List<Map<String, Object>> optionDetails =
                    new ArrayList<>();

            for (Option option : options) {

                boolean isCorrect =
                        Boolean.TRUE.equals(option.getCorrect());

                boolean isSelected =
                        selectedOption != null
                                && selectedOption.getId().equals(option.getId());

                String optionText =
                        option.getOptionLabel() + ". "
                                + option.getOptionText();

                if (isSelected) {
                    selectedAnswerText = optionText;
                }

                if (isCorrect) {
                    correctAnswerText = optionText;
                }

                Map<String, Object> optionData =
                        new LinkedHashMap<>();

                optionData.put("label", option.getOptionLabel());
                optionData.put("text", option.getOptionText());
                optionData.put("correct", isCorrect);
                optionData.put("selected", isSelected);

                optionDetails.add(optionData);
            }

            String status;

            if (savedAnswer == null || selectedOption == null) {
                status = "Unanswered";
            } else if (Boolean.TRUE.equals(savedAnswer.getCorrect())) {
                status = "Correct";
            } else {
                status = "Incorrect";
            }

            int marks = question.getMarks() != null
                    ? question.getMarks()
                    : 0;

            int marksObtained = savedAnswer != null
                    && savedAnswer.getMarksObtained() != null
                    ? savedAnswer.getMarksObtained()
                    : 0;

            Map<String, Object> questionData =
                    new LinkedHashMap<>();

            questionData.put("questionNumber", questionNumber++);
            questionData.put("questionId", questionId);
            questionData.put("questionText", question.getQuestionText());
            questionData.put("marks", marks);
            questionData.put("marksObtained", marksObtained);
            questionData.put("status", status);
            questionData.put("selectedAnswer", selectedAnswerText);
            questionData.put("correctAnswer", correctAnswerText);
            questionData.put("options", optionDetails);

            result.add(questionData);
        }

        return result;
    }
}













































//package com.sevak.sevakramonlineexamsystem.service;
//
//import com.sevak.sevakramonlineexamsystem.entity.*;
//import com.sevak.sevakramonlineexamsystem.repository.AnswerRepository;
//import com.sevak.sevakramonlineexamsystem.repository.ExamAttemptRepository;
//import com.sevak.sevakramonlineexamsystem.repository.OptionRepository;
//import com.sevak.sevakramonlineexamsystem.repository.QuestionRepository;
//import com.sevak.sevakramonlineexamsystem.repository.ResultRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//@Service
//public class AnswerService {
//
//    private final AnswerRepository answerRepository;
//    private final ExamAttemptRepository examAttemptRepository;
//    private final QuestionRepository questionRepository;
//    private final OptionRepository optionRepository;
//    private final ResultRepository resultRepository;
//
//    public AnswerService(
//            AnswerRepository answerRepository,
//            ExamAttemptRepository examAttemptRepository,
//            QuestionRepository questionRepository,
//            OptionRepository optionRepository,
//            ResultRepository resultRepository) {
//
//        this.answerRepository = answerRepository;
//        this.examAttemptRepository = examAttemptRepository;
//        this.questionRepository = questionRepository;
//        this.optionRepository = optionRepository;
//        this.resultRepository = resultRepository;
//    }
//
//    // =========================================================
//    // SUBMIT EXAM
//    // =========================================================
//
//    @Transactional
//    public ExamAttempt submitExam(
//            Long attemptId,
//            Map<Long, Long> answers) {
//
//        // 1. Validate attempt ID
//        if (attemptId == null) {
//            throw new IllegalArgumentException(
//                    "Exam attempt ID is required."
//            );
//        }
//
//        // 2. Find exam attempt
//        ExamAttempt attempt = examAttemptRepository
//                .findById(attemptId)
//                .orElseThrow(() ->
//                        new IllegalArgumentException(
//                                "Exam attempt not found: " + attemptId
//                        )
//                );
//
//        // 3. Validate attempt status
//        if (attempt.getStatus()
//                != ExamAttempt.AttemptStatus.IN_PROGRESS) {
//
//            throw new IllegalStateException(
//                    "This exam attempt has already been submitted."
//            );
//        }
//
//        // 4. Get exam
//        Exam exam = attempt.getExam();
//
//        if (exam == null) {
//            throw new IllegalStateException(
//                    "Exam is not associated with this attempt."
//            );
//        }
//
//        Long examId = exam.getId();
//
//        // 5. Get active questions for this exam
//        List<Question> questions =
//                questionRepository.findByExamIdAndActiveTrue(examId);
//
//        if (questions == null || questions.isEmpty()) {
//            throw new IllegalStateException(
//                    "No active questions found for this exam."
//            );
//        }
//
//        int score = 0;
//        int correctAnswers = 0;
//        int wrongAnswers = 0;
//        int unanswered = 0;
//
//        Set<Long> processedQuestionIds = new HashSet<>();
//
//        // 6. Process each question
//        for (Question question : questions) {
//
//            Long questionId = question.getId();
//
//            if (questionId == null
//                    || !processedQuestionIds.add(questionId)) {
//                continue;
//            }
//
//            Long selectedOptionId = null;
//
//            if (answers != null) {
//                selectedOptionId = answers.get(questionId);
//            }
//
//            // Question not answered
//            if (selectedOptionId == null) {
//                unanswered++;
//                continue;
//            }
//
//            // 7. Find selected option
//            final Long optionId = selectedOptionId;
//
//            Option selectedOption = optionRepository
//                    .findById(optionId)
//                    .orElseThrow(() ->
//                            new IllegalArgumentException(
//                                    "Selected option not found: " + optionId
//                            )
//                    );
//
//            // 8. Validate option belongs to this question
//            if (selectedOption.getQuestion() == null
//                    || !questionId.equals(
//                    selectedOption.getQuestion().getId())) {
//
//                throw new IllegalArgumentException(
//                        "Selected option does not belong to question "
//                                + questionId
//                );
//            }
//
//            // 9. Find existing answer or create new answer
//            Answer answer = answerRepository
//                    .findByAttemptIdAndQuestionId(
//                            attemptId,
//                            questionId
//                    )
//                    .orElseGet(Answer::new);
//
//            answer.setAttempt(attempt);
//            answer.setQuestion(question);
//            answer.setSelectedOption(selectedOption);
//
//            boolean isCorrect =
//                    Boolean.TRUE.equals(selectedOption.getCorrect());
//
//            answer.setCorrect(isCorrect);
//
//            int questionMarks = question.getMarks() == null
//                    ? 0
//                    : question.getMarks();
//
//            int marksObtained = isCorrect
//                    ? questionMarks
//                    : 0;
//
//            answer.setMarksObtained(marksObtained);
//            answer.setAnsweredAt(LocalDateTime.now());
//
//            // 10. Save answer
//            answerRepository.save(answer);
//
//            // 11. Calculate result
//            if (isCorrect) {
//                correctAnswers++;
//                score += marksObtained;
//            } else {
//                wrongAnswers++;
//            }
//        }
//
//        // 12. Calculate total marks
//        Integer configuredTotalMarks = exam.getTotalMarks();
//
//        int totalMarks = configuredTotalMarks == null
//                ? 0
//                : configuredTotalMarks;
//
//        // Use question marks if exam total marks are not configured
//        if (totalMarks <= 0) {
//            totalMarks = 0;
//
//            for (Question question : questions) {
//                if (question.getMarks() != null) {
//                    totalMarks += question.getMarks();
//                }
//            }
//        }
//
//        // 13. Calculate percentage
//        double percentage = 0.0;
//
//        if (totalMarks > 0) {
//            percentage = ((double) score / totalMarks) * 100.0;
//        }
//
//        // 14. Update exam attempt
//        attempt.setScore(score);
//        attempt.setTotalMarks(totalMarks);
//        attempt.setCorrectAnswers(correctAnswers);
//        attempt.setWrongAnswers(wrongAnswers);
//        attempt.setUnanswered(unanswered);
//        attempt.setPercentage(percentage);
//        attempt.setSubmittedAt(LocalDateTime.now());
//
//        attempt.setStatus(
//                ExamAttempt.AttemptStatus.COMPLETED
//        );
//
//        // 15. Save completed attempt
//        ExamAttempt savedAttempt = examAttemptRepository.save(attempt);
//
//        // 16. Save the corresponding result record.
//        // Reuse an existing record if this attempt already has one.
//        Result result = resultRepository.findByAttemptId(savedAttempt.getId())
//                .orElseGet(Result::new);
//
//        result.setAttempt(savedAttempt);
//        result.setScore(savedAttempt.getScore());
//        result.setTotalMarks(savedAttempt.getTotalMarks());
//        result.setPercentage(savedAttempt.getPercentage());
//        result.setCorrectAnswers(savedAttempt.getCorrectAnswers());
//        result.setWrongAnswers(savedAttempt.getWrongAnswers());
//        result.setUnanswered(savedAttempt.getUnanswered());
//
//        int passingMarks = exam.getPassingMarks() == null
//                ? 0
//                : exam.getPassingMarks();
//        result.setPassingMarks(passingMarks);
//
//        int finalScore = savedAttempt.getScore() == null
//                ? 0
//                : savedAttempt.getScore();
//        result.setPassed(finalScore >= passingMarks);
//        result.setResultDate(savedAttempt.getSubmittedAt() != null
//                ? savedAttempt.getSubmittedAt()
//                : LocalDateTime.now());
//
//        resultRepository.save(result);
//
//        return savedAttempt;
//    }
//
//    // =========================================================
//    // GET ANSWERS BY ATTEMPT ID
//    // Initialize lazy relationships before returning answers
//    // =========================================================
//
//    @Transactional(readOnly = true)
//    public List<Answer> getAnswersByAttemptId(Long attemptId) {
//
//        if (attemptId == null) {
//            throw new IllegalArgumentException(
//                    "Exam attempt ID is required."
//            );
//        }
//
//        List<Answer> answers =
//                answerRepository.findByAttemptId(attemptId);
//
//        for (Answer answer : answers) {
//
//            // Initialize Question relationship
//            if (answer.getQuestion() != null) {
//                answer.getQuestion().getId();
//                answer.getQuestion().getQuestionText();
//                answer.getQuestion().getMarks();
//            }
//
//            // Initialize selected Option relationship
//            if (answer.getSelectedOption() != null) {
//                answer.getSelectedOption().getId();
//                answer.getSelectedOption().getOptionLabel();
//                answer.getSelectedOption().getOptionText();
//                answer.getSelectedOption().getCorrect();
//            }
//
//            // Initialize ExamAttempt relationship
//            if (answer.getAttempt() != null) {
//                answer.getAttempt().getId();
//            }
//        }
//
//        return answers;
//    }
//    @Transactional(readOnly = true)
//    public List<Map<String, Object>> getDetailedReview(Long attemptId) {
//
//        if (attemptId == null || attemptId <= 0) {
//            throw new IllegalArgumentException("A valid exam attempt ID is required.");
//        }
//
//        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
//                .orElseThrow(() ->
//                        new IllegalArgumentException(
//                                "Exam attempt not found: " + attemptId
//                        )
//                );
//
//        if (attempt.getStatus() != ExamAttempt.AttemptStatus.COMPLETED
//                && attempt.getStatus() != ExamAttempt.AttemptStatus.AUTO_SUBMITTED) {
//
//            throw new IllegalArgumentException(
//                    "Detailed review is available only for completed exams."
//            );
//        }
//
//        if (attempt.getExam() == null || attempt.getExam().getId() == null) {
//            throw new IllegalStateException("Exam is not associated with this attempt.");
//        }
//
//        Long examId = attempt.getExam().getId();
//
//        // Retrieve all questions, including unanswered questions.
//        List<Question> questions =
//                questionRepository.findByExamId(examId);
//
//        questions.sort(
//                Comparator.comparing(
//                        Question::getQuestionOrder,
//                        Comparator.nullsLast(Comparator.naturalOrder())
//                )
//        );
//
//        // Retrieve answers saved for this attempt.
//        List<Answer> savedAnswers =
//                answerRepository.findDetailedAnswersByAttemptId(attemptId);
//
//        Map<Long, Answer> answerMap = new LinkedHashMap<>();
//
//        for (Answer answer : savedAnswers) {
//            answerMap.put(
//                    answer.getQuestion().getId(),
//                    answer
//            );
//        }
//
//        List<Map<String, Object>> result = new ArrayList<>();
//
//        int questionNumber = 1;
//
//        for (Question question : questions) {
//
//            Long questionId = question.getId();
//
//            Answer savedAnswer = answerMap.get(questionId);
//
//            Option selectedOption = savedAnswer != null
//                    ? savedAnswer.getSelectedOption()
//                    : null;
//
//            List<Option> options =
//                    optionRepository.findByQuestionId(questionId);
//
//            String selectedAnswerText = null;
//            String correctAnswerText = null;
//
//            List<Map<String, Object>> optionDetails =
//                    new ArrayList<>();
//
//            for (Option option : options) {
//
//                boolean isCorrect =
//                        Boolean.TRUE.equals(option.getCorrect());
//
//                boolean isSelected =
//                        selectedOption != null
//                                && selectedOption.getId().equals(option.getId());
//
//                String optionText =
//                        option.getOptionLabel() + ". "
//                                + option.getOptionText();
//
//                if (isSelected) {
//                    selectedAnswerText = optionText;
//                }
//
//                if (isCorrect) {
//                    correctAnswerText = optionText;
//                }
//
//                Map<String, Object> optionData =
//                        new LinkedHashMap<>();
//
//                optionData.put("label", option.getOptionLabel());
//                optionData.put("text", option.getOptionText());
//                optionData.put("correct", isCorrect);
//                optionData.put("selected", isSelected);
//
//                optionDetails.add(optionData);
//            }
//
//            String status;
//
//            if (savedAnswer == null || selectedOption == null) {
//                status = "Unanswered";
//            } else if (Boolean.TRUE.equals(savedAnswer.getCorrect())) {
//                status = "Correct";
//            } else {
//                status = "Incorrect";
//            }
//
//            int marks = question.getMarks() != null
//                    ? question.getMarks()
//                    : 0;
//
//            int marksObtained = savedAnswer != null
//                    && savedAnswer.getMarksObtained() != null
//                    ? savedAnswer.getMarksObtained()
//                    : 0;
//
//            Map<String, Object> questionData =
//                    new LinkedHashMap<>();
//
//            questionData.put("questionNumber", questionNumber++);
//            questionData.put("questionId", questionId);
//            questionData.put("questionText", question.getQuestionText());
//            questionData.put("marks", marks);
//            questionData.put("marksObtained", marksObtained);
//            questionData.put("status", status);
//            questionData.put("selectedAnswer", selectedAnswerText);
//            questionData.put("correctAnswer", correctAnswerText);
//            questionData.put("options", optionDetails);
//
//            result.add(questionData);
//        }
//
//        return result;
//    }
//}



//package com.sevak.sevakramonlineexamsystem.service;
//
//import com.sevak.sevakramonlineexamsystem.entity.*;
//import com.sevak.sevakramonlineexamsystem.repository.AnswerRepository;
//import com.sevak.sevakramonlineexamsystem.repository.ExamAttemptRepository;
//import com.sevak.sevakramonlineexamsystem.repository.OptionRepository;
//import com.sevak.sevakramonlineexamsystem.repository.QuestionRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//@Service
//public class AnswerService {
//
//    private final AnswerRepository answerRepository;
//    private final ExamAttemptRepository examAttemptRepository;
//    private final QuestionRepository questionRepository;
//    private final OptionRepository optionRepository;
//
//    public AnswerService(
//            AnswerRepository answerRepository,
//            ExamAttemptRepository examAttemptRepository,
//            QuestionRepository questionRepository,
//            OptionRepository optionRepository) {
//
//        this.answerRepository = answerRepository;
//        this.examAttemptRepository = examAttemptRepository;
//        this.questionRepository = questionRepository;
//        this.optionRepository = optionRepository;
//    }
//
//    // =========================================================
//    // SUBMIT EXAM
//    // =========================================================
//
//    @Transactional
//    public ExamAttempt submitExam(
//            Long attemptId,
//            Map<Long, Long> answers) {
//
//        // 1. Validate attempt ID
//        if (attemptId == null) {
//            throw new IllegalArgumentException(
//                    "Exam attempt ID is required."
//            );
//        }
//
//        // 2. Find exam attempt
//        ExamAttempt attempt = examAttemptRepository
//                .findById(attemptId)
//                .orElseThrow(() ->
//                        new IllegalArgumentException(
//                                "Exam attempt not found: " + attemptId
//                        )
//                );
//
//        // 3. Validate attempt status
//        if (attempt.getStatus()
//                != ExamAttempt.AttemptStatus.IN_PROGRESS) {
//
//            throw new IllegalStateException(
//                    "This exam attempt has already been submitted."
//            );
//        }
//
//        // 4. Get exam
//        Exam exam = attempt.getExam();
//
//        if (exam == null) {
//            throw new IllegalStateException(
//                    "Exam is not associated with this attempt."
//            );
//        }
//
//        Long examId = exam.getId();
//
//        // 5. Get active questions for this exam
//        List<Question> questions =
//                questionRepository.findByExamIdAndActiveTrue(examId);
//
//        if (questions == null || questions.isEmpty()) {
//            throw new IllegalStateException(
//                    "No active questions found for this exam."
//            );
//        }
//
//        int score = 0;
//        int correctAnswers = 0;
//        int wrongAnswers = 0;
//        int unanswered = 0;
//
//        Set<Long> processedQuestionIds = new HashSet<>();
//
//        // 6. Process each question
//        for (Question question : questions) {
//
//            Long questionId = question.getId();
//
//            if (questionId == null
//                    || !processedQuestionIds.add(questionId)) {
//                continue;
//            }
//
//            Long selectedOptionId = null;
//
//            if (answers != null) {
//                selectedOptionId = answers.get(questionId);
//            }
//
//            // Question not answered
//            if (selectedOptionId == null) {
//                unanswered++;
//                continue;
//            }
//
//            // 7. Find selected option
//            final Long optionId = selectedOptionId;
//
//            Option selectedOption = optionRepository
//                    .findById(optionId)
//                    .orElseThrow(() ->
//                            new IllegalArgumentException(
//                                    "Selected option not found: " + optionId
//                            )
//                    );
//
//            // 8. Validate option belongs to this question
//            if (selectedOption.getQuestion() == null
//                    || !questionId.equals(
//                    selectedOption.getQuestion().getId())) {
//
//                throw new IllegalArgumentException(
//                        "Selected option does not belong to question "
//                                + questionId
//                );
//            }
//
//            // 9. Find existing answer or create new answer
//            Answer answer = answerRepository
//                    .findByAttemptIdAndQuestionId(
//                            attemptId,
//                            questionId
//                    )
//                    .orElseGet(Answer::new);
//
//            answer.setAttempt(attempt);
//            answer.setQuestion(question);
//            answer.setSelectedOption(selectedOption);
//
//            boolean isCorrect =
//                    Boolean.TRUE.equals(selectedOption.getCorrect());
//
//            answer.setCorrect(isCorrect);
//
//            int questionMarks = question.getMarks() == null
//                    ? 0
//                    : question.getMarks();
//
//            int marksObtained = isCorrect
//                    ? questionMarks
//                    : 0;
//
//            answer.setMarksObtained(marksObtained);
//            answer.setAnsweredAt(LocalDateTime.now());
//
//            // 10. Save answer
//            answerRepository.save(answer);
//
//            // 11. Calculate result
//            if (isCorrect) {
//                correctAnswers++;
//                score += marksObtained;
//            } else {
//                wrongAnswers++;
//            }
//        }
//
//        // 12. Calculate total marks
//        Integer configuredTotalMarks = exam.getTotalMarks();
//
//        int totalMarks = configuredTotalMarks == null
//                ? 0
//                : configuredTotalMarks;
//
//        // Use question marks if exam total marks are not configured
//        if (totalMarks <= 0) {
//            totalMarks = 0;
//
//            for (Question question : questions) {
//                if (question.getMarks() != null) {
//                    totalMarks += question.getMarks();
//                }
//            }
//        }
//
//        // 13. Calculate percentage
//        double percentage = 0.0;
//
//        if (totalMarks > 0) {
//            percentage = ((double) score / totalMarks) * 100.0;
//        }
//
//        // 14. Update exam attempt
//        attempt.setScore(score);
//        attempt.setTotalMarks(totalMarks);
//        attempt.setCorrectAnswers(correctAnswers);
//        attempt.setWrongAnswers(wrongAnswers);
//        attempt.setUnanswered(unanswered);
//        attempt.setPercentage(percentage);
//        attempt.setSubmittedAt(LocalDateTime.now());
//
//        attempt.setStatus(
//                ExamAttempt.AttemptStatus.COMPLETED
//        );
//
//        // 15. Save completed attempt
//        return examAttemptRepository.save(attempt);
//    }
//
//    // =========================================================
//    // GET ANSWERS BY ATTEMPT ID
//    // =========================================================
//
//    // =========================================================
//// GET ANSWERS BY ATTEMPT ID
//// Initialize lazy relationships before returning answers
//// =========================================================
//
//    @Transactional(readOnly = true)
//    public List<Answer> getAnswersByAttemptId(Long attemptId) {
//
//        if (attemptId == null) {
//            throw new IllegalArgumentException(
//                    "Exam attempt ID is required."
//            );
//        }
//
//        List<Answer> answers =
//                answerRepository.findByAttemptId(attemptId);
//
//        for (Answer answer : answers) {
//
//            // Initialize Question relationship
//            if (answer.getQuestion() != null) {
//                answer.getQuestion().getId();
//                answer.getQuestion().getQuestionText();
//                answer.getQuestion().getMarks();
//            }
//
//            // Initialize selected Option relationship
//            if (answer.getSelectedOption() != null) {
//                answer.getSelectedOption().getId();
//                answer.getSelectedOption().getOptionLabel();
//                answer.getSelectedOption().getOptionText();
//                answer.getSelectedOption().getCorrect();
//            }
//
//            // Initialize ExamAttempt relationship
//            if (answer.getAttempt() != null) {
//                answer.getAttempt().getId();
//            }
//        }
//
//        return answers;
//    }
//    @Transactional(readOnly = true)
//    public List<Map<String, Object>> getDetailedReview(Long attemptId) {
//
//        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
//                .orElseThrow(() ->
//                        new IllegalArgumentException(
//                                "Exam attempt not found: " + attemptId
//                        )
//                );
//
//        if (attempt.getStatus() != ExamAttempt.AttemptStatus.COMPLETED
//                && attempt.getStatus() != ExamAttempt.AttemptStatus.AUTO_SUBMITTED) {
//
//            throw new IllegalArgumentException(
//                    "Detailed review is available only for completed exams."
//            );
//        }
//
//        Long examId = attempt.getExam().getId();
//
//        // Retrieve all questions, including unanswered questions.
//        List<Question> questions =
//                questionRepository.findByExamId(examId);
//
//        questions.sort(
//                Comparator.comparing(
//                        Question::getQuestionOrder,
//                        Comparator.nullsLast(Comparator.naturalOrder())
//                )
//        );
//
//        // Retrieve answers saved for this attempt.
//        List<Answer> savedAnswers =
//                answerRepository.findDetailedAnswersByAttemptId(attemptId);
//
//        Map<Long, Answer> answerMap = new LinkedHashMap<>();
//
//        for (Answer answer : savedAnswers) {
//            answerMap.put(
//                    answer.getQuestion().getId(),
//                    answer
//            );
//        }
//
//        List<Map<String, Object>> result = new ArrayList<>();
//
//        int questionNumber = 1;
//
//        for (Question question : questions) {
//
//            Long questionId = question.getId();
//
//            Answer savedAnswer = answerMap.get(questionId);
//
//            Option selectedOption = savedAnswer != null
//                    ? savedAnswer.getSelectedOption()
//                    : null;
//
//            List<Option> options =
//                    optionRepository.findByQuestionId(questionId);
//
//            String selectedAnswerText = null;
//            String correctAnswerText = null;
//
//            List<Map<String, Object>> optionDetails =
//                    new ArrayList<>();
//
//            for (Option option : options) {
//
//                boolean isCorrect =
//                        Boolean.TRUE.equals(option.getCorrect());
//
//                boolean isSelected =
//                        selectedOption != null
//                                && selectedOption.getId().equals(option.getId());
//
//                String optionText =
//                        option.getOptionLabel() + ". "
//                                + option.getOptionText();
//
//                if (isSelected) {
//                    selectedAnswerText = optionText;
//                }
//
//                if (isCorrect) {
//                    correctAnswerText = optionText;
//                }
//
//                Map<String, Object> optionData =
//                        new LinkedHashMap<>();
//
//                optionData.put("label", option.getOptionLabel());
//                optionData.put("text", option.getOptionText());
//                optionData.put("correct", isCorrect);
//                optionData.put("selected", isSelected);
//
//                optionDetails.add(optionData);
//            }
//
//            String status;
//
//            if (savedAnswer == null || selectedOption == null) {
//                status = "Unanswered";
//            } else if (Boolean.TRUE.equals(savedAnswer.getCorrect())) {
//                status = "Correct";
//            } else {
//                status = "Incorrect";
//            }
//
//            int marks = question.getMarks() != null
//                    ? question.getMarks()
//                    : 0;
//
//            int marksObtained = savedAnswer != null
//                    && savedAnswer.getMarksObtained() != null
//                    ? savedAnswer.getMarksObtained()
//                    : 0;
//
//            Map<String, Object> questionData =
//                    new LinkedHashMap<>();
//
//            questionData.put("questionNumber", questionNumber++);
//            questionData.put("questionId", questionId);
//            questionData.put("questionText", question.getQuestionText());
//            questionData.put("marks", marks);
//            questionData.put("marksObtained", marksObtained);
//            questionData.put("status", status);
//            questionData.put("selectedAnswer", selectedAnswerText);
//            questionData.put("correctAnswer", correctAnswerText);
//            questionData.put("options", optionDetails);
//
//            result.add(questionData);
//        }
//
//        return result;
//    }
//}