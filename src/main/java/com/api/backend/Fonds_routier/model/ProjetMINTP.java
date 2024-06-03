package com.api.backend.Fonds_routier.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class ProjetMINTP extends Projet{

    @Column(columnDefinition = "TEXT") @NotNull
    private String projet;
    private String code_route;
    private float lineaire_route;
    private float lineaire_oa;
    private String departement;
    private String commune;
    @NotNull
    private String categorie;
    @NotNull
    private String type_travaux;

}
