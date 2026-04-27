package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.listaespera.*;
import com.cloud_tecnological.mednova.entity.ListaEsperaCitaEntity;
import com.cloud_tecnological.mednova.repositories.listaespera.ListaEsperaCitaJpaRepository;
import com.cloud_tecnological.mednova.repositories.listaespera.ListaEsperaCitaQueryRepository;
import com.cloud_tecnological.mednova.services.ListaEsperaCitaService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListaEsperaCitaServiceImpl implements ListaEsperaCitaService {

    private final ListaEsperaCitaJpaRepository listaEsperaJpa;
    private final ListaEsperaCitaQueryRepository listaEsperaQuery;

    public ListaEsperaCitaServiceImpl(ListaEsperaCitaJpaRepository listaEsperaJpa,
                                       ListaEsperaCitaQueryRepository listaEsperaQuery) {
        this.listaEsperaJpa   = listaEsperaJpa;
        this.listaEsperaQuery = listaEsperaQuery;
    }

    @Override
    @Transactional
    public WaitListResponseDto create(CreateWaitListRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        if (dto.getEspecialidadId() == null && dto.getServicioSaludId() == null) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Debe especificar al menos especialidad o servicio de salud");
        }

        if (dto.getEspecialidadId() != null &&
            listaEsperaQuery.existsActivaForPaciente(dto.getPacienteId(), dto.getEspecialidadId(), empresaId, sedeId)) {
            throw new GlobalException(HttpStatus.CONFLICT, "El paciente ya está en lista de espera para esta especialidad");
        }

        ListaEsperaCitaEntity entity = new ListaEsperaCitaEntity();
        entity.setEmpresa_id(empresaId);
        entity.setSede_id(sedeId);
        entity.setPaciente_id(dto.getPacienteId());
        entity.setEspecialidad_id(dto.getEspecialidadId());
        entity.setServicio_salud_id(dto.getServicioSaludId());
        entity.setPrioridad(dto.getPrioridad() != null ? dto.getPrioridad() : 3);
        entity.setFecha_preferida_desde(dto.getFechaPreferidaDesde());
        entity.setFecha_preferida_hasta(dto.getFechaPreferidaHasta());
        entity.setObservaciones(dto.getObservaciones());
        entity.setUsuario_creacion(usuarioId);
        ListaEsperaCitaEntity saved = listaEsperaJpa.save(entity);

        return listaEsperaQuery.findActiveById(saved.getId(), empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar entrada de lista de espera"));
    }

    @Override
    public WaitListResponseDto findById(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        return listaEsperaQuery.findActiveById(id, empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Entrada de lista de espera no encontrada"));
    }

    @Override
    public PageImpl<WaitListTableDto> listActivos(PageableDto<?> request) {
        return listaEsperaQuery.listActivos(request, TenantContext.getEmpresaId(), TenantContext.getSedeId());
    }

    @Override
    @Transactional
    public Boolean cancel(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        ListaEsperaCitaEntity entity = listaEsperaJpa.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Entrada de lista de espera no encontrada"));
        if (!entity.getEmpresa_id().equals(empresaId) || !entity.getSede_id().equals(sedeId) || !Boolean.TRUE.equals(entity.getActivo())) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Entrada de lista de espera no encontrada");
        }

        entity.setEstado("CANCELADA");
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuarioId);
        listaEsperaJpa.save(entity);
        return true;
    }
}
