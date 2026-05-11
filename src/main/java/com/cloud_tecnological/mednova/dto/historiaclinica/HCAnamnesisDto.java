package com.cloud_tecnological.mednova.dto.historiaclinica;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class HCAnamnesisDto {

    /** Agrupados por tipo (PATOLOGICO, ALERGICO, QUIRURGICO, etc.). */
    private Map<String, List<String>> personalHistoryByType;

    private List<String> familyHistory;

    /** Hábitos activos. */
    private List<String> habits;

    /** Vacunas aplicadas. */
    private List<String> vaccines;

    /** Medicación habitual activa. */
    private List<String> habitualMedications;
}
