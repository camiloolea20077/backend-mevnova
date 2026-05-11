package com.cloud_tecnological.mednova.dto.notaenfermeria;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class NotaEnfermeriaFilterParams {

    /** Filtro por atención (consola hospitalización). */
    private Long encounterId;

    /** Filtro por paciente (cronología completa). */
    private Long patientId;

    /** Filtro por profesional. */
    private Long professionalId;

    /** Filtro por tipo (INGRESO, EVOLUCION, NOVEDAD, ENTREGA_TURNO, etc.). */
    private String noteType;

    /** Filtro por turno (MANANA, TARDE, NOCHE). */
    private String shift;

    /** Filtro por estado de firma. */
    private Boolean signed;

    /** Rango de fecha (inclusivo). */
    private LocalDate dateFrom;
    private LocalDate dateTo;

    /** Filtra solo notas activas (no soft-deleted lógico). */
    private Boolean onlyActive;
}
