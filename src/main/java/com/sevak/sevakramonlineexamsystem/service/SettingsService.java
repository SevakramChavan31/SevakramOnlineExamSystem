package com.sevak.sevakramonlineexamsystem.service;

import com.sevak.sevakramonlineexamsystem.entity.SystemSettings;
import com.sevak.sevakramonlineexamsystem.repository.SystemSettingsRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {

    private final SystemSettingsRepository settingsRepository;

    public SettingsService(SystemSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    // =========================================================
    // GET SETTINGS
    // Loads the existing settings row and repairs invalid numeric
    // defaults only. Existing valid values and boolean choices are
    // preserved.
    // =========================================================

    @Transactional
    public SystemSettings getSettings() {
        SystemSettings settings = settingsRepository.findById(1L)
                .orElseGet(this::createDefaultSettings);

        boolean changed = false;

        if (settings.getDefaultExamDuration() == null
                || settings.getDefaultExamDuration() <= 0) {
            settings.setDefaultExamDuration(30);
            changed = true;
        }

        if (settings.getPassingPercentage() == null
                || settings.getPassingPercentage() < 0
                || settings.getPassingPercentage() > 100) {
            settings.setPassingPercentage(40);
            changed = true;
        } else if (settings.getPassingPercentage() == 0) {
            // A zero passing percentage is technically allowed by the
            // validation rules, but it is not the intended initial default.
            // Repair it to 40 when loading existing incomplete settings.
            settings.setPassingPercentage(40);
            changed = true;
        }

        if (settings.getDefaultQuestionsPerExam() == null
                || settings.getDefaultQuestionsPerExam() <= 0) {
            settings.setDefaultQuestionsPerExam(30);
            changed = true;
        }

        if (settings.getDefaultMarksPerQuestion() == null
                || settings.getDefaultMarksPerQuestion() <= 0) {
            settings.setDefaultMarksPerQuestion(2);
            changed = true;
        }

        if (changed) {
            settings = settingsRepository.save(settings);
        }

        return settings;
    }

    // =========================================================
    // SAVE SETTINGS
    // =========================================================

    @Transactional
    public SystemSettings saveSettings(SystemSettings settings) {
        validateSettings(settings);

        SystemSettings existingSettings = settingsRepository.findById(1L)
                .orElseGet(this::createDefaultSettings);

        // GENERAL SETTINGS
        existingSettings.setSystemName(settings.getSystemName().trim());
        existingSettings.setAdministratorEmail(
                settings.getAdministratorEmail().trim());
        existingSettings.setContactNumber(
                settings.getContactNumber() != null
                        && !settings.getContactNumber().trim().isEmpty()
                        ? settings.getContactNumber().trim()
                        : null);
        existingSettings.setTimeZone(settings.getTimeZone().trim());
        existingSettings.setDefaultLanguage(settings.getDefaultLanguage().trim());
        existingSettings.setDateFormat(settings.getDateFormat().trim());
        existingSettings.setExaminationSystemEnabled(
                Boolean.TRUE.equals(settings.getExaminationSystemEnabled()));
        existingSettings.setMaintenanceMode(
                Boolean.TRUE.equals(settings.getMaintenanceMode()));

        // EXAM SETTINGS
        existingSettings.setDefaultExamDuration(settings.getDefaultExamDuration());
        existingSettings.setPassingPercentage(settings.getPassingPercentage());
        existingSettings.setDefaultQuestionsPerExam(
                settings.getDefaultQuestionsPerExam());
        existingSettings.setDefaultMarksPerQuestion(
                settings.getDefaultMarksPerQuestion());
        existingSettings.setAllowQuestionNavigation(
                Boolean.TRUE.equals(settings.getAllowQuestionNavigation()));
        existingSettings.setAllowMarkForReview(
                Boolean.TRUE.equals(settings.getAllowMarkForReview()));
        existingSettings.setAutoSubmitOnTimeExpiry(
                Boolean.TRUE.equals(settings.getAutoSubmitOnTimeExpiry()));
        existingSettings.setShowResultImmediately(
                Boolean.TRUE.equals(settings.getShowResultImmediately()));

        // NOTIFICATION SETTINGS
        existingSettings.setEmailNotificationsEnabled(
                Boolean.TRUE.equals(settings.getEmailNotificationsEnabled()));
        existingSettings.setExamReminderEnabled(
                Boolean.TRUE.equals(settings.getExamReminderEnabled()));
        existingSettings.setResultNotificationEnabled(
                Boolean.TRUE.equals(settings.getResultNotificationEnabled()));
        existingSettings.setNewExamNotificationEnabled(
                Boolean.TRUE.equals(settings.getNewExamNotificationEnabled()));

        return settingsRepository.save(existingSettings);
    }

    // =========================================================
    // CREATE DEFAULT SETTINGS
    // =========================================================

    private SystemSettings createDefaultSettings() {
        SystemSettings settings = new SystemSettings();

        // GENERAL DEFAULTS
        settings.setSystemName("Online Examination System");
        // The controller replaces these with the logged-in Admin profile.
        settings.setAdministratorEmail("admin@onlineexam.com");
        settings.setContactNumber("+91 8687777800");
        settings.setTimeZone("Asia/Kolkata (IST)");
        settings.setDefaultLanguage("English");
        settings.setDateFormat("DD MMM YYYY");
        settings.setExaminationSystemEnabled(true);
        settings.setMaintenanceMode(false);

        // EXAM DEFAULTS
        settings.setDefaultExamDuration(30);
        settings.setPassingPercentage(40);
        settings.setDefaultQuestionsPerExam(30);
        settings.setDefaultMarksPerQuestion(2);
        settings.setAllowQuestionNavigation(true);
        settings.setAllowMarkForReview(true);
        settings.setAutoSubmitOnTimeExpiry(true);
        settings.setShowResultImmediately(true);

        // NOTIFICATION DEFAULTS
        settings.setEmailNotificationsEnabled(true);
        settings.setExamReminderEnabled(true);
        settings.setResultNotificationEnabled(true);
        settings.setNewExamNotificationEnabled(true);

        return settingsRepository.save(settings);
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateSettings(SystemSettings settings) {
        if (settings == null) {
            throw new RuntimeException("Settings are required");
        }

        if (settings.getSystemName() == null
                || settings.getSystemName().trim().isEmpty()) {
            throw new RuntimeException("System name is required");
        }

        if (settings.getSystemName().trim().length() > 150) {
            throw new RuntimeException("System name cannot exceed 150 characters");
        }

        if (settings.getAdministratorEmail() == null
                || settings.getAdministratorEmail().trim().isEmpty()) {
            throw new RuntimeException("Administrator email is required");
        }

        if (!settings.getAdministratorEmail().trim()
                .matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new RuntimeException("Please enter a valid administrator email");
        }

        if (settings.getContactNumber() != null
                && settings.getContactNumber().trim().length() > 20) {
            throw new RuntimeException("Contact number cannot exceed 20 characters");
        }

        if (settings.getTimeZone() == null
                || settings.getTimeZone().trim().isEmpty()) {
            throw new RuntimeException("Time zone is required");
        }

        if (settings.getDefaultLanguage() == null
                || settings.getDefaultLanguage().trim().isEmpty()) {
            throw new RuntimeException("Default language is required");
        }

        if (settings.getDateFormat() == null
                || settings.getDateFormat().trim().isEmpty()) {
            throw new RuntimeException("Date format is required");
        }

        if (settings.getDefaultExamDuration() == null
                || settings.getDefaultExamDuration() <= 0) {
            throw new RuntimeException(
                    "Default exam duration must be greater than 0");
        }

        if (settings.getPassingPercentage() == null
                || settings.getPassingPercentage() < 0
                || settings.getPassingPercentage() > 100) {
            throw new RuntimeException(
                    "Passing percentage must be between 0 and 100");
        }

        if (settings.getDefaultQuestionsPerExam() == null
                || settings.getDefaultQuestionsPerExam() <= 0) {
            throw new RuntimeException(
                    "Default questions per exam must be greater than 0");
        }

        if (settings.getDefaultMarksPerQuestion() == null
                || settings.getDefaultMarksPerQuestion() <= 0) {
            throw new RuntimeException(
                    "Default marks per question must be greater than 0");
        }
    }
}
