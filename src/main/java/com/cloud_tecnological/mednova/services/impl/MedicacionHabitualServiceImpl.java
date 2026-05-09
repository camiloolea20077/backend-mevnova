package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.medicacion.CreateMedicacionHabitualRequestDto;
import com.cloud_tecnological.mednova.dto.medicacion.MedicacionHabitualFilterParams;
import com.cloud_tecnological.mednova.dto.medicacion.MedicacionHabitualResponseDto;
import com.cloud_tecnological.mednova.dto.medicacion.MedicacionHabitualTableDto;
import com.cloud_tecnological.mednova.dto.medicacion.UpdateMedicacionHabitualRequestDto;
import com.cloud_tecnological.mednova.entity.MedicacionHabitualEntity;
import com.cloud_tecnological.mednova.repositories.medicacion.MedicacionHabitualJpaRepository;
import com.cloud_tecnological.mednova.repositories.medicacion.MedicacionHabitualQueryRepository;
import com.cloud_tecnological.mednova.services.MedicacionHabitualService;
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
public class MedicacionHabitualServiceImpl implements MedicacionHabitualService {

    private final MedicacionHabitualJpaRepository jpa;
    private final MedicacionHabitualQueryRepository query;

    public MedicacionHabitualServiceImpl(MedicacionHabitualJpaRepository jpa,
                                         MedicacionHabitualQueryRepository query) {
        this.jpa   = jpa;
        this.query = query;
    }

    @Override
    @Transactional
    public MedicacionHabitualResponseDto create(CreateMedicacionHabitualRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        validatePaciente(empresa_id, request.getPatientId());
        validateServicioSalud(request.getHealthServiceId());
        validateViaAdministracion(request.getAdministrationRouteId());
        validateFrecuenciaDosis(request.getDoseFrequencyId());
        validateDateRange(request.getStartDate(), request.getEndDate());

        MedicacionHabitualEntity entity = new MedicacionHabitualEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setPaciente_id(request.getPatientId());
        entity.setServicio_salud_id(request.getHealthServiceId());
        entity.setNombre_medicamento(request.getMedicationName().trim());
        entity.setDosis(request.getDose());
        entity.setVia_administracion_id(request.getAdministrationRouteId());
        entity.setFrecuencia_dosis_id(request.getDoseFrequencyId());
        entity.setFecha_inicio(request.getStartDate());
        entity.setFecha_fin(request.getEndDate());
        entity.setIndicacion(request.getIndication());
        entity.setProfesional_prescriptor(request.getPrescribingProfessional());
        entity.setEs_activo(request.getIsCurrentlyTaking() == null ? true : request.getIsCurrentlyTaking());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);
        MedicacionHabitualEntity saved = jpa.save(entity);

        return query.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la medicación creada"));
    }

    @Override
    @Transactional
    public MedicacionHabitualResponseDto update(Long id, UpdateMedicacionHabitualRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        MedicacionHabitualEntity entity = getValidEntity(id, empresa_id);

        validateServicioSalud(request.getHealthServiceId());
        validateViaAdministracion(request.getAdministrationRouteId());
        validateFrecuenciaDosis(request.getDoseFrequencyId());
        validateDateRange(request.getStartDate(), request.getEndDate());

        entity.setServicio_salud_id(request.getHealthServiceId());
        entity.setNombre_medicamento(request.getMedicationName().trim());
        entity.setDosis(request.getDose());
        entity.setVia_administracion_id(request.getAdministrationRouteId());
        entity.setFrecuencia_dosis_id(request.getDoseFrequencyId());
        entity.setFecha_inicio(request.getStartDate());
        entity.setFecha_fin(request.getEndDate());
        entity.setIndicacion(request.getIndication());
        entity.setProfesional_prescriptor(request.getPrescribingProfessional());
        if (request.getIsCurrentlyTaking() != null) {
            entity.setEs_activo(request.getIsCurrentlyTaking());
        }
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la medicación actualizada"));
    }

    @Override
    public MedicacionHabitualResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Medicación habitual no encontrada"));
    }

    @Override
    public PageImpl<MedicacionHabitualTableDto> list(PageableDto<MedicacionHabitualFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return query.listMedicaciones(pageable, empresa_id);
    }

    @Override
    @Transactional
    public MedicacionHabitualResponseDto setCurrentlyTaking(Long id, boolean currentlyTaking) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        MedicacionHabitualEntity entity = getValidEntity(id, empresa_id);
        entity.setEs_activo(currentlyTaking);
        if (!currentlyTaking && entity.getFecha_fin() == null) {
            entity.setFecha_fin(LocalDate.now());
        }
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la medicación"));
    }

    @Override
    @Transactional
    public MedicacionHabitualResponseDto setActive(Long id, boolean active) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        MedicacionHabitualEntity entity = getValidEntity(id, empresa_id);
        entity.setActivo(active);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la medicación"));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        MedicacionHabitualEntity entity = getValidEntity(id, empresa_id);
        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private MedicacionHabitualEntity getValidEntity(Long id, Long empresa_id) {
        MedicacionHabitualEntity entity = jpa.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Medicación habitual no encontrada"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Medicación habitual no encontrada");
        }
        return entity;
    }

    private void validatePaciente(Long empresa_id, Long paciente_id) {
        if (!query.pacienteExistsInEmpresa(paciente_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }
    }

    private void validateServicioSalud(Long servicio_id) {
        if (servicio_id == null) return;
        if (!query.servicioSaludExists(servicio_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Servicio de salud no encontrado");
        }
    }

    private void validateViaAdministracion(Long via_id) {
        if (via_id == null) return;
        if (!query.viaAdministracionExists(via_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Vía de administración no encontrada");
        }
    }

    private void validateFrecuenciaDosis(Long frecuencia_id) {
        if (frecuencia_id == null) return;
        if (!query.frecuenciaDosisExists(frecuencia_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Frecuencia de dosis no encontrada");
        }
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }
}
