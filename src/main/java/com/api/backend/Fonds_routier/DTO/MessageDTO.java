package com.api.backend.Fonds_routier.DTO;

import com.api.backend.Fonds_routier.enums.ProgrammeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {

    private String type;
    private String message;


}
