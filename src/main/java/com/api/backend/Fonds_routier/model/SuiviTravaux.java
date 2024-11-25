package com.api.backend.Fonds_routier.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class SuiviTravaux implements Cloneable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Temporal(TemporalType.DATE)
    private Date date;
    @NotNull
    private double tauxAvancement;
    @NotNull
    private double tauxConsommation;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String proposition;
    @ManyToOne @JsonIgnore
    private Projet projet;

    public Object clone() throws CloneNotSupportedException{
        return super.clone();
    }
}
