package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.lote.LoteFilterParams;
import com.cloud_tecnological.mednova.dto.lote.LoteResponseDto;
import com.cloud_tecnological.mednova.dto.lote.LoteTableDto;
import com.cloud_tecnological.mednova.dto.lote.UpdateLoteRequestDto;
import com.cloud_tecnological.mednova.entity.LoteEntity;
import com.cloud_tecnological.mednova.repositories.lote.LoteJpaRepository;
import com.cloud_tecnological.mednova.repositories.lote.LoteQueryRepository;
import com.cloud_tecnological.mednova.services.LoteService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoteServiceImpl implements LoteService {

    private final LoteJpaRepository   jpaRepository;
    private final LoteQueryRepository queryRepository;

    public LoteServiceImpl(LoteJpaRepository jpaRepository,
                           LoteQueryRepository queryRepository) {
        this.jpaRepository   = jpaRepository;
        this.queryRepository = queryRepository;
    }

    @Override
    public LoteResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Lote no encontrado"));
    }

    @Override
    public PageImpl<LoteTableDto> list(PageableDto<LoteFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.listLotes(pageable, empresa_id);
    }

    @Override
    @Transactional
    public LoteResponseDto update(Long id, UpdateLoteRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        LoteEntity entity = getValidEntity(id, empresa_id);

        // Solo se permiten cambios en datos NO críticos (registro INVIMA, observaciones).
        // numero_lote y fecha_vencimiento son inmutables (regla de negocio HU-FASE2-070).
        entity.setRegistro_invima(request.getInvimaRegister());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el lote actualizado"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        LoteEntity entity = getValidEntity(id, empresa_id);

        // Solo bloqueamos al INACTIVAR: lote con stock no se elimina/inactiva.
        if (Boolean.TRUE.equals(entity.getActivo()) && queryRepository.hasStock(id, empresa_id)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "No se puede inactivar un lote con stock disponible");
        }

        entity.setActivo(!Boolean.TRUE.equals(entity.getActivo()));
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);
        return entity.getActivo();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private LoteEntity getValidEntity(Long id, Long empresa_id) {
        LoteEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Lote no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Lote no encontrado");
        }
        return entity;
    }
}
