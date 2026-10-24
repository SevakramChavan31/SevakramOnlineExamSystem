package com.sevak.sevakramonlineexamsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@Profile("!cloud")
public class EmailConfig {

    // =========================================================
    // GMAIL MAIL SENDER
    // Used for Admin Login / Admin 2FA
    // =========================================================

    @Value("${spring.mail.host}")
    private String gmailHost;

    @Value("${spring.mail.port}")
    private int gmailPort;

    @Value("${spring.mail.username}")
    private String gmailUsername;

    @Value("${spring.mail.password}")
    private String gmailPassword;


    @Bean(name = "gmailMailSender")
    @Primary
    public JavaMailSender gmailMailSender() {

        JavaMailSenderImpl mailSender =
                new JavaMailSenderImpl();

        mailSender.setHost(gmailHost);
        mailSender.setPort(gmailPort);
        mailSender.setUsername(gmailUsername);
        mailSender.setPassword(gmailPassword);

        Properties properties =
                mailSender.getJavaMailProperties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        properties.put("mail.smtp.ssl.checkserveridentity", "true");

        properties.put("mail.smtp.connectiontimeout", "5000");
        properties.put("mail.smtp.timeout", "5000");
        properties.put("mail.smtp.writetimeout", "5000");

        return mailSender;
    }


    // =========================================================
    // RESEND SMTP SENDER
    // Used for Student OTP and Support Emails
    // =========================================================

    @Value("${resend.smtp.host}")
    private String resendHost;

    @Value("${resend.smtp.port}")
    private int resendPort;

    @Value("${resend.smtp.username}")
    private String resendUsername;

    @Value("${resend.smtp.password}")
    private String resendPassword;


    @Bean(name = "resendMailSender")
    public JavaMailSender resendMailSender() {

        JavaMailSenderImpl mailSender =
                new JavaMailSenderImpl();

        mailSender.setHost(resendHost);
        mailSender.setPort(resendPort);
        mailSender.setUsername(resendUsername);
        mailSender.setPassword(resendPassword);

        Properties properties =
                mailSender.getJavaMailProperties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.ssl.trust", "smtp.resend.com");
        properties.put("mail.smtp.ssl.checkserveridentity", "true");

        properties.put("mail.smtp.connectiontimeout", "5000");
        properties.put("mail.smtp.timeout", "5000");
        properties.put("mail.smtp.writetimeout", "5000");

        return mailSender;
    }
}