package com.cloud_tecnological.mednova.dto.paciente;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class PacienteTableDto {

    private Long id;
    private Long thirdPartyId;
    private String fullName;
    private String documentTypeCode;
    private String documentNumber;
    private LocalDate birthDate;
    private String bloodGroupName;
    private String rhFactorName;
    private Boolean active;
}
