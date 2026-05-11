package com.cloud_tecnological.mednova.dto.historiaclinica;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class HCMedicationDto {

    private List<String> prescriptions;
    private List<String> dispensations;
    /** Resumen del MAR: total programadas, administradas, omitidas. */
    private Long marProgrammed;
    private Long marAdministered;
    private Long marOmitted;
    private LocalDateTime lastAdministrationAt;
}
