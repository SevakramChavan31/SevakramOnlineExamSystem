package com.sevak.sevakramonlineexamsystem.config;

import com.sevak.sevakramonlineexamsystem.filter.AuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<AuthenticationFilter>
    authenticationFilterRegistration(
            AuthenticationFilter authenticationFilter) {

        FilterRegistrationBean<AuthenticationFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(authenticationFilter);

        registration.addUrlPatterns(
                "/student/*",
                "/admin/*"
        );

        registration.setOrder(1);

        return registration;
    }
}