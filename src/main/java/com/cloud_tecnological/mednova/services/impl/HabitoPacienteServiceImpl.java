package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.habito.CreateHabitoPacienteRequestDto;
import com.cloud_tecnological.mednova.dto.habito.HabitoPacienteFilterParams;
import com.cloud_tecnological.mednova.dto.habito.HabitoPacienteResponseDto;
import com.cloud_tecnological.mednova.dto.habito.HabitoPacienteTableDto;
import com.cloud_tecnological.mednova.dto.habito.UpdateHabitoPacienteRequestDto;
import com.cloud_tecnological.mednova.entity.HabitoPacienteEntity;
import com.cloud_tecnological.mednova.repositories.habito.HabitoPacienteJpaRepository;
import com.cloud_tecnological.mednova.repositories.habito.HabitoPacienteQueryRepository;
import com.cloud_tecnological.mednova.services.HabitoPacienteService;
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
public class HabitoPacienteServiceImpl implements HabitoPacienteService {

    private static final String ESTADO_ACTIVO = "ACTIVO";
    private static final String ESTADO_EX_CONSUMIDOR = "EX_CONSUMIDOR";

    private final HabitoPacienteJpaRepository jpa;
    private final HabitoPacienteQueryRepository query;

    public HabitoPacienteServiceImpl(HabitoPacienteJpaRepository jpa,
                                     HabitoPacienteQueryRepository query) {
        this.jpa   = jpa;
        this.query = query;
    }

    @Override
    @Transactional
    public HabitoPacienteResponseDto create(CreateHabitoPacienteRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        validatePaciente(empresa_id, request.getPatientId());
        validateDateRange(request.getStartDate(), request.getEndDate());

        String estado = (request.getStatus() == null || request.getStatus().isBlank())
                ? ESTADO_ACTIVO
                : request.getStatus();

        validateExConsumidorNeedsEndDate(estado, request.getEndDate());
        validateUniqueActivePerType(empresa_id, request.getPatientId(),
                request.getHabitType(), estado, null);

        HabitoPacienteEntity entity = new HabitoPacienteEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setPaciente_id(request.getPatientId());
        entity.setTipo_habito(request.getHabitType());
        entity.setDescripcion(request.getDescription());
        entity.setFrecuencia(request.getFrequency());
        entity.setCantidad(request.getQuantity());
        entity.setTiempo_consumo(request.getConsumptionTime());
        entity.setFecha_inicio(request.getStartDate());
        entity.setFecha_fin(request.getEndDate());
        entity.setEstado(estado);
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);
        HabitoPacienteEntity saved = jpa.save(entity);

        return query.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el hábito creado"));
    }

    @Override
    @Transactional
    public HabitoPacienteResponseDto update(Long id, UpdateHabitoPacienteRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        HabitoPacienteEntity entity = getValidEntity(id, empresa_id);

        validateDateRange(request.getStartDate(), request.getEndDate());
        validateExConsumidorNeedsEndDate(request.getStatus(), request.getEndDate());
        validateUniqueActivePerType(empresa_id, entity.getPaciente_id(),
                request.getHabitType(), request.getStatus(), id);

        entity.setTipo_habito(request.getHabitType());
        entity.setDescripcion(request.getDescription());
        entity.setFrecuencia(request.getFrequency());
        entity.setCantidad(request.getQuantity());
        entity.setTiempo_consumo(request.getConsumptionTime());
        entity.setFecha_inicio(request.getStartDate());
        entity.setFecha_fin(request.getEndDate());
        entity.setEstado(request.getStatus());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el hábito actualizado"));
    }

    @Override
    public HabitoPacienteResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Hábito no encontrado"));
    }

    @Override
    public PageImpl<HabitoPacienteTableDto> list(PageableDto<HabitoPacienteFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return query.listHabitos(pageable, empresa_id);
    }

    @Override
    @Transactional
    public HabitoPacienteResponseDto setActive(Long id, boolean active) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        HabitoPacienteEntity entity = getValidEntity(id, empresa_id);
        entity.setActivo(active);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el hábito"));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        HabitoPacienteEntity entity = getValidEntity(id, empresa_id);
        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private HabitoPacienteEntity getValidEntity(Long id, Long empresa_id) {
        HabitoPacienteEntity entity = jpa.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Hábito no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Hábito no encontrado");
        }
        return entity;
    }

    private void validatePaciente(Long empresa_id, Long paciente_id) {
        if (!query.pacienteExistsInEmpresa(paciente_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }

    private void validateExConsumidorNeedsEndDate(String estado, LocalDate endDate) {
        if (ESTADO_EX_CONSUMIDOR.equals(estado) && endDate == null) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "Un hábito EX_CONSUMIDOR debe registrar fecha de fin");
        }
    }

    private void validateUniqueActivePerType(Long empresa_id, Long paciente_id,
                                             String tipo_habito, String estado, Long excludeId) {
        if (!ESTADO_ACTIVO.equals(estado)) return;
        if (query.activeHabitExistsForType(paciente_id, tipo_habito, empresa_id, excludeId)) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Ya existe un hábito ACTIVO de tipo " + tipo_habito + " para este paciente");
        }
    }
}
