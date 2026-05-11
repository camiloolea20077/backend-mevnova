package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.adjuntoclinico.AdjuntoClinicoFilterParams;
import com.cloud_tecnological.mednova.dto.adjuntoclinico.AdjuntoClinicoResponseDto;
import com.cloud_tecnological.mednova.dto.adjuntoclinico.AdjuntoClinicoTableDto;
import com.cloud_tecnological.mednova.dto.adjuntoclinico.CreateAdjuntoClinicoRequestDto;
import com.cloud_tecnological.mednova.dto.adjuntoclinico.UpdateAdjuntoClinicoRequestDto;
import com.cloud_tecnological.mednova.entity.AdjuntoClinicoEntity;
import com.cloud_tecnological.mednova.repositories.adjuntoclinico.AdjuntoClinicoJpaRepository;
import com.cloud_tecnological.mednova.repositories.adjuntoclinico.AdjuntoClinicoQueryRepository;
import com.cloud_tecnological.mednova.services.AdjuntoClinicoService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdjuntoClinicoServiceImpl implements AdjuntoClinicoService {

    private static final String PERMISO_CONFIDENCIAL = "HC_VER_ADJUNTOS_CONFIDENCIALES";

    private final AdjuntoClinicoJpaRepository jpa;
    private final AdjuntoClinicoQueryRepository query;

    public AdjuntoClinicoServiceImpl(AdjuntoClinicoJpaRepository jpa,
                                     AdjuntoClinicoQueryRepository query) {
        this.jpa = jpa;
        this.query = query;
    }

    @Override
    @Transactional
    public AdjuntoClinicoResponseDto create(CreateAdjuntoClinicoRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        validatePaciente(empresa_id, request.getPatientId());
        if (request.getEncounterId() != null) {
            validateAtencion(empresa_id, request.getEncounterId());
        }
        if (request.getUploadingProfessionalId() != null) {
            validateProfesional(empresa_id, request.getUploadingProfessionalId());
        }

        AdjuntoClinicoEntity entity = new AdjuntoClinicoEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setSede_id(sede_id);
        entity.setPaciente_id(request.getPatientId());
        entity.setAtencion_id(request.getEncounterId());
        entity.setTipo_documento(request.getDocumentType());
        entity.setNombre_archivo(request.getFileName());
        entity.setDescripcion(request.getDescription());
        entity.setUrl_archivo(request.getFileUrl());
        entity.setMime_type(request.getMimeType());
        entity.setTamano_bytes(request.getSizeBytes());
        entity.setProfesional_carga_id(request.getUploadingProfessionalId());
        entity.setFecha_documento(request.getDocumentDate());
        entity.setEs_confidencial(Boolean.TRUE.equals(request.getIsConfidential()));
        entity.setUsuario_creacion(usuario_id);
        AdjuntoClinicoEntity saved = jpa.save(entity);

        return query.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el adjunto creado"));
    }

    @Override
    @Transactional
    public AdjuntoClinicoResponseDto update(Long id, UpdateAdjuntoClinicoRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        AdjuntoClinicoEntity entity = getValidEntity(id, empresa_id);
        enforceConfidentialReadPermission(entity);

        entity.setTipo_documento(request.getDocumentType());
        entity.setNombre_archivo(request.getFileName());
        entity.setDescripcion(request.getDescription());
        entity.setFecha_documento(request.getDocumentDate());
        if (request.getIsConfidential() != null) {
            entity.setEs_confidencial(request.getIsConfidential());
        }
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el adjunto actualizado"));
    }

    @Override
    public AdjuntoClinicoResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        AdjuntoClinicoResponseDto dto = query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Adjunto no encontrado"));
        // CA3: si es confidencial y el usuario no tiene permiso → 404 (no revela existencia).
        if (Boolean.TRUE.equals(dto.getIsConfidential())
                && !TenantContext.hasPermission(PERMISO_CONFIDENCIAL)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Adjunto no encontrado");
        }
        return dto;
    }

    @Override
    public PageImpl<AdjuntoClinicoTableDto> list(PageableDto<AdjuntoClinicoFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        boolean canSeeConfidential = TenantContext.hasPermission(PERMISO_CONFIDENCIAL);
        return query.listAdjuntos(pageable, empresa_id, canSeeConfidential);
    }

    @Override
    @Transactional
    public AdjuntoClinicoResponseDto setConfidential(Long id, boolean confidential) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        AdjuntoClinicoEntity entity = getValidEntity(id, empresa_id);
        enforceConfidentialReadPermission(entity);

        entity.setEs_confidencial(confidential);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el adjunto"));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        AdjuntoClinicoEntity entity = getValidEntity(id, empresa_id);
        enforceConfidentialReadPermission(entity);

        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AdjuntoClinicoEntity getValidEntity(Long id, Long empresa_id) {
        AdjuntoClinicoEntity entity = jpa.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Adjunto no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Adjunto no encontrado");
        }
        return entity;
    }

    /** CA3: si el adjunto es confidencial y el usuario no tiene permiso, no debe poder operarlo. */
    private void enforceConfidentialReadPermission(AdjuntoClinicoEntity entity) {
        if (Boolean.TRUE.equals(entity.getEs_confidencial())
                && !TenantContext.hasPermission(PERMISO_CONFIDENCIAL)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Adjunto no encontrado");
        }
    }

    private void validatePaciente(Long empresa_id, Long paciente_id) {
        if (!query.pacienteExistsInEmpresa(paciente_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }
    }

    private void validateAtencion(Long empresa_id, Long atencion_id) {
        if (!query.atencionExistsInEmpresa(atencion_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada");
        }
    }

    private void validateProfesional(Long empresa_id, Long profesional_id) {
        if (!query.profesionalActivoInEmpresa(profesional_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Profesional no encontrado");
        }
    }
}
