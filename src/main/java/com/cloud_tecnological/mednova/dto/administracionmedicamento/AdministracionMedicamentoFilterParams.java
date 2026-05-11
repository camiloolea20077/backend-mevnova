package com.cloud_tecnological.mednova.dto.administracionmedicamento;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AdministracionMedicamentoFilterParams {

    /** Filtro por atención (panel MAR de la hospitalización). */
    private Long encounterId;

    /** Filtro por paciente. */
    private Long patientId;

    /** Filtro por profesional registrador. */
    private Long professionalId;

    /** Filtro por detalle de prescripción (trazabilidad de un medicamento). */
    private Long prescriptionDetailId;

    /** Filtro por estado (PROGRAMADA, ADMINISTRADA, OMITIDA, RECHAZADA, SUSPENDIDA). */
    private String status;

    /** Solo dosis pendientes (estado = PROGRAMADA). */
    private Boolean onlyPending;

    /** Solo dosis con reacción adversa registrada. */
    private Boolean onlyWithAdverseReaction;

    /** Rango de fecha programada (inclusivo). */
    private LocalDate scheduledFrom;
    private LocalDate scheduledTo;

    /** Solo registros lógicamente activos. */
    private Boolean onlyActive;
}
