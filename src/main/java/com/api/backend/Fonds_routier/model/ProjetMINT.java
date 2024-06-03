package com.api.backend.Fonds_routier.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class ProjetMINT extends Projet{

    private String mission;
    @Column(columnDefinition = "TEXT")
    private String objectif;
    private String allotissement;
    @NotNull
    private String ordonnateur;

}
