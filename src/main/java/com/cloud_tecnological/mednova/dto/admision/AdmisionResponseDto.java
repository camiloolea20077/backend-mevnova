package com.cloud_tecnological.mednova.dto.admision;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class AdmisionResponseDto {
    private Long id;
    private Long empresa_id;
    private Long sede_id;
    private String numero_admision;
    private Long paciente_id;
    private String patient_name;
    private String document_number;
    private Long tipo_admision_id;
    private String admission_type_name;
    private Long estado_admision_id;
    private String status_name;
    private String status_code;
    private Long origen_atencion_id;
    private String care_origin_name;
    private Long pagador_id;
    private String payer_name;
    private Long contrato_id;
    private Long acompanante_id;
    private String motivo_ingreso;
    private LocalDateTime fecha_admision;
    private LocalDateTime fecha_egreso;
    private String tipo_egreso;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime created_at;
    private Long attention_id;
    private String attention_status_code;
    private String nivel_triage;
}
