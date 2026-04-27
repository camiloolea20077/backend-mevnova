package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.recursofisico.CreateRecursoFisicoRequestDto;
import com.cloud_tecnological.mednova.dto.recursofisico.RecursoFisicoResponseDto;
import com.cloud_tecnological.mednova.dto.recursofisico.RecursoFisicoTableDto;
import com.cloud_tecnological.mednova.dto.recursofisico.UpdateRecursoFisicoRequestDto;
import com.cloud_tecnological.mednova.entity.RecursoFisicoEntity;
import com.cloud_tecnological.mednova.repositories.recursofisico.RecursoFisicoJpaRepository;
import com.cloud_tecnological.mednova.repositories.recursofisico.RecursoFisicoQueryRepository;
import com.cloud_tecnological.mednova.services.RecursoFisicoService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RecursoFisicoServiceImpl implements RecursoFisicoService {

    private final RecursoFisicoJpaRepository jpaRepository;
    private final RecursoFisicoQueryRepository queryRepository;

    public RecursoFisicoServiceImpl(RecursoFisicoJpaRepository jpaRepository,
                                    RecursoFisicoQueryRepository queryRepository) {
        this.jpaRepository   = jpaRepository;
        this.queryRepository = queryRepository;
    }

    @Override
    @Transactional
    public RecursoFisicoResponseDto create(CreateRecursoFisicoRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        if (queryRepository.existsByCodigo(dto.getCodigo(), sedeId, empresaId, null)) {
            throw new GlobalException(HttpStatus.CONFLICT,
                "Ya existe un recurso físico con el código '" + dto.getCodigo() + "' en esta sede");
        }

        RecursoFisicoEntity entity = new RecursoFisicoEntity();
        entity.setEmpresa_id(empresaId);
        entity.setSede_id(sedeId);
        entity.setCodigo(dto.getCodigo().toUpperCase());
        entity.setNombre(dto.getNombre());
        entity.setTipo_recurso(dto.getTipoRecurso());
        entity.setUbicacion(dto.getUbicacion());
        entity.setDescripcion(dto.getDescripcion());
        entity.setUsuario_creacion(usuarioId);

        RecursoFisicoEntity saved = jpaRepository.save(entity);

        return queryRepository.findActiveById(saved.getId(), empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el recurso"));
    }

    @Override
    public RecursoFisicoResponseDto findById(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        return queryRepository.findActiveById(id, empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Recurso físico no encontrado"));
    }

    @Override
    @Transactional
    public RecursoFisicoResponseDto update(Long id, UpdateRecursoFisicoRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        RecursoFisicoEntity entity = jpaRepository.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Recurso físico no encontrado"));

        validateTenant(entity, empresaId, sedeId);

        if (queryRepository.existsByCodigo(dto.getCodigo(), sedeId, empresaId, id)) {
            throw new GlobalException(HttpStatus.CONFLICT,
                "Ya existe un recurso físico con el código '" + dto.getCodigo() + "' en esta sede");
        }

        entity.setCodigo(dto.getCodigo().toUpperCase());
        entity.setNombre(dto.getNombre());
        entity.setTipo_recurso(dto.getTipoRecurso());
        entity.setUbicacion(dto.getUbicacion());
        entity.setDescripcion(dto.getDescripcion());
        if (dto.getActivo() != null) entity.setActivo(dto.getActivo());
        entity.setUsuario_modificacion(usuarioId);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el recurso"));
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        RecursoFisicoEntity entity = jpaRepository.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Recurso físico no encontrado"));

        validateTenant(entity, empresaId, sedeId);

        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuarioId);
        jpaRepository.save(entity);
        return true;
    }

    @Override
    public PageImpl<RecursoFisicoTableDto> listActivos(PageableDto<?> request) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        return queryRepository.listActivos(request, empresaId, sedeId);
    }

    private void validateTenant(RecursoFisicoEntity entity, Long empresaId, Long sedeId) {
        if (entity.getDeleted_at() != null
                || !entity.getEmpresa_id().equals(empresaId)
                || !entity.getSede_id().equals(sedeId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Recurso físico no encontrado");
        }
    }
}
