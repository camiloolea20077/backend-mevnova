package com.cloud_tecnological.mednova.dto.cobrorule;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ServiceExemptionResponseDto {

    private Long id;
    private Long empresa_id;
    private Long servicio_salud_id;
    private String service_name;
    private String tipo_cobro;
    private String motivo_exencion;
    private LocalDate vigencia_desde;
    private LocalDate vigencia_hasta;
    private Boolean activo;
    private LocalDateTime created_at;
}
