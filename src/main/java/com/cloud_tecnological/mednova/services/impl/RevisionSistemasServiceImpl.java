package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.revision.CreateRevisionSistemasRequestDto;
import com.cloud_tecnological.mednova.dto.revision.RevisionSistemasFilterParams;
import com.cloud_tecnological.mednova.dto.revision.RevisionSistemasResponseDto;
import com.cloud_tecnological.mednova.dto.revision.RevisionSistemasTableDto;
import com.cloud_tecnological.mednova.dto.revision.UpdateRevisionSistemasRequestDto;
import com.cloud_tecnological.mednova.entity.RevisionSistemasEntity;
import com.cloud_tecnological.mednova.repositories.revision.RevisionSistemasJpaRepository;
import com.cloud_tecnological.mednova.repositories.revision.RevisionSistemasQueryRepository;
import com.cloud_tecnological.mednova.services.RevisionSistemasService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RevisionSistemasServiceImpl implements RevisionSistemasService {

    private final RevisionSistemasJpaRepository jpa;
    private final RevisionSistemasQueryRepository query;

    public RevisionSistemasServiceImpl(RevisionSistemasJpaRepository jpa,
                                       RevisionSistemasQueryRepository query) {
        this.jpa   = jpa;
        this.query = query;
    }

    @Override
    @Transactional
    public RevisionSistemasResponseDto create(CreateRevisionSistemasRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        validateAtencion(empresa_id, request.getEncounterId());

        boolean sinAlteracion = request.getWithoutAlteration() == null || request.getWithoutAlteration();
        validateAlterationFindingsConsistency(sinAlteracion, request.getFindings());
        validateUniqueReviewPerSystem(empresa_id, request.getEncounterId(), request.getSystem(), null);

        RevisionSistemasEntity entity = new RevisionSistemasEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setAtencion_id(request.getEncounterId());
        entity.setSistema(request.getSystem());
        entity.setSin_alteracion(sinAlteracion);
        entity.setHallazgos(request.getFindings());
        entity.setUsuario_creacion(usuario_id);
        RevisionSistemasEntity saved = jpa.save(entity);

        return query.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la revisión por sistemas creada"));
    }

    @Override
    @Transactional
    public RevisionSistemasResponseDto update(Long id, UpdateRevisionSistemasRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();

        RevisionSistemasEntity entity = getValidEntity(id, empresa_id);

        boolean sinAlteracion = request.getWithoutAlteration() == null
                ? entity.getSin_alteracion()
                : request.getWithoutAlteration();
        validateAlterationFindingsConsistency(sinAlteracion, request.getFindings());
        validateUniqueReviewPerSystem(empresa_id, entity.getAtencion_id(), request.getSystem(), id);

        entity.setSistema(request.getSystem());
        entity.setSin_alteracion(sinAlteracion);
        entity.setHallazgos(request.getFindings());
        jpa.save(entity);

        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la revisión por sistemas actualizada"));
    }

    @Override
    public RevisionSistemasResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Revisión por sistemas no encontrada"));
    }

    @Override
    public PageImpl<RevisionSistemasTableDto> list(PageableDto<RevisionSistemasFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return query.listRevisiones(pageable, empresa_id);
    }

    @Override
    @Transactional
    public RevisionSistemasResponseDto setActive(Long id, boolean active) {
        Long empresa_id = TenantContext.getEmpresaId();

        RevisionSistemasEntity entity = getValidEntity(id, empresa_id);
        entity.setActivo(active);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la revisión por sistemas"));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();

        RevisionSistemasEntity entity = getValidEntity(id, empresa_id);
        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        jpa.save(entity);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private RevisionSistemasEntity getValidEntity(Long id, Long empresa_id) {
        RevisionSistemasEntity entity = jpa.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Revisión por sistemas no encontrada"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Revisión por sistemas no encontrada");
        }
        return entity;
    }

    private void validateAtencion(Long empresa_id, Long atencion_id) {
        if (!query.atencionExistsInEmpresa(atencion_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada");
        }
    }

    private void validateAlterationFindingsConsistency(boolean sinAlteracion, String hallazgos) {
        if (!sinAlteracion && (hallazgos == null || hallazgos.isBlank())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "Debe registrar hallazgos cuando el sistema presenta alteración");
        }
    }

    private void validateUniqueReviewPerSystem(Long empresa_id, Long atencion_id,
                                               String sistema, Long excludeId) {
        if (query.reviewExistsForAtencionAndSystem(atencion_id, sistema, empresa_id, excludeId)) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Ya existe una revisión del sistema " + sistema + " para esta atención");
        }
    }
}
