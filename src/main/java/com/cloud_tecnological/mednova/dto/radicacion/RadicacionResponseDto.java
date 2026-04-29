package com.cloud_tecnological.mednova.dto.radicacion;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class RadicacionResponseDto {
    private Long id;
    private Long empresa_id;
    private Long sede_id;
    private Long factura_id;
    private String invoice_number;
    private Long pagador_id;
    private String payer_name;
    private Long estado_radicacion_id;
    private String estado_code;
    private String estado_name;
    private String numero_radicado;
    private LocalDate fecha_radicacion;
    private LocalDate fecha_limite_respuesta;
    private LocalDate fecha_respuesta;
    private String soporte_url;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime created_at;
}
