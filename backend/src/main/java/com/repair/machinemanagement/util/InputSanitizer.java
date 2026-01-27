package com.repair.machinemanagement.util;

/**
 * Utilitaire pour la sanitisation des entrées utilisateur
 * Protection contre les injections XSS et autres attaques
 */
public class InputSanitizer {
    
    /**
     * Nettoie une chaîne de caractères en échappant les caractères HTML dangereux
     * Protection contre XSS (Cross-Site Scripting)
     */
    public static String sanitizeHtml(String input) {
        if (input == null) {
            return null;
        }
        
        // L'ampersand doit être remplacé en dernier pour éviter le double-encodage
        return input
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;")
            .replace("&", "&amp;");
    }
    
    /**
     * Nettoie une chaîne SQL en supprimant les caractères potentiellement dangereux
     * Note: Utiliser de préférence les requêtes préparées avec JPA
     */
    public static String sanitizeSql(String input) {
        if (input == null) {
            return null;
        }
        
        // Supprimer les caractères SQL dangereux
        return input
            .replace("'", "''")
            .replace(";", "")
            .replace("--", "")
            .replace("/*", "")
            .replace("*/", "")
            .replace("xp_", "")
            .replace("sp_", "");
    }
    
    /**
     * Valide et nettoie un email
     */
    public static String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }
        
        // Convertir en minuscules et supprimer les espaces
        email = email.toLowerCase().trim();
        
        // Vérifier le format basique (regex simple) - déjà en minuscules
        if (!email.matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$")) {
            throw new IllegalArgumentException("Email invalide");
        }
        
        return email;
    }
    
    /**
     * Valide et nettoie un numéro de téléphone
     */
    public static String sanitizePhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }
        
        // Supprimer tous les caractères non numériques sauf le +
        phone = phone.replaceAll("[^0-9+]", "");
        
        // Vérifier la longueur
        if (phone.length() < 8 || phone.length() > 15) {
            throw new IllegalArgumentException("Numéro de téléphone invalide");
        }
        
        return phone;
    }
    
    /**
     * Nettoie une URL en vérifiant qu'elle est sûre
     */
    public static String sanitizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        url = url.trim();
        
        // Vérifier que l'URL commence par http:// ou https://
        if (!url.matches("^https?://.*")) {
            throw new IllegalArgumentException("URL invalide - doit commencer par http:// ou https://");
        }
        
        // Bloquer les protocoles dangereux
        if (url.toLowerCase().startsWith("javascript:") || 
            url.toLowerCase().startsWith("data:") ||
            url.toLowerCase().startsWith("vbscript:")) {
            throw new IllegalArgumentException("Protocole URL non autorisé");
        }
        
        return url;
    }
    
    /**
     * Limite la longueur d'une chaîne de caractères
     */
    public static String truncate(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        
        if (input.length() <= maxLength) {
            return input;
        }
        
        return input.substring(0, maxLength);
    }
}
