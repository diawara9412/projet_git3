package com.repair.machinemanagement.service;

import com.repair.machinemanagement.dto.ClientLoginRequest;
import com.repair.machinemanagement.dto.ClientLoginResponse;
import com.repair.machinemanagement.entity.Client;
import com.repair.machinemanagement.repository.ClientRepository;
import com.repair.machinemanagement.security.ClientDetailsImpl;
import com.repair.machinemanagement.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service d'authentification pour les clients.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientAuthService {
    
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    
    /**
     * Authentifie un client avec son identifiant/email et mot de passe.
     */
    public ClientLoginResponse login(ClientLoginRequest request) {
        log.debug("Tentative de connexion avec identifiant: {}", request.getIdentifiant());
        
        // Rechercher le client par email ou identifiant
        Client client = clientRepository.findByEmailOrIdentifiant(
            request.getIdentifiant(), 
            request.getIdentifiant()
        ).orElseThrow(() -> {
            log.warn("Client non trouvé avec identifiant: {}", request.getIdentifiant());
            return new BadCredentialsException("Identifiant ou mot de passe incorrect");
        });
        
        log.debug("Client trouvé: {} ({})", client.getIdentifiant(), client.getEmail());
        
        // Vérifier si le compte est actif
        if (!client.getActive()) {
            log.warn("Tentative de connexion sur compte désactivé: {}", client.getIdentifiant());
            throw new DisabledException("Votre compte a été désactivé. Contactez l'administration.");
        }
        
        // Vérifier le mot de passe
        if (!passwordEncoder.matches(request.getPassword(), client.getPassword())) {
            log.warn("Mot de passe incorrect pour le client: {}", client.getIdentifiant());
            throw new BadCredentialsException("Identifiant ou mot de passe incorrect");
        }
        
        // Créer l'authentification
        ClientDetailsImpl clientDetails = ClientDetailsImpl.build(client);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            clientDetails, 
            null, 
            clientDetails.getAuthorities()
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Générer le token JWT
        String jwt = tokenProvider.generateClientToken(clientDetails);
        
        log.info("Client connecté: {}", client.getIdentifiant());
        
        return ClientLoginResponse.builder()
                .id(client.getId())
                .identifiant(client.getIdentifiant())
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .email(client.getEmail())
                .role("CLIENT")
                .token(jwt)
                .message("Connexion réussie")
                .build();
    }
}
