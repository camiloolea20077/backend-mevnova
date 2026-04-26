package com.cloud_tecnological.mednova.dto.admision;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class AdmisionTableDto {
    private Long id;
    private String numero_admision;
    private String patient_name;
    private String document_number;
    private String admission_type_name;
    private String status_name;
    private String status_code;
    private String payer_name;
    private LocalDateTime fecha_admision;
    private String nivel_triage;
    private Long attention_id;
    private Long total_rows;
}
