package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.cobrorule.*;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface CobroRuleService {
    CobroRuleResponseDto create(CreateCobroRuleRequestDto dto);
    CobroRuleResponseDto findById(Long id);
    Boolean update(Long id, UpdateCobroRuleRequestDto dto);
    Boolean delete(Long id);
    PageImpl<CobroRuleTableDto> listActive(PageableDto<?> request);

    ServiceExemptionResponseDto createExemption(CreateServiceExemptionRequestDto dto);
    ServiceExemptionResponseDto findExemptionById(Long id);
    Boolean deleteExemption(Long id);
    PageImpl<ServiceExemptionTableDto> listExemptions(PageableDto<?> request);
}
