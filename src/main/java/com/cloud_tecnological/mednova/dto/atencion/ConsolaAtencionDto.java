package com.cloud_tecnological.mednova.dto.atencion;

import com.cloud_tecnological.mednova.dto.diagnostico.DiagnosticoResponseDto;
import com.cloud_tecnological.mednova.dto.ordenclinica.OrdenClinicaResponseDto;
import com.cloud_tecnological.mednova.dto.prescripcion.PrescripcionResponseDto;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ConsolaAtencionDto {

    // Atencion
    private Long id;
    private Long admision_id;
    private String numero_atencion;
    private String numero_admision;
    private Long estado_atencion_id;
    private String status_code;
    private String status_name;
    private String nivel_triage;
    private String conducta;
    private LocalDateTime fecha_inicio;
    private LocalDateTime fecha_cierre;

    // Paciente
    private Long paciente_id;
    private String patient_name;
    private String document_number;
    private String document_type;
    private String sex;
    private Integer age;
    private String alergias;
    private String observaciones_clinicas;

    // Pagador
    private Long pagador_id;
    private String payer_name;
    private Long contrato_id;

    // Signos vitales
    private Integer tension_sistolica;
    private Integer tension_diastolica;
    private Integer frecuencia_cardiaca;
    private Integer frecuencia_respiratoria;
    private BigDecimal temperatura;
    private Integer saturacion_oxigeno;
    private BigDecimal peso;
    private BigDecimal talla;
    private BigDecimal glucometria;

    // Notas clínicas
    private String motivo_consulta;
    private String enfermedad_actual;
    private String antecedentes;
    private String examen_fisico;
    private String analisis;
    private String plan;
    private String observaciones;

    // Listas (asignadas manualmente en el servicio)
    private List<DiagnosticoResponseDto> diagnosticos;
    private List<OrdenClinicaResponseDto> ordenes;
    private List<PrescripcionResponseDto> prescripciones;
}
