package com.sevak.sevakramonlineexamsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private ExamAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private Option selectedOption;

    @Column(name = "is_correct")
    private Boolean correct;

    @Column(name = "marks_obtained")
    private Integer marksObtained;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Answer() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExamAttempt getAttempt() {
        return attempt;
    }

    public void setAttempt(ExamAttempt attempt) {
        this.attempt = attempt;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Option getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(Option selectedOption) {
        this.selectedOption = selectedOption;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public Integer getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(Integer marksObtained) {
        this.marksObtained = marksObtained;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (answeredAt == null) {
            answeredAt = now;
        }

        if (correct == null) {
            correct = false;
        }

        if (marksObtained == null) {
            marksObtained = 0;
        }
    }
}

















//package com.sevak.sevakramonlineexamsystem.entity;
//
//import jakarta.persistence.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "answers")
//public class Answer {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "attempt_id", nullable = false)
//    private ExamAttempt attempt;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "question_id", nullable = false)
//    private Question question;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "selected_option_id")
//    private Option selectedOption;
//
//    @Column(name = "is_correct")
//    private Boolean correct;
//
//    @Column(name = "marks_obtained")
//    private Integer marksObtained;
//
//    @Column(name = "answered_at")
//    private LocalDateTime answeredAt;
//
//    @Column(name = "created_at", nullable = false)
//    private LocalDateTime createdAt;
//
//
//    // ================= CONSTRUCTOR =================
//
//    public Answer() {
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
//    public ExamAttempt getAttempt() {
//        return attempt;
//    }
//
//    public void setAttempt(ExamAttempt attempt) {
//        this.attempt = attempt;
//    }
//
//    public Question getQuestion() {
//        return question;
//    }
//
//    public void setQuestion(Question question) {
//        this.question = question;
//    }
//
//    public Option getSelectedOption() {
//        return selectedOption;
//    }
//
//    public void setSelectedOption(Option selectedOption) {
//        this.selectedOption = selectedOption;
//    }
//
//    public Boolean getCorrect() {
//        return correct;
//    }
//
//    public void setCorrect(Boolean correct) {
//        this.correct = correct;
//    }
//
//    public Integer getMarksObtained() {
//        return marksObtained;
//    }
//
//    public void setMarksObtained(Integer marksObtained) {
//        this.marksObtained = marksObtained;
//    }
//
//    public LocalDateTime getAnsweredAt() {
//        return answeredAt;
//    }
//
//    public void setAnsweredAt(LocalDateTime answeredAt) {
//        this.answeredAt = answeredAt;
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
//
//    // ================= PRE-PERSIST =================
//
//    @PrePersist
//    protected void onCreate() {
//
//        createdAt = LocalDateTime.now();
//
//        if (answeredAt == null) {
//            answeredAt = LocalDateTime.now();
//        }
//
//        if (correct == null) {
//            correct = false;
//        }
//
//        if (marksObtained == null) {
//            marksObtained = 0;
//        }
//    }
//}