package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.auditoria.AuditoriaFilterDto;
import com.cloud_tecnological.mednova.dto.auditoria.AuditoriaResponseDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface AuditoriaService {

    AuditoriaResponseDto findById(Long id);

    PageImpl<AuditoriaResponseDto> listAuditoria(PageableDto<AuditoriaFilterDto> pageable);
}
