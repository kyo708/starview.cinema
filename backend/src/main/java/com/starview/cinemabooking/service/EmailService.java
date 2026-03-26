package com.starview.cinemabooking.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.starview.cinemabooking.dtos.EmailTicketRequest;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	@Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Async
    public void sendTicketEmail(EmailTicketRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        String brevoUrl = "https://api.brevo.com/v3/smtp/email";

        // 1. Set the strict API Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // 2. Build the HTML Ticket
        String htmlBody = "<h2>🎟️ Vé Xem Phim Của Bạn: " + request.getMovie_name() + "</h2>"
                + "<p><strong>Suất chiếu:</strong> " + request.getShowtime() + "</p>"
                + "<p><strong>Ghế:</strong> " + request.getSeats() + "</p>"
                + "<p><strong>Tổng tiền:</strong> " + request.getTotal_price() + "</p>"
                + "<br/><img src='" + request.getQr_image_link() + "' alt='Ticket QR Code'/>";

        // 3. Construct the JSON Payload using standard Java Maps
        Map<String, Object> body = new HashMap<>();
        
        // Who is sending it (Must match your verified Brevo email)
        body.put("sender", Map.of("name", "StarView Cinema", "email", senderEmail));
        
        // Who is receiving it (Can be anyone!)
        body.put("to", List.of(Map.of("email", request.getTo_email())));
        
        body.put("subject", "🎟️ Vé Xem Phim: " + request.getMovie_name());
        body.put("htmlContent", htmlBody);

        // 4. Fire the HTTPS Request through the firewall
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(brevoUrl, entity, String.class);
            System.out.println("✅ Email sent successfully to " + request.getTo_email() + " via Brevo!");
        } catch (Exception e) {
            System.err.println("❌ Failed to send email via Brevo: " + e.getMessage());
        }
    }
}
