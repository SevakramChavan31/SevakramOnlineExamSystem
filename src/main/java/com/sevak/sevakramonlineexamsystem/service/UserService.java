package com.sevak.sevakramonlineexamsystem.service;

import com.sevak.sevakramonlineexamsystem.dto.RegisterRequest;
import com.sevak.sevakramonlineexamsystem.entity.User;
import com.sevak.sevakramonlineexamsystem.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /*
     * OTP storage for Forgot Password.
     *
     * Key   = user email
     * Value = OTP details
     */
    private final Map<String, OtpData> otpStorage =
            new ConcurrentHashMap<>();

    /*
     * Separate OTP storage for Admin Login 2FA.
     *
     * This is intentionally separate from
     * Forgot Password OTP storage.
     */
    private final Map<String, LoginOtpData> loginOtpStorage =
            new ConcurrentHashMap<>();

    /*
     * Separate OTP storage for Student Login.
     * Admin Login OTP and Forgot Password OTP remain independent.
     */
    private final Map<String, LoginOtpData> studentLoginOtpStorage =
            new ConcurrentHashMap<>();

    private final SecureRandom secureRandom =
            new SecureRandom();


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }


    // =========================================================
    // REGISTER
    // =========================================================

    public void registerUser(RegisterRequest request) {

        if (request.getEmail() == null
                || request.getEmail().trim().isEmpty()) {

            throw new RuntimeException(
                    "Email is required"
            );
        }

        String email = request.getEmail().trim();

        if (userRepository.existsByEmail(email)) {

            throw new RuntimeException(
                    "Email is already registered"
            );
        }

        if (request.getPassword() == null
                || request.getPassword().trim().isEmpty()) {

            throw new RuntimeException(
                    "Password is required"
            );
        }

        if (request.getConfirmPassword() == null
                || request.getConfirmPassword().trim().isEmpty()) {

            throw new RuntimeException(
                    "Please confirm your password"
            );
        }

        if (!request.getPassword()
                .equals(request.getConfirmPassword())) {

            throw new RuntimeException(
                    "Passwords do not match"
            );
        }

        if (request.getPassword().length() < 8) {

            throw new RuntimeException(
                    "Password must be at least 8 characters"
            );
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(email);
        user.setMobile(request.getMobile());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setQualification(request.getQualification());

        /*
         * Always store password as BCrypt.
         */
        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setRole(User.Role.STUDENT);
        user.setActive(true);

        userRepository.save(user);
    }


    // =========================================================
    // LOGIN
    // =========================================================

    public User loginUser(
            String email,
            String password) {

        if (email == null
                || email.trim().isEmpty()) {

            throw new RuntimeException(
                    "Email is required"
            );
        }

        if (password == null
                || password.isEmpty()) {

            throw new RuntimeException(
                    "Password is required"
            );
        }

        User user =
                userRepository
                        .findByEmail(email.trim())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid email or password"
                                )
                        );

        if (Boolean.FALSE.equals(user.getActive())) {

            throw new RuntimeException(
                    "Your account is inactive"
            );
        }

        boolean passwordValid;

        /*
         * BCrypt password validation.
         */
        if (isBCryptPassword(user.getPassword())) {

            passwordValid =
                    passwordEncoder.matches(
                            password,
                            user.getPassword()
                    );

        } else {

            /*
             * Support old plain-text passwords.
             */
            passwordValid =
                    password.equals(user.getPassword());
        }

        if (!passwordValid) {

            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        /*
         * Upgrade old plain-text password to BCrypt.
         */
        if (!isBCryptPassword(user.getPassword())) {

            user.setPassword(
                    passwordEncoder.encode(password)
            );
        }

        /*
         * Last login is recorded only after
         * complete authentication flow succeeds.
         *
         * For Admin with 2FA, completeLogin()
         * will update lastLoginAt after OTP verification.
         */
        userRepository.save(user);

        return user;
    }


    // =========================================================
    // CHANGE ADMIN PASSWORD
    // =========================================================

    public void changeAdminPassword(
            Long userId,
            String currentPassword,
            String newPassword,
            String confirmPassword) {

        // -----------------------------------------------------
        // USER ID VALIDATION
        // -----------------------------------------------------

        if (userId == null || userId <= 0) {

            throw new RuntimeException(
                    "Invalid user session"
            );
        }


        // -----------------------------------------------------
        // CURRENT PASSWORD VALIDATION
        // -----------------------------------------------------

        if (currentPassword == null
                || currentPassword.isEmpty()) {

            throw new RuntimeException(
                    "Current password is required"
            );
        }


        // -----------------------------------------------------
        // NEW PASSWORD VALIDATION
        // -----------------------------------------------------

        if (newPassword == null
                || newPassword.trim().isEmpty()) {

            throw new RuntimeException(
                    "New password is required"
            );
        }


        if (confirmPassword == null
                || confirmPassword.trim().isEmpty()) {

            throw new RuntimeException(
                    "Please confirm your new password"
            );
        }


        if (newPassword.length() < 8) {

            throw new RuntimeException(
                    "New password must be at least 8 characters"
            );
        }


        // -----------------------------------------------------
        // CONFIRM PASSWORD
        // -----------------------------------------------------

        if (!newPassword.equals(confirmPassword)) {

            throw new RuntimeException(
                    "New password and confirm password do not match"
            );
        }


        // -----------------------------------------------------
        // FIND USER
        // -----------------------------------------------------

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User account not found"
                                )
                        );


        // -----------------------------------------------------
        // ACTIVE ACCOUNT CHECK
        // -----------------------------------------------------

        if (Boolean.FALSE.equals(user.getActive())) {

            throw new RuntimeException(
                    "Your account is inactive"
            );
        }


        // -----------------------------------------------------
        // ADMIN CHECK
        // -----------------------------------------------------

        if (user.getRole() != User.Role.ADMIN) {

            throw new RuntimeException(
                    "Only an Admin can change the administrator password"
            );
        }


        // -----------------------------------------------------
        // CURRENT PASSWORD VERIFICATION
        // -----------------------------------------------------

        boolean currentPasswordValid;

        if (isBCryptPassword(user.getPassword())) {

            currentPasswordValid =
                    passwordEncoder.matches(
                            currentPassword,
                            user.getPassword()
                    );

        } else {

            currentPasswordValid =
                    currentPassword.equals(
                            user.getPassword()
                    );
        }


        if (!currentPasswordValid) {

            throw new RuntimeException(
                    "Current password is incorrect"
            );
        }


        // -----------------------------------------------------
        // PREVENT SAME PASSWORD
        // -----------------------------------------------------

        if (isBCryptPassword(user.getPassword())
                && passwordEncoder.matches(
                newPassword,
                user.getPassword())) {

            throw new RuntimeException(
                    "New password must be different from current password"
            );
        }

        if (!isBCryptPassword(user.getPassword())
                && newPassword.equals(user.getPassword())) {

            throw new RuntimeException(
                    "New password must be different from current password"
            );
        }


        // -----------------------------------------------------
        // ENCODE NEW PASSWORD
        // -----------------------------------------------------

        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );


        // -----------------------------------------------------
        // SAVE USER
        // -----------------------------------------------------

        userRepository.save(user);
    }


    // =========================================================
    // GET USER BY ID
    // =========================================================

    public User getUserById(Long userId) {

        if (userId == null || userId <= 0) {

            throw new RuntimeException(
                    "Invalid user ID"
            );
        }

        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User account not found"
                        )
                );
    }


    // =========================================================
    // UPDATE ADMIN PROFILE
    // =========================================================

    public void updateAdminProfile(
            Long userId,
            String firstName,
            String lastName,
            String email,
            String mobile) {

        if (userId == null || userId <= 0) {

            throw new RuntimeException(
                    "Invalid user session"
            );
        }

        if (firstName == null
                || firstName.trim().isEmpty()) {

            throw new RuntimeException(
                    "First name is required"
            );
        }

        if (lastName == null
                || lastName.trim().isEmpty()) {

            throw new RuntimeException(
                    "Last name is required"
            );
        }

        if (email == null
                || email.trim().isEmpty()) {

            throw new RuntimeException(
                    "Email address is required"
            );
        }

        String cleanFirstName =
                firstName.trim();

        String cleanLastName =
                lastName.trim();

        String cleanEmail =
                email.trim();

        if (!cleanEmail.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            throw new RuntimeException(
                    "Please enter a valid email address"
            );
        }

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Admin account not found"
                                )
                        );

        if (Boolean.FALSE.equals(user.getActive())) {

            throw new RuntimeException(
                    "Your account is inactive"
            );
        }

        if (user.getRole() != User.Role.ADMIN) {

            throw new RuntimeException(
                    "Only an Admin can update the administrator profile"
            );
        }

        userRepository
                .findByEmail(cleanEmail)
                .ifPresent(existingUser -> {

                    if (!existingUser
                            .getId()
                            .equals(userId)) {

                        throw new RuntimeException(
                                "This email address is already registered"
                        );
                    }
                });

        user.setFullName(
                cleanFirstName
                        + " "
                        + cleanLastName
        );

        user.setEmail(cleanEmail);

        user.setMobile(
                mobile == null
                        || mobile.trim().isEmpty()
                        ? null
                        : mobile.trim()
        );

        userRepository.save(user);
    }


    // =========================================================
    // TWO FACTOR AUTHENTICATION
    // =========================================================

    public boolean toggleAdminTwoFactor(Long userId) {

        if (userId == null || userId <= 0) {

            throw new RuntimeException(
                    "Invalid user session"
            );
        }

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User account not found"
                                )
                        );

        if (Boolean.FALSE.equals(user.getActive())) {

            throw new RuntimeException(
                    "Your account is inactive"
            );
        }

        if (user.getRole() != User.Role.ADMIN) {

            throw new RuntimeException(
                    "Only an Admin can change Two-Factor Authentication settings"
            );
        }

        boolean currentStatus =
                Boolean.TRUE.equals(
                        user.getTwoFactorEnabled()
                );

        user.setTwoFactorEnabled(
                !currentStatus
        );

        userRepository.save(user);

        return !currentStatus;
    }


    public boolean isTwoFactorEnabled(Long userId) {

        if (userId == null || userId <= 0) {

            throw new RuntimeException(
                    "Invalid user session"
            );
        }

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User account not found"
                                )
                        );

        if (user.getRole() != User.Role.ADMIN) {

            throw new RuntimeException(
                    "Only an Admin can access Two-Factor Authentication settings"
            );
        }

        return Boolean.TRUE.equals(
                user.getTwoFactorEnabled()
        );
    }


    // =========================================================
    // COMPLETE LOGIN
    // =========================================================

    public void completeLogin(User user) {

        if (user == null
                || user.getId() == null) {

            throw new RuntimeException(
                    "Invalid user login"
            );
        }

        if (Boolean.FALSE.equals(user.getActive())) {

            throw new RuntimeException(
                    "Your account is inactive"
            );
        }

        /*
         * Update last login only after
         * successful complete authentication.
         */
        user.setLastLoginAt(
                LocalDateTime.now()
        );

        userRepository.save(user);
    }


    // =========================================================
    // SEND ADMIN LOGIN 2FA OTP
    // =========================================================
    //
    // ADMIN OTP
    //
    // From:
    //sevakramchavan2003@gmail.com
    // To:
    // Admin registered email
    //
    // =========================================================

    public void sendAdminLoginOtp(User user) {

        if (user == null
                || user.getId() == null) {

            throw new RuntimeException(
                    "Invalid login request"
            );
        }

        if (user.getRole() != User.Role.ADMIN) {

            throw new RuntimeException(
                    "Two-Factor Authentication is available only for Admin login"
            );
        }

        if (Boolean.FALSE.equals(user.getActive())) {

            throw new RuntimeException(
                    "Your account is inactive"
            );
        }

        if (!Boolean.TRUE.equals(
                user.getTwoFactorEnabled())) {

            throw new RuntimeException(
                    "Two-Factor Authentication is not enabled"
            );
        }

        String key =
                user.getEmail()
                        .trim()
                        .toLowerCase();

        LoginOtpData existingOtp =
                loginOtpStorage.get(key);

        if (existingOtp != null) {

            LocalDateTime resendAllowedAt =
                    existingOtp
                            .createdAt()
                            .plusSeconds(60);

            if (LocalDateTime.now()
                    .isBefore(resendAllowedAt)) {

                long remainingSeconds =
                        java.time.Duration.between(
                                LocalDateTime.now(),
                                resendAllowedAt
                        ).getSeconds();

                if (remainingSeconds < 1) {
                    remainingSeconds = 1;
                }

                throw new RuntimeException(
                        "Please wait "
                                + remainingSeconds
                                + " seconds before requesting a new OTP"
                );
            }
        }

        // Generate 6-digit OTP
        int otpNumber =
                100000
                        + secureRandom.nextInt(900000);

        String otp =
                String.valueOf(otpNumber);

        LocalDateTime createdAt =
                LocalDateTime.now();

        // Admin OTP valid for 5 minutes
        LocalDateTime expiry =
                createdAt.plusMinutes(5);

        loginOtpStorage.put(
                key,
                new LoginOtpData(
                        otp,
                        expiry,
                        createdAt,
                        0
                )
        );

        /*
         * IMPORTANT:
         *
         * Admin Login 2FA uses Gmail.
         *
         * DO NOT call sendOtpEmail() here,
         * because sendOtpEmail() is now reserved
         * for Student Forgot Password.
         */
        emailService.sendLoginOtpEmail(
                user.getEmail(),
                otp
        );
    }


    // =========================================================
    // VERIFY ADMIN LOGIN 2FA OTP
    // =========================================================

    public User verifyAdminLoginOtp(
            String email,
            String otp) {

        if (email == null
                || email.trim().isEmpty()) {

            throw new RuntimeException(
                    "Email is required"
            );
        }

        if (otp == null
                || otp.trim().isEmpty()) {

            throw new RuntimeException(
                    "OTP is required"
            );
        }

        String key =
                email.trim()
                        .toLowerCase();

        LoginOtpData otpData =
                loginOtpStorage.get(key);

        if (otpData == null) {

            throw new RuntimeException(
                    "OTP has expired. Please login again."
            );
        }

        // Check expiry
        if (LocalDateTime.now()
                .isAfter(otpData.expiry())) {

            loginOtpStorage.remove(key);

            throw new RuntimeException(
                    "OTP has expired. Please login again."
            );
        }

        // Maximum attempts
        if (otpData.failedAttempts() >= 5) {

            loginOtpStorage.remove(key);

            throw new RuntimeException(
                    "Too many incorrect OTP attempts. Please login again."
            );
        }

        // Validate OTP
        if (!otpData.otp()
                .equals(otp.trim())) {

            int failedAttempts =
                    otpData.failedAttempts() + 1;

            loginOtpStorage.put(
                    key,
                    new LoginOtpData(
                            otpData.otp(),
                            otpData.expiry(),
                            otpData.createdAt(),
                            failedAttempts
                    )
            );

            int remainingAttempts =
                    5 - failedAttempts;

            throw new RuntimeException(
                    "Invalid OTP. "
                            + remainingAttempts
                            + " attempts remaining."
            );
        }

        User user =
                userRepository
                        .findByEmail(email.trim())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Admin account not found"
                                )
                        );

        if (Boolean.FALSE.equals(user.getActive())) {

            loginOtpStorage.remove(key);

            throw new RuntimeException(
                    "Your account is inactive"
            );
        }

        if (user.getRole() != User.Role.ADMIN) {

            loginOtpStorage.remove(key);

            throw new RuntimeException(
                    "Invalid Admin account"
            );
        }

        if (!Boolean.TRUE.equals(
                user.getTwoFactorEnabled())) {

            loginOtpStorage.remove(key);

            throw new RuntimeException(
                    "Two-Factor Authentication is not enabled"
            );
        }

        /*
         * OTP successfully verified.
         */
        loginOtpStorage.remove(key);

        return user;
    }


    // =========================================================
    // RESEND ADMIN LOGIN 2FA OTP
    // =========================================================

    public void resendAdminLoginOtp(
            String email) {

        if (email == null
                || email.trim().isEmpty()) {

            throw new RuntimeException(
                    "Email is required"
            );
        }

        User user =
                userRepository
                        .findByEmail(email.trim())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Admin account not found"
                                )
                        );

        /*
         * This internally calls
         * sendLoginOtpEmail()
         *
         * So Admin resend also uses Gmail.
         */
        sendAdminLoginOtp(user);
    }


    // =========================================================
    // SEND STUDENT LOGIN OTP
    // =========================================================

    public void sendStudentLoginOtp(User user) {

        if (user == null || user.getId() == null) {
            throw new RuntimeException("Invalid student login request.");
        }

        if (user.getRole() != User.Role.STUDENT) {
            throw new RuntimeException("Only students can use Student Login.");
        }

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new RuntimeException("Your account is inactive.");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Student email is required.");
        }

        String cleanEmail = user.getEmail().trim();
        String key = cleanEmail.toLowerCase();

        LoginOtpData existingOtp = studentLoginOtpStorage.get(key);

        if (existingOtp != null) {
            LocalDateTime resendAllowedAt =
                    existingOtp.createdAt().plusSeconds(60);

            if (LocalDateTime.now().isBefore(resendAllowedAt)) {
                long remainingSeconds =
                        java.time.Duration.between(
                                LocalDateTime.now(),
                                resendAllowedAt
                        ).getSeconds();

                throw new RuntimeException(
                        "Please wait " + Math.max(1, remainingSeconds)
                                + " seconds before requesting a new OTP."
                );
            }
        }

        String otp = String.valueOf(
                100000 + secureRandom.nextInt(900000)
        );

        LocalDateTime createdAt = LocalDateTime.now();

        LoginOtpData otpData = new LoginOtpData(
                otp,
                createdAt.plusMinutes(5),
                createdAt,
                0
        );

        studentLoginOtpStorage.put(key, otpData);

        try {
            // Uses the dedicated Student Login Gmail email method.
            emailService.sendStudentLoginOtpEmail(cleanEmail, otp);
        } catch (RuntimeException ex) {
            studentLoginOtpStorage.remove(key, otpData);
            throw new RuntimeException(
                    "Unable to send Student Login OTP. Please try again."
            );
        }
    }

    // =========================================================
    // VERIFY STUDENT LOGIN OTP
    // =========================================================

    public User verifyStudentLoginOtp(String email, String otp) {

        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required.");
        }

        if (otp == null || otp.trim().isEmpty()) {
            throw new RuntimeException("OTP is required.");
        }

        String cleanEmail = email.trim();
        String key = cleanEmail.toLowerCase();

        LoginOtpData otpData = studentLoginOtpStorage.get(key);

        if (otpData == null) {
            throw new RuntimeException(
                    "OTP has expired or was not requested. Please login again."
            );
        }

        if (LocalDateTime.now().isAfter(otpData.expiry())) {
            studentLoginOtpStorage.remove(key, otpData);
            throw new RuntimeException(
                    "OTP has expired. Please login again."
            );
        }

        if (otpData.failedAttempts() >= 5) {
            studentLoginOtpStorage.remove(key, otpData);
            throw new RuntimeException(
                    "Too many incorrect OTP attempts. Please login again."
            );
        }

        if (!otpData.otp().equals(otp.trim())) {
            int failedAttempts = otpData.failedAttempts() + 1;

            if (failedAttempts >= 5) {
                studentLoginOtpStorage.remove(key, otpData);
                throw new RuntimeException(
                        "Too many incorrect OTP attempts. Please login again."
                );
            }

            studentLoginOtpStorage.replace(
                    key,
                    otpData,
                    new LoginOtpData(
                            otpData.otp(),
                            otpData.expiry(),
                            otpData.createdAt(),
                            failedAttempts
                    )
            );

            throw new RuntimeException(
                    "Invalid OTP. " + (5 - failedAttempts)
                            + " attempts remaining."
            );
        }

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() ->
                        new RuntimeException("Student account not found.")
                );

        if (user.getRole() != User.Role.STUDENT) {
            studentLoginOtpStorage.remove(key, otpData);
            throw new RuntimeException("Invalid Student account.");
        }

        if (Boolean.FALSE.equals(user.getActive())) {
            studentLoginOtpStorage.remove(key, otpData);
            throw new RuntimeException("Your account is inactive.");
        }

        studentLoginOtpStorage.remove(key, otpData);
        return user;
    }

    // =========================================================
    // RESEND STUDENT LOGIN OTP
    // =========================================================

    public void resendStudentLoginOtp(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required.");
        }

        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() ->
                        new RuntimeException("Student account not found.")
                );

        if (user.getRole() != User.Role.STUDENT) {
            throw new RuntimeException("Invalid Student account.");
        }

        sendStudentLoginOtp(user);
    }


    // =========================================================
    // SEND STUDENT FORGOT PASSWORD OTP
    // =========================================================
    //
    // STUDENT OTP
    //
    // From:
    // sevakramchavan2003@gmail.com
    //
    // To:
    // Student registered email
    //
    // CC:
    // configured CC email
    //
    // =========================================================

    public void sendForgotPasswordOtp(
            String email) {

        if (email == null
                || email.trim().isEmpty()) {

            throw new RuntimeException(
                    "Email is required"
            );
        }

        String cleanEmail =
                email.trim();

        User user =
                userRepository
                        .findByEmail(cleanEmail)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No account found with this email"
                                )
                        );

        if (Boolean.FALSE.equals(user.getActive())) {

            throw new RuntimeException(
                    "Your account is inactive"
            );
        }

        String key =
                cleanEmail.toLowerCase();


        // -----------------------------------------------------
        // CHECK EXISTING OTP
        // -----------------------------------------------------
        //
        // Prevent another OTP request
        // within 60 seconds.
        //

        OtpData existingOtp =
                otpStorage.get(key);

        if (existingOtp != null) {

            LocalDateTime resendAllowedAt =
                    existingOtp
                            .createdAt()
                            .plusSeconds(60);

            if (LocalDateTime.now()
                    .isBefore(resendAllowedAt)) {

                long remainingSeconds =
                        java.time.Duration.between(
                                LocalDateTime.now(),
                                resendAllowedAt
                        ).getSeconds();

                if (remainingSeconds < 1) {
                    remainingSeconds = 1;
                }

                throw new RuntimeException(
                        "Please wait "
                                + remainingSeconds
                                + " seconds before requesting a new OTP"
                );
            }
        }


        // -----------------------------------------------------
        // GENERATE 6-DIGIT OTP
        // -----------------------------------------------------

        int otpNumber =
                100000
                        + secureRandom.nextInt(900000);

        String otp =
                String.valueOf(otpNumber);


        // -----------------------------------------------------
        // OTP VALID FOR 10 MINUTES
        // -----------------------------------------------------

        LocalDateTime createdAt =
                LocalDateTime.now();

        LocalDateTime expiry =
                createdAt.plusMinutes(10);


        // -----------------------------------------------------
        // STORE OTP
        // -----------------------------------------------------

        otpStorage.put(
                key,
                new OtpData(
                        otp,
                        expiry,
                        createdAt,
                        false,
                        0
                )
        );


        // -----------------------------------------------------
        // SEND STUDENT FORGOT PASSWORD OTP
        // -----------------------------------------------------
        //
        // IMPORTANT:
        //
        // EmailService.sendOtpEmail()
        // is configured to use RESEND.
        //
        // FROM:
        //admin
        //
        // TO:
        // student's registered email
        //
        // CC:
        // configured personal/admin email
        //
        // -----------------------------------------------------

        emailService.sendOtpEmail(
                cleanEmail,
                otp
        );
    }


    // =========================================================
    // VERIFY FORGOT PASSWORD OTP
    // =========================================================

    public void verifyForgotPasswordOtp(
            String email,
            String otp) {

        if (email == null
                || email.trim().isEmpty()) {

            throw new RuntimeException(
                    "Email is required"
            );
        }

        if (otp == null
                || otp.trim().isEmpty()) {

            throw new RuntimeException(
                    "OTP is required"
            );
        }

        String cleanEmail =
                email.trim();

        String cleanOtp =
                otp.trim();

        String key =
                cleanEmail.toLowerCase();

        OtpData otpData =
                otpStorage.get(key);

        if (otpData == null) {

            throw new RuntimeException(
                    "OTP has expired. Please request a new OTP."
            );
        }


        // -----------------------------------------------------
        // CHECK EXPIRY
        // -----------------------------------------------------

        if (LocalDateTime.now()
                .isAfter(otpData.expiry())) {

            otpStorage.remove(key);

            throw new RuntimeException(
                    "OTP has expired. Please request a new OTP."
            );
        }


        // -----------------------------------------------------
        // CHECK MAXIMUM ATTEMPTS
        // -----------------------------------------------------

        if (otpData.failedAttempts() >= 5) {

            otpStorage.remove(key);

            throw new RuntimeException(
                    "Too many incorrect OTP attempts. "
                            + "Please request a new OTP."
            );
        }


        // -----------------------------------------------------
        // CHECK OTP
        // -----------------------------------------------------

        if (!otpData.otp()
                .equals(cleanOtp)) {

            int failedAttempts =
                    otpData.failedAttempts() + 1;

            otpStorage.put(
                    key,
                    new OtpData(
                            otpData.otp(),
                            otpData.expiry(),
                            otpData.createdAt(),
                            false,
                            failedAttempts
                    )
            );

            int remainingAttempts =
                    5 - failedAttempts;

            throw new RuntimeException(
                    "Invalid OTP. "
                            + remainingAttempts
                            + " attempts remaining."
            );
        }


        // -----------------------------------------------------
        // OTP SUCCESSFULLY VERIFIED
        // -----------------------------------------------------

        otpStorage.put(
                key,
                new OtpData(
                        otpData.otp(),
                        otpData.expiry(),
                        otpData.createdAt(),
                        true,
                        otpData.failedAttempts()
                )
        );
    }


    // =========================================================
    // RESET PASSWORD
    // =========================================================

    public void resetPassword(
            String email,
            String newPassword,
            String confirmPassword) {

        if (email == null
                || email.trim().isEmpty()) {

            throw new RuntimeException(
                    "Email is required"
            );
        }

        String cleanEmail =
                email.trim();

        String key =
                cleanEmail.toLowerCase();

        OtpData otpData =
                otpStorage.get(key);

        if (otpData == null
                || !otpData.verified()) {

            throw new RuntimeException(
                    "Please verify the OTP before resetting your password"
            );
        }


        // -----------------------------------------------------
        // CHECK OTP EXPIRY AGAIN
        // -----------------------------------------------------

        if (LocalDateTime.now()
                .isAfter(otpData.expiry())) {

            otpStorage.remove(key);

            throw new RuntimeException(
                    "OTP has expired. Please request a new OTP."
            );
        }


        // -----------------------------------------------------
        // NEW PASSWORD VALIDATION
        // -----------------------------------------------------

        if (newPassword == null
                || newPassword.trim().isEmpty()) {

            throw new RuntimeException(
                    "New password is required"
            );
        }


        if (confirmPassword == null
                || confirmPassword.trim().isEmpty()) {

            throw new RuntimeException(
                    "Please confirm your password"
            );
        }


        if (newPassword.length() < 8) {

            throw new RuntimeException(
                    "Password must be at least 8 characters"
            );
        }


        // -----------------------------------------------------
        // CONFIRM PASSWORD
        // -----------------------------------------------------

        if (!newPassword.equals(confirmPassword)) {

            throw new RuntimeException(
                    "Passwords do not match"
            );
        }


        // -----------------------------------------------------
        // FIND USER
        // -----------------------------------------------------

        User user =
                userRepository
                        .findByEmail(cleanEmail)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No account found with this email"
                                )
                        );


        // -----------------------------------------------------
        // ACTIVE ACCOUNT CHECK
        // -----------------------------------------------------

        if (Boolean.FALSE.equals(user.getActive())) {

            throw new RuntimeException(
                    "Your account is inactive"
            );
        }


        // -----------------------------------------------------
        // PREVENT SAME PASSWORD
        // -----------------------------------------------------

        if (isBCryptPassword(user.getPassword())
                && passwordEncoder.matches(
                newPassword,
                user.getPassword())) {

            throw new RuntimeException(
                    "New password must be different from current password"
            );
        }


        // -----------------------------------------------------
        // ENCODE NEW PASSWORD
        // -----------------------------------------------------

        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );


        // -----------------------------------------------------
        // SAVE USER
        // -----------------------------------------------------

        userRepository.save(user);


        // -----------------------------------------------------
        // OTP CAN NO LONGER BE USED
        // -----------------------------------------------------

        otpStorage.remove(key);
    }


    // =========================================================
    // PASSWORD FORMAT CHECK
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


    // =========================================================
    // LOGIN OTP DATA
    // =========================================================

    private record LoginOtpData(
            String otp,
            LocalDateTime expiry,
            LocalDateTime createdAt,
            int failedAttempts) {
    }


    // =========================================================
    // FORGOT PASSWORD OTP DATA
    // =========================================================

    private record OtpData(
            String otp,
            LocalDateTime expiry,
            LocalDateTime createdAt,
            boolean verified,
            int failedAttempts) {
    }
}
