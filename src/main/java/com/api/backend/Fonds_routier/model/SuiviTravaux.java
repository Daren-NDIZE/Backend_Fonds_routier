package com.api.backend.Fonds_routier.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class SuiviTravaux {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Temporal(TemporalType.DATE)
    private Date date;
    private double tauxAvancement;
    private double tauxConsommation;
    @Column(columnDefinition = "LONGTEXT")
    private String description;
    @ManyToOne @JsonIgnore
    private Projet projet;
}
