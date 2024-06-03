package com.api.backend.Fonds_routier.DTO;

import com.api.backend.Fonds_routier.enums.ProgrammeStatut;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DecisionDTO {

    private ProgrammeStatut statut;
    private String observation;
    private MultipartFile file;

}
