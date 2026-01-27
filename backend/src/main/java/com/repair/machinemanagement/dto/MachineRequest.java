package com.repair.machinemanagement.dto;

import com.repair.machinemanagement.entity.Machine;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MachineRequest {
    @NotBlank(message = "Marque est obligatoire")
    @Size(min = 2, max = 100, message = "La marque doit contenir entre 2 et 100 caractères")
    private String marque;
    
    @NotBlank(message = "Modèle est obligatoire")
    @Size(min = 2, max = 100, message = "Le modèle doit contenir entre 2 et 100 caractères")
    private String modele;
    
    @Size(max = 100, message = "Le numéro de série ne doit pas dépasser 100 caractères")
    private String numeroSerie;
    
    @NotBlank(message = "Défaut est obligatoire")
    @Size(min = 5, max = 1000, message = "La description du défaut doit contenir entre 5 et 1000 caractères")
    private String defaut;
    
    @Size(max = 500, message = "L'URL de la photo ne doit pas dépasser 500 caractères")
    private String photoUrl;
    
    @NotNull(message = "Date de rendez-vous est obligatoire")
    @FutureOrPresent(message = "La date de rendez-vous ne peut pas être dans le passé")
    private LocalDate rendezVous;
    
    @DecimalMin(value = "0.0", message = "Le montant ne peut pas être négatif")
    private Double montant;
    
    private Boolean paye;
    
    @Size(max = 1000, message = "La remarque du technicien ne doit pas dépasser 1000 caractères")
    private String remarqueTechnicien;
    
    @NotNull(message = "Client est obligatoire")
    @Positive(message = "L'ID du client doit être un nombre positif")
    private Long clientId;
    
    @NotNull(message = "Secrétaire est obligatoire")
    @Positive(message = "L'ID du secrétaire doit être un nombre positif")
    private Long secretaireId;
    
    @Positive(message = "L'ID du technicien doit être un nombre positif")
    private Long technicienId;
    
    private Machine.Statut statut;
}
