package com.repair.machinemanagement.service;

import com.repair.machinemanagement.entity.Client;
import com.repair.machinemanagement.entity.Machine;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service pour l'envoi d'emails aux clients.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Machine Repair Management}")
    private String appName;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Envoie les identifiants de connexion au nouveau client.
     * @param client Le client créé
     * @param plainPassword Le mot de passe en clair (avant hashage)
     */
    @Async
    public void sendClientCredentials(Client client, String plainPassword) {
        try {
            // Validate email address
            if (client.getEmail() == null || client.getEmail().isBlank()) {
                log.error("Impossible d'envoyer l'email: adresse email vide pour le client {}", client.getIdentifiant());
                throw new IllegalArgumentException("Adresse email invalide");
            }

            Context context = new Context();
            context.setVariable("clientNom", client.getNom());
            context.setVariable("clientPrenom", client.getPrenom());
            context.setVariable("identifiant", client.getIdentifiant());
            context.setVariable("password", plainPassword);
            context.setVariable("appName", appName);
            context.setVariable("loginUrl", frontendUrl + "/client/login");

            String htmlContent;
            try {
                htmlContent = templateEngine.process("client-credentials", context);
            } catch (Exception e) {
                log.error("Erreur lors du traitement du template pour le client {}: {}", 
                    client.getIdentifiant(), e.getMessage(), e);
                // Fallback: créer un email simple en texte brut si le template échoue
                htmlContent = createFallbackCredentialsEmail(client, plainPassword);
            }

            sendHtmlEmail(
                client.getEmail(),
                "Vos identifiants de connexion - " + appName,
                htmlContent
            );

            log.info("Email d'identifiants envoyé avec succès à: {} pour le client {}", 
                client.getEmail(), client.getIdentifiant());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email au client {} ({}): {}", 
                client.getIdentifiant(), client.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Échec de l'envoi de l'email: " + e.getMessage(), e);
        }
    }

    /**
     * Crée un email de secours simple si le template Thymeleaf échoue.
     */
    private String createFallbackCredentialsEmail(Client client, String plainPassword) {
        return String.format("""
            <html>
            <body>
                <h2>Bienvenue sur %s</h2>
                <p>Bonjour %s %s,</p>
                <p>Votre compte a été créé avec succès. Voici vos identifiants de connexion :</p>
                <ul>
                    <li><strong>Identifiant :</strong> %s</li>
                    <li><strong>Mot de passe :</strong> %s</li>
                </ul>
                <p>Vous pouvez vous connecter à l'adresse : <a href="%s">%s</a></p>
                <p>Cordialement,<br/>L'équipe %s</p>
            </body>
            </html>
            """, 
            appName, client.getPrenom(), client.getNom(), 
            client.getIdentifiant(), plainPassword,
            frontendUrl, frontendUrl,
            appName
        );
    }

    /**
     * Envoie une notification de changement de statut de machine.
     * @param client Le client propriétaire de la machine
     * @param machine La machine dont le statut a changé
     */
    @Async
    public void sendMachineStatusUpdate(Client client, Machine machine) {
        try {
            Context context = new Context();
            context.setVariable("clientNom", client.getNom());
            context.setVariable("clientPrenom", client.getPrenom());
            context.setVariable("machineMarque", machine.getMarque());
            context.setVariable("machineModele", machine.getModele());
            context.setVariable("statut", getStatutLabel(machine.getStatut()));
            context.setVariable("appName", appName);
            context.setVariable("trackingUrl", frontendUrl + "/client/machines");

            String htmlContent = templateEngine.process("machine-status-update", context);

            sendHtmlEmail(
                client.getEmail(),
                "Mise à jour de votre machine - " + appName,
                htmlContent
            );

            log.info("Email de mise à jour envoyé à: {}", client.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", client.getEmail(), e.getMessage());
        }
    }

    /**
     * Envoie une notification quand la machine est prête à être récupérée.
     * @param client Le client
     * @param machine La machine terminée
     */
    @Async
    public void sendMachineReadyNotification(Client client, Machine machine) {
        try {
            Context context = new Context();
            context.setVariable("clientNom", client.getNom());
            context.setVariable("clientPrenom", client.getPrenom());
            context.setVariable("machineMarque", machine.getMarque());
            context.setVariable("machineModele", machine.getModele());
            context.setVariable("montant", machine.getMontant());
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("machine-ready", context);

            sendHtmlEmail(
                client.getEmail(),
                "Votre machine est prête ! - " + appName,
                htmlContent
            );

            log.info("Email 'machine prête' envoyé à: {}", client.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", client.getEmail(), e.getMessage());
        }
    }

    /**
     * Envoie un email HTML.
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    /**
     * Convertit le statut enum en libellé français.
     */
    private String getStatutLabel(Machine.Statut statut) {
        return switch (statut) {
            case EN_ATTENTE -> "En attente";
            case EN_COURS -> "En cours de réparation";
            case TERMINE -> "Réparation terminée";
            case ANOMALIE -> "Anomalie détectée";
            case PAYE -> "Payé";
            case REMIS_AU_CLIENT -> "Remis au client";
        };
    }
}
