package com.repair.machinemanagement.controller;

import com.repair.machinemanagement.dto.ClientLoginRequest;
import com.repair.machinemanagement.dto.ClientLoginResponse;
import com.repair.machinemanagement.dto.LoginRequest;
import com.repair.machinemanagement.dto.LoginResponse;
import com.repair.machinemanagement.dto.PasswordChangeRequest;
import com.repair.machinemanagement.entity.Client;
import com.repair.machinemanagement.entity.Machine;
import com.repair.machinemanagement.service.AuthService;
import com.repair.machinemanagement.service.ClientAuthService;
import com.repair.machinemanagement.service.ClientService;
import com.repair.machinemanagement.service.MachineService;
import com.repair.machinemanagement.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(
    origins = {"http://localhost:3001", "http://localhost:3000", "http://localhost:5173", "http://localhost:5174"},
    allowCredentials = "true"
)
public class AuthController {

    private final AuthService authService;
    private final ClientAuthService clientAuthService;
    private final ClientService clientService;
    private final MachineService machineService;
    private final JwtTokenProvider jwtTokenProvider;
    
    private static final String JWT_COOKIE_NAME = "auth_token";
    private static final int COOKIE_MAX_AGE = 24 * 60 * 60; // 24 hours

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            LoginResponse loginResponse = authService.login(request);
            
            // Set JWT in HttpOnly cookie
            setAuthCookie(response, loginResponse.getToken());
            
            // Return response without token (token is in cookie)
            return ResponseEntity.ok(Map.of(
                "id", loginResponse.getId(),
                "nom", loginResponse.getNom(),
                "prenom", loginResponse.getPrenom(),
                "email", loginResponse.getEmail(),
                "role", loginResponse.getRole(),
                "message", loginResponse.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint de connexion pour les clients et admins (client-portal).
     * Accepte les connexions d'admins et de clients.
     */
    @PostMapping("/client/login")
    public ResponseEntity<?> clientLogin(@Valid @RequestBody ClientLoginRequest request, HttpServletResponse response) {
        try {
            // Tenter d'abord la connexion en tant que client
            try {
                ClientLoginResponse clientResponse = clientAuthService.login(request);
                
                // Set JWT in HttpOnly cookie
                setAuthCookie(response, clientResponse.getToken());
                
                return ResponseEntity.ok(Map.of(
                    "id", clientResponse.getId(),
                    "identifiant", clientResponse.getIdentifiant(),
                    "nom", clientResponse.getNom(),
                    "prenom", clientResponse.getPrenom(),
                    "email", clientResponse.getEmail(),
                    "role", clientResponse.getRole(),
                    "message", clientResponse.getMessage()
                ));
            } catch (Exception clientEx) {
                // Si la connexion client échoue, tenter avec les utilisateurs (admin/staff)
                try {
                    LoginRequest staffRequest = new LoginRequest();
                    staffRequest.setEmail(request.getIdentifiant());
                    staffRequest.setPassword(request.getPassword());
                    LoginResponse staffResponse = authService.login(staffRequest);
                    
                    // Set JWT in HttpOnly cookie
                    setAuthCookie(response, staffResponse.getToken());
                    
                    return ResponseEntity.ok(Map.of(
                        "id", staffResponse.getId(),
                        "identifiant", staffResponse.getEmail(),
                        "nom", staffResponse.getNom(),
                        "prenom", staffResponse.getPrenom(),
                        "email", staffResponse.getEmail(),
                        "role", staffResponse.getRole(),
                        "message", staffResponse.getMessage()
                    ));
                } catch (Exception staffEx) {
                    // Si les deux échouent, retourner l'erreur du client
                    throw clientEx;
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Logout endpoint - clears the auth cookie
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        clearAuthCookie(response);
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }
    
    /**
     * Verify authentication status and return user info
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verifyAuth(@CookieValue(value = JWT_COOKIE_NAME, required = false) String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("authenticated", false));
        }
        
        try {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            if (username != null && jwtTokenProvider.validateToken(token)) {
                // Token is valid, return user info from token
                Map<String, Object> userInfo = jwtTokenProvider.getUserInfoFromToken(token);
                userInfo.put("authenticated", true);
                return ResponseEntity.ok(userInfo);
            }
        } catch (Exception e) {
            // Token is invalid
        }
        
        return ResponseEntity.status(401).body(Map.of("authenticated", false));
    }

    /**
     * Récupère les informations du client connecté.
     */
    @GetMapping("/client/me")
    public ResponseEntity<?> getClientProfile(@RequestHeader("Authorization") String token) {
        try {
            // Le token est validé par le filtre JWT
            return ResponseEntity.ok(Map.of("message", "Profile récupéré"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupère les machines d'un client.
     */
    @GetMapping("/client/{clientId}/machines")
    public ResponseEntity<?> getClientMachines(@PathVariable Long clientId) {
        try {
            List<Machine> machines = machineService.getMachinesByClient(clientId);
            return ResponseEntity.ok(machines);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Change le mot de passe d'un client.
     */
    @PostMapping("/client/{clientId}/change-password")
    public ResponseEntity<?> changeClientPassword(
            @PathVariable Long clientId,
            @Valid @RequestBody PasswordChangeRequest request) {
        try {
            clientService.changePassword(clientId, request);
            return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupère les détails d'un client.
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getClientById(@PathVariable Long clientId) {
        try {
            Client client = clientService.getClientById(clientId);
            return ResponseEntity.ok(client);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupère tous les clients (pour les admins).
     */
    @GetMapping("/admin/clients")
    public ResponseEntity<?> getAllClients() {
        try {
            List<Client> clients = clientService.getAllClients();
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupère toutes les machines (pour les admins).
     */
    @GetMapping("/admin/machines")
    public ResponseEntity<?> getAllMachines() {
        try {
            List<Machine> machines = machineService.getAllMachines();
            return ResponseEntity.ok(machines);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Helper method to set authentication cookie
     * Note: In production, set cookie.setSecure(true) to require HTTPS
     */
    private void setAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // TODO: Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setAttribute("SameSite", "Lax"); // Protection CSRF
        response.addCookie(cookie);
    }
    
    /**
     * Helper method to clear authentication cookie
     */
    private void clearAuthCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
