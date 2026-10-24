package com.sevak.sevakramonlineexamsystem.controller;

import com.sevak.sevakramonlineexamsystem.dto.OptionResponse;
import com.sevak.sevakramonlineexamsystem.entity.Exam;
import com.sevak.sevakramonlineexamsystem.entity.Question;
import com.sevak.sevakramonlineexamsystem.repository.ExamRepository;
import  com.sevak.sevakramonlineexamsystem.service.QuestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final ExamRepository examRepository;

    public QuestionController(
            QuestionService questionService,
            ExamRepository examRepository) {

        this.questionService = questionService;
        this.examRepository = examRepository;
    }

    // =========================================================
    // SHOW QUESTIONS PAGE
    // URL: /admin/questions
    // =========================================================
    @GetMapping
    public String showQuestions(
            @RequestParam(required = false) Long examId,
            Model model) {

        // Load all exams for dropdown
        List<Exam> exams = examRepository.findAll();

        model.addAttribute("exams", exams);
        model.addAttribute("selectedExamId", examId);

        // Load questions for selected exam
        if (examId != null) {

            List<Question> questions =
                    questionService.getQuestionsByExamId(examId);

            model.addAttribute("questions", questions);

        } else {

            model.addAttribute("questions", List.of());
        }

        return "admin-questions";
    }

    // =========================================================
    // CREATE QUESTION
    // URL: POST /admin/questions
    // =========================================================
    @PostMapping
    public String createQuestion(
            @RequestParam Long examId,
            @RequestParam String questionText,
            @RequestParam String questionType,
            @RequestParam Integer marks,
            @RequestParam(required = false) Integer questionOrder,
            @RequestParam List<String> optionTexts,
            @RequestParam Integer correctOptionIndex,
            Model model) {

        try {

            questionService.createQuestion(
                    examId,
                    questionText,
                    questionType,
                    marks,
                    questionOrder,
                    optionTexts,
                    correctOptionIndex
            );

            return "redirect:/admin/questions?examId=" + examId;

        } catch (RuntimeException ex) {

            model.addAttribute("error", ex.getMessage());

            model.addAttribute(
                    "exams",
                    examRepository.findAll()
            );

            model.addAttribute(
                    "selectedExamId",
                    examId
            );

            model.addAttribute(
                    "questions",
                    questionService.getQuestionsByExamId(examId)
            );

            return "admin-questions";
        }
    }

    // =========================================================
    // GET OPTIONS FOR QUESTION
    // URL: GET /admin/questions/{id}/options
    // =========================================================
    @GetMapping("/{id}/options")
    @ResponseBody
    public List<OptionResponse> getOptions(@PathVariable Long id) {

        return questionService.getOptionsByQuestionId(id)
                .stream()
                .map(option -> new OptionResponse(
                        option.getId(),
                        option.getOptionLabel(),
                        option.getOptionText(),
                        option.getCorrect()
                ))
                .collect(Collectors.toList());
    }

    // ==========================================
// DELETE QUESTION
// ==========================================

    @PostMapping("/{id}/delete")
    public String deleteQuestion(
            @PathVariable Long id,
            @RequestParam Long examId,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        try {

            String message = questionService.deleteQuestion(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    message
            );

        } catch (RuntimeException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Unable to delete question: " + ex.getMessage()
            );
        }

        return "redirect:/admin/questions?examId=" + examId;
    }

    // =========================================================
    // TOGGLE QUESTION STATUS
    // URL: POST /admin/questions/{id}/toggle-status
    // =========================================================
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(
            @PathVariable Long id,
            @RequestParam Long examId) {

        questionService.toggleQuestionStatus(id);

        return "redirect:/admin/questions?examId=" + examId;
    }
    @PostMapping("/{id}/update")
    public String updateQuestion(
            @PathVariable Long id,
            @RequestParam Long examId,
            @RequestParam String questionText,
            @RequestParam String questionType,
            @RequestParam Integer marks,
            @RequestParam(required = false) Integer questionOrder,
            @RequestParam List<String> optionTexts,
            @RequestParam Integer correctOptionIndex,
            Model model) {

        try {

            questionService.updateQuestion(
                    id,
                    questionText,
                    questionType,
                    marks,
                    questionOrder,
                    optionTexts,
                    correctOptionIndex
            );

            return "redirect:/admin/questions?examId=" + examId;

        } catch (RuntimeException ex) {

            model.addAttribute("error", ex.getMessage());

            model.addAttribute(
                    "exams",
                    examRepository.findAll()
            );

            model.addAttribute(
                    "selectedExamId",
                    examId
            );

            model.addAttribute(
                    "questions",
                    questionService.getQuestionsByExamId(examId)
            );

            return "admin-questions";
        }
    }
}