package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.cita.*;
import com.cloud_tecnological.mednova.entity.CitaEntity;
import com.cloud_tecnological.mednova.repositories.cita.CitaJpaRepository;
import com.cloud_tecnological.mednova.repositories.cita.CitaQueryRepository;
import com.cloud_tecnological.mednova.repositories.disponibilidad.DisponibilidadCitaQueryRepository;
import com.cloud_tecnological.mednova.repositories.agenda.AgendaJpaRepository;
import com.cloud_tecnological.mednova.services.CitaService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CitaServiceImpl implements CitaService {

    private final CitaJpaRepository citaJpa;
    private final CitaQueryRepository citaQuery;
    private final DisponibilidadCitaQueryRepository disponibilidadQuery;
    private final AgendaJpaRepository agendaJpa;

    public CitaServiceImpl(CitaJpaRepository citaJpa,
                            CitaQueryRepository citaQuery,
                            DisponibilidadCitaQueryRepository disponibilidadQuery,
                            AgendaJpaRepository agendaJpa) {
        this.citaJpa             = citaJpa;
        this.citaQuery           = citaQuery;
        this.disponibilidadQuery = disponibilidadQuery;
        this.agendaJpa           = agendaJpa;
    }

    @Override
    @Transactional
    public AppointmentResponseDto create(CreateAppointmentRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        if (!citaQuery.existsPacienteInEmpresa(dto.getPacienteId(), empresaId)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Paciente no encontrado");
        }

        var disponibilidadOpt = disponibilidadQuery.findById(dto.getDisponibilidadId(), empresaId);
        if (disponibilidadOpt.isEmpty()) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Slot de disponibilidad no encontrado");
        }
        var disponibilidad = disponibilidadOpt.get();

        if (!"DISPONIBLE".equals(disponibilidad.getEstadoDisponibilidad())) {
            throw new GlobalException(HttpStatus.CONFLICT, "El slot ya no tiene cupos disponibles");
        }

        if (citaQuery.hasCitaActivaEnSlot(dto.getPacienteId(), dto.getDisponibilidadId())) {
            throw new GlobalException(HttpStatus.CONFLICT, "El paciente ya tiene una cita en este horario");
        }

        Long estadoOcupado   = disponibilidadQuery.findEstadoDisponibilidadIdByCodigo("OCUPADA");
        boolean decremented  = disponibilidadQuery.decrementCupos(dto.getDisponibilidadId(), estadoOcupado);
        if (!decremented) {
            throw new GlobalException(HttpStatus.CONFLICT, "No hay cupos disponibles en este horario");
        }

        // Get agenda_id from disponibilidad
        String numCita = citaQuery.generateNumeroCita(empresaId, sedeId);

        // Fetch agenda_id via disponibilidad -> agenda_id (stored in disponibilidadResponseDto)
        Long agendaId = disponibilidad.getAgendaId();

        // Build fecha_cita as LocalDateTime (date + hora_inicio)
        LocalDateTime fechaCita = LocalDateTime.of(disponibilidad.getFecha(), disponibilidad.getHoraInicio());

        CitaEntity cita = new CitaEntity();
        cita.setEmpresa_id(empresaId);
        cita.setSede_id(sedeId);
        cita.setNumero_cita(numCita);
        cita.setDisponibilidad_id(dto.getDisponibilidadId());
        cita.setAgenda_id(agendaId);
        cita.setPaciente_id(dto.getPacienteId());
        cita.setServicio_salud_id(dto.getServicioSaludId());
        cita.setTipo_cita_id(dto.getTipoCitaId());
        cita.setFecha_cita(fechaCita);
        cita.setMotivo(dto.getMotivo());
        cita.setObservaciones(dto.getObservaciones());
        cita.setUsuario_creacion(usuarioId);

        Long estadoAgendada = citaQuery.findEstadoCitaIdByCodigo("AGENDADA");
        if (estadoAgendada == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Estado 'AGENDADA' no configurado");
        }
        cita.setEstado_cita_id(estadoAgendada);

        // Try to get especialidad from agenda
        agendaJpa.findById(agendaId).ifPresent(ag -> cita.setEspecialidad_id(ag.getEspecialidad_id()));

        CitaEntity saved = citaJpa.save(cita);

        return citaQuery.findActiveById(saved.getId(), empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar cita creada"));
    }

    @Override
    public AppointmentResponseDto findById(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        return citaQuery.findActiveById(id, empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
    }

    @Override
    public PageImpl<AppointmentTableDto> listActive(PageableDto<?> request) {
        return citaQuery.listActive(request, TenantContext.getEmpresaId(), TenantContext.getSedeId());
    }

    @Override
    @Transactional
    public Boolean cancel(Long id, CancelAppointmentRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        CitaEntity cita = citaJpa.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
        validateTenant(cita, empresaId, sedeId);

        Long estadoCancelada = citaQuery.findEstadoCitaIdByCodigo("CANCELADA");
        if (estadoCancelada == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Estado 'CANCELADA' no configurado");
        }

        cita.setEstado_cita_id(estadoCancelada);
        cita.setMotivo_cancelacion_id(dto.getMotivoCancelacionId());
        if (dto.getObservaciones() != null) cita.setObservaciones(dto.getObservaciones());
        cita.setUsuario_modificacion(usuarioId);
        citaJpa.save(cita);

        // Free the slot
        Long estadoDisponible = disponibilidadQuery.findEstadoDisponibilidadIdByCodigo("DISPONIBLE");
        disponibilidadQuery.incrementCupos(cita.getDisponibilidad_id(), estadoDisponible);

        return true;
    }

    @Override
    @Transactional
    public AppointmentResponseDto reschedule(Long id, RescheduleAppointmentRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        CitaEntity cita = citaJpa.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
        validateTenant(cita, empresaId, sedeId);

        var nuevaDisp = disponibilidadQuery.findById(dto.getNuevaDisponibilidadId(), empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Nuevo slot de disponibilidad no encontrado"));

        if (!"DISPONIBLE".equals(nuevaDisp.getEstadoDisponibilidad())) {
            throw new GlobalException(HttpStatus.CONFLICT, "El nuevo slot no tiene cupos disponibles");
        }

        Long estadoOcupado   = disponibilidadQuery.findEstadoDisponibilidadIdByCodigo("OCUPADA");
        boolean decremented  = disponibilidadQuery.decrementCupos(dto.getNuevaDisponibilidadId(), estadoOcupado);
        if (!decremented) {
            throw new GlobalException(HttpStatus.CONFLICT, "No hay cupos disponibles en el nuevo horario");
        }

        // Free old slot
        Long estadoDisponible = disponibilidadQuery.findEstadoDisponibilidadIdByCodigo("DISPONIBLE");
        disponibilidadQuery.incrementCupos(cita.getDisponibilidad_id(), estadoDisponible);

        LocalDateTime nuevaFechaCita = LocalDateTime.of(nuevaDisp.getFecha(), nuevaDisp.getHoraInicio());

        cita.setDisponibilidad_id(dto.getNuevaDisponibilidadId());
        cita.setAgenda_id(nuevaDisp.getAgendaId());
        cita.setFecha_cita(nuevaFechaCita);
        cita.setMotivo_reprogramacion_id(dto.getMotivoReprogramacionId());
        if (dto.getObservaciones() != null) cita.setObservaciones(dto.getObservaciones());
        cita.setUsuario_modificacion(usuarioId);
        citaJpa.save(cita);

        return citaQuery.findActiveById(id, empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar cita reprogramada"));
    }

    private void validateTenant(CitaEntity cita, Long empresaId, Long sedeId) {
        if (!Boolean.TRUE.equals(cita.getActivo()) || !cita.getEmpresa_id().equals(empresaId) || !cita.getSede_id().equals(sedeId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Cita no encontrada");
        }
    }
}
