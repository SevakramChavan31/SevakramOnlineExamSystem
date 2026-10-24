package com.sevak.sevakramonlineexamsystem.controller;

import com.sevak.sevakramonlineexamsystem.entity.SystemSettings;
import com.sevak.sevakramonlineexamsystem.entity.User;
import  com.sevak.sevakramonlineexamsystem.service.SettingsService;
import  com.sevak.sevakramonlineexamsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/settings")
public class SettingsController {

    private final SettingsService settingsService;
    private final UserService userService;

    public SettingsController(
            SettingsService settingsService,
            UserService userService) {

        this.settingsService = settingsService;
        this.userService = userService;
    }

    // =========================================================
    // SHOW SETTINGS
    // =========================================================

    @GetMapping
    public String showSettings(
            HttpSession session,
            Model model) {

        try {
            Long userId = getSessionUserId(session);
            User admin = getLoggedInAdmin(session);

            SystemSettings settings = settingsService.getSettings();

            // Synchronize settings with the logged-in Admin profile.
            synchronizeAdminDetails(settings, admin);

            model.addAttribute("settings", settings);
            model.addAttribute("profileUser", admin);

            loadTwoFactorStatus(userId, model);

            return "admin-settings";

        } catch (RuntimeException ex) {

            return "redirect:/login";
        }
    }

    // =========================================================
    // SAVE SYSTEM SETTINGS
    // =========================================================

    @PostMapping
    public String saveSettings(
            @ModelAttribute("settings") SystemSettings settings,
            HttpSession session,
            Model model) {

        try {
            User admin = getLoggedInAdmin(session);

            // Always use the actual Admin profile details.
            settings.setAdministratorEmail(admin.getEmail());
            settings.setContactNumber(admin.getMobile());

            settingsService.saveSettings(settings);

            SystemSettings savedSettings = settingsService.getSettings();

            synchronizeAdminDetails(savedSettings, admin);

            model.addAttribute("settings", savedSettings);
            model.addAttribute("profileUser", admin);

            loadTwoFactorStatus(admin.getId(), model);

            model.addAttribute(
                    "success",
                    "Settings saved successfully."
            );

        } catch (RuntimeException ex) {

            addSettingsPageData(settings, session, model);

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "admin-settings";
    }

    // =========================================================
    // CHANGE ADMIN PASSWORD
    // =========================================================

// =========================================================
// CHANGE ADMIN PASSWORD
// Logout automatically after successful password change.
// =========================================================

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            Model model) {

        try {
            Long userId = getSessionUserId(session);

            // Change password
            userService.changeAdminPassword(
                    userId,
                    currentPassword,
                    newPassword,
                    confirmPassword
            );

            // Invalidate session after successful password change
            session.invalidate();

            // Redirect to login page
            return "redirect:/login?passwordChanged=true";

        } catch (RuntimeException ex) {

            // If password change fails, keep the user on Settings
            addSettingsPageData(
                    settingsService.getSettings(),
                    session,
                    model
            );

            model.addAttribute("error", ex.getMessage());

            return "admin-settings";
        }
    }

    // =========================================================
    // UPDATE ADMIN PROFILE
    // =========================================================

