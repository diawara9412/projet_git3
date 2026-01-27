package com.repair.machinemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la demande de changement de mot de passe.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeRequest {
    
    @NotBlank(message = "Ancien mot de passe est obligatoire")
    private String oldPassword;
    
    @NotBlank(message = "Nouveau mot de passe est obligatoire")
    @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", 
             message = "Le mot de passe doit contenir au moins une majuscule, une minuscule et un chiffre")
    private String newPassword;
    
    @NotBlank(message = "Confirmation du mot de passe est obligatoire")
    private String confirmPassword;
}
