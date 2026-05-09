package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.antecedente.AntecedenteFamiliarFilterParams;
import com.cloud_tecnological.mednova.dto.antecedente.AntecedenteFamiliarResponseDto;
import com.cloud_tecnological.mednova.dto.antecedente.AntecedenteFamiliarTableDto;
import com.cloud_tecnological.mednova.dto.antecedente.CreateAntecedenteFamiliarRequestDto;
import com.cloud_tecnological.mednova.dto.antecedente.UpdateAntecedenteFamiliarRequestDto;
import com.cloud_tecnological.mednova.entity.AntecedenteFamiliarEntity;
import com.cloud_tecnological.mednova.repositories.antecedente.AntecedenteFamiliarJpaRepository;
import com.cloud_tecnological.mednova.repositories.antecedente.AntecedenteFamiliarQueryRepository;
import com.cloud_tecnological.mednova.services.AntecedenteFamiliarService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AntecedenteFamiliarServiceImpl implements AntecedenteFamiliarService {

    private final AntecedenteFamiliarJpaRepository jpa;
    private final AntecedenteFamiliarQueryRepository query;

    public AntecedenteFamiliarServiceImpl(AntecedenteFamiliarJpaRepository jpa,
                                          AntecedenteFamiliarQueryRepository query) {
        this.jpa   = jpa;
        this.query = query;
    }

    @Override
    @Transactional
    public AntecedenteFamiliarResponseDto create(CreateAntecedenteFamiliarRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        validatePaciente(empresa_id, request.getPatientId());
        validateCatalogoDiagnostico(request.getCatalogDiagnosisId());
        validateDeceasedConsistency(request.getIsDeceased(), request.getCauseOfDeath());

        AntecedenteFamiliarEntity entity = new AntecedenteFamiliarEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setPaciente_id(request.getPatientId());
        entity.setParentesco(request.getKinship().trim());
        entity.setCatalogo_diagnostico_id(request.getCatalogDiagnosisId());
        entity.setDescripcion(request.getDescription());
        entity.setEdad_aparicion(request.getAgeOfOnset());
        entity.setEs_fallecido(request.getIsDeceased() != null && request.getIsDeceased());
        entity.setCausa_fallecimiento(request.getCauseOfDeath());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);
        AntecedenteFamiliarEntity saved = jpa.save(entity);

        return query.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el antecedente familiar creado"));
    }

    @Override
    @Transactional
    public AntecedenteFamiliarResponseDto update(Long id, UpdateAntecedenteFamiliarRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        AntecedenteFamiliarEntity entity = getValidEntity(id, empresa_id);

        validateCatalogoDiagnostico(request.getCatalogDiagnosisId());
        validateDeceasedConsistency(request.getIsDeceased(), request.getCauseOfDeath());

        entity.setParentesco(request.getKinship().trim());
        entity.setCatalogo_diagnostico_id(request.getCatalogDiagnosisId());
        entity.setDescripcion(request.getDescription());
        entity.setEdad_aparicion(request.getAgeOfOnset());
        if (request.getIsDeceased() != null) {
            entity.setEs_fallecido(request.getIsDeceased());
        }
        entity.setCausa_fallecimiento(request.getCauseOfDeath());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el antecedente familiar actualizado"));
    }

    @Override
    public AntecedenteFamiliarResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Antecedente familiar no encontrado"));
    }

    @Override
    public PageImpl<AntecedenteFamiliarTableDto> list(PageableDto<AntecedenteFamiliarFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return query.listAntecedentes(pageable, empresa_id);
    }

    @Override
    @Transactional
    public AntecedenteFamiliarResponseDto setActive(Long id, boolean active) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        AntecedenteFamiliarEntity entity = getValidEntity(id, empresa_id);
        entity.setActivo(active);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el antecedente familiar"));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        AntecedenteFamiliarEntity entity = getValidEntity(id, empresa_id);
        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AntecedenteFamiliarEntity getValidEntity(Long id, Long empresa_id) {
        AntecedenteFamiliarEntity entity = jpa.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Antecedente familiar no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Antecedente familiar no encontrado");
        }
        return entity;
    }

    private void validatePaciente(Long empresa_id, Long paciente_id) {
        if (!query.pacienteExistsInEmpresa(paciente_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }
    }

    private void validateCatalogoDiagnostico(Long catalogo_id) {
        if (catalogo_id == null) return;
        if (!query.catalogoDiagnosticoExists(catalogo_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Diagnóstico CIE-10 no encontrado");
        }
    }

    private void validateDeceasedConsistency(Boolean isDeceased, String causeOfDeath) {
        if (Boolean.FALSE.equals(isDeceased) && causeOfDeath != null && !causeOfDeath.isBlank()) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "No se puede registrar causa de fallecimiento si el familiar no está fallecido");
        }
    }
}
