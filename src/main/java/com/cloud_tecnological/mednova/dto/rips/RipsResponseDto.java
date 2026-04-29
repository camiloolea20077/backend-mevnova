package com.cloud_tecnological.mednova.dto.rips;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RipsResponseDto {
    private Long id;
    private Long empresa_id;
    private Long factura_id;
    private String invoice_number;
    private Long pagador_id;
    private String payer_name;
    private LocalDateTime fecha_generacion;
    private String estado;
    private String version_norma;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime created_at;
}
