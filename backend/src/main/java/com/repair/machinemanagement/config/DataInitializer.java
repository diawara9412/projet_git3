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

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Vérifie si un admin existe déjà
        boolean adminExists = userRepository.existsByRole(User.Role.ADMIN);

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
        } else {
            System.out.println("✔ Administrateur déjà existant, création ignorée.");
        }
        
        // Créer un client de test si aucun client n'existe
        boolean clientExists = clientRepository.existsByIdentifiant("CLT-00001");
        
        if (!clientExists) {
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
            log.info("Vérification: Client CLT-00001 existe = {}", nowExists);
            
            System.out.println("✔ Client de test créé automatiquement !");
            System.out.println("   Identifiant: CLT-00001");
            System.out.println("   Email: client.test@example.com");
            System.out.println("   Mot de passe: test123");
        } else {
            System.out.println("✔ Client de test déjà existant, création ignorée.");
            log.info("Client CLT-00001 déjà existant dans la base");
        }
    }
}
