package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.radicacion.CrearRadicacionRequestDto;
import com.cloud_tecnological.mednova.dto.radicacion.RadicacionResponseDto;
import com.cloud_tecnological.mednova.dto.radicacion.RadicacionTableDto;
import com.cloud_tecnological.mednova.dto.radicacion.RegistrarRespuestaRequestDto;
import com.cloud_tecnological.mednova.entity.RadicacionEntity;
import com.cloud_tecnological.mednova.repositories.factura.FacturaJpaRepository;
import com.cloud_tecnological.mednova.repositories.radicacion.RadicacionJpaRepository;
import com.cloud_tecnological.mednova.repositories.radicacion.RadicacionQueryRepository;
import com.cloud_tecnological.mednova.services.RadicacionService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class RadicacionServiceImpl implements RadicacionService {

    private final RadicacionJpaRepository radicacionJpa;
    private final RadicacionQueryRepository radicacionQuery;
    private final FacturaJpaRepository facturaJpa;

    public RadicacionServiceImpl(
            RadicacionJpaRepository radicacionJpa,
            RadicacionQueryRepository radicacionQuery,
            FacturaJpaRepository facturaJpa) {
        this.radicacionJpa   = radicacionJpa;
        this.radicacionQuery = radicacionQuery;
        this.facturaJpa      = facturaJpa;
    }

    @Override
    @Transactional
    public RadicacionResponseDto crear(CrearRadicacionRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        var factura = facturaJpa.findById(dto.getFacturaId())
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Factura no encontrada"));

        if (!factura.getEmpresa_id().equals(empresaId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Factura no encontrada");
        }

        Long estadoId = radicacionQuery.findEstadoRadicacionIdByCodigo("RADICADA");
        if (estadoId == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Estado de radicación 'RADICADA' no configurado en el sistema");
        }

        RadicacionEntity entity = new RadicacionEntity();
        entity.setEmpresa_id(empresaId);
        entity.setSede_id(sedeId);
        entity.setFactura_id(dto.getFacturaId());
        entity.setPagador_id(factura.getPagador_id());
        entity.setEstado_radicacion_id(estadoId);
        entity.setNumero_radicado(dto.getNumeroRadicado());
        entity.setFecha_radicacion(dto.getFechaRadicacion() != null
            ? dto.getFechaRadicacion() : LocalDate.now());
        entity.setFecha_limite_respuesta(dto.getFechaLimiteRespuesta());
        entity.setSoporte_url(dto.getSoporteUrl());
        entity.setObservaciones(dto.getObservaciones());
        entity.setUsuario_creacion(usuarioId);
        RadicacionEntity saved = radicacionJpa.save(entity);

        return radicacionQuery.findActiveById(saved.getId(), empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar radicación"));
    }

    @Override
    public RadicacionResponseDto findById(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        return radicacionQuery.findActiveById(id, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Radicación no encontrada"));
    }

    @Override
    public PageImpl<RadicacionTableDto> listActive(PageableDto<?> request) {
        Long empresaId = TenantContext.getEmpresaId();
        return radicacionQuery.listActive(request, empresaId);
    }

    @Override
    @Transactional
    public RadicacionResponseDto registrarRespuesta(Long id, RegistrarRespuestaRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long usuarioId = TenantContext.getUsuarioId();

        RadicacionEntity entity = radicacionJpa.findActiveByIdAndEmpresaId(id, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Radicación no encontrada"));

        Long nuevoEstadoId = radicacionQuery.findEstadoRadicacionIdByCodigo(dto.getEstadoCodigo());
        if (nuevoEstadoId == null) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                "Estado de radicación '" + dto.getEstadoCodigo() + "' no encontrado");
        }

        entity.setEstado_radicacion_id(nuevoEstadoId);
        entity.setFecha_respuesta(dto.getFechaRespuesta() != null
            ? dto.getFechaRespuesta() : LocalDate.now());
        if (dto.getObservaciones() != null && !dto.getObservaciones().isBlank()) {
            entity.setObservaciones(dto.getObservaciones());
        }
        entity.setUsuario_modificacion(usuarioId);
        radicacionJpa.save(entity);

        return radicacionQuery.findActiveById(id, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar radicación"));
    }

    @Override
    @Transactional
    public Boolean anular(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long usuarioId = TenantContext.getUsuarioId();

        RadicacionEntity entity = radicacionJpa.findActiveByIdAndEmpresaId(id, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Radicación no encontrada"));

        entity.setActivo(false);
        entity.setDeleted_at(java.time.LocalDateTime.now());
        entity.setUsuario_modificacion(usuarioId);
        radicacionJpa.save(entity);
        return true;
    }
}
