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
            
            // Créer un utilisateur client de test si aucun n'existe
            log.info("Vérification de l'existence du client CLT-00001...");
            boolean clientUserExists = userRepository.existsByIdentifiant("CLT-00001");
            log.info("Client utilisateur CLT-00001 existe déjà: {}", clientUserExists);
            
            if (!clientUserExists) {
                log.info("Création du client de test CLT-00001 dans la table users...");
                User testClientUser = User.builder()
                        .nom("Test")
                        .prenom("Client")
                        .adresse("456 Avenue de Test, 75002 Paris")
                        .numero("0623456789")
                        .email("client.test@example.com")
                        .identifiant("CLT-00001")
                        .password(passwordEncoder.encode("test123"))
                        .role(User.Role.CLIENT)
                        .active(true)
                        .credentialsSent(false)
                        .build();
                
                User saved = userRepository.save(testClientUser);
                userRepository.flush(); // Force la persistence
                
                // Vérifier immédiatement que le client a été sauvegardé
                log.info("Client utilisateur de test sauvegardé avec ID: {}", saved.getId());
                
                // Double vérification
                boolean nowExists = userRepository.existsByIdentifiant("CLT-00001");
                log.info("Vérification après création: Client utilisateur CLT-00001 existe = {}", nowExists);
                
                // Triple vérification par recherche directe
                Optional<User> foundClientUser = userRepository.findByIdentifiant("CLT-00001");
                log.info("Recherche directe après création: {}", foundClientUser.isPresent() ? "trouvé" : "NON TROUVÉ");
                
                System.out.println("✔ Client de test créé automatiquement dans la table users!");
                System.out.println("   Identifiant: CLT-00001");
                System.out.println("   Email: client.test@example.com");
                System.out.println("   Mot de passe: test123");
            } else {
                System.out.println("✔ Client de test déjà existant, création ignorée.");
                log.info("Client utilisateur CLT-00001 déjà existant dans la base");
                
                // Vérifier qu'on peut bien le trouver
                Optional<User> foundClientUser = userRepository.findByIdentifiant("CLT-00001");
                log.info("Test de recherche du client utilisateur existant: {}", foundClientUser.isPresent() ? "trouvé" : "NON TROUVÉ");
                if (foundClientUser.isPresent()) {
                    User c = foundClientUser.get();
                    log.info("Détails: ID={}, Identifiant={}, Nom={} {}, Email={}, Role={}, Active={}", 
                        c.getId(), c.getIdentifiant(), c.getNom(), c.getPrenom(), c.getEmail(), c.getRole(), c.getActive());
                }
            }
            
            // Afficher l'état final
            long finalUserCount = userRepository.count();
            long finalClientCount = clientRepository.count();
            log.info("Nombre final d'utilisateurs dans la base: {}", finalUserCount);
            log.info("Nombre final de clients (ancienne table) dans la base: {}", finalClientCount);
            
        } catch (Exception e) {
            log.error("ERREUR dans DataInitializer: ", e);
            throw e;
        }
        
        log.info("=== FIN DataInitializer ===");
    }
}
