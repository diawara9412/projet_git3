package com.repair.machinemanagement.config;

import com.repair.machinemanagement.entity.Client;
import com.repair.machinemanagement.entity.User;
import com.repair.machinemanagement.repository.ClientRepository;
import com.repair.machinemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=== DÉBUT DataInitializer ===");
        
        try {
            // Vérifier la connexion à la base
            long totalClients = clientRepository.count();
            long totalUsers = userRepository.count();
            log.info("État de la base: {} utilisateurs, {} clients", totalUsers, totalClients);
            
            // Lister les clients existants pour debug
            if (totalClients > 0) {
                List<Client> allClients = clientRepository.findAll();
                log.info("Clients existants:");
                for (Client c : allClients) {
                    log.info("  - ID={}, Identifiant={}, Nom={} {}, Email={}", 
                        c.getId(), c.getIdentifiant(), c.getNom(), c.getPrenom(), c.getEmail());
                }
            }
            
            // Vérifie si un admin existe déjà
            boolean adminExists = userRepository.existsByRole(User.Role.ADMIN);
            log.info("Admin existe: {}", adminExists);

            if (!adminExists) {
                User admin = User.builder()
                        .nom("Administrateur")
                        .prenom("Principal")
                        .adresse("123 Rue de la Réparation, 75001 Paris")
                        .numero("0612345678")
                        .email("admin@repair.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(User.Role.ADMIN)
                        .active(true)
                        .build();

                userRepository.save(admin);
                userRepository.flush(); // Force la persistence

                System.out.println("✔ Administrateur créé automatiquement !");
                System.out.println("   Email: admin@repair.com");
                System.out.println("   Mot de passe: admin123");
                log.info("Administrateur créé avec succès");
            } else {
                System.out.println("✔ Administrateur déjà existant, création ignorée.");
                log.info("Administrateur déjà existant");
            }
            
            // Créer un client de test si aucun client n'existe
            log.info("Vérification de l'existence du client CLT-00001...");
            boolean clientExists = clientRepository.existsByIdentifiant("CLT-00001");
            log.info("Client CLT-00001 existe déjà: {}", clientExists);
            
            if (!clientExists) {
                log.info("Création du client de test CLT-00001...");
                Client testClient = Client.builder()
                        .nom("Test")
                        .prenom("Client")
                        .adresse("456 Avenue de Test, 75002 Paris")
                        .numero("0623456789")
                        .email("client.test@example.com")
                        .identifiant("CLT-00001")
                        .password(passwordEncoder.encode("test123"))
                        .active(true)
                        .credentialsSent(false)
                        .build();
                
                Client saved = clientRepository.save(testClient);
                clientRepository.flush(); // Force la persistence
                
                // Vérifier immédiatement que le client a été sauvegardé
                log.info("Client de test sauvegardé avec ID: {}", saved.getId());
                
                // Double vérification
                boolean nowExists = clientRepository.existsByIdentifiant("CLT-00001");
                log.info("Vérification après création: Client CLT-00001 existe = {}", nowExists);
                
                // Triple vérification par recherche directe
                Optional<Client> foundClient = clientRepository.findByIdentifiant("CLT-00001");
                log.info("Recherche directe après création: {}", foundClient.isPresent() ? "trouvé" : "NON TROUVÉ");
                
                System.out.println("✔ Client de test créé automatiquement !");
                System.out.println("   Identifiant: CLT-00001");
                System.out.println("   Email: client.test@example.com");
                System.out.println("   Mot de passe: test123");
            } else {
                System.out.println("✔ Client de test déjà existant, création ignorée.");
                log.info("Client CLT-00001 déjà existant dans la base");
                
                // Vérifier qu'on peut bien le trouver
                Optional<Client> foundClient = clientRepository.findByIdentifiant("CLT-00001");
                log.info("Test de recherche du client existant: {}", foundClient.isPresent() ? "trouvé" : "NON TROUVÉ");
                if (foundClient.isPresent()) {
                    Client c = foundClient.get();
                    log.info("Détails: ID={}, Nom={} {}, Email={}, Active={}", 
                        c.getId(), c.getNom(), c.getPrenom(), c.getEmail(), c.getActive());
                }
            }
            
            // Afficher l'état final
            long finalClientCount = clientRepository.count();
            log.info("Nombre final de clients dans la base: {}", finalClientCount);
            
        } catch (Exception e) {
            log.error("ERREUR dans DataInitializer: ", e);
            throw e;
        }
        
        log.info("=== FIN DataInitializer ===");
    }
}
