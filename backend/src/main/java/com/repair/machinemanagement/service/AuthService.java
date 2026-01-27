package com.repair.machinemanagement.service;

import com.repair.machinemanagement.dto.LoginRequest;
import com.repair.machinemanagement.dto.LoginResponse;
import com.repair.machinemanagement.entity.User;
import com.repair.machinemanagement.repository.UserRepository;
import com.repair.machinemanagement.security.JwtTokenProvider;
import com.repair.machinemanagement.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        return LoginResponse.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(jwt)
                .message("Connexion réussie")
                .build();
    }
}
