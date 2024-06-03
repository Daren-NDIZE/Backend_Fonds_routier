package com.api.backend.Fonds_routier.DTO;

import com.api.backend.Fonds_routier.model.Projet;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data @AllArgsConstructor @NoArgsConstructor
public class SuiviDTO {

    private long engagement;
    private String statut;
    private String motif;
    private String prestataire;
    private MultipartFile file;

}
