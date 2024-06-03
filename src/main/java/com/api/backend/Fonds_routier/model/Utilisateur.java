package com.api.backend.Fonds_routier.model;

import com.api.backend.Fonds_routier.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class Utilisateur {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
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
    @Enumerated(EnumType.STRING) @NotNull
    private UserRole role;
    @JsonIgnore @NotNull
    private String password;
}
