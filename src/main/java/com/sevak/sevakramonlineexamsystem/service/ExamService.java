package com.sevak.sevakramonlineexamsystem.service;

import com.sevak.sevakramonlineexamsystem.entity.Exam;
import com.sevak.sevakramonlineexamsystem.repository.ExamAttemptRepository;
import com.sevak.sevakramonlineexamsystem.repository.ExamRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;

    public ExamService(
            ExamRepository examRepository,
            ExamAttemptRepository examAttemptRepository) {

        this.examRepository = examRepository;
        this.examAttemptRepository = examAttemptRepository;
    }

    // =========================================================
    // CREATE EXAM
    // =========================================================

    public Exam createExam(Exam exam) {

        validateExam(exam);

        exam.setTitle(exam.getTitle().trim());
        exam.setActive(true);

        return examRepository.save(exam);
    }

    // =========================================================
    // GET ALL EXAMS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Exam> getAllExams() {

        return examRepository.findAll();
    }

    // =========================================================
    // GET ACTIVE EXAMS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Exam> getActiveExams() {

        return examRepository.findByActiveTrue();
    }

    // =========================================================
    // GET EXAM BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public Exam getExamById(Long id) {

        validateId(id);

        return examRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Exam not found with ID: " + id
                        )
                );
    }

    // =========================================================
    // UPDATE EXAM
    // =========================================================

    public Exam updateExam(Long id, Exam examDetails) {

        validateId(id);
        validateExam(examDetails);

        Exam existingExam = getExamById(id);

        existingExam.setTitle(
                examDetails.getTitle().trim()
        );

        existingExam.setDescription(
                examDetails.getDescription()
        );

        existingExam.setCategory(
                examDetails.getCategory()
        );

        existingExam.setDifficulty(
                examDetails.getDifficulty()
        );

        existingExam.setTotalQuestions(
                examDetails.getTotalQuestions()
        );

        existingExam.setDurationMinutes(
                examDetails.getDurationMinutes()
        );

        existingExam.setTotalMarks(
                examDetails.getTotalMarks()
        );

        existingExam.setPassingMarks(
                examDetails.getPassingMarks()
        );

        // Preserve the existing active status.
        return examRepository.save(existingExam);
    }

    // =========================================================
    // DELETE EXAM
    // =========================================================

    public void deleteExam(Long id) {

        validateId(id);

        Exam exam = getExamById(id);

        // Preserve historical student attempts.
        if (examAttemptRepository.existsByExamId(id)) {

            throw new IllegalStateException(
                    "This exam cannot be permanently deleted because "
                            + "student attempts exist. Deactivate the exam instead."
            );
        }

        try {

            examRepository.delete(exam);

            // Execute the delete now so FK violations can be caught here.
            examRepository.flush();

        } catch (DataIntegrityViolationException exception) {

            throw new IllegalStateException(
                    "The exam could not be deleted because related "
                            + "records still reference it. Check related "
                            + "questions and other database relationships.",
                    exception
            );
        }
    }

    // =========================================================
    // ACTIVATE / DEACTIVATE EXAM
    // =========================================================

    public Exam toggleExamStatus(Long id) {

        validateId(id);

        Exam exam = getExamById(id);

        boolean currentStatus =
                Boolean.TRUE.equals(exam.getActive());

        exam.setActive(!currentStatus);

        return examRepository.save(exam);
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateExam(Exam exam) {

        if (exam == null) {
            throw new IllegalArgumentException(
                    "Exam details are required."
            );
        }

        if (exam.getTitle() == null
                || exam.getTitle().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Exam title is required."
            );
        }

        if (exam.getTitle().trim().length() > 150) {

            throw new IllegalArgumentException(
                    "Exam title cannot exceed 150 characters."
            );
        }

        if (exam.getTotalQuestions() == null
                || exam.getTotalQuestions() <= 0) {

            throw new IllegalArgumentException(
                    "Total questions must be greater than 0."
            );
        }

        if (exam.getDurationMinutes() == null
                || exam.getDurationMinutes() <= 0) {

            throw new IllegalArgumentException(
                    "Duration must be greater than 0."
            );
        }

        if (exam.getTotalMarks() == null
                || exam.getTotalMarks() <= 0) {

            throw new IllegalArgumentException(
                    "Total marks must be greater than 0."
            );
        }

        if (exam.getPassingMarks() == null
                || exam.getPassingMarks() < 0) {

            throw new IllegalArgumentException(
                    "Passing marks cannot be negative."
            );
        }

        if (exam.getPassingMarks() > exam.getTotalMarks()) {

            throw new IllegalArgumentException(
                    "Passing marks cannot be greater than total marks."
            );
        }
    }

    // =========================================================
    // ID VALIDATION
    // =========================================================

    private void validateId(Long id) {

        if (id == null || id <= 0) {

            throw new IllegalArgumentException(
                    "A valid exam ID is required."
            );
        }
    }
}










