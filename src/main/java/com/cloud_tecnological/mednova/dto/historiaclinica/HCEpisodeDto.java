package com.cloud_tecnological.mednova.dto.historiaclinica;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class HCEpisodeDto {

    private Long admissionId;
    private String admissionNumber;
    private LocalDateTime admissionDate;
    private LocalDateTime dischargeDate;
    private String dischargeType;
    private Integer encounterCount;
    private List<String> diagnoses;
}
