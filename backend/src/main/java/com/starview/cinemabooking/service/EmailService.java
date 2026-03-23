package com.starview.cinemabooking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.starview.cinemabooking.dtos.EmailPayloadDto;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendTicketEmail(EmailPayloadDto emailPayload) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data="
                    + emailPayload.getTicketId();

            String htmlContent = "<html>"
                    + "<body>"
                    + "<h1>Xác nhận vé xem phim của bạn</h1>"
                    + "<p>Cảm ơn bạn đã đặt vé tại StarView Cinemas!</p>"
                    + "<p><strong>Phim:</strong> " + emailPayload.getMovieTitle() + "</p>"
                    + "<p><strong>Suất chiếu:</strong> " + emailPayload.getShowtime() + "</p>"
                    + "<p><strong>Ghế:</strong> " + emailPayload.getSeats() + "</p>"
                    + "<p><strong>Tổng cộng:</strong> " + String.format("%,.0f", emailPayload.getTotalPrice())
                    + " VND</p>"
                    + "<p>Vui lòng xuất trình mã QR này tại rạp:</p>"
                    + "<img src='" + qrCodeUrl + "' alt='Mã QR vé của bạn' />"
                    + "<p>Chúc bạn có một buổi xem phim vui vẻ!</p>"
                    + "</body>"
                    + "</html>";

            helper.setTo(emailPayload.getTo());
            helper.setSubject("Vé xem phim của bạn tại StarView Cinemas - Mã vé #" + emailPayload.getTicketId());
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            // Cần cơ chế retry hoặc logging để đảm bảo email được gửi thành công
            System.err.println("Lỗi khi gửi email: " + e.getMessage());
        }
    }
}