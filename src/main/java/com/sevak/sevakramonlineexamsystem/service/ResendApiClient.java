package com.sevak.sevakramonlineexamsystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
@Profile("cloud")
public class ResendApiClient {

    @Value("${RESEND_SMTP_PASSWORD:}")
    private String apiKey;

    @Value("${resend.smtp.from:}")
    private String fromEmail;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void sendEmail(
            String to,
            String subject,
            String text
    ) throws Exception {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Resend API key is missing. Check RESEND_SMTP_PASSWORD."
            );
        }

        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException(
                    "Sender email is missing. Check resend.smtp.from."
            );
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("from", fromEmail);
        payload.put("to", new String[]{to});
        payload.put("subject", subject);
        payload.put("text", text);

        String json = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("User-Agent", "OnlineExaminationSystem/1.0")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new IllegalStateException(
                    "Resend email failed. HTTP status: "
                            + response.statusCode()
                            + ", response: "
                            + response.body()
            );
        }

        System.out.println("Email accepted by Resend API.");
    }
}