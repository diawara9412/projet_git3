package com.repair.machinemanagement.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global des exceptions pour une meilleure sécurité et gestion des erreurs.
 * Empêche la fuite d'informations sensibles dans les messages d'erreur.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Gestion des erreurs de validation des entrées (Jakarta Validation).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Validation error: {}", errors);
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validation des données échouée");
        response.put("details", errors);
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Gestion des erreurs d'authentification.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(
            BadCredentialsException ex) {
        log.warn("Bad credentials attempt");
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "Identifiants invalides");
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Gestion des erreurs d'authentification générales.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(
            AuthenticationException ex) {
        log.warn("Authentication error: {}", ex.getMessage());
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "Erreur d'authentification");
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Gestion des erreurs d'accès refusé (autorisations).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(
            AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "Accès refusé. Vous n'avez pas les permissions nécessaires.");
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Gestion des erreurs RuntimeException génériques.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(
            RuntimeException ex) {
        log.error("Runtime error: {}", ex.getMessage());
        
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Gestion des erreurs générales non prévues.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(
            Exception ex) {
        log.error("Unexpected error: ", ex);
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "Une erreur inattendue s'est produite");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
