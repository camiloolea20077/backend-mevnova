package com.cloud_tecnological.mednova.dto.historiaclinica;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class HCHeaderDto {

    private Long patientId;
    private String documentNumber;
    private String documentType;
    private String fullName;
    private String sex;
    private LocalDate birthDate;
    private Integer ageYears;
    private String bloodGroup;
    private String rhFactor;
    private String careGroup;
    private String knownAllergies;

    /** CA4: alergias destacadas (descripciones de antecedentes ALERGICO activos). */
    private List<String> highlightedAllergies;
    private boolean hasAllergies;
}
