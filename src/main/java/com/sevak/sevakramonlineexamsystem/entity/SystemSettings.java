package com.sevak.sevakramonlineexamsystem.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
public class SystemSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================================================
    // GENERAL SETTINGS
    // =========================================================

    @Column(
            name = "system_name",
            nullable = false,
            length = 150
    )
    private String systemName;


    @Column(
            name = "administrator_email",
            nullable = false,
            length = 150
    )
    private String administratorEmail;


    @Column(
            name = "contact_number",
            length = 20
    )
    private String contactNumber;


    @Column(
            name = "time_zone",
            nullable = false,
            length = 50
    )
    private String timeZone;


    @Column(
            name = "default_language",
            nullable = false,
            length = 30
    )
    private String defaultLanguage;


    @Column(
            name = "date_format",
            nullable = false,
            length = 30
    )
    private String dateFormat;


    @Column(
            name = "examination_system_enabled",
            nullable = false
    )
    private Boolean examinationSystemEnabled = true;


    @Column(
            name = "maintenance_mode",
            nullable = false
    )
    private Boolean maintenanceMode = false;


    // =========================================================
    // EXAM SETTINGS
    // =========================================================

    @Column(
            name = "default_exam_duration",
            nullable = false
    )
    private Integer defaultExamDuration = 30;


    @Column(
            name = "passing_percentage",
            nullable = false
    )
    private Integer passingPercentage = 40;


    @Column(
            name = "default_questions_per_exam",
            nullable = false
    )
    private Integer defaultQuestionsPerExam = 30;


    @Column(
            name = "default_marks_per_question",
            nullable = false
    )
    private Integer defaultMarksPerQuestion = 2;


    @Column(
            name = "allow_question_navigation",
            nullable = false
    )
    private Boolean allowQuestionNavigation = true;


    @Column(
            name = "allow_mark_for_review",
            nullable = false
    )
    private Boolean allowMarkForReview = true;


    @Column(
            name = "auto_submit_on_time_expiry",
            nullable = false
    )
    private Boolean autoSubmitOnTimeExpiry = true;


    @Column(
            name = "show_result_immediately",
            nullable = false
    )
    private Boolean showResultImmediately = true;


    // =========================================================
    // NOTIFICATION SETTINGS
    // =========================================================

    @Column(
            name = "email_notifications_enabled",
            nullable = false
    )
    private Boolean emailNotificationsEnabled = true;


    @Column(
            name = "exam_reminder_enabled",
            nullable = false
    )
    private Boolean examReminderEnabled = true;


    @Column(
            name = "result_notification_enabled",
            nullable = false
    )
    private Boolean resultNotificationEnabled = true;


    @Column(
            name = "new_exam_notification_enabled",
            nullable = false
    )
    private Boolean newExamNotificationEnabled = true;


    // =========================================================
    // AUDIT FIELDS
    // =========================================================

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;


    @Column(
            name = "updated_at"
    )
    private LocalDateTime updatedAt;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SystemSettings() {
    }


    // =========================================================
    // PRE-PERSIST
    // =========================================================

    @PrePersist
    protected void onCreate() {

        createdAt = LocalDateTime.now();


        // -----------------------------------------------------
        // GENERAL DEFAULTS
        // -----------------------------------------------------

        if (examinationSystemEnabled == null) {
            examinationSystemEnabled = true;
        }

        if (maintenanceMode == null) {
            maintenanceMode = false;
        }


        // -----------------------------------------------------
        // EXAM DEFAULTS
        // -----------------------------------------------------

        if (defaultExamDuration == null) {
            defaultExamDuration = 30;
        }

        if (passingPercentage == null) {
            passingPercentage = 40;
        }

        if (defaultQuestionsPerExam == null) {
            defaultQuestionsPerExam = 30;
        }

        if (defaultMarksPerQuestion == null) {
            defaultMarksPerQuestion = 2;
        }

        if (allowQuestionNavigation == null) {
            allowQuestionNavigation = true;
        }

        if (allowMarkForReview == null) {
            allowMarkForReview = true;
        }

        if (autoSubmitOnTimeExpiry == null) {
            autoSubmitOnTimeExpiry = true;
        }

        if (showResultImmediately == null) {
            showResultImmediately = true;
        }


        // -----------------------------------------------------
        // NOTIFICATION DEFAULTS
        // -----------------------------------------------------

        if (emailNotificationsEnabled == null) {
            emailNotificationsEnabled = true;
        }

        if (examReminderEnabled == null) {
            examReminderEnabled = true;
        }

        if (resultNotificationEnabled == null) {
            resultNotificationEnabled = true;
        }

        if (newExamNotificationEnabled == null) {
            newExamNotificationEnabled = true;
        }
    }


    // =========================================================
    // PRE-UPDATE
    // =========================================================

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }


    // =========================================================
    // GENERAL GETTERS / SETTERS
    // =========================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }


    public String getAdministratorEmail() {
        return administratorEmail;
    }

    public void setAdministratorEmail(
            String administratorEmail) {

        this.administratorEmail = administratorEmail;
    }


    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }


    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }


    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(
            String defaultLanguage) {

        this.defaultLanguage = defaultLanguage;
    }


    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }


    public Boolean getExaminationSystemEnabled() {
        return examinationSystemEnabled;
    }

    public void setExaminationSystemEnabled(
            Boolean examinationSystemEnabled) {

        this.examinationSystemEnabled =
                examinationSystemEnabled;
    }


    public Boolean getMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(
            Boolean maintenanceMode) {

        this.maintenanceMode =
                maintenanceMode;
    }


    // =========================================================
    // EXAM GETTERS / SETTERS
    // =========================================================

    public Integer getDefaultExamDuration() {
        return defaultExamDuration;
    }

    public void setDefaultExamDuration(
            Integer defaultExamDuration) {

        this.defaultExamDuration =
                defaultExamDuration;
    }


    public Integer getPassingPercentage() {
        return passingPercentage;
    }

    public void setPassingPercentage(
            Integer passingPercentage) {

        this.passingPercentage =
                passingPercentage;
    }


    public Integer getDefaultQuestionsPerExam() {
        return defaultQuestionsPerExam;
    }

    public void setDefaultQuestionsPerExam(
            Integer defaultQuestionsPerExam) {

        this.defaultQuestionsPerExam =
                defaultQuestionsPerExam;
    }


    public Integer getDefaultMarksPerQuestion() {
        return defaultMarksPerQuestion;
    }

    public void setDefaultMarksPerQuestion(
            Integer defaultMarksPerQuestion) {

        this.defaultMarksPerQuestion =
                defaultMarksPerQuestion;
    }


    public Boolean getAllowQuestionNavigation() {
        return allowQuestionNavigation;
    }

    public void setAllowQuestionNavigation(
            Boolean allowQuestionNavigation) {

        this.allowQuestionNavigation =
                allowQuestionNavigation;
    }


    public Boolean getAllowMarkForReview() {
        return allowMarkForReview;
    }

    public void setAllowMarkForReview(
            Boolean allowMarkForReview) {

        this.allowMarkForReview =
                allowMarkForReview;
    }


    public Boolean getAutoSubmitOnTimeExpiry() {
        return autoSubmitOnTimeExpiry;
    }

    public void setAutoSubmitOnTimeExpiry(
            Boolean autoSubmitOnTimeExpiry) {

        this.autoSubmitOnTimeExpiry =
                autoSubmitOnTimeExpiry;
    }


    public Boolean getShowResultImmediately() {
        return showResultImmediately;
    }

    public void setShowResultImmediately(
            Boolean showResultImmediately) {

        this.showResultImmediately =
                showResultImmediately;
    }


    // =========================================================
    // NOTIFICATION GETTERS / SETTERS
    // =========================================================

    public Boolean getEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public void setEmailNotificationsEnabled(
            Boolean emailNotificationsEnabled) {

        this.emailNotificationsEnabled =
                emailNotificationsEnabled;
    }


    public Boolean getExamReminderEnabled() {
        return examReminderEnabled;
    }

    public void setExamReminderEnabled(
            Boolean examReminderEnabled) {

        this.examReminderEnabled =
                examReminderEnabled;
    }


    public Boolean getResultNotificationEnabled() {
        return resultNotificationEnabled;
    }

    public void setResultNotificationEnabled(
            Boolean resultNotificationEnabled) {

        this.resultNotificationEnabled =
                resultNotificationEnabled;
    }


    public Boolean getNewExamNotificationEnabled() {
        return newExamNotificationEnabled;
    }

    public void setNewExamNotificationEnabled(
            Boolean newExamNotificationEnabled) {

        this.newExamNotificationEnabled =
                newExamNotificationEnabled;
    }


    // =========================================================
    // AUDIT GETTERS / SETTERS
    // =========================================================

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt) {

        this.updatedAt = updatedAt;
    }
}