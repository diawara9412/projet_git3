package com.repair.machinemanagement.service;

import com.repair.machinemanagement.dto.ClientLoginRequest;
import com.repair.machinemanagement.dto.ClientLoginResponse;
import com.repair.machinemanagement.entity.Client;
import com.repair.machinemanagement.repository.ClientRepository;
import com.repair.machinemanagement.security.ClientDetailsImpl;
import com.repair.machinemanagement.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientAuthServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private ClientAuthService clientAuthService;

    private Client activeClient;
    private Client inactiveClient;
    private ClientLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        activeClient = Client.builder()
                .id(1L)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean@test.com")
                .identifiant("CLT-00001")
                .password("$2a$10$encodedPassword")
                .active(true)
                .build();

        inactiveClient = Client.builder()
                .id(2L)
                .nom("Martin")
                .prenom("Marie")
                .email("marie@test.com")
                .identifiant("CLT-00002")
                .password("$2a$10$encodedPassword")
                .active(false)
                .build();

        loginRequest = new ClientLoginRequest();
        loginRequest.setIdentifiant("CLT-00001");
        loginRequest.setPassword("password123");
    }

    @Test
    void testLoginSuccess() {
        when(clientRepository.findByEmailOrIdentifiant(anyString(), anyString()))
                .thenReturn(Optional.of(activeClient));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenProvider.generateClientToken(any(ClientDetailsImpl.class)))
                .thenReturn("jwt-token");

        ClientLoginResponse response = clientAuthService.login(loginRequest);

        assertNotNull(response);
        assertEquals("CLT-00001", response.getIdentifiant());
        assertEquals("Dupont", response.getNom());
        assertEquals("CLIENT", response.getRole());
        assertEquals("jwt-token", response.getToken());
        verify(clientRepository).findByEmailOrIdentifiant("CLT-00001", "CLT-00001");
        verify(passwordEncoder).matches("password123", "$2a$10$encodedPassword");
    }

    @Test
    void testLoginWithEmail() {
        loginRequest.setIdentifiant("jean@test.com");
        
        when(clientRepository.findByEmailOrIdentifiant(anyString(), anyString()))
                .thenReturn(Optional.of(activeClient));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenProvider.generateClientToken(any(ClientDetailsImpl.class)))
                .thenReturn("jwt-token");

        ClientLoginResponse response = clientAuthService.login(loginRequest);

        assertNotNull(response);
        assertEquals("CLT-00001", response.getIdentifiant());
        verify(clientRepository).findByEmailOrIdentifiant("jean@test.com", "jean@test.com");
    }

    @Test
    void testLoginClientNotFound() {
        when(clientRepository.findByEmailOrIdentifiant(anyString(), anyString()))
                .thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> clientAuthService.login(loginRequest)
        );

        assertEquals("Identifiant ou mot de passe incorrect", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testLoginInactiveAccount() {
        when(clientRepository.findByEmailOrIdentifiant(anyString(), anyString()))
                .thenReturn(Optional.of(inactiveClient));

        DisabledException exception = assertThrows(
                DisabledException.class,
                () -> clientAuthService.login(loginRequest)
        );

        assertEquals("Votre compte a été désactivé. Contactez l'administration.", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testLoginIncorrectPassword() {
        when(clientRepository.findByEmailOrIdentifiant(anyString(), anyString()))
                .thenReturn(Optional.of(activeClient));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> clientAuthService.login(loginRequest)
        );

        assertEquals("Identifiant ou mot de passe incorrect", exception.getMessage());
        verify(tokenProvider, never()).generateClientToken(any());
    }
}
