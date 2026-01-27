package com.repair.machinemanagement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ClientRequest {
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
    
    @NotBlank(message = "Email est obligatoire pour créer un compte")
    @Email(message = "Email invalide")
    @Size(max = 255, message = "L'email ne doit pas dépasser 255 caractères")
    private String email;
    
    @Size(max = 500, message = "Le champ 'autres' ne doit pas dépasser 500 caractères")
    private String autres;
    
    // Optionnel : si true, envoie les identifiants par email
    private Boolean sendCredentials = true;
}
