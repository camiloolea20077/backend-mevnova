package com.cloud_tecnological.mednova.dto.atencion;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ColaUrgenciasDto {
    private Long admision_id;
    private Long attention_id;
    private String numero_admision;
    private String patient_name;
    private String document_number;
    private String nivel_triage;
    private LocalDateTime fecha_admision;
    private Long wait_minutes;
    private Boolean beyond_sla;
    private Long sla_minutes;
    private String attention_status_code;
    private String attention_status_name;
    private String motivo_consulta;
}
