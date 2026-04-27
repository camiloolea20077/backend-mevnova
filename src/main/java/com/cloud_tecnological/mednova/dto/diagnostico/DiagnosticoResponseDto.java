package com.cloud_tecnological.mednova.dto.diagnostico;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class DiagnosticoResponseDto {
    private Long id;
    private Long atencion_id;
    private Long empresa_id;
    private Long catalogo_diagnostico_id;
    private String diagnostico_codigo;
    private String diagnostico_nombre;
    private String capitulo;
    private String tipo_diagnostico;
    private Boolean es_confirmado;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime created_at;
}
