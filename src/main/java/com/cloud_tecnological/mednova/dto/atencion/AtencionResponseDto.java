package com.cloud_tecnological.mednova.dto.atencion;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class AtencionResponseDto {
    private Long id;
    private Long admision_id;
    private String numero_atencion;
    private Long estado_atencion_id;
    private String status_code;
    private String status_name;
    private String nivel_triage;
    private String motivo_consulta;
    private LocalDateTime fecha_inicio;
    private LocalDateTime fecha_cierre;
    private Integer tension_sistolica;
    private Integer tension_diastolica;
    private Integer frecuencia_cardiaca;
    private Integer frecuencia_respiratoria;
    private BigDecimal temperatura;
    private Integer saturacion_oxigeno;
    private BigDecimal peso;
    private BigDecimal talla;
    private BigDecimal glucometria;
    private String observaciones;
    private LocalDateTime created_at;
}