//package com.sevak.sevakramonlineexamsystem.service;
//
//import com.sevak.sevakramonlineexamsystem.entity.Exam;
//import com.sevak.sevakramonlineexamsystem.repository.ExamAttemptRepository;
//import com.sevak.sevakramonlineexamsystem.repository.ExamRepository;
//
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//@Transactional
//public class ExamService {
//
//    private final ExamRepository examRepository;
//    private final ExamAttemptRepository examAttemptRepository;
//
//    public ExamService(
//            ExamRepository examRepository,
//            ExamAttemptRepository examAttemptRepository) {
//
//        this.examRepository = examRepository;
//        this.examAttemptRepository = examAttemptRepository;
//    }
//
//    // =========================================================
//    // CREATE EXAM
//    // =========================================================
//
//    public Exam createExam(Exam exam) {
//
//        validateExam(exam);
//
//        exam.setActive(true);
//        exam.setTitle(exam.getTitle().trim());
//
//        return examRepository.save(exam);
//    }
//
//    // =========================================================
//    // GET ALL EXAMS
//    // =========================================================
//
//    @Transactional(readOnly = true)
//    public List<Exam> getAllExams() {
//
//        return examRepository.findAll();
//    }
//
//    // =========================================================
//    // GET ACTIVE EXAMS
//    // =========================================================
//
//    @Transactional(readOnly = true)
//    public List<Exam> getActiveExams() {
//
//        return examRepository.findByActiveTrue();
//    }
//
//    // =========================================================
//    // GET EXAM BY ID
//    // =========================================================
//
//    @Transactional(readOnly = true)
//    public Exam getExamById(Long id) {
//
//        validateId(id);
//
//        return examRepository.findById(id)
//                .orElseThrow(() ->
//                        new IllegalArgumentException(
//                                "Exam not found with ID: " + id
//                        )
//                );
//    }
//
//    // =========================================================
//    // UPDATE EXAM
//    // =========================================================
//
//    public Exam updateExam(Long id, Exam examDetails) {
//
//        validateId(id);
//        validateExam(examDetails);
//
//        Exam existingExam = getExamById(id);
//
//        existingExam.setTitle(
//                examDetails.getTitle().trim()
//        );
//
//        existingExam.setDescription(
//                examDetails.getDescription()
//        );
//
//        existingExam.setCategory(
//                examDetails.getCategory()
//        );
//
//        existingExam.setDifficulty(
//                examDetails.getDifficulty()
//        );
//
//        existingExam.setTotalQuestions(
//                examDetails.getTotalQuestions()
//        );
//
//        existingExam.setDurationMinutes(
//                examDetails.getDurationMinutes()
//        );
//
//        existingExam.setTotalMarks(
//                examDetails.getTotalMarks()
//        );
//
//        existingExam.setPassingMarks(
//                examDetails.getPassingMarks()
//        );
//
//        return examRepository.save(existingExam);
//    }
//
//    // =========================================================
//    // DELETE EXAM
//    // =========================================================
//
//    public void deleteExam(Long id) {
//
//        validateId(id);
//
//        Exam exam = getExamById(id);
//
//        // Preserve historical student attempts.
//        if (examAttemptRepository.existsByExamId(id)) {
//
//            throw new IllegalStateException(
//                    "This exam cannot be permanently deleted because "
//                            + "student attempts exist. Deactivate the exam instead."
//            );
//        }
//
//        try {
//
//            examRepository.delete(exam);
//
//            // Force SQL execution here so database constraint
//            // violations can be detected within this method.
//            examRepository.flush();
//
//        } catch (DataIntegrityViolationException exception) {
//
//            throw new IllegalStateException(
//                    "The exam could not be deleted because related "
//                            + "records still reference it. Check related "
//                            + "questions and other database relationships.",
//                    exception
//            );
//        }
//    }
//
//    // =========================================================
//    // ACTIVATE / DEACTIVATE EXAM
//    // =========================================================
//
//    public Exam toggleExamStatus(Long id) {
//
//        validateId(id);
//
//        Exam exam = getExamById(id);
//
//        boolean currentStatus =
//                Boolean.TRUE.equals(exam.getActive());
//
//        exam.setActive(!currentStatus);
//
//        return examRepository.save(exam);
//    }
//
//    // =========================================================
//    // VALIDATION
//    // =========================================================
//
//    private void validateExam(Exam exam) {
//
//        if (exam == null) {
//            throw new IllegalArgumentException(
//                    "Exam details are required."
//            );
//        }
//
//        if (exam.getTitle() == null
//                || exam.getTitle().trim().isEmpty()) {
//
//            throw new IllegalArgumentException(
//                    "Exam title is required."
//            );
//        }
//
//        if (exam.getTitle().trim().length() > 150) {
//
//            throw new IllegalArgumentException(
//                    "Exam title cannot exceed 150 characters."
//            );
//        }
//
//        if (exam.getTotalQuestions() == null
//                || exam.getTotalQuestions() <= 0) {
//
//            throw new IllegalArgumentException(
//                    "Total questions must be greater than 0."
//            );
//        }
//
//        if (exam.getDurationMinutes() == null
//                || exam.getDurationMinutes() <= 0) {
//
//            throw new IllegalArgumentException(
//                    "Duration must be greater than 0."
//            );
//        }
//
//        if (exam.getTotalMarks() == null
//                || exam.getTotalMarks() <= 0) {
//
//            throw new IllegalArgumentException(
//                    "Total marks must be greater than 0."
//            );
//        }
//
//        if (exam.getPassingMarks() == null
//                || exam.getPassingMarks() < 0) {
//
//            throw new IllegalArgumentException(
//                    "Passing marks cannot be negative."
//            );
//        }
//
//        if (exam.getPassingMarks() > exam.getTotalMarks()) {
//
//            throw new IllegalArgumentException(
//                    "Passing marks cannot be greater than total marks."
//            );
//        }
//    }
//
//    // =========================================================
//    // ID VALIDATION
//    // =========================================================
//
//    private void validateId(Long id) {
//
//        if (id == null || id <= 0) {
//
//            throw new IllegalArgumentException(
//                    "A valid exam ID is required."
//            );
//        }
//    }
//}