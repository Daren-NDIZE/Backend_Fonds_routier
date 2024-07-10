package com.api.backend.Fonds_routier.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordDTO {

    private String password;
    private String newPassword;
    private String confirmPassword;

}
