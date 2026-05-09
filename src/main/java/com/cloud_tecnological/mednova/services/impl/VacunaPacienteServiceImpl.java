package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.vacuna.CreateVacunaPacienteRequestDto;
import com.cloud_tecnological.mednova.dto.vacuna.UpdateVacunaPacienteRequestDto;
import com.cloud_tecnological.mednova.dto.vacuna.VacunaPacienteFilterParams;
import com.cloud_tecnological.mednova.dto.vacuna.VacunaPacienteResponseDto;
import com.cloud_tecnological.mednova.dto.vacuna.VacunaPacienteTableDto;
import com.cloud_tecnological.mednova.entity.VacunaPacienteEntity;
import com.cloud_tecnological.mednova.repositories.vacuna.VacunaPacienteJpaRepository;
import com.cloud_tecnological.mednova.repositories.vacuna.VacunaPacienteQueryRepository;
import com.cloud_tecnological.mednova.services.VacunaPacienteService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class VacunaPacienteServiceImpl implements VacunaPacienteService {

    private final VacunaPacienteJpaRepository jpa;
    private final VacunaPacienteQueryRepository query;

    public VacunaPacienteServiceImpl(VacunaPacienteJpaRepository jpa,
                                     VacunaPacienteQueryRepository query) {
        this.jpa   = jpa;
        this.query = query;
    }

    @Override
    @Transactional
    public VacunaPacienteResponseDto create(CreateVacunaPacienteRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        validatePaciente(empresa_id, request.getPatientId());
        validateViaAdministracion(request.getAdministrationRouteId());
        validateProfesional(empresa_id, request.getApplyingProfessionalId());
        validateApplicationDate(request.getApplicationDate());
        validateNextDoseDate(request.getApplicationDate(), request.getNextDoseDate());
        validateDoseInScheme(request.getDoseNumber(), request.getTotalSchemeDoses());

        VacunaPacienteEntity entity = new VacunaPacienteEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setPaciente_id(request.getPatientId());
        entity.setNombre_vacuna(request.getVaccineName().trim());
        entity.setCodigo_vacuna(request.getVaccineCode());
        entity.setDosis(request.getDoseNumber());
        entity.setTotal_dosis_esquema(request.getTotalSchemeDoses());
        entity.setFecha_aplicacion(request.getApplicationDate());
        entity.setFecha_proxima_dosis(request.getNextDoseDate());
        entity.setLaboratorio(request.getLaboratory());
        entity.setNumero_lote(request.getBatchNumber());
        entity.setVia_administracion_id(request.getAdministrationRouteId());
        entity.setProfesional_aplica_id(request.getApplyingProfessionalId());
        entity.setInstitucion_aplica(request.getApplyingInstitution());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);
        VacunaPacienteEntity saved = jpa.save(entity);

        return query.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la vacuna creada"));
    }

    @Override
    @Transactional
    public VacunaPacienteResponseDto update(Long id, UpdateVacunaPacienteRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        VacunaPacienteEntity entity = getValidEntity(id, empresa_id);

        validateViaAdministracion(request.getAdministrationRouteId());
        validateProfesional(empresa_id, request.getApplyingProfessionalId());
        validateApplicationDate(request.getApplicationDate());
        validateNextDoseDate(request.getApplicationDate(), request.getNextDoseDate());
        validateDoseInScheme(request.getDoseNumber(), request.getTotalSchemeDoses());

        entity.setNombre_vacuna(request.getVaccineName().trim());
        entity.setCodigo_vacuna(request.getVaccineCode());
        entity.setDosis(request.getDoseNumber());
        entity.setTotal_dosis_esquema(request.getTotalSchemeDoses());
        entity.setFecha_aplicacion(request.getApplicationDate());
        entity.setFecha_proxima_dosis(request.getNextDoseDate());
        entity.setLaboratorio(request.getLaboratory());
        entity.setNumero_lote(request.getBatchNumber());
        entity.setVia_administracion_id(request.getAdministrationRouteId());
        entity.setProfesional_aplica_id(request.getApplyingProfessionalId());
        entity.setInstitucion_aplica(request.getApplyingInstitution());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la vacuna actualizada"));
    }

    @Override
    public VacunaPacienteResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Vacuna no encontrada"));
    }

    @Override
    public PageImpl<VacunaPacienteTableDto> list(PageableDto<VacunaPacienteFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return query.listVacunas(pageable, empresa_id);
    }

    @Override
    @Transactional
    public VacunaPacienteResponseDto setActive(Long id, boolean active) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        VacunaPacienteEntity entity = getValidEntity(id, empresa_id);
        entity.setActivo(active);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la vacuna"));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        VacunaPacienteEntity entity = getValidEntity(id, empresa_id);
        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private VacunaPacienteEntity getValidEntity(Long id, Long empresa_id) {
        VacunaPacienteEntity entity = jpa.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Vacuna no encontrada"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Vacuna no encontrada");
        }
        return entity;
    }

    private void validatePaciente(Long empresa_id, Long paciente_id) {
        if (!query.pacienteExistsInEmpresa(paciente_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }
    }

    private void validateViaAdministracion(Long via_id) {
        if (via_id == null) return;
        if (!query.viaAdministracionExists(via_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Vía de administración no encontrada");
        }
    }

    private void validateProfesional(Long empresa_id, Long profesional_id) {
        if (profesional_id == null) return;
        if (!query.profesionalActivoInEmpresa(profesional_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Profesional no encontrado");
        }
    }

    private void validateApplicationDate(LocalDate applicationDate) {
        if (applicationDate != null && applicationDate.isAfter(LocalDate.now())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "La fecha de aplicación no puede ser futura");
        }
    }

    private void validateNextDoseDate(LocalDate applicationDate, LocalDate nextDoseDate) {
        if (applicationDate != null && nextDoseDate != null && nextDoseDate.isBefore(applicationDate)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "La fecha de próxima dosis no puede ser anterior a la fecha de aplicación");
        }
    }

    private void validateDoseInScheme(Integer dose, Integer totalScheme) {
        if (dose != null && totalScheme != null && dose > totalScheme) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "El número de dosis no puede superar el total del esquema");
        }
    }
}