    @PostMapping("/profile")
    public String updateAdminProfile(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("profileEmail") String profileEmail,
            @RequestParam(value = "profileMobile", required = false)
            String profileMobile,
            HttpSession session,
            Model model) {

        try {
            Long userId = getSessionUserId(session);

            /*
             * IMPORTANT:
             * UserService.updateAdminProfile() requires FIVE
             * parameters, including firstName and lastName separately.
             */
            userService.updateAdminProfile(
                    userId,
                    firstName,
                    lastName,
                    profileEmail,
                    profileMobile
            );

            // Fetch the updated profile from the database.
            User updatedUser = userService.getUserById(userId);

            // Update the session with the current profile name.
            session.setAttribute(
                    "userName",
                    updatedUser.getFullName()
            );

            SystemSettings settings = settingsService.getSettings();

            // Synchronize General Settings with the updated profile.
            synchronizeAdminDetails(settings, updatedUser);

            // Persist the updated email and mobile in system_settings.
            settingsService.saveSettings(settings);

            // Load the latest saved settings.
            SystemSettings savedSettings = settingsService.getSettings();

            synchronizeAdminDetails(savedSettings, updatedUser);

            model.addAttribute("settings", savedSettings);
            model.addAttribute("profileUser", updatedUser);

            loadTwoFactorStatus(userId, model);

            model.addAttribute(
                    "success",
                    "Administrator profile updated successfully."
            );

        } catch (RuntimeException ex) {

            addSettingsPageData(
                    settingsService.getSettings(),
                    session,
                    model
            );

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "admin-settings";
    }

    // =========================================================
    // TOGGLE TWO-FACTOR AUTHENTICATION
    // =========================================================

    @PostMapping("/two-factor")
    public String toggleTwoFactor(
            HttpSession session,
            Model model) {

        try {
            Long userId = getSessionUserId(session);

            boolean enabled = userService.toggleAdminTwoFactor(userId);

            addSettingsPageData(
                    settingsService.getSettings(),
                    session,
                    model
            );

            model.addAttribute("twoFactorEnabled", enabled);

            model.addAttribute(
                    "success",
                    enabled
                            ? "Two-Factor Authentication enabled successfully."
                            : "Two-Factor Authentication disabled successfully."
            );

        } catch (RuntimeException ex) {

            addSettingsPageData(
                    settingsService.getSettings(),
                    session,
                    model
            );

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "admin-settings";
    }

    // =========================================================
    // SHARED SETTINGS PAGE DATA
    // =========================================================

    private void addSettingsPageData(
            SystemSettings settings,
            HttpSession session,
            Model model) {

        try {
            User admin = getLoggedInAdmin(session);

            synchronizeAdminDetails(settings, admin);

            model.addAttribute("settings", settings);
            model.addAttribute("profileUser", admin);

            loadTwoFactorStatus(admin.getId(), model);

        } catch (RuntimeException ex) {

            model.addAttribute("settings", settings);
            model.addAttribute("twoFactorEnabled", false);
        }
    }

    // =========================================================
    // LOAD TWO-FACTOR AUTHENTICATION STATUS
    // =========================================================

    private void loadTwoFactorStatus(
            Long userId,
            Model model) {

        try {
            boolean enabled = userService.isTwoFactorEnabled(userId);

            model.addAttribute("twoFactorEnabled", enabled);

        } catch (RuntimeException ex) {

            model.addAttribute("twoFactorEnabled", false);
        }
    }

    // =========================================================
    // GET SESSION USER ID
    // =========================================================

    private Long getSessionUserId(HttpSession session) {

        Object userIdObject = session.getAttribute("userId");

        if (userIdObject == null) {
            throw new RuntimeException(
                    "Your session has expired. Please login again."
            );
        }

        if (userIdObject instanceof Number) {
            return ((Number) userIdObject).longValue();
        }

        try {
            return Long.valueOf(userIdObject.toString());

        } catch (NumberFormatException ex) {

            throw new RuntimeException(
                    "Invalid user session. Please login again."
            );
        }
    }

    // =========================================================
    // GET LOGGED-IN ADMIN
    // =========================================================

    private User getLoggedInAdmin(HttpSession session) {

        Long userId = getSessionUserId(session);

        User admin = userService.getUserById(userId);

        if (admin.getRole() != User.Role.ADMIN) {

            throw new RuntimeException(
                    "Only an Admin can access System Settings."
            );
        }

        if (admin.getEmail() == null
                || admin.getEmail().trim().isEmpty()) {

            throw new RuntimeException(
                    "Admin email is not configured."
            );
        }

        return admin;
    }

    // =========================================================
    // SYNCHRONIZE ADMIN DETAILS
    // =========================================================

    private void synchronizeAdminDetails(
            SystemSettings settings,
            User admin) {

        if (settings == null || admin == null) {
            return;
        }

        settings.setAdministratorEmail(admin.getEmail());
        settings.setContactNumber(admin.getMobile());
    }
}