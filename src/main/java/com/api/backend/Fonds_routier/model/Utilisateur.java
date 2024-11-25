package com.api.backend.Fonds_routier.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class Utilisateur {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull
    private String nom;
    @NotNull
    private String prenom;
    @NotNull
    private String username;
    @NotNull
    private long telephone;
    @NotNull
    private String email;
    @NotNull
    private String administration;
    @ManyToOne
    private Role role;
    @JsonIgnore @NotNull
    private String password;
    @JsonIgnore
    private Date connexionDate;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "utilisateur",cascade = CascadeType.REMOVE) @JsonIgnore
    private List<VerificationCode> verificationCode;


}
