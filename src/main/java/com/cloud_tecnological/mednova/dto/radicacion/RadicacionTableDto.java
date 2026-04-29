package com.cloud_tecnological.mednova.dto.radicacion;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RadicacionTableDto {
    private Long id;
    private String invoice_number;
    private String payer_name;
    private String estado_code;
    private String estado_name;
    private String numero_radicado;
    private LocalDate fecha_radicacion;
    private LocalDate fecha_limite_respuesta;
    private LocalDate fecha_respuesta;
    private Boolean activo;
    private Long total_rows;
}
