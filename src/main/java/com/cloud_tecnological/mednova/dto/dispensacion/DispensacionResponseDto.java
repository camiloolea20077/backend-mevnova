package com.cloud_tecnological.mednova.dto.dispensacion;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class DispensacionResponseDto {

    private Long id;
    private String dispensationNumber;

    private Long warehouseId;
    private String warehouseName;

    private Long prescriptionId;
    private String prescriptionNumber;

    private Long patientId;
    private String patientName;

    private Long dispensingProfessionalId;
    private String dispensingProfessionalName;

    private Long receivingProfessionalId;
    private String receivingProfessionalName;

    private LocalDateTime dispensationDate;
    private String state;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;

    private List<DetalleDispensacionResponseDto> items;
}
