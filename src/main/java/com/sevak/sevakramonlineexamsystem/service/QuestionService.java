package com.sevak.sevakramonlineexamsystem.service;

import com.sevak.sevakramonlineexamsystem.entity.Exam;
import com.sevak.sevakramonlineexamsystem.entity.Option;
import com.sevak.sevakramonlineexamsystem.entity.Question;
import com.sevak.sevakramonlineexamsystem.repository.AnswerRepository;
import com.sevak.sevakramonlineexamsystem.repository.ExamRepository;
import com.sevak.sevakramonlineexamsystem.repository.OptionRepository;
import com.sevak.sevakramonlineexamsystem.repository.QuestionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final ExamRepository examRepository;
    private final AnswerRepository answerRepository;

    public QuestionService(
            QuestionRepository questionRepository,
            OptionRepository optionRepository,
            ExamRepository examRepository,
            AnswerRepository answerRepository) {

        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.examRepository = examRepository;
        this.answerRepository = answerRepository;
    }

    // ==========================================
    // CREATE QUESTION WITH OPTIONS
    // ==========================================

    @Transactional
    public Question createQuestion(
            Long examId,
            String questionText,
            String questionType,
            Integer marks,
            Integer questionOrder,
            List<String> optionTexts,
            Integer correctOptionIndex) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Exam not found with id: " + examId
                        ));

        validateQuestion(
                questionText,
                questionType,
                marks
        );

        validateOptions(
                optionTexts,
                correctOptionIndex
        );

        Question question = new Question();

        question.setExam(exam);
        question.setQuestionText(questionText);
        question.setQuestionType(questionType);
        question.setMarks(marks);
        question.setQuestionOrder(questionOrder);
        question.setActive(true);

        Question savedQuestion =
                questionRepository.save(question);

        for (int i = 0; i < optionTexts.size(); i++) {

            Option option = new Option();

            option.setQuestion(savedQuestion);

            option.setOptionLabel(
                    String.valueOf((char) ('A' + i))
            );

            option.setOptionText(
                    optionTexts.get(i)
            );

            option.setCorrect(
                    i == correctOptionIndex
            );

            option.setOptionOrder(i + 1);
            option.setActive(true);

            optionRepository.save(option);
        }

        return savedQuestion;
    }

    // ==========================================
    // GET ALL QUESTIONS FOR AN EXAM
    // ==========================================

    public List<Question> getQuestionsByExamId(Long examId) {

        return questionRepository.findByExamId(examId);
    }

    // ==========================================
    // UPDATE QUESTION
    // ==========================================

    @Transactional
    public void updateQuestion(
            Long questionId,
            String questionText,
            String questionType,
            Integer marks,
            Integer questionOrder,
            List<String> optionTexts,
            Integer correctOptionIndex) {

        Question question = getQuestionById(questionId);

        validateQuestion(
                questionText,
                questionType,
                marks
        );

        validateOptions(
                optionTexts,
                correctOptionIndex
        );

        // Prevent modifying a question that has
        // historical answers.
        if (answerRepository.existsByQuestionId(questionId)) {
            throw new RuntimeException(
                    "This question has student answer records. "
                            + "Editing it is restricted to preserve "
                            + "historical exam results."
            );
        }

        question.setQuestionText(questionText);
        question.setQuestionType(questionType);
        question.setMarks(marks);
        question.setQuestionOrder(questionOrder);

        questionRepository.save(question);

        List<Option> existingOptions =
                optionRepository.findByQuestionId(questionId);

        if (existingOptions.size() != optionTexts.size()) {
            throw new RuntimeException(
                    "The number of options cannot be changed "
                            + "using this update operation."
            );
        }

        for (int i = 0; i < optionTexts.size(); i++) {

            Option option = existingOptions.get(i);

            option.setOptionText(optionTexts.get(i));
            option.setCorrect(i == correctOptionIndex);

            option.setOptionLabel(
                    String.valueOf((char) ('A' + i))
            );

            option.setOptionOrder(i + 1);

            optionRepository.save(option);
        }
    }

    // ==========================================
    // GET ACTIVE QUESTIONS FOR AN EXAM
    // ==========================================

    public List<Question> getActiveQuestionsByExamId(Long examId) {

        return questionRepository
                .findByExamIdAndActiveTrue(examId);
    }

    // ==========================================
    // GET QUESTION BY ID
    // ==========================================

    public Question getQuestionById(Long id) {

        return questionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Question not found with id: " + id
                        ));
    }

    // ==========================================
    // GET OPTIONS FOR QUESTION
    // ==========================================

    public List<Option> getOptionsByQuestionId(Long questionId) {

        return optionRepository
                .findByQuestionId(questionId);
    }

    // ==========================================
    // DELETE QUESTION SAFELY
    // ==========================================

    @Transactional
    public String deleteQuestion(Long id) {

        Question question = getQuestionById(id);

        // Check whether any answer references this question.
        boolean hasAnswers =
                answerRepository.existsByQuestionId(id);

        if (hasAnswers) {

            // Preserve student answers and exam history.
            if (Boolean.TRUE.equals(question.getActive())) {

                question.setActive(false);
                questionRepository.save(question);

                return "Question was deactivated because "
                        + "student answers exist. "
                        + "Exam history has been preserved.";
            }

            return "Question is already inactive. "
                    + "Student answers and exam history "
                    + "have been preserved.";
        }

        // No answer records reference this question.
        // Delete its options first to satisfy foreign keys.
        List<Option> options =
                optionRepository.findByQuestionId(id);

        optionRepository.deleteAll(options);

        // Delete the question permanently.
        questionRepository.delete(question);

        return "Question and its options were deleted successfully.";
    }

    // ==========================================
    // ACTIVATE / DEACTIVATE QUESTION
    // ==========================================

    @Transactional
    public Question toggleQuestionStatus(Long id) {

        Question question = getQuestionById(id);

        question.setActive(
                !Boolean.TRUE.equals(question.getActive())
        );

        return questionRepository.save(question);
    }

    // ==========================================
    // VALIDATE QUESTION
    // ==========================================

    private void validateQuestion(
            String questionText,
            String questionType,
            Integer marks) {

        if (questionText == null ||
                questionText.trim().isEmpty()) {

            throw new RuntimeException(
                    "Question text is required"
            );
        }

        if (questionType == null ||
                questionType.trim().isEmpty()) {

            throw new RuntimeException(
                    "Question type is required"
            );
        }

        if (marks == null || marks <= 0) {

            throw new RuntimeException(
                    "Marks must be greater than 0"
            );
        }
    }

    // ==========================================
    // VALIDATE OPTIONS
    // ==========================================

    private void validateOptions(
            List<String> optionTexts,
            Integer correctOptionIndex) {

        if (optionTexts == null ||
                optionTexts.size() < 2) {

            throw new RuntimeException(
                    "At least two options are required"
            );
        }

        for (String optionText : optionTexts) {

            if (optionText == null ||
                    optionText.trim().isEmpty()) {

                throw new RuntimeException(
                        "Option text cannot be empty"
                );
            }
        }

        if (correctOptionIndex == null ||
                correctOptionIndex < 0 ||
                correctOptionIndex >= optionTexts.size()) {

            throw new RuntimeException(
                    "Please select a valid correct option"
            );
        }
    }
}