package com.sevak.sevakramonlineexamsystem.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(length = 50)
    private String category;

    @Column(length = 20)
    private String difficulty;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;

    @Column(name = "passing_marks", nullable = false)
    private Integer passingMarks;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Exam() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }

    public Integer getPassingMarks() {
        return passingMarks;
    }

    public void setPassingMarks(Integer passingMarks) {
        this.passingMarks = passingMarks;
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
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "exams")
//public class Exam {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, length = 150)
//    private String title;
//
//    @Column(length = 500)
//    private String description;
//
//    @Column(length = 50)
//    private String category;
//
//    @Column(length = 20)
//    private String difficulty;
//
//    @Column(name = "total_questions", nullable = false)
//    private Integer totalQuestions;
//
//    @Column(name = "duration_minutes", nullable = false)
//    private Integer durationMinutes;
//
//    @Column(name = "total_marks", nullable = false)
//    private Integer totalMarks;
//
//    @Column(name = "passing_marks", nullable = false)
//    private Integer passingMarks;
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
//    // ================= CONSTRUCTOR =================
//
//    public Exam() {
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
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public String getCategory() {
//        return category;
//    }
//
//    public void setCategory(String category) {
//        this.category = category;
//    }
//
//    public String getDifficulty() {
//        return difficulty;
//    }
//
//    public void setDifficulty(String difficulty) {
//        this.difficulty = difficulty;
//    }
//
//    public Integer getTotalQuestions() {
//        return totalQuestions;
//    }
//
//    public void setTotalQuestions(Integer totalQuestions) {
//        this.totalQuestions = totalQuestions;
//    }
//
//    public Integer getDurationMinutes() {
//        return durationMinutes;
//    }
//
//    public void setDurationMinutes(Integer durationMinutes) {
//        this.durationMinutes = durationMinutes;
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
//    public Integer getPassingMarks() {
//        return passingMarks;
//    }
//
//    public void setPassingMarks(Integer passingMarks) {
//        this.passingMarks = passingMarks;
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
//    // ================= PRE-PERSIST =================
//
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
//    // ================= PRE-UPDATE =================
//
//    @PreUpdate
//    protected void onUpdate() {
//
//        updatedAt = LocalDateTime.now();
//    }
//}