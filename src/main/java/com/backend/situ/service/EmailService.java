package com.backend.situ.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    // TODO: try ResourceLoader, Thymeleaf or FreeMarker to load a “.html” template as email content.
    public void sendRegistrationEmail(String userEmail, String temporaryPassword) throws MessagingException {
        String subject = "Bienvenido, tu contraseña temporal";
        String htmlContent = "<h1>Bienvenido</h1>"
                + "<p>Gracias por registrarte. Tu contraseña temporal es: <strong>" + temporaryPassword + "</strong></p>"
                + "<p>Cambia tu contraseña tan pronto como inicies sesión.</p>";

        sendHtmlEmail(userEmail, subject, htmlContent);
    }

    public void sendEmailVerification(String newEmail, String verificationLink) throws MessagingException {
        String subject = "Verifica tu nueva dirección de correo";
        String htmlContent = "<h1>Verificación de correo</h1>"
                + "<p>Por favor haz clic en el siguiente enlace para verificar tu correo: "
                + "<a href='" + verificationLink + "'>Verificar correo</a></p>";

        sendHtmlEmail(newEmail, subject, htmlContent);
    }
}
