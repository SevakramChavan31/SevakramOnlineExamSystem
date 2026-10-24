package com.sevak.sevakramonlineexamsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@Profile("cloud")
public class CloudEmailConfig {

    /*
     * Cloud startup configuration.
     *
     * These beans satisfy EmailService dependencies.
     * They do not provide working email delivery.
     */

    @Bean(name = "gmailMailSender")
    @Primary
    public JavaMailSender gmailMailSender() {

        JavaMailSenderImpl mailSender =
                new JavaMailSenderImpl();

        mailSender.setHost("localhost");
        mailSender.setPort(25);

        return mailSender;
    }

    @Bean(name = "resendMailSender")
    public JavaMailSender resendMailSender() {

        JavaMailSenderImpl mailSender =
                new JavaMailSenderImpl();

        mailSender.setHost("localhost");
        mailSender.setPort(25);

        return mailSender;
    }
}