package com.sevak.sevakramonlineexamsystem.config;

import com.sevak.sevakramonlineexamsystem.entity.User;
import com.sevak.sevakramonlineexamsystem.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminDataInitializer {

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    CommandLineRunner createDefaultAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // Create admin only if it does not already exist
            if (!userRepository.existsByEmail(adminEmail)) {

                User admin = new User();

                admin.setFullName("System Administrator");
                admin.setEmail(adminEmail);
                admin.setMobile("9999999999");
                admin.setQualification("Admin");

                // Encrypt admin password before saving
                admin.setPassword(
                        passwordEncoder.encode(adminPassword)
                );

                // Set ADMIN role
                admin.setRole(User.Role.ADMIN);

                // Activate account
                admin.setActive(true);

                userRepository.save(admin);

                System.out.println(
                        "Default admin account created successfully."
                );
            } else {
                System.out.println(
                        "Admin account already exists. No new admin created."
                );
            }
        };
    }
}