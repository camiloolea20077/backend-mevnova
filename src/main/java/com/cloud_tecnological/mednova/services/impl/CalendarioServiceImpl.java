package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.calendario.*;
import com.cloud_tecnological.mednova.entity.CalendarioCitaEntity;
import com.cloud_tecnological.mednova.entity.DetalleCalendarioCitaEntity;
import com.cloud_tecnological.mednova.repositories.calendario.CalendarioCitaJpaRepository;
import com.cloud_tecnological.mednova.repositories.calendario.CalendarioCitaQueryRepository;
import com.cloud_tecnological.mednova.repositories.calendario.DetalleCalendarioCitaJpaRepository;
import com.cloud_tecnological.mednova.services.CalendarioService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CalendarioServiceImpl implements CalendarioService {

    private final CalendarioCitaJpaRepository calendarioJpa;
    private final DetalleCalendarioCitaJpaRepository detalleJpa;
    private final CalendarioCitaQueryRepository calendarioQuery;

    public CalendarioServiceImpl(CalendarioCitaJpaRepository calendarioJpa,
                                  DetalleCalendarioCitaJpaRepository detalleJpa,
                                  CalendarioCitaQueryRepository calendarioQuery) {
        this.calendarioJpa  = calendarioJpa;
        this.detalleJpa     = detalleJpa;
        this.calendarioQuery = calendarioQuery;
    }

    @Override
    @Transactional
    public CalendarioResponseDto create(CreateCalendarioRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long usuarioId = TenantContext.getUsuarioId();

        if (calendarioQuery.existsByCodigo(dto.getCodigo().toUpperCase(), empresaId)) {
            throw new GlobalException(HttpStatus.CONFLICT, "Ya existe un calendario con ese código");
        }

        CalendarioCitaEntity entity = new CalendarioCitaEntity();
        entity.setEmpresa_id(empresaId);
        entity.setCodigo(dto.getCodigo().toUpperCase().trim());
        entity.setNombre(dto.getNombre().trim());
        entity.setDescripcion(dto.getDescripcion());
        entity.setUsuario_creacion(usuarioId);
        CalendarioCitaEntity saved = calendarioJpa.save(entity);

        return calendarioQuery.findActiveById(saved.getId(), empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar calendario creado"));
    }

    @Override
    public CalendarioResponseDto findById(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        return calendarioQuery.findActiveById(id, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Calendario no encontrado"));
    }

    @Override
    public PageImpl<CalendarioTableDto> listActivos(PageableDto<?> request) {
        return calendarioQuery.listActivos(request, TenantContext.getEmpresaId());
    }

    @Override
    @Transactional
    public CalendarioResponseDto addDetalle(Long calendarioId, AddDetalleCalendarioRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();

        CalendarioCitaEntity calendario = calendarioJpa.findById(calendarioId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Calendario no encontrado"));
        if (!calendario.getEmpresa_id().equals(empresaId) || !Boolean.TRUE.equals(calendario.getActivo())) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Calendario no encontrado");
        }

        // upsert: delete existing entry for same date then insert
        detalleJpa.findByCalendarioId(calendarioId).stream()
            .filter(d -> d.getFecha().equals(dto.getFecha()))
            .forEach(detalleJpa::delete);

        DetalleCalendarioCitaEntity detalle = new DetalleCalendarioCitaEntity();
        detalle.setEmpresa_id(empresaId);
        detalle.setCalendario_id(calendarioId);
        detalle.setFecha(dto.getFecha());
        detalle.setEs_habil(dto.getEsHabil() != null ? dto.getEsHabil() : true);
        detalle.setEs_festivo(dto.getEsFestivo() != null ? dto.getEsFestivo() : false);
        detalle.setDescripcion(dto.getDescripcion());
        detalleJpa.save(detalle);

        return calendarioQuery.findActiveById(calendarioId, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar calendario"));
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long usuarioId = TenantContext.getUsuarioId();

        CalendarioCitaEntity entity = calendarioJpa.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Calendario no encontrado"));
        if (!entity.getEmpresa_id().equals(empresaId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Calendario no encontrado");
        }

        entity.setActivo(false);
        entity.setUsuario_modificacion(usuarioId);
        calendarioJpa.save(entity);
        return true;
    }
}
