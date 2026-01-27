package com.repair.machinemanagement.dto;

import com.repair.machinemanagement.entity.User;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank(message = "Nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;
    
    @NotBlank(message = "Prénom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String prenom;
    
    @NotBlank(message = "Adresse est obligatoire")
    @Size(min = 5, max = 255, message = "L'adresse doit contenir entre 5 et 255 caractères")
    private String adresse;
    
    @NotBlank(message = "Numéro est obligatoire")
    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Numéro de téléphone invalide")
    private String numero;
    
    @NotBlank(message = "Email est obligatoire")
    @Email(message = "Email invalide")
    @Size(max = 255, message = "L'email ne doit pas dépasser 255 caractères")
    private String email;
    
    @NotBlank(message = "Mot de passe est obligatoire")
    @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", 
             message = "Le mot de passe doit contenir au moins une majuscule, une minuscule et un chiffre")
    private String password;
    
    @NotNull(message = "Rôle est obligatoire")
    private User.Role role;
}
