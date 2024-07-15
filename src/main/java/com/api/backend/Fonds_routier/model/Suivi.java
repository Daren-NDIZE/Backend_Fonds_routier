package com.api.backend.Fonds_routier.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class Suivi implements Cloneable{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long engagement;
    @NotNull
    private String statut;
    @Column(columnDefinition = "TEXT")
    private String motif;
    @OneToOne(mappedBy = "suivi") @JsonIgnore
    private Projet projet;

    public Object clone() throws CloneNotSupportedException{
        return super.clone();
    }
}
