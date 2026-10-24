package com.sevak.sevakramonlineexamsystem.service;

import com.sevak.sevakramonlineexamsystem.entity.ExamAttempt;
import com.sevak.sevakramonlineexamsystem.entity.User;
import com.sevak.sevakramonlineexamsystem.repository.ExamAttemptRepository;
import com.sevak.sevakramonlineexamsystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentService {

    private final UserRepository userRepository;
    private final ExamAttemptRepository examAttemptRepository;

    public StudentService(
            UserRepository userRepository,
            ExamAttemptRepository examAttemptRepository) {

        this.userRepository = userRepository;
        this.examAttemptRepository = examAttemptRepository;
    }

    // =========================================================
    // Get all students
    // =========================================================

    public List<User> getAllStudents() {

        return userRepository.findByRole(User.Role.STUDENT);
    }

    // =========================================================
    // Get student by ID
    // =========================================================

    public User getStudentById(Long id) {

        User student = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Student not found with id: " + id));

        if (student.getRole() != User.Role.STUDENT) {
            throw new RuntimeException("User is not a student");
        }

        return student;
    }

    // =========================================================
    // Activate / Deactivate student
    // =========================================================

    public User toggleStudentStatus(Long id) {

        User student = getStudentById(id);

        student.setActive(!student.getActive());

        return userRepository.save(student);
    }

    // =========================================================
    // Delete student
    // =========================================================

    public void deleteStudent(Long id) {

        User student = getStudentById(id);

        userRepository.delete(student);
    }

    // =========================================================
    // Search students
    // =========================================================

    public List<User> searchStudents(String keyword) {

        return userRepository
                .findByRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                        User.Role.STUDENT,
                        keyword,
                        User.Role.STUDENT,
                        keyword
                );
    }

    // =========================================================
    // New students this month
    // =========================================================

    public long getNewStudentsThisMonth() {

        LocalDateTime startOfMonth =
                LocalDate.now()
                        .withDayOfMonth(1)
                        .atStartOfDay();

        return userRepository.countByRoleAndCreatedAtGreaterThanEqual(
                User.Role.STUDENT,
                startOfMonth
        );
    }

    // =========================================================
    // Exams attempted by student
    // =========================================================

    public long getExamsAttempted(Long userId) {

        List<ExamAttempt.AttemptStatus> completedStatuses =
                List.of(
                        ExamAttempt.AttemptStatus.COMPLETED,
                        ExamAttempt.AttemptStatus.AUTO_SUBMITTED
                );

        return examAttemptRepository.countCompletedAttemptsByUserId(
                userId,
                completedStatuses
        );
    }

    // =========================================================
    // Average score of student
    // =========================================================

    public double getAverageScore(Long userId) {

        List<ExamAttempt.AttemptStatus> completedStatuses =
                List.of(
                        ExamAttempt.AttemptStatus.COMPLETED,
                        ExamAttempt.AttemptStatus.AUTO_SUBMITTED
                );

        Double average =
                examAttemptRepository.getAveragePercentageByUserId(
                        userId,
                        completedStatuses
                );

        return average != null ? average : 0.0;
    }
}