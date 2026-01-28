package com.repair.machinemanagement.service;

import com.repair.machinemanagement.dto.LoginRequest;
import com.repair.machinemanagement.dto.LoginResponse;
import com.repair.machinemanagement.entity.User;
import com.repair.machinemanagement.repository.UserRepository;
import com.repair.machinemanagement.security.JwtTokenProvider;
import com.repair.machinemanagement.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    
    public LoginResponse login(LoginRequest request) {
        String loginValue = request.getEmail().trim();
        log.debug("Tentative de connexion avec: '{}'", loginValue);
        
        // Rechercher l'utilisateur par email ou identifiant
        User user = userRepository.findByEmailOrIdentifiant(loginValue, loginValue)
                .orElseThrow(() -> {
                    log.warn("Utilisateur non trouvé avec login: '{}'", loginValue);
                    return new BadCredentialsException("Identifiant ou mot de passe incorrect");
                });
        
        log.debug("Utilisateur trouvé: {} ({})", user.getEmail(), user.getRole());
        
        // Vérifier si le compte est actif
        if (!user.getActive()) {
            log.warn("Tentative de connexion sur compte désactivé: {}", user.getEmail());
            throw new DisabledException("Votre compte a été désactivé. Contactez l'administration.");
        }
        
        // Vérifier le mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Mot de passe incorrect pour: {}", loginValue);
            throw new BadCredentialsException("Identifiant ou mot de passe incorrect");
        }
        
        // Créer l'authentification
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, 
            null, 
            userDetails.getAuthorities()
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        log.info("Utilisateur connecté: {} ({})", user.getEmail(), user.getRole());
        
        return LoginResponse.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .identifiant(user.getIdentifiant())
                .role(user.getRole().name())
                .token(jwt)
                .message("Connexion réussie")
                .build();
    }
}
