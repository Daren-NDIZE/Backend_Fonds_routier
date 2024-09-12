package com.api.backend.Fonds_routier.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class SyntheseDTO {

    public String type;
    public long[] prevision;
    public long[] engagement;
    public float[] lineaire;
}
