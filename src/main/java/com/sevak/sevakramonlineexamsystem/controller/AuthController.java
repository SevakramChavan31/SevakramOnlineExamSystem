package com.sevak.sevakramonlineexamsystem.controller;

import com.sevak.sevakramonlineexamsystem.dto.RegisterRequest;
import com.sevak.sevakramonlineexamsystem.entity.User;
import com.sevak.sevakramonlineexamsystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // LOGIN PAGE
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String passwordChanged,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String resetSuccess,
            Model model,  HttpServletRequest request) {

        model.addAttribute(
                "loginRole",
                "admin".equalsIgnoreCase(role) ? "admin" : "student"
        );

        if ("true".equals(passwordChanged)) {
            model.addAttribute(
                    "success",
                    "Password changed successfully. Please login with your new password."
            );
        }

        if ("true".equals(logout)) {
            model.addAttribute(
                    "success",
                    "You have been logged out successfully."
            );
        }

        if ("true".equals(resetSuccess)) {
            model.addAttribute(
                    "success",
                    "Password reset successfully. Please login with your new password."
            );
        }
        request.getSession(true);
        CsrfToken csrfToken =
                (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken == null) {
            csrfToken = (CsrfToken) request.getAttribute("_csrf");
        }

        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }

//        request.getSession(true);
//
//        CsrfToken csrfToken =
//                (CsrfToken) request.getAttribute(CsrfToken.class.getName());
//
//        if (csrfToken != null) {
//            csrfToken.getToken();
//        }

        return "login";
    }

    // LOGIN USER
    @PostMapping("/login")
    public String loginUser(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "student") String role,
            HttpSession session,
            Model model) {

        try {
            User user = userService.loginUser(email, password);

            if ("admin".equalsIgnoreCase(role)
                    && user.getRole() != User.Role.ADMIN) {

                model.addAttribute("error", "This account is not an Admin account.");
                model.addAttribute("loginRole", "admin");
                return "login";
            }

            if ("student".equalsIgnoreCase(role)
                    && user.getRole() != User.Role.STUDENT) {

                model.addAttribute("error", "This account is not a Student account.");
                model.addAttribute("loginRole", "student");
                return "login";
            }

            // ADMIN TWO-FACTOR AUTHENTICATION
            if (user.getRole() == User.Role.ADMIN
                    && Boolean.TRUE.equals(user.getTwoFactorEnabled())) {

                session.setAttribute("pending2faEmail", user.getEmail());

                try {
                    userService.sendAdminLoginOtp(user);

                    session.setAttribute(
                            "otpSuccess",
                            "A verification OTP has been sent to your registered email."
                    );

                    session.removeAttribute("otpError");

                } catch (RuntimeException ex) {
                    session.setAttribute(
                            "otpError",
                            ex.getMessage() != null
                                    ? ex.getMessage()
                                    : "Unable to send OTP."
                    );
                }

                return "redirect:/login/verify-otp";
            }

            // STUDENT LOGIN OTP
            // Do not authenticate the student until OTP verification succeeds.
            if (user.getRole() == User.Role.STUDENT) {

                // Clear any previous authentication and pending OTP state.
                session.removeAttribute("userId");
                session.removeAttribute("userName");
                session.removeAttribute("role");
                session.removeAttribute("pending2faEmail");
                session.removeAttribute("otpSuccess");
                session.removeAttribute("otpError");
                session.removeAttribute("resendSuccess");

                session.setAttribute(
                        "pendingStudentLoginEmail",
                        user.getEmail()
                );

                session.removeAttribute("studentLoginOtpSuccess");
                session.removeAttribute("studentLoginOtpError");
                session.removeAttribute("studentLoginResendSuccess");

                try {
                    userService.sendStudentLoginOtp(user);

                    session.setAttribute(
                            "studentLoginOtpSuccess",
                            "A Student Login OTP has been sent to your registered email."
                    );
                } catch (RuntimeException ex) {
                    session.setAttribute(
                            "studentLoginOtpError",
                            ex.getMessage() != null
                                    ? ex.getMessage()
                                    : "Unable to send Student Login OTP."
                    );
                }

                return "redirect:/student-login/verify-otp";
            }

            // Admin without enabled 2FA keeps the existing login flow.
            userService.completeLogin(user);

            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getFullName());
            session.setAttribute("role", user.getRole().name());

            if (user.getRole() == User.Role.ADMIN) {
                return "redirect:/admin/dashboard";
            }

            return "redirect:/login";

        } catch (RuntimeException ex) {
            model.addAttribute(
                    "error",
                    ex.getMessage() != null ? ex.getMessage() : "Login failed."
            );

            model.addAttribute(
                    "loginRole",
                    "admin".equalsIgnoreCase(role) ? "admin" : "student"
            );

            return "login";
        }
    }

    // ADMIN OTP PAGE
    @GetMapping("/login/verify-otp")
    public String adminLoginOtpPage(
            HttpSession session,
            Model model) {

        Object pendingEmail = session.getAttribute("pending2faEmail");

        if (pendingEmail == null) {
            return "redirect:/login?role=admin";
        }

        model.addAttribute("email", pendingEmail.toString());

        Object otpSuccess = session.getAttribute("otpSuccess");
        if (otpSuccess != null) {
            model.addAttribute("success", otpSuccess.toString());
            session.removeAttribute("otpSuccess");
        }

        Object resendSuccess = session.getAttribute("resendSuccess");
        if (resendSuccess != null) {
            model.addAttribute("success", resendSuccess.toString());
            session.removeAttribute("resendSuccess");
        }

        Object otpError = session.getAttribute("otpError");
        if (otpError != null) {
            model.addAttribute("error", otpError.toString());
            session.removeAttribute("otpError");
        }

        return "login-otp";
    }

    // VERIFY ADMIN OTP
    @PostMapping("/login/verify-otp")
    public String verifyAdminLoginOtp(
            @RequestParam String otp,
            HttpSession session,
            Model model) {

        Object pendingEmail = session.getAttribute("pending2faEmail");

        if (pendingEmail == null) {
            return "redirect:/login?role=admin";
        }

        String email = pendingEmail.toString();

        try {
            User user = userService.verifyAdminLoginOtp(email, otp);

            userService.completeLogin(user);

            session.removeAttribute("pending2faEmail");
            session.removeAttribute("otpSuccess");
            session.removeAttribute("resendSuccess");
            session.removeAttribute("otpError");

            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getFullName());
            session.setAttribute("role", user.getRole().name());

            return "redirect:/admin/dashboard";

        } catch (RuntimeException ex) {
            model.addAttribute("email", email);
            model.addAttribute(
                    "error",
                    ex.getMessage() != null ? ex.getMessage() : "Invalid or expired OTP."
            );

            return "login-otp";
        }
    }

    // RESEND ADMIN OTP
    @PostMapping("/login/resend-otp")
    public String resendAdminLoginOtp(HttpSession session) {

        Object pendingEmail = session.getAttribute("pending2faEmail");

        if (pendingEmail == null) {
            return "redirect:/login?role=admin";
        }

        try {
            userService.resendAdminLoginOtp(pendingEmail.toString());

            session.setAttribute(
                    "resendSuccess",
                    "A new verification OTP has been sent."
            );

        } catch (RuntimeException ex) {
            session.setAttribute(
                    "otpError",
                    ex.getMessage() != null ? ex.getMessage() : "Unable to resend OTP."
            );
        }

        return "redirect:/login/verify-otp";
    }

    // =========================================================
    // STUDENT LOGIN OTP PAGE
    // =========================================================

    @GetMapping("/student-login/verify-otp")
    public String studentLoginOtpPage(
            HttpSession session,
            Model model) {

        Object pendingEmail =
                session.getAttribute("pendingStudentLoginEmail");

        if (pendingEmail == null) {
            return "redirect:/login?role=student";
        }

        model.addAttribute("email", pendingEmail.toString());

        Object success =
                session.getAttribute("studentLoginOtpSuccess");
        if (success != null) {
            model.addAttribute("success", success.toString());
            session.removeAttribute("studentLoginOtpSuccess");
        }

        Object resendSuccess =
                session.getAttribute("studentLoginResendSuccess");
        if (resendSuccess != null) {
            model.addAttribute("success", resendSuccess.toString());
            session.removeAttribute("studentLoginResendSuccess");
        }

        Object error =
                session.getAttribute("studentLoginOtpError");
        if (error != null) {
            model.addAttribute("error", error.toString());
            session.removeAttribute("studentLoginOtpError");
        }

        return "student-login-otp";
    }

    // =========================================================
    // VERIFY STUDENT LOGIN OTP
    // =========================================================

    @PostMapping("/student-login/verify-otp")
    public String verifyStudentLoginOtp(
            @RequestParam String otp,
            HttpSession session,
            Model model) {

        Object pendingEmail =
                session.getAttribute("pendingStudentLoginEmail");

        if (pendingEmail == null) {
            return "redirect:/login?role=student";
        }

        String email = pendingEmail.toString();

        try {
            User user = userService.verifyStudentLoginOtp(email, otp);

            // Complete login only after the OTP has been verified.
            userService.completeLogin(user);

            session.removeAttribute("pendingStudentLoginEmail");
            session.removeAttribute("studentLoginOtpSuccess");
            session.removeAttribute("studentLoginOtpError");
            session.removeAttribute("studentLoginResendSuccess");

            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getFullName());
            session.setAttribute("role", user.getRole().name());

            return "redirect:/student/dashboard";

        } catch (RuntimeException ex) {
            model.addAttribute("email", email);
            model.addAttribute(
                    "error",
                    ex.getMessage() != null
                            ? ex.getMessage()
                            : "Invalid or expired OTP."
            );

            return "student-login-otp";
        }
    }

    // =========================================================
    // RESEND STUDENT LOGIN OTP
    // =========================================================

    @PostMapping("/student-login/resend-otp")
    public String resendStudentLoginOtp(HttpSession session) {

        Object pendingEmail =
                session.getAttribute("pendingStudentLoginEmail");

        if (pendingEmail == null) {
            return "redirect:/login?role=student";
        }

        try {
            userService.resendStudentLoginOtp(pendingEmail.toString());

            session.setAttribute(
                    "studentLoginResendSuccess",
                    "A new Student Login OTP has been sent to your email."
            );

            session.removeAttribute("studentLoginOtpError");

        } catch (RuntimeException ex) {
            session.setAttribute(
                    "studentLoginOtpError",
                    ex.getMessage() != null
                            ? ex.getMessage()
                            : "Unable to resend Student Login OTP."
            );
        }

        return "redirect:/student-login/verify-otp";
    }

    // REGISTER PAGE
    @GetMapping("/register")
    public String registerPage(Model model) {

        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }

        return "register";
    }

    // REGISTER USER
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(request);
            return "redirect:/?success=registered";

        } catch (RuntimeException ex) {
            model.addAttribute(
                    "error",
                    ex.getMessage() != null
                            ? ex.getMessage()
                            : "Registration failed."
            );

            model.addAttribute("registerRequest", request);
            return "register";
        }
    }

    // FORGOT PASSWORD PAGE
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    // SEND FORGOT PASSWORD OTP
    @PostMapping("/forgot-password")
    public String sendForgotPasswordOtp(
            @RequestParam String email,
            Model model) {

        try {
            userService.sendForgotPasswordOtp(email);

            model.addAttribute("email", email.trim());
            return "verify-otp";

        } catch (RuntimeException ex) {
            model.addAttribute(
                    "error",
                    ex.getMessage() != null
                            ? ex.getMessage()
                            : "Unable to send password reset OTP."
            );

            model.addAttribute("email", email);
            return "forgot-password";
        }
    }

    // VERIFY FORGOT PASSWORD OTP PAGE
    @GetMapping("/verify-otp")
    public String verifyOtpPage(
            @RequestParam(required = false) String email,
            Model model) {

        if (email == null || email.trim().isEmpty()) {
            return "redirect:/forgot-password";
        }

        model.addAttribute("email", email.trim());
        return "verify-otp";
    }

    // VERIFY FORGOT PASSWORD OTP
    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam String email,
            @RequestParam String otp,
            Model model) {

        try {
            userService.verifyForgotPasswordOtp(email, otp);

            model.addAttribute("email", email.trim());
            return "reset-password";

        } catch (RuntimeException ex) {
            model.addAttribute(
                    "error",
                    ex.getMessage() != null
                            ? ex.getMessage()
                            : "Invalid or expired OTP."
            );

            model.addAttribute("email", email);
            return "verify-otp";
        }
    }

    // RESET PASSWORD PAGE
    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam(required = false) String email,
            Model model) {

        if (email == null || email.trim().isEmpty()) {
            return "redirect:/forgot-password";
        }

        model.addAttribute("email", email.trim());
        return "reset-password";
    }

    // RESET PASSWORD
    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String email,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model) {

        try {
            userService.resetPassword(email, newPassword, confirmPassword);

            return "redirect:/login?resetSuccess=true";

        } catch (RuntimeException ex) {
            model.addAttribute(
                    "error",
                    ex.getMessage() != null
                            ? ex.getMessage()
                            : "Password reset failed."
            );

            model.addAttribute("email", email);
            return "reset-password";
        }
    }

    // LOGOUT
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/login?logout=true";
    }
}
