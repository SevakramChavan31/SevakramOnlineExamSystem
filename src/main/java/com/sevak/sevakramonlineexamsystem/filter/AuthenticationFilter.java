package com.sevak.sevakramonlineexamsystem.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest =
                (HttpServletRequest) request;

        HttpServletResponse httpResponse =
                (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();

        // Allow public pages and static resources.
        if (isPublicResource(uri)) {
            chain.doFilter(request, response);
            return;
        }

        // Get the existing session without creating a new one.
        HttpSession session =
                httpRequest.getSession(false);

        // Require authentication for protected pages.
        if (session == null
                || session.getAttribute("userId") == null) {

            httpResponse.sendRedirect(
                    httpResponse.encodeRedirectURL(
                            httpRequest.getContextPath() + "/login"
                    )
            );

            return;
        }

        // Get the logged-in user's role.
        String role =
                (String) session.getAttribute("role");

        // Student URL protection.
        if (uri.startsWith("/student/")) {

            if (!"STUDENT".equals(role)) {

                httpResponse.sendRedirect(
                        httpResponse.encodeRedirectURL(
                                httpRequest.getContextPath() + "/login"
                        )
                );

                return;
            }
        }

        // Admin URL protection.
        if (uri.startsWith("/admin/")) {

            if (!"ADMIN".equals(role)) {

                httpResponse.sendRedirect(
                        httpResponse.encodeRedirectURL(
                                httpRequest.getContextPath() + "/login"
                        )
                );

                return;
            }
        }

        // User is authenticated and authorized.
        chain.doFilter(request, response);
    }

    // =========================================================
    // PUBLIC RESOURCES
    // =========================================================

    private boolean isPublicResource(String uri) {

        return uri.equals("/")
                || uri.equals("/login")
                || uri.equals("/login/verify-otp")
                || uri.equals("/login/resend-otp")

                // Student Login OTP routes
                || uri.equals("/student-login/verify-otp")
                || uri.equals("/student-login/resend-otp")

                // Registration
                || uri.equals("/register")

                // Forgot Password OTP and reset
                || uri.equals("/forgot-password")
                || uri.equals("/verify-otp")
                || uri.equals("/reset-password")

                // Static resources
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.startsWith("/favicon")

                // Existing API routes
                || uri.startsWith("/api/");
    }
}