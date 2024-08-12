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

    @Column(columnDefinition = "TEXT")
    private String mission;
    @Column(columnDefinition = "TEXT")
    private String objectif;
    @Column(columnDefinition = "TEXT")
    private String allotissement;
    @NotNull
    private String ordonnateur;

}
