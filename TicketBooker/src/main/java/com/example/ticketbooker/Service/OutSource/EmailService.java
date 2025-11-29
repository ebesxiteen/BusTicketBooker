package com.example.ticketbooker.Service.OutSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

@Component
public class EmailService {

    @Value("${spring.mail.username}")
    private String sender;
    
    private JavaMailSender mailSender;
    private TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Gửi email HTML dùng template Thymeleaf
     *
     * @param receiver     email người nhận
     * @param subject      tiêu đề email
     * @param htmlTemplate tên file template (VD: "email/ticket-confirmation")
     * @param context      context Thymeleaf chứa biến
     */
    public boolean sendEmail(String receiver, String subject, String htmlTemplate, Context context) {
        try {
            MimeMessage htmlMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(htmlMessage, "utf-8");

            helper.setFrom(sender);
            helper.setTo(receiver);
            helper.setSubject(subject);

            String htmlContent = templateEngine.process(htmlTemplate, context);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(htmlMessage);
            return true;
        } catch (Exception e) {
            System.err.println("[EmailService] Lỗi khi gửi email tới " + receiver + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public boolean sendHtmlContent(String receiver, String subject, String htmlContent) {
    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setFrom(sender);
        helper.setTo(receiver);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = nội dung là HTML

        mailSender.send(message);
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

}
