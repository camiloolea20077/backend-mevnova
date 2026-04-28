package com.cloud_tecnological.mednova.dto.cobrorule;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ServiceExemptionTableDto {

    private Long id;
    private Long servicio_salud_id;
    private String service_name;
    private String tipo_cobro;
    private String motivo_exencion;
    private LocalDate vigencia_desde;
    private LocalDate vigencia_hasta;
    private Boolean activo;
    private Long total_rows;
}
