package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.antecedente.AntecedentePersonalFilterParams;
import com.cloud_tecnological.mednova.dto.antecedente.AntecedentePersonalResponseDto;
import com.cloud_tecnological.mednova.dto.antecedente.AntecedentePersonalTableDto;
import com.cloud_tecnological.mednova.dto.antecedente.CreateAntecedentePersonalRequestDto;
import com.cloud_tecnological.mednova.dto.antecedente.TipoAntecedenteResponseDto;
import com.cloud_tecnological.mednova.dto.antecedente.UpdateAntecedentePersonalRequestDto;
import com.cloud_tecnological.mednova.entity.AntecedentePersonalEntity;
import com.cloud_tecnological.mednova.repositories.antecedente.AntecedentePersonalJpaRepository;
import com.cloud_tecnological.mednova.repositories.antecedente.AntecedentePersonalQueryRepository;
import com.cloud_tecnological.mednova.services.AntecedentePersonalService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AntecedentePersonalServiceImpl implements AntecedentePersonalService {

    private final AntecedentePersonalJpaRepository jpa;
    private final AntecedentePersonalQueryRepository query;

    public AntecedentePersonalServiceImpl(AntecedentePersonalJpaRepository jpa,
                                          AntecedentePersonalQueryRepository query) {
        this.jpa   = jpa;
        this.query = query;
    }

    @Override
    @Transactional
    public AntecedentePersonalResponseDto create(CreateAntecedentePersonalRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        validateReferences(empresa_id,
                request.getPatientId(),
                request.getAntecedentTypeId(),
                request.getCatalogDiagnosisId(),
                request.getRegisteringProfessionalId());
        validateDateRange(request.getStartDate(), request.getEndDate());

        AntecedentePersonalEntity entity = new AntecedentePersonalEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setPaciente_id(request.getPatientId());
        entity.setTipo_antecedente_id(request.getAntecedentTypeId());
        entity.setCatalogo_diagnostico_id(request.getCatalogDiagnosisId());
        entity.setDescripcion(request.getDescription());
        entity.setFecha_inicio(request.getStartDate());
        entity.setFecha_fin(request.getEndDate());
        entity.setEs_activo(request.getIsActiveCondition() == null ? true : request.getIsActiveCondition());
        entity.setSeveridad(request.getSeverity());
        entity.setObservaciones(request.getObservations());
        entity.setProfesional_registro_id(request.getRegisteringProfessionalId());
        entity.setUsuario_creacion(usuario_id);
        AntecedentePersonalEntity saved = jpa.save(entity);

        return query.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el antecedente creado"));
    }

    @Override
    @Transactional
    public AntecedentePersonalResponseDto update(Long id, UpdateAntecedentePersonalRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        AntecedentePersonalEntity entity = getValidEntity(id, empresa_id);

        // No se cambia el paciente del antecedente: pertenece a la HC del paciente original.
        validateTipoAntecedente(request.getAntecedentTypeId());
        validateCatalogoDiagnostico(request.getCatalogDiagnosisId());
        validateProfesional(empresa_id, request.getRegisteringProfessionalId());
        validateDateRange(request.getStartDate(), request.getEndDate());

        entity.setTipo_antecedente_id(request.getAntecedentTypeId());
        entity.setCatalogo_diagnostico_id(request.getCatalogDiagnosisId());
        entity.setDescripcion(request.getDescription());
        entity.setFecha_inicio(request.getStartDate());
        entity.setFecha_fin(request.getEndDate());
        if (request.getIsActiveCondition() != null) {
            entity.setEs_activo(request.getIsActiveCondition());
        }
        entity.setSeveridad(request.getSeverity());
        entity.setObservaciones(request.getObservations());
        entity.setProfesional_registro_id(request.getRegisteringProfessionalId());
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el antecedente actualizado"));
    }

    @Override
    public AntecedentePersonalResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Antecedente no encontrado"));
    }

    @Override
    public PageImpl<AntecedentePersonalTableDto> list(PageableDto<AntecedentePersonalFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return query.listAntecedentes(pageable, empresa_id);
    }

    @Override
    @Transactional
    public AntecedentePersonalResponseDto setActive(Long id, boolean active) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        AntecedentePersonalEntity entity = getValidEntity(id, empresa_id);
        entity.setActivo(active);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el antecedente"));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        AntecedentePersonalEntity entity = getValidEntity(id, empresa_id);
        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);
    }

    @Override
    public List<TipoAntecedenteResponseDto> listAntecedentTypes() {
        // Catálogo global: solo se requiere TenantContext válido (vía implícita por el filtro de seguridad).
        TenantContext.getEmpresaId();
        return query.listTiposAntecedente();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AntecedentePersonalEntity getValidEntity(Long id, Long empresa_id) {
        AntecedentePersonalEntity entity = jpa.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Antecedente no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Antecedente no encontrado");
        }
        return entity;
    }

    private void validateReferences(Long empresa_id, Long paciente_id, Long tipo_id,
                                    Long catalogo_id, Long profesional_id) {
        if (!query.pacienteExistsInEmpresa(paciente_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }
        validateTipoAntecedente(tipo_id);
        validateCatalogoDiagnostico(catalogo_id);
        validateProfesional(empresa_id, profesional_id);
    }

    private void validateTipoAntecedente(Long tipo_id) {
        if (!query.tipoAntecedenteActivoExists(tipo_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Tipo de antecedente no encontrado");
        }
    }

    private void validateCatalogoDiagnostico(Long catalogo_id) {
        if (catalogo_id == null) return;
        if (!query.catalogoDiagnosticoExists(catalogo_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Diagnóstico CIE-10 no encontrado");
        }
    }

    private void validateProfesional(Long empresa_id, Long profesional_id) {
        if (profesional_id == null) return;
        if (!query.profesionalActivoInEmpresa(profesional_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Profesional no encontrado");
        }
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }
}
