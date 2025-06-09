package com.gmn26.springboot_authentication.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public void sendOtp(String email, String subject, String otpCode) throws MessagingException {
        Context context = new Context();
        context.setVariable("otpCode", otpCode);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id"));
        String formattedDate = LocalDateTime.now().format(formatter);

        context.setVariable("sendDate", formattedDate);


        String htmlContent = templateEngine.process("otp-email", context);

        MimeMessage msg = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(msg, true);

        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(msg);
    }

    public void sendToken(String email, String subject, String token) throws MessagingException {
        Context context = new Context();
        context.setVariable("token", token);
        context.setVariable("email", email);

        String htmlContent = templateEngine.process("token-email", context);

        MimeMessage msg = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(msg, true);

        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(msg);
    }
}
