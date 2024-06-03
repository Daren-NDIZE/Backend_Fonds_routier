package com.api.backend.Fonds_routier.model;

import com.api.backend.Fonds_routier.enums.ProgrammeStatut;
import com.api.backend.Fonds_routier.enums.ProgrammeType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class Programme implements Cloneable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull
    private String intitule;
    @NotNull
    private int annee;
    @NotNull
    private long budget;
    private long prevision;

    @Enumerated(EnumType.STRING) @NotNull
    private ProgrammeType type;

    @Enumerated(EnumType.STRING) @NotNull
    private ProgrammeStatut statut;
    @NotNull
    private String ordonnateur;
    @Column(columnDefinition = "LONGTEXT")
    private String observation;
    private String url_resolution;
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "programme")
    private List<Projet> projetList;


    public Object clone() throws CloneNotSupportedException{
        return super.clone();
    }
}
