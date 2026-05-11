package com.cloud_tecnological.mednova.dto.historiaclinica;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class HCSummaryDto {

    private List<String> activeDiagnoses;
    private List<String> habitualMedications;
    private LocalDateTime lastEncounterAt;
    private LocalDateTime nextAppointmentAt;
}
