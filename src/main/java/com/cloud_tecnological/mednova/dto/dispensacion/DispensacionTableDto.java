package com.cloud_tecnological.mednova.dto.dispensacion;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class DispensacionTableDto {

    private Long id;
    private String dispensationNumber;
    private String prescriptionNumber;
    private String patientName;
    private String warehouseName;
    private String dispensingProfessionalName;
    private LocalDateTime dispensationDate;
    private String state;
    private Boolean active;
}
