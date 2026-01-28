package com.repair.machinemanagement.service;

import com.repair.machinemanagement.dto.ClientRequest;
import com.repair.machinemanagement.dto.PasswordChangeRequest;
import com.repair.machinemanagement.entity.Client;
import com.repair.machinemanagement.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {
    
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int PASSWORD_LENGTH = 10;
    
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }
    
    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
    }
    
    public Client getClientByIdentifiant(String identifiant) {
        return clientRepository.findByIdentifiant(identifiant)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
    }
    
    public Client getClientByEmailOrIdentifiant(String login) {
        return clientRepository.findByEmailOrIdentifiant(login, login)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
    }
    
    public List<Client> searchClients(String keyword) {
        return clientRepository.findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(keyword, keyword);
    }
    
    /**
     * Crée un nouveau client avec génération automatique d'identifiants.
     * Un email est envoyé au client avec ses identifiants de connexion.
     */
    @Transactional
    public Client createClient(ClientRequest request) {
        // Vérifications d'unicité
        if (clientRepository.existsByNumero(request.getNumero())) {
            throw new RuntimeException("Numéro de téléphone déjà utilisé");
        }
        
        if (request.getEmail() != null && clientRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }
        
        // Génération de l'identifiant unique
        String identifiant = generateUniqueIdentifiant();
        
        // Génération d'un mot de passe aléatoire
        String plainPassword = generateRandomPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);
        
        Client client = Client.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .adresse(request.getAdresse())
                .numero(request.getNumero())
                .email(request.getEmail())
                .identifiant(identifiant)
                .password(hashedPassword)
                .active(true)
                .credentialsSent(false)
                .autres(request.getAutres())
                .build();
        
        Client savedClient = clientRepository.save(client);
        
        // Envoi de l'email avec les identifiants
        if (request.getEmail() != null && !request.getEmail().isBlank() 
            && (request.getSendCredentials() == null || request.getSendCredentials())) {
            try {
                emailService.sendClientCredentials(savedClient, plainPassword);
                savedClient.setCredentialsSent(true);
                savedClient = clientRepository.save(savedClient);
                log.info("Identifiants envoyés avec succès au client: {}", savedClient.getIdentifiant());
            } catch (Exception e) {
                log.error("Erreur lors de l'envoi de l'email au client {}: {}", 
                    savedClient.getIdentifiant(), e.getMessage(), e);
                // On ne bloque pas la création si l'email échoue, mais on garde credentialsSent=false
            }
        } else {
            log.info("Email non envoyé pour le client {}: email={}, sendCredentials={}", 
                savedClient.getIdentifiant(), request.getEmail(), request.getSendCredentials());
        }
        
        return savedClient;
    }
    
    /**
     * Met à jour les informations d'un client.
     */
    @Transactional
    public Client updateClient(Long id, ClientRequest request) {
        Client client = getClientById(id);
        
        // Vérifier si le nouveau numéro est déjà utilisé par un autre client
        if (!client.getNumero().equals(request.getNumero()) && 
            clientRepository.existsByNumero(request.getNumero())) {
            throw new RuntimeException("Numéro de téléphone déjà utilisé");
        }
        
        // Vérifier si le nouvel email est déjà utilisé par un autre client
        if (request.getEmail() != null && !request.getEmail().equals(client.getEmail()) &&
            clientRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }
        
        client.setNom(request.getNom());
        client.setPrenom(request.getPrenom());
        client.setAdresse(request.getAdresse());
        client.setNumero(request.getNumero());
        client.setEmail(request.getEmail());
        client.setAutres(request.getAutres());
        
        return clientRepository.save(client);
    }
    
    /**
     * Change le mot de passe d'un client.
     */
    @Transactional
    public void changePassword(Long clientId, PasswordChangeRequest request) {
        Client client = getClientById(clientId);
        
        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.getOldPassword(), client.getPassword())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }
        
        // Vérifier que le nouveau mot de passe et la confirmation correspondent
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }
        
        // Encoder et sauvegarder le nouveau mot de passe
        client.setPassword(passwordEncoder.encode(request.getNewPassword()));
        clientRepository.save(client);
        
        log.info("Mot de passe changé pour le client: {}", client.getIdentifiant());
    }
    
    /**
     * Renvoie les identifiants à un client.
     */
    @Transactional
    public void resendCredentials(Long clientId) {
        Client client = getClientById(clientId);
        
        if (client.getEmail() == null || client.getEmail().isBlank()) {
            throw new RuntimeException("Le client n'a pas d'adresse email");
        }
        
        // Générer un nouveau mot de passe
        String plainPassword = generateRandomPassword();
        client.setPassword(passwordEncoder.encode(plainPassword));
        clientRepository.save(client);
        
        // Envoyer l'email
        emailService.sendClientCredentials(client, plainPassword);
        client.setCredentialsSent(true);
        clientRepository.save(client);
        
        log.info("Identifiants renvoyés au client: {}", client.getIdentifiant());
    }
    
    /**
     * Active ou désactive un compte client.
     */
    @Transactional
    public Client toggleClientStatus(Long clientId) {
        Client client = getClientById(clientId);
        client.setActive(!client.getActive());
        return clientRepository.save(client);
    }
    
    public void deleteClient(Long id) {
        Client client = getClientById(id);
        clientRepository.delete(client);
    }
    
    /**
     * Génère un identifiant unique de type CLT-XXXXX.
     */
    private String generateUniqueIdentifiant() {
        long count = clientRepository.count() + 1;
        String identifiant;
        do {
            identifiant = String.format("CLT-%05d", count);
            count++;
        } while (clientRepository.existsByIdentifiant(identifiant));
        
        return identifiant;
    }
    
    /**
     * Génère un mot de passe aléatoire sécurisé.
     */
    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
