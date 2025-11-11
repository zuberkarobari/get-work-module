package com.getwork.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender emailSender;

    public void sendVerificationEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);

        emailSender.send(message);
    }
}





























//package com.getwork.auth.service;
//
//import com.getwork.auth.entity.User;
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.thymeleaf.context.Context;
//import org.thymeleaf.spring6.SpringTemplateEngine;
//
//import java.io.File;
//
//@Slf4j
//@Service
//public class EmailService {
//
//    private final JavaMailSender emailSender;
//    private final SpringTemplateEngine templateEngine;
//    private EmailService emailService;
//
//    @Autowired
//    public EmailService(JavaMailSender emailSender, SpringTemplateEngine templateEngine) {
//        this.emailSender = emailSender;
//        this.templateEngine = templateEngine;
//    }
//
//
//    public void sendVerificationEmailV1(String to, String subject, String text) throws MessagingException {
//        MimeMessage message = emailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//
//        helper.setTo(to);
//        helper.setSubject(subject);
//        helper.setText(text, true);
//
//        emailSender.send(message);
//    }
//    public void sendVerificationEmailV2(String to, String subject, String username) throws MessagingException {
//        MimeMessage message = emailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//
//        Context context = new Context();
//        context.setVariable("username", username);
//
//        String htmlContent = templateEngine.process("email/verification.html", context);
//        helper.setText(htmlContent, true);
//        helper.setSubject(subject);
//        helper.setTo(to);
//        ClassPathResource logo = new ClassPathResource("static/images/logo.png");
//        helper.addInline("logoImage", logo);
//
//        emailSender.send(message);
//    }
//
//}
