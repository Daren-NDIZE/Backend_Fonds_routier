package com.api.backend.Fonds_routier.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResLoginDTO {

   private boolean authenticate;
   private String token;



}
