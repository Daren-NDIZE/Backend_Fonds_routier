package com.api.backend.Fonds_routier.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class UserDTO {

    private String nom;
    private String prenom;
    private String username;
    private long telephone;
    private String email;
    private String roleName;
    private String password;
}
