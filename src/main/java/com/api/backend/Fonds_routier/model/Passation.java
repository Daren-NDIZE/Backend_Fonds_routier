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
public class Passation implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Temporal(TemporalType.DATE)
    private Date date;
    @NotNull
    private Date dateOs;
    @NotNull
    private String numeroMarche;
    @NotNull
    private String contractualisation;
    @ManyToOne @JsonIgnore
    private Projet projet;

    public Object clone() throws CloneNotSupportedException{
        return super.clone();
    }

}
