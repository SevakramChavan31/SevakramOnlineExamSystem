package com.sevak.sevakramonlineexamsystem.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_attempts")
public class ExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttemptStatus status;

    @Column(name = "score")
    private Integer score;

    @Column(name = "total_marks")
    private Integer totalMarks;

    @Column(name = "correct_answers")
    private Integer correctAnswers;

    @Column(name = "wrong_answers")
    private Integer wrongAnswers;

    @Column(name = "unanswered")
    private Integer unanswered;

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ExamAttempt() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Integer getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(Integer wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }

    public Integer getUnanswered() {
        return unanswered;
    }

    public void setUnanswered(Integer unanswered) {
        this.unanswered = unanswered;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (startedAt == null) {
            startedAt = now;
        }

        if (status == null) {
            status = AttemptStatus.IN_PROGRESS;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum AttemptStatus {
        IN_PROGRESS,
        COMPLETED,
        AUTO_SUBMITTED
    }
}

































//package com.sevak.sevakramonlineexamsystem.entity;
//
//import jakarta.persistence.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "exam_attempts")
//public class ExamAttempt {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "exam_id", nullable = false)
//    private Exam exam;
//
//    @Column(name = "started_at", nullable = false)
//    private LocalDateTime startedAt;
//
//    @Column(name = "submitted_at")
//    private LocalDateTime submittedAt;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 20)
//    private AttemptStatus status;
//
//    @Column(name = "score")
//    private Integer score;
//
//    @Column(name = "total_marks")
//    private Integer totalMarks;
//
//    @Column(name = "correct_answers")
//    private Integer correctAnswers;
//
//    @Column(name = "wrong_answers")
//    private Integer wrongAnswers;
//
//    @Column(name = "unanswered")
//    private Integer unanswered;
//
//    @Column(name = "percentage")
//    private Double percentage;
//
//    @Column(name = "created_at", nullable = false)
//    private LocalDateTime createdAt;
//
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//
//    // ================= CONSTRUCTOR =================
//
//    public ExamAttempt() {
//    }
//
//
//    // ================= GETTERS & SETTERS =================
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public User getUser() {
//        return user;
//    }
//
//    public void setUser(User user) {
//        this.user = user;
//    }
//
//    public Exam getExam() {
//        return exam;
//    }
//
//    public void setExam(Exam exam) {
//        this.exam = exam;
//    }
//
//    public LocalDateTime getStartedAt() {
//        return startedAt;
//    }
//
//    public void setStartedAt(LocalDateTime startedAt) {
//        this.startedAt = startedAt;
//    }
//
//    public LocalDateTime getSubmittedAt() {
//        return submittedAt;
//    }
//
//    public void setSubmittedAt(LocalDateTime submittedAt) {
//        this.submittedAt = submittedAt;
//    }
//
//    public AttemptStatus getStatus() {
//        return status;
//    }
//
//    public void setStatus(AttemptStatus status) {
//        this.status = status;
//    }
//
//    public Integer getScore() {
//        return score;
//    }
//
//    public void setScore(Integer score) {
//        this.score = score;
//    }
//
//    public Integer getTotalMarks() {
//        return totalMarks;
//    }
//
//    public void setTotalMarks(Integer totalMarks) {
//        this.totalMarks = totalMarks;
//    }
//
//    public Integer getCorrectAnswers() {
//        return correctAnswers;
//    }
//
//    public void setCorrectAnswers(Integer correctAnswers) {
//        this.correctAnswers = correctAnswers;
//    }
//
//    public Integer getWrongAnswers() {
//        return wrongAnswers;
//    }
//
//    public void setWrongAnswers(Integer wrongAnswers) {
//        this.wrongAnswers = wrongAnswers;
//    }
//
//    public Integer getUnanswered() {
//        return unanswered;
//    }
//
//    public void setUnanswered(Integer unanswered) {
//        this.unanswered = unanswered;
//    }
//
//    public Double getPercentage() {
//        return percentage;
//    }
//
//    public void setPercentage(Double percentage) {
//        this.percentage = percentage;
//    }
//
//    public LocalDateTime getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(LocalDateTime createdAt) {
//        this.createdAt = createdAt;
//    }
//
//    public LocalDateTime getUpdatedAt() {
//        return updatedAt;
//    }
//
//    public void setUpdatedAt(LocalDateTime updatedAt) {
//        this.updatedAt = updatedAt;
//    }
//
//
//    // ================= PRE-PERSIST =================
//
//    @PrePersist
//    protected void onCreate() {
//
//        createdAt = LocalDateTime.now();
//
//        if (startedAt == null) {
//            startedAt = LocalDateTime.now();
//        }
//
//        if (status == null) {
//            status = AttemptStatus.IN_PROGRESS;
//        }
//    }
//
//
//    // ================= PRE-UPDATE =================
//
//    @PreUpdate
//    protected void onUpdate() {
//
//        updatedAt = LocalDateTime.now();
//    }
//
//
//    // ================= ENUM =================
//
//    public enum AttemptStatus {
//
//        IN_PROGRESS,
//        COMPLETED,
//        AUTO_SUBMITTED
//    }
//}