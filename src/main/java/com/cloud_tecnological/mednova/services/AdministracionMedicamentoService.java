package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdministerDoseRequestDto;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdministracionMedicamentoFilterParams;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdministracionMedicamentoResponseDto;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdministracionMedicamentoTableDto;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdverseReactionRequestDto;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.OmitDoseRequestDto;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.ScheduleAdministracionRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public interface AdministracionMedicamentoService {

    /** CA1: programar las dosis a partir de un detalle_prescripcion (intervalo y total). */
    List<AdministracionMedicamentoResponseDto> schedule(ScheduleAdministracionRequestDto request);

    /** CA2: registrar la administración real con trazabilidad de lote. */
    AdministracionMedicamentoResponseDto administer(Long id, AdministerDoseRequestDto request);

    /** CA3: marcar como OMITIDA (o RECHAZADA) con motivo. */
    AdministracionMedicamentoResponseDto omit(Long id, OmitDoseRequestDto request);

    /** Suspender una dosis programada. */
    AdministracionMedicamentoResponseDto suspend(Long id, String reason);

    /** CA4: registrar reacción adversa sobre una dosis administrada. */
    AdministracionMedicamentoResponseDto registerAdverseReaction(Long id, AdverseReactionRequestDto request);

    AdministracionMedicamentoResponseDto findById(Long id);

    PageImpl<AdministracionMedicamentoTableDto> list(PageableDto<AdministracionMedicamentoFilterParams> pageable);

    void softDelete(Long id);
}
