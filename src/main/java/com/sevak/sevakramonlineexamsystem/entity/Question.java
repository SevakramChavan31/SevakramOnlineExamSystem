package com.sevak.sevakramonlineexamsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_type", nullable = false, length = 30)
    private String questionType;

    @Column(nullable = false)
    private Integer marks;

    @Column(name = "question_order")
    private Integer questionOrder;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Question() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }

    public Integer getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(Integer questionOrder) {
        this.questionOrder = questionOrder;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}





//package com.sevak.sevakramonlineexamsystem.entity;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "questions")
//public class Question {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "exam_id", nullable = false)
//    private Exam exam;
//
//    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
//    private String questionText;
//
//    @Column(name = "question_type", nullable = false, length = 30)
//    private String questionType;
//
//    @Column(nullable = false)
//    private Integer marks;
//
//    @Column(name = "question_order")
//    private Integer questionOrder;
//
//    @Column(nullable = false)
//    private Boolean active = true;
//
//    @Column(name = "created_at", nullable = false)
//    private LocalDateTime createdAt;
//
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//
//    // Constructor
//    public Question() {
//    }
//
//
//    // Getters and Setters
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
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
//    public String getQuestionText() {
//        return questionText;
//    }
//
//    public void setQuestionText(String questionText) {
//        this.questionText = questionText;
//    }
//
//    public String getQuestionType() {
//        return questionType;
//    }
//
//    public void setQuestionType(String questionType) {
//        this.questionType = questionType;
//    }
//
//    public Integer getMarks() {
//        return marks;
//    }
//
//    public void setMarks(Integer marks) {
//        this.marks = marks;
//    }
//
//    public Integer getQuestionOrder() {
//        return questionOrder;
//    }
//
//    public void setQuestionOrder(Integer questionOrder) {
//        this.questionOrder = questionOrder;
//    }
//
//    public Boolean getActive() {
//        return active;
//    }
//
//    public void setActive(Boolean active) {
//        this.active = active;
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
//    // Before Insert
//    @PrePersist
//    protected void onCreate() {
//
//        createdAt = LocalDateTime.now();
//
//        if (active == null) {
//            active = true;
//        }
//    }
//
//
//    // Before Update
//    @PreUpdate
//    protected void onUpdate() {
//
//        updatedAt = LocalDateTime.now();
//    }
//}