package com.sevak.sevakramonlineexamsystem.controller;

import com.sevak.sevakramonlineexamsystem.entity.User;
import com.sevak.sevakramonlineexamsystem.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/student")
public class StudentProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public StudentProfileController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // =========================================================
    // VIEW PROFILE
    // =========================================================

    @GetMapping("/profile")
    public String profile(
            HttpSession session,
            Model model) {

        Long userId =
                (Long) session.getAttribute("userId");

        if (userId == null) {

            return "redirect:/login";
        }


        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );


        model.addAttribute(
                "user",
                user
        );


        return "student-profile";
    }


    // =========================================================
    // SHOW EDIT PROFILE
    // =========================================================

    @GetMapping("/profile/edit")
    public String editProfile(
            HttpSession session,
            Model model) {

        Long userId =
                (Long) session.getAttribute("userId");

        if (userId == null) {

            return "redirect:/login";
        }


        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );


        model.addAttribute(
                "user",
                user
        );


        return "student-profile-edit";
    }


    // =========================================================
    // UPDATE PROFILE
    // =========================================================

    @PostMapping("/profile/edit")
    public String updateProfile(

            @RequestParam String fullName,

            @RequestParam String mobile,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate dateOfBirth,

            @RequestParam String qualification,

            HttpSession session,

            RedirectAttributes redirectAttributes) {


        Long userId =
                (Long) session.getAttribute("userId");


        if (userId == null) {

            return "redirect:/login";
        }


        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );


        // -----------------------------------------------------
        // Validate name
        // -----------------------------------------------------

        if (fullName == null
                || fullName.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Full name is required."
            );

            return "redirect:/student/profile/edit";
        }


        // -----------------------------------------------------
        // Validate mobile
        // -----------------------------------------------------

        if (mobile == null
                || mobile.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Mobile number is required."
            );

            return "redirect:/student/profile/edit";
        }


        if (!mobile.trim().matches("\\d{10}")) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Mobile number must contain exactly 10 digits."
            );

            return "redirect:/student/profile/edit";
        }


        // -----------------------------------------------------
        // Validate qualification
        // -----------------------------------------------------

        if (qualification == null
                || qualification.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Qualification is required."
            );

            return "redirect:/student/profile/edit";
        }


        // -----------------------------------------------------
        // Update profile
        // -----------------------------------------------------

        user.setFullName(
                fullName.trim()
        );

        user.setMobile(
                mobile.trim()
        );

        user.setDateOfBirth(
                dateOfBirth
        );

        user.setQualification(
                qualification.trim()
        );


        // -----------------------------------------------------
        // Save
        // -----------------------------------------------------

        userRepository.save(user);


        redirectAttributes.addFlashAttribute(
                "success",
                "Profile updated successfully."
        );


        return "redirect:/student/profile";
    }


    // =========================================================
    // CHANGE PASSWORD PAGE
    // =========================================================

    @GetMapping("/profile/change-password")
    public String changePasswordPage(
            HttpSession session,
            Model model) {

        Long userId =
                (Long) session.getAttribute("userId");


        if (userId == null) {

            return "redirect:/login";
        }


        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );


        model.addAttribute(
                "user",
                user
        );


        return "student-change-password";
    }


    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    @PostMapping("/profile/change-password")
    public String changePassword(

            @RequestParam String currentPassword,

            @RequestParam String newPassword,

            @RequestParam String confirmPassword,

            HttpSession session,

            RedirectAttributes redirectAttributes) {


        // -----------------------------------------------------
        // Get logged-in user
        // -----------------------------------------------------

        Long userId =
                (Long) session.getAttribute("userId");


        if (userId == null) {

            return "redirect:/login";
        }


        // -----------------------------------------------------
        // Find user
        // -----------------------------------------------------

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );


        // -----------------------------------------------------
        // Current password required
        // -----------------------------------------------------

        if (currentPassword == null
                || currentPassword.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Current password is required."
            );

            return "redirect:/student/profile/change-password";
        }


        // -----------------------------------------------------
        // Verify current password
        // -----------------------------------------------------

        boolean currentPasswordValid;


        if (isBCryptPassword(
                user.getPassword())) {

            // BCrypt password

            currentPasswordValid =
                    passwordEncoder.matches(
                            currentPassword,
                            user.getPassword()
                    );

        } else {

            // Legacy plain-text password

            currentPasswordValid =
                    currentPassword.equals(
                            user.getPassword()
                    );
        }


        // -----------------------------------------------------
        // Current password incorrect
        // -----------------------------------------------------

        if (!currentPasswordValid) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Current password is incorrect."
            );

            return "redirect:/student/profile/change-password";
        }


        // -----------------------------------------------------
        // New password required
        // -----------------------------------------------------

        if (newPassword == null
                || newPassword.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "New password is required."
            );

            return "redirect:/student/profile/change-password";
        }


        // -----------------------------------------------------
        // Minimum length
        // -----------------------------------------------------

        if (newPassword.length() < 8) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "New password must contain at least 8 characters."
            );

            return "redirect:/student/profile/change-password";
        }


        // -----------------------------------------------------
        // Confirm password
        // -----------------------------------------------------

        if (confirmPassword == null
                || confirmPassword.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Please confirm your new password."
            );

            return "redirect:/student/profile/change-password";
        }


        // -----------------------------------------------------
        // Password match
        // -----------------------------------------------------

        if (!newPassword.equals(
                confirmPassword)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "New password and confirm password do not match."
            );

            return "redirect:/student/profile/change-password";
        }


        // -----------------------------------------------------
        // New password must be different
        // -----------------------------------------------------

        if (currentPassword.equals(
                newPassword)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "New password must be different from the current password."
            );

            return "redirect:/student/profile/change-password";
        }


        // -----------------------------------------------------
        // IMPORTANT:
        // Always store NEW password as BCrypt
        // -----------------------------------------------------

        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );


        // -----------------------------------------------------
        // Save
        // -----------------------------------------------------

        userRepository.save(user);

// Invalidate current session
        session.invalidate();

        return "redirect:/login?passwordChanged=true";
    }


    // =========================================================
    // CHECK BCRYPT PASSWORD
    // =========================================================

    private boolean isBCryptPassword(
            String password) {

        if (password == null) {

            return false;
        }


        return password.startsWith("$2a$")
                || password.startsWith("$2b$")
                || password.startsWith("$2y$");
    }
}