package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.disponibilidad.DisponibilidadResponseDto;
import com.cloud_tecnological.mednova.dto.disponibilidad.GenerateAvailabilityRequestDto;
import com.cloud_tecnological.mednova.dto.disponibilidad.SearchDisponibilidadRequestDto;
import com.cloud_tecnological.mednova.entity.AgendaProfesionalEntity;
import com.cloud_tecnological.mednova.repositories.agenda.AgendaJpaRepository;
import com.cloud_tecnological.mednova.repositories.agenda.BloqueAgendaJpaRepository;
import com.cloud_tecnological.mednova.repositories.calendario.CalendarioCitaQueryRepository;
import com.cloud_tecnological.mednova.repositories.disponibilidad.DisponibilidadCitaQueryRepository;
import com.cloud_tecnological.mednova.entity.BloqueAgendaEntity;
import com.cloud_tecnological.mednova.services.DisponibilidadService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

@Service
public class DisponibilidadServiceImpl implements DisponibilidadService {

    private final AgendaJpaRepository agendaJpa;
    private final BloqueAgendaJpaRepository bloqueJpa;
    private final CalendarioCitaQueryRepository calendarioQuery;
    private final DisponibilidadCitaQueryRepository disponibilidadQuery;

    public DisponibilidadServiceImpl(AgendaJpaRepository agendaJpa,
                                      BloqueAgendaJpaRepository bloqueJpa,
                                      CalendarioCitaQueryRepository calendarioQuery,
                                      DisponibilidadCitaQueryRepository disponibilidadQuery) {
        this.agendaJpa          = agendaJpa;
        this.bloqueJpa          = bloqueJpa;
        this.calendarioQuery    = calendarioQuery;
        this.disponibilidadQuery = disponibilidadQuery;
    }

    @Override
    @Transactional
    public Integer generateAvailability(GenerateAvailabilityRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();

        AgendaProfesionalEntity agenda = agendaJpa.findById(dto.getAgendaId())
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Agenda no encontrada"));
        if (!agenda.getEmpresa_id().equals(empresaId) || !agenda.getSede_id().equals(sedeId) || !Boolean.TRUE.equals(agenda.getActivo())) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Agenda no encontrada");
        }

        LocalDate fechaDesde = dto.getFechaDesde();
        LocalDate fechaHasta = dto.getFechaHasta();
        if (fechaHasta.isBefore(fechaDesde)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "fechaHasta debe ser mayor o igual a fechaDesde");
        }

        // Respect vigencia of agenda
        if (agenda.getFecha_vigencia_hasta() != null && fechaDesde.isAfter(agenda.getFecha_vigencia_hasta())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "El rango de fechas está fuera de la vigencia de la agenda");
        }
        if (fechaHasta.isAfter(agenda.getFecha_vigencia_hasta() != null ? agenda.getFecha_vigencia_hasta() : fechaHasta)) {
            fechaHasta = agenda.getFecha_vigencia_hasta();
        }

        List<BloqueAgendaEntity> bloques = bloqueJpa.findByAgendaId(agenda.getId());
        if (bloques.isEmpty()) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "La agenda no tiene bloques configurados");
        }

        Map<LocalDate, Boolean> habilityMap = calendarioQuery.findHabilityMap(agenda.getCalendario_id(), fechaDesde, fechaHasta);

        Long estadoDisponible = disponibilidadQuery.findEstadoDisponibilidadIdByCodigo("DISPONIBLE");
        if (estadoDisponible == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Estado 'DISPONIBLE' no configurado");
        }

        int duracion = agenda.getDuracion_cita_minutos();
        int slotsCreated = 0;

        LocalDate current = fechaDesde;
        while (!current.isAfter(fechaHasta)) {
            Boolean esHabil = habilityMap.getOrDefault(current, true); // default: working day
            if (Boolean.TRUE.equals(esHabil)) {
                int isoDow = current.getDayOfWeek().getValue(); // 1=Mon … 7=Sun

                for (BloqueAgendaEntity bloque : bloques) {
                    if (!bloque.getDia_semana().equals(isoDow)) continue;

                    LocalTime slotStart = bloque.getHora_inicio();
                    LocalTime slotEnd   = slotStart.plusMinutes(duracion);

                    while (!slotEnd.isAfter(bloque.getHora_fin())) {
                        disponibilidadQuery.insertIfNotExists(
                            empresaId, sedeId, agenda.getId(),
                            current, slotStart, slotEnd,
                            bloque.getCupos(), estadoDisponible
                        );
                        slotsCreated++;
                        slotStart = slotEnd;
                        slotEnd   = slotStart.plusMinutes(duracion);
                    }
                }
            }
            current = current.plusDays(1);
        }

        return slotsCreated;
    }

    @Override
    public List<DisponibilidadResponseDto> search(SearchDisponibilidadRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        return disponibilidadQuery.searchAvailable(dto, empresaId, sedeId);
    }
}
