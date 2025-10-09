package com.the.service;

import com.the.dto.request.ResetPasswordEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.from}")
    private String emailFrom;

    @Value("${endpoint.resetPassword}")
    private String apiResetPassword;

    @Value("${jwt.expiryMinute}")
    private String expiryMinute;

    @KafkaListener(topics = "reset-password-topic", groupId = "reset-password-group")
    public void sendConfirmLinkByKafka(ResetPasswordEvent event) throws MessagingException, UnsupportedEncodingException {
        log.info("Sending link to user, email={}", event);

        String emailTo = event.getEmail();
        String username = event.getUsername();
        String resetToken = event.getToken();
        String type = event.getType();

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        Context context = new Context();

        String linkReset = String.format("%s/resetToken=%s", apiResetPassword, resetToken);

        Map<String, Object> properties = new HashMap<>();
        properties.put("linkReset", linkReset);
        properties.put("username", username);
        properties.put("type", type);
        properties.put("expiryMinute", expiryMinute);
        context.setVariables(properties);

        helper.setFrom(emailFrom, "The");
        helper.setTo(emailTo);
        helper.setSubject("Please change your password");
        String html = templateEngine.process("reset-password.html", context);
        helper.setText(html, true);

        mailSender.send(mimeMessage);
    }
}
