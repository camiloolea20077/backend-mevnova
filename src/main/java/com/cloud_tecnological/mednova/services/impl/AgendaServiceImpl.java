package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.agenda.*;
import com.cloud_tecnological.mednova.dto.traslado.BulkTransferRequestDto;
import com.cloud_tecnological.mednova.dto.traslado.BulkTransferResponseDto;
import com.cloud_tecnological.mednova.entity.*;
import com.cloud_tecnological.mednova.repositories.agenda.AgendaJpaRepository;
import com.cloud_tecnological.mednova.repositories.agenda.AgendaQueryRepository;
import com.cloud_tecnological.mednova.repositories.agenda.BloqueAgendaJpaRepository;
import com.cloud_tecnological.mednova.repositories.cita.CitaJpaRepository;
import com.cloud_tecnological.mednova.repositories.cita.CitaQueryRepository;
import com.cloud_tecnological.mednova.repositories.disponibilidad.DisponibilidadCitaQueryRepository;
import com.cloud_tecnological.mednova.repositories.listaespera.ListaEsperaCitaJpaRepository;
import com.cloud_tecnological.mednova.repositories.listaespera.ListaEsperaCitaQueryRepository;
import com.cloud_tecnological.mednova.repositories.traslado.DetalleTrasladoAgendaJpaRepository;
import com.cloud_tecnological.mednova.repositories.traslado.TrasladoAgendaJpaRepository;
import com.cloud_tecnological.mednova.services.AgendaService;
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
import java.util.Map;

@Service
public class AgendaServiceImpl implements AgendaService {

    private final AgendaJpaRepository agendaJpa;
    private final AgendaQueryRepository agendaQuery;
    private final BloqueAgendaJpaRepository bloqueJpa;
    private final CitaJpaRepository citaJpa;
    private final CitaQueryRepository citaQuery;
    private final DisponibilidadCitaQueryRepository disponibilidadQuery;
    private final ListaEsperaCitaJpaRepository listaEsperaJpa;
    private final ListaEsperaCitaQueryRepository listaEsperaQuery;
    private final TrasladoAgendaJpaRepository trasladoJpa;
    private final DetalleTrasladoAgendaJpaRepository detalleTrasladoJpa;

    public AgendaServiceImpl(AgendaJpaRepository agendaJpa,
                              AgendaQueryRepository agendaQuery,
                              BloqueAgendaJpaRepository bloqueJpa,
                              CitaJpaRepository citaJpa,
                              CitaQueryRepository citaQuery,
                              DisponibilidadCitaQueryRepository disponibilidadQuery,
                              ListaEsperaCitaJpaRepository listaEsperaJpa,
                              ListaEsperaCitaQueryRepository listaEsperaQuery,
                              TrasladoAgendaJpaRepository trasladoJpa,
                              DetalleTrasladoAgendaJpaRepository detalleTrasladoJpa) {
        this.agendaJpa         = agendaJpa;
        this.agendaQuery       = agendaQuery;
        this.bloqueJpa         = bloqueJpa;
        this.citaJpa           = citaJpa;
        this.citaQuery         = citaQuery;
        this.disponibilidadQuery = disponibilidadQuery;
        this.listaEsperaJpa    = listaEsperaJpa;
        this.listaEsperaQuery  = listaEsperaQuery;
        this.trasladoJpa       = trasladoJpa;
        this.detalleTrasladoJpa = detalleTrasladoJpa;
    }

