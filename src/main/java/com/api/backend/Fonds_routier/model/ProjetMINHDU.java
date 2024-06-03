package com.api.backend.Fonds_routier.model;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class ProjetMINHDU extends Projet{

    @NotNull
    private String ville;
    @NotNull
    private String type_travaux;
    private String troçon;
    private float lineaire;
    @NotNull
    private String ordonnateur;

}
