package com.api.backend.Fonds_routier.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class Payement {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String decompte;
    private double m_TTC;
    private double air;
    private double nap;
    private double m_TVA;
    private double m_HTVA;
    @Temporal(TemporalType.DATE)
    private Date date;
    @Column(columnDefinition = "TEXT")
    private String observation;

    @ManyToOne @JsonIgnore
    private Projet projet;

}