    @Override
    @Transactional
    public AgendaResponseDto create(CreateAgendaRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        if (!agendaQuery.existsProfesionalInEmpresa(dto.getProfesionalId(), empresaId)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Profesional no encontrado en la empresa");
        }
        if (!agendaQuery.existsCalendarioInEmpresa(dto.getCalendarioId(), empresaId)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Calendario no encontrado en la empresa");
        }

        Long estadoActivo = agendaQuery.findEstadoAgendaIdByCodigo("ACTIVA");
        if (estadoActivo == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Estado de agenda 'ACTIVA' no configurado");
        }

        AgendaProfesionalEntity agenda = new AgendaProfesionalEntity();
        agenda.setEmpresa_id(empresaId);
        agenda.setSede_id(sedeId);
        agenda.setProfesional_id(dto.getProfesionalId());
        agenda.setEspecialidad_id(dto.getEspecialidadId());
        agenda.setRecurso_fisico_id(dto.getRecursoFisicoId());
        agenda.setCalendario_id(dto.getCalendarioId());
        agenda.setEstado_agenda_id(estadoActivo);
        agenda.setDuracion_cita_minutos(dto.getDuracionCitaMinutos() != null ? dto.getDuracionCitaMinutos() : 20);
        agenda.setFecha_vigencia_desde(dto.getFechaVigenciaDesde());
        agenda.setFecha_vigencia_hasta(dto.getFechaVigenciaHasta());
        agenda.setObservaciones(dto.getObservaciones());
        agenda.setUsuario_creacion(usuarioId);
        AgendaProfesionalEntity saved = agendaJpa.save(agenda);

        for (CreateBloqueAgendaRequestDto bloqueDto : dto.getBloques()) {
            BloqueAgendaEntity bloque = new BloqueAgendaEntity();
            bloque.setEmpresa_id(empresaId);
            bloque.setAgenda_id(saved.getId());
            bloque.setDia_semana(bloqueDto.getDiaSemana());
            bloque.setHora_inicio(bloqueDto.getHoraInicio());
            bloque.setHora_fin(bloqueDto.getHoraFin());
            bloque.setCupos(bloqueDto.getCupos() != null ? bloqueDto.getCupos() : 1);
            bloqueJpa.save(bloque);
        }

        return agendaQuery.findActiveById(saved.getId(), empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar agenda creada"));
    }

    @Override
    public AgendaResponseDto findById(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        return agendaQuery.findActiveById(id, empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Agenda no encontrada"));
    }

    @Override
    public PageImpl<AgendaTableDto> listActivos(PageableDto<?> request) {
        return agendaQuery.listActivos(request, TenantContext.getEmpresaId(), TenantContext.getSedeId());
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        AgendaProfesionalEntity entity = agendaJpa.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Agenda no encontrada"));
        if (!entity.getEmpresa_id().equals(empresaId) || !entity.getSede_id().equals(sedeId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Agenda no encontrada");
        }

        entity.setActivo(false);
        entity.setUsuario_modificacion(usuarioId);
        agendaJpa.save(entity);
        return true;
    }

    @Override
    @Transactional
    public BulkTransferResponseDto bulkTransfer(BulkTransferRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        AgendaProfesionalEntity agendaOrigen = agendaJpa.findById(dto.getAgendaOrigenId())
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Agenda origen no encontrada"));
        if (!agendaOrigen.getEmpresa_id().equals(empresaId) || !agendaOrigen.getSede_id().equals(sedeId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Agenda origen no encontrada");
        }

        Long estadoCancelada = citaQuery.findEstadoCitaIdByCodigo("CANCELADA");
        Long estadoAgendada  = citaQuery.findEstadoCitaIdByCodigo("AGENDADA");
        Long estadoDisponible = disponibilidadQuery.findEstadoDisponibilidadIdByCodigo("DISPONIBLE");
        Long estadoOcupado    = disponibilidadQuery.findEstadoDisponibilidadIdByCodigo("OCUPADA");

        List<Map<String, Object>> citas = citaQuery.findAgendadasByAgendaAndFecha(dto.getAgendaOrigenId(), dto.getFechaOrigen());

        TrasladoAgendaEntity traslado = new TrasladoAgendaEntity();
        traslado.setEmpresa_id(empresaId);
        traslado.setSede_id(sedeId);
        traslado.setAgenda_origen_id(dto.getAgendaOrigenId());
        traslado.setFecha_origen(dto.getFechaOrigen());
        traslado.setAgenda_destino_id(dto.getAgendaDestinoId());
        traslado.setFecha_destino(dto.getFechaDestino());
        traslado.setMotivo(dto.getMotivo());
        traslado.setTotal_citas(citas.size());
        traslado.setCitas_trasladadas(0);
        traslado.setCitas_fallidas(0);
        traslado.setUsuario_creacion(usuarioId);
        TrasladoAgendaEntity savedTraslado = trasladoJpa.save(traslado);

        int trasladadas = 0;
        int fallidas    = 0;

        for (Map<String, Object> row : citas) {
            Long citaId          = ((Number) row.get("id")).longValue();
            Long disponibilidadId = ((Number) row.get("disponibilidad_id")).longValue();
            Long pacienteId      = ((Number) row.get("paciente_id")).longValue();
            Long tipoCitaId      = row.get("tipo_cita_id") != null ? ((Number) row.get("tipo_cita_id")).longValue() : null;
            Long especialidadId  = row.get("especialidad_id") != null ? ((Number) row.get("especialidad_id")).longValue() : null;

            CitaEntity cita = citaJpa.findById(citaId).orElse(null);
            if (cita == null) { fallidas++; saveDetalle(savedTraslado.getId(), citaId, "FALLIDA", "Cita no encontrada"); continue; }

            String resultado;
            String observacion;

            boolean tieneDestino = dto.getAgendaDestinoId() != null && dto.getFechaDestino() != null;
            if (tieneDestino) {
                Long nuevoSlotId = findFirstAvailableSlotForAgendaFecha(dto.getAgendaDestinoId(), dto.getFechaDestino(), empresaId);
                if (nuevoSlotId != null && disponibilidadQuery.decrementCupos(nuevoSlotId, estadoOcupado)) {
                    // free old slot
                    disponibilidadQuery.incrementCupos(disponibilidadId, estadoDisponible);
                    cita.setDisponibilidad_id(nuevoSlotId);
                    cita.setAgenda_id(dto.getAgendaDestinoId());
                    cita.setEstado_cita_id(estadoAgendada);
                    cita.setObservaciones("Traslado: " + dto.getMotivo());
                    cita.setUsuario_modificacion(usuarioId);
                    citaJpa.save(cita);
                    resultado  = "TRASLADADA";
                    observacion = null;
                    trasladadas++;
                } else {
                    // Add to wait list
                    agregarAListaEspera(pacienteId, especialidadId, row, empresaId, sedeId, usuarioId);
                    cita.setEstado_cita_id(estadoCancelada);
                    cita.setObservaciones("Traslado sin disponibilidad: " + dto.getMotivo());
                    cita.setUsuario_modificacion(usuarioId);
                    citaJpa.save(cita);
                    disponibilidadQuery.incrementCupos(disponibilidadId, estadoDisponible);
                    resultado  = "EN_LISTA_ESPERA";
                    observacion = "Sin disponibilidad en agenda destino";
                    fallidas++;
                }
            } else {
                // No destination — cancel and add to wait list
                agregarAListaEspera(pacienteId, especialidadId, row, empresaId, sedeId, usuarioId);
                cita.setEstado_cita_id(estadoCancelada);
                cita.setObservaciones("Traslado masivo: " + dto.getMotivo());
                cita.setUsuario_modificacion(usuarioId);
                citaJpa.save(cita);
                disponibilidadQuery.incrementCupos(disponibilidadId, estadoDisponible);
                resultado  = "EN_LISTA_ESPERA";
                observacion = "Sin agenda destino configurada";
                trasladadas++;
            }
            saveDetalle(savedTraslado.getId(), citaId, resultado, observacion);
        }

        savedTraslado.setCitas_trasladadas(trasladadas);
        savedTraslado.setCitas_fallidas(fallidas);
        trasladoJpa.save(savedTraslado);

        BulkTransferResponseDto response = new BulkTransferResponseDto();
        response.setTrasladoId(savedTraslado.getId());
        response.setFechaOrigen(dto.getFechaOrigen());
        response.setFechaDestino(dto.getFechaDestino());
        response.setTotalCitas(citas.size());
        response.setCitasTrasladadas(trasladadas);
        response.setCitasFallidas(fallidas);
        return response;
    }

    private Long findFirstAvailableSlotForAgendaFecha(Long agendaId, LocalDate fecha, Long empresaId) {
        com.cloud_tecnological.mednova.dto.disponibilidad.SearchDisponibilidadRequestDto req =
            new com.cloud_tecnological.mednova.dto.disponibilidad.SearchDisponibilidadRequestDto();
        req.setAgendaId(agendaId);
        req.setFechaDesde(fecha);
        req.setFechaHasta(fecha);
        var results = disponibilidadQuery.searchAvailable(req, empresaId, TenantContext.getSedeId());
        if (results.isEmpty()) return null;
        return results.get(0).getId();
    }

    private void agregarAListaEspera(Long pacienteId, Long especialidadId, Map<String, Object> row,
                                      Long empresaId, Long sedeId, Long usuarioId) {
        if (especialidadId == null) return;
        ListaEsperaCitaEntity le = new ListaEsperaCitaEntity();
        le.setEmpresa_id(empresaId);
        le.setSede_id(sedeId);
        le.setPaciente_id(pacienteId);
        le.setEspecialidad_id(especialidadId);
        le.setPrioridad(3);
        le.setEstado("ACTIVA");
        le.setObservaciones("Generado por traslado masivo");
        le.setUsuario_creacion(usuarioId);
        listaEsperaJpa.save(le);
    }

    private void saveDetalle(Long trasladoId, Long citaId, String resultado, String observacion) {
        DetalleTrasladoAgendaEntity detalle = new DetalleTrasladoAgendaEntity();
        detalle.setTraslado_id(trasladoId);
        detalle.setCita_id(citaId);
        detalle.setResultado(resultado);
        detalle.setObservacion(observacion);
        detalleTrasladoJpa.save(detalle);
    }
}
