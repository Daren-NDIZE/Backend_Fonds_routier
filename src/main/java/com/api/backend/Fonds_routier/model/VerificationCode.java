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
public class VerificationCode {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull
    private int code;
    @NotNull
    private Date expiredAt;
    @ManyToOne @JsonIgnore
    private Utilisateur utilisateur;
}
