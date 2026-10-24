package com.sevak.sevakramonlineexamsystem.controller;

import com.sevak.sevakramonlineexamsystem.entity.User;
import com.sevak.sevakramonlineexamsystem.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // =========================================================
    // Show all students
    // =========================================================

    @GetMapping
    public String showStudents(
            @RequestParam(required = false) String keyword,
            Model model) {

        List<User> students;

        if (keyword != null && !keyword.trim().isEmpty()) {

            students = studentService.searchStudents(
                    keyword.trim()
            );

            model.addAttribute(
                    "keyword",
                    keyword
            );

        } else {

            students = studentService.getAllStudents();

            model.addAttribute(
                    "keyword",
                    ""
            );
        }

        // =====================================================
        // Student statistics
        // =====================================================

        Map<Long, Long> examsAttempted =
                new HashMap<>();

        Map<Long, Double> averageScores =
                new HashMap<>();

        for (User student : students) {

            Long studentId = student.getId();

            long examCount =
                    studentService.getExamsAttempted(studentId);

            examsAttempted.put(
                    studentId,
                    examCount
            );

            double averageScore =
                    studentService.getAverageScore(studentId);

            averageScores.put(
                    studentId,
                    averageScore
            );
        }

        // =====================================================
        // Send data to Thymeleaf
        // =====================================================

        model.addAttribute(
                "students",
                students
        );

        model.addAttribute(
                "examsAttempted",
                examsAttempted
        );

        model.addAttribute(
                "averageScores",
                averageScores
        );

        model.addAttribute(
                "newStudentsThisMonth",
                studentService.getNewStudentsThisMonth()
        );

        return "admin-students";
    }

    // =========================================================
    // Activate / Deactivate
    // =========================================================

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(
            @PathVariable Long id) {

        studentService.toggleStudentStatus(id);

        return "redirect:/admin/students";
    }

    // =========================================================
    // Delete student
    // =========================================================

    @PostMapping("/{id}/delete")
    public String deleteStudent(
            @PathVariable Long id) {

        studentService.deleteStudent(id);

        return "redirect:/admin/students";
    }
}