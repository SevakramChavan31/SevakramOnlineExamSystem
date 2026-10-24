package com.sevak.sevakramonlineexamsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "question_options")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "option_label", nullable = false, length = 5)
    private String optionLabel;

    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
    private String optionText;

    @Column(name = "is_correct", nullable = false)
    private Boolean correct = false;

    @Column(name = "option_order")
    private Integer optionOrder;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Option() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getOptionLabel() {
        return optionLabel;
    }

    public void setOptionLabel(String optionLabel) {
        this.optionLabel = optionLabel;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public Integer getOptionOrder() {
        return optionOrder;
    }

    public void setOptionOrder(Integer optionOrder) {
        this.optionOrder = optionOrder;
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

        if (correct == null) {
            correct = false;
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
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "question_options")
//public class Option {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "question_id", nullable = false)
//    private Question question;
//
//    @Column(name = "option_label", nullable = false, length = 5)
//    private String optionLabel;
//
//    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
//    private String optionText;
//
//    @Column(name = "is_correct", nullable = false)
//    private Boolean correct = false;
//
//    @Column(name = "option_order")
//    private Integer optionOrder;
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
//
//    public Option() {
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
//    public Question getQuestion() {
//        return question;
//    }
//
//    public void setQuestion(Question question) {
//        this.question = question;
//    }
//
//    public String getOptionLabel() {
//        return optionLabel;
//    }
//
//    public void setOptionLabel(String optionLabel) {
//        this.optionLabel = optionLabel;
//    }
//
//    public String getOptionText() {
//        return optionText;
//    }
//
//    public void setOptionText(String optionText) {
//        this.optionText = optionText;
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
//    public Integer getOptionOrder() {
//        return optionOrder;
//    }
//
//    public void setOptionOrder(Integer optionOrder) {
//        this.optionOrder = optionOrder;
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
//
//    @PrePersist
//    protected void onCreate() {
//
//        createdAt = LocalDateTime.now();
//
//        if (correct == null) {
//            correct = false;
//        }
//
//        if (active == null) {
//            active = true;
//        }
//    }
//
//
//    // Before Update
//
//    @PreUpdate
//    protected void onUpdate() {
//
//        updatedAt = LocalDateTime.now();
//    }
//}