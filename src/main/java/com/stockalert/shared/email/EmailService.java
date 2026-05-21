package com.stockalert.shared.email;

import com.stockalert.shared.exception.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    public void sendHtml(String to, String subject, String htmlBody) {
        sendHtmlWithAttachment(to, subject, htmlBody, null, null);
    }

    public void sendHtmlWithAttachment(String to, String subject, String htmlBody, byte[] attachment, String attachmentName) {
        if (from == null || from.isBlank()) {
            throw new BusinessException("El correo remitente no esta configurado");
        }
        if (to == null || to.isBlank()) {
            throw new BusinessException("El correo del destinatario es obligatorio");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            if (attachment != null && attachment.length > 0 && attachmentName != null && !attachmentName.isBlank()) {
                helper.addAttachment(attachmentName, new org.springframework.core.io.ByteArrayResource(attachment));
            }
            mailSender.send(message);
        } catch (MessagingException | MailException ex) {
            throw new BusinessException("No se pudo enviar el correo: " + ex.getMessage());
        }
    }
}
