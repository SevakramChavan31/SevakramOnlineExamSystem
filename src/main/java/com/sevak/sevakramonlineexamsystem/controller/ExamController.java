package com.sevak.sevakramonlineexamsystem.controller;

import com.sevak.sevakramonlineexamsystem.entity.Exam;
import com.sevak.sevakramonlineexamsystem.service.ExamService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    // =========================================================
    // SHOW ALL EXAMS
    // =========================================================

    @GetMapping
    public String showExams(Model model) {

        model.addAttribute(
                "exams",
                examService.getAllExams()
        );

        model.addAttribute(
                "exam",
                new Exam()
        );

        return "admin-exams";
    }

    // =========================================================
    // CREATE EXAM
    // =========================================================

    @PostMapping
    public String createExam(
            @ModelAttribute("exam") Exam exam,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            examService.createExam(exam);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Exam created successfully."
            );

            return "redirect:/admin/exams";

        } catch (RuntimeException ex) {

            model.addAttribute("exam", exam);
            model.addAttribute("error", getErrorMessage(ex));
            model.addAttribute("exams", examService.getAllExams());

            return "admin-exams";
        }
    }

    // =========================================================
    // UPDATE EXAM
    // =========================================================

    @PostMapping("/{id}/update")
    public String updateExam(
            @PathVariable Long id,
            @ModelAttribute("exam") Exam exam,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            examService.updateExam(id, exam);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Exam updated successfully."
            );

            return "redirect:/admin/exams";

        } catch (RuntimeException ex) {

            model.addAttribute("exam", exam);
            model.addAttribute("error", getErrorMessage(ex));
            model.addAttribute("exams", examService.getAllExams());

            return "admin-exams";
        }
    }

    // =========================================================
    // DELETE EXAM
    // =========================================================

    @PostMapping("/{id}/delete")
    public String deleteExam(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            examService.deleteExam(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Exam deleted successfully."
            );

        } catch (RuntimeException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    getErrorMessage(ex)
            );
        }

        return "redirect:/admin/exams";
    }

    // =========================================================
    // ACTIVATE / DEACTIVATE EXAM
    // =========================================================

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            examService.toggleExamStatus(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Exam status updated successfully."
            );

        } catch (RuntimeException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    getErrorMessage(ex)
            );
        }

        return "redirect:/admin/exams";
    }

    // =========================================================
    // ERROR MESSAGE HELPER
    // =========================================================

    private String getErrorMessage(RuntimeException ex) {

        if (ex == null
                || ex.getMessage() == null
                || ex.getMessage().trim().isEmpty()) {

            return "An unexpected error occurred. Please try again.";
        }

        return ex.getMessage();
    }
}