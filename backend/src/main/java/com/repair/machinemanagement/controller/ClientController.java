package com.repair.machinemanagement.controller;

import com.repair.machinemanagement.dto.ClientRequest;
import com.repair.machinemanagement.entity.Client;
import com.repair.machinemanagement.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173","http://localhost:3001"})
public class ClientController {
    
    private final ClientService clientService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETAIRE', 'TECHNICIEN')")
    public ResponseEntity<List<Client>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETAIRE', 'TECHNICIEN')")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETAIRE', 'TECHNICIEN')")
    public ResponseEntity<List<Client>> searchClients(@RequestParam String keyword) {
        return ResponseEntity.ok(clientService.searchClients(keyword));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETAIRE')")
    public ResponseEntity<?> createClient(@Valid @RequestBody ClientRequest request) {
        try {
            Client client = clientService.createClient(request);
            Map<String, Object> response = new HashMap<>();
            response.put("client", client);
            response.put("message", "Client créé avec succès. Un email avec les identifiants a été envoyé.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETAIRE')")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        try {
            Client client = clientService.updateClient(id, request);
            return ResponseEntity.ok(client);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Renvoie les identifiants de connexion au client par email.
     */
    @PostMapping("/{id}/resend-credentials")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETAIRE')")
    public ResponseEntity<?> resendCredentials(@PathVariable Long id) {
        try {
            clientService.resendCredentials(id);
            return ResponseEntity.ok(Map.of("message", "Identifiants renvoyés avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Active ou désactive un compte client.
     */
    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETAIRE')")
    public ResponseEntity<?> toggleClientStatus(@PathVariable Long id) {
        try {
            Client client = clientService.toggleClientStatus(id);
            String status = client.getActive() ? "activé" : "désactivé";
            return ResponseEntity.ok(Map.of(
                "client", client,
                "message", "Compte client " + status
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        try {
            clientService.deleteClient(id);
            return ResponseEntity.ok(Map.of("message", "Client supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
