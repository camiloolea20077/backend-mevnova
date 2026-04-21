package com.cloud_tecnological.mednova.dto.serviciohabilitado;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ServicioHabilitadoTableDto {

    private Long id;
    private String serviceCode;
    private String serviceName;
    private String sedeName;
    private String modality;
    private String complexity;
    private LocalDate enablementDate;
    private LocalDate expirationDate;
    private Boolean valid;
    private Boolean active;
}
