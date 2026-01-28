package com.repair.machinemanagement.service;

import com.repair.machinemanagement.dto.ClientRequest;
import com.repair.machinemanagement.entity.Client;
import com.repair.machinemanagement.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ClientService clientService;

    private ClientRequest clientRequest;
    private Client testClient;

    @BeforeEach
    void setUp() {
        clientRequest = new ClientRequest();
        clientRequest.setNom("Dupont");
        clientRequest.setPrenom("Jean");
        clientRequest.setAdresse("123 Rue Test");
        clientRequest.setNumero("0612345678");
        clientRequest.setEmail("jean.dupont@test.com");
        clientRequest.setSendCredentials(true);

        testClient = Client.builder()
                .id(1L)
                .nom("Dupont")
                .prenom("Jean")
                .adresse("123 Rue Test")
                .numero("0612345678")
                .email("jean.dupont@test.com")
                .identifiant("CLT-00001")
                .password("encodedPassword")
                .active(true)
                .credentialsSent(false)
                .build();
    }

    @Test
    void testCreateClientSuccess() {
        when(clientRepository.existsByNumero(anyString())).thenReturn(false);
        when(clientRepository.existsByEmail(anyString())).thenReturn(false);
        when(clientRepository.existsByIdentifiant(anyString())).thenReturn(false);
        when(clientRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        doNothing().when(emailService).sendClientCredentials(any(Client.class), anyString());

        Client created = clientService.createClient(clientRequest);

        assertNotNull(created);
        assertEquals("Dupont", created.getNom());
        assertEquals("jean.dupont@test.com", created.getEmail());
        verify(clientRepository, times(2)).save(any(Client.class)); // Once for creation, once for credentialsSent
        verify(emailService).sendClientCredentials(any(Client.class), anyString());
    }

    @Test
    void testCreateClientEmailFailureDoesNotBlockCreation() {
        when(clientRepository.existsByNumero(anyString())).thenReturn(false);
        when(clientRepository.existsByEmail(anyString())).thenReturn(false);
        when(clientRepository.existsByIdentifiant(anyString())).thenReturn(false);
        when(clientRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        doThrow(new RuntimeException("Email service error"))
                .when(emailService).sendClientCredentials(any(Client.class), anyString());

        Client created = clientService.createClient(clientRequest);

        assertNotNull(created);
        assertEquals("Dupont", created.getNom());
        // credentialsSent should remain false when email fails
        assertFalse(created.getCredentialsSent());
        // Client is saved only once (initial creation), not again after failed email
        verify(clientRepository, times(1)).save(any(Client.class));
        verify(emailService).sendClientCredentials(any(Client.class), anyString());
    }

    @Test
    void testCreateClientDuplicateEmail() {
        when(clientRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> clientService.createClient(clientRequest));
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void testCreateClientDuplicateNumero() {
        when(clientRepository.existsByNumero(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> clientService.createClient(clientRequest));
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void testCreateClientWithoutSendingCredentials() {
        clientRequest.setSendCredentials(false);
        
        when(clientRepository.existsByNumero(anyString())).thenReturn(false);
        when(clientRepository.existsByEmail(anyString())).thenReturn(false);
        when(clientRepository.existsByIdentifiant(anyString())).thenReturn(false);
        when(clientRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        Client created = clientService.createClient(clientRequest);

        assertNotNull(created);
        verify(emailService, never()).sendClientCredentials(any(Client.class), anyString());
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void testGetClientById() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));

        Client found = clientService.getClientById(1L);

        assertNotNull(found);
        assertEquals("CLT-00001", found.getIdentifiant());
        verify(clientRepository).findById(1L);
    }

    @Test
    void testGetClientByIdNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> clientService.getClientById(99L));
    }
}
