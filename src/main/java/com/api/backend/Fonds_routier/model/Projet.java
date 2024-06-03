package com.api.backend.Fonds_routier.model;

import com.api.backend.Fonds_routier.enums.Region;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class Projet implements Cloneable {

    @Id @GeneratedValue(strategy = GenerationType.TABLE)
    private long id;
    @Enumerated(EnumType.STRING) @NotNull
    private Region region;
    @NotNull
    private long ttc;
    private long budget_anterieur;
    @NotNull
    private long budget_n;
    private long budget_n1;
    private long budget_n2;
    @Column(columnDefinition = "TEXT")
    private String observation;
    private String prestataire;
    private String bordereau;
    private String financement;
    @OneToOne
    private Suivi suivi;
    @ManyToOne @JsonIgnore
    private Programme programme;

    public Object clone() throws CloneNotSupportedException{
        return super.clone();
    }
}
