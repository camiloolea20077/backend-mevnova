package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.plancuidados.ChangePlanStatusRequestDto;
import com.cloud_tecnological.mednova.dto.plancuidados.CreatePlanCuidadosRequestDto;
import com.cloud_tecnological.mednova.dto.plancuidados.PlanCuidadosFilterParams;
import com.cloud_tecnological.mednova.dto.plancuidados.PlanCuidadosResponseDto;
import com.cloud_tecnological.mednova.dto.plancuidados.PlanCuidadosTableDto;
import com.cloud_tecnological.mednova.dto.plancuidados.UpdatePlanCuidadosRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface PlanCuidadosService {

    PlanCuidadosResponseDto create(CreatePlanCuidadosRequestDto request);

    PlanCuidadosResponseDto update(Long id, UpdatePlanCuidadosRequestDto request);

    PlanCuidadosResponseDto changeStatus(Long id, ChangePlanStatusRequestDto request);

    PlanCuidadosResponseDto findById(Long id);

    PageImpl<PlanCuidadosTableDto> list(PageableDto<PlanCuidadosFilterParams> pageable);

    PlanCuidadosResponseDto setActive(Long id, boolean active);

    void softDelete(Long id);
}
