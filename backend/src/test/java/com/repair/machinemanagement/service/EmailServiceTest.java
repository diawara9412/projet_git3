package com.repair.machinemanagement.service;

import com.repair.machinemanagement.entity.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private Client testClient;

    @BeforeEach
    void setUp() {
        // Set up required properties
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");
        ReflectionTestUtils.setField(emailService, "appName", "Test App");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:3000");

        testClient = Client.builder()
                .id(1L)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@test.com")
                .identifiant("CLT-00001")
                .build();
    }

    @Test
    void testSendClientCredentialsSuccess() {
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Test email content</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Should not throw exception
        assertDoesNotThrow(() -> 
            emailService.sendClientCredentials(testClient, "password123")
        );

        verify(templateEngine).process(eq("client-credentials"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendClientCredentialsWithTemplateError() {
        // Template engine throws exception
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenThrow(new RuntimeException("Template processing error"));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Should use fallback and still send email
        assertDoesNotThrow(() -> 
            emailService.sendClientCredentials(testClient, "password123")
        );

        verify(templateEngine).process(eq("client-credentials"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class)); // Email should still be sent with fallback
    }

    @Test
    void testSendClientCredentialsWithNullEmail() {
        testClient.setEmail(null);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> emailService.sendClientCredentials(testClient, "password123")
        );

        assertTrue(exception.getMessage().contains("Échec de l'envoi de l'email"));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendClientCredentialsWithBlankEmail() {
        testClient.setEmail("");

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> emailService.sendClientCredentials(testClient, "password123")
        );

        assertTrue(exception.getMessage().contains("Échec de l'envoi de l'email"));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendClientCredentialsWithMailSendingError() {
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Test email content</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(MimeMessage.class));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> emailService.sendClientCredentials(testClient, "password123")
        );

        assertTrue(exception.getMessage().contains("Échec de l'envoi de l'email"));
        verify(mailSender).send(any(MimeMessage.class));
    }
}
