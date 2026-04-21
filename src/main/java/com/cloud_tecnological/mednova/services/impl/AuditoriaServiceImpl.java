package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.auditoria.AuditoriaFilterDto;
import com.cloud_tecnological.mednova.dto.auditoria.AuditoriaResponseDto;
import com.cloud_tecnological.mednova.repositories.auditoria.AuditoriaQueryRepository;
import com.cloud_tecnological.mednova.services.AuditoriaService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaQueryRepository auditoriaQueryRepository;

    public AuditoriaServiceImpl(AuditoriaQueryRepository auditoriaQueryRepository) {
        this.auditoriaQueryRepository = auditoriaQueryRepository;
    }

    @Override
    public AuditoriaResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return auditoriaQueryRepository.findById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Registro de auditoría no encontrado"));
    }

    @Override
    public PageImpl<AuditoriaResponseDto> listAuditoria(PageableDto<AuditoriaFilterDto> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return auditoriaQueryRepository.listAuditoria(pageable, empresa_id);
    }
}
