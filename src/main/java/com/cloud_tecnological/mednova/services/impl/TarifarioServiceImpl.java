package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.tarifario.*;
import com.cloud_tecnological.mednova.entity.DetalleTarifarioEntity;
import com.cloud_tecnological.mednova.entity.TarifarioEntity;
import com.cloud_tecnological.mednova.repositories.serviciosalud.ServicioSaludJpaRepository;
import com.cloud_tecnological.mednova.repositories.tarifario.DetalleTarifarioJpaRepository;
import com.cloud_tecnological.mednova.repositories.tarifario.TarifarioJpaRepository;
import com.cloud_tecnological.mednova.repositories.tarifario.TarifarioQueryRepository;
import com.cloud_tecnological.mednova.services.TarifarioService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TarifarioServiceImpl implements TarifarioService {

    private final TarifarioJpaRepository       jpaRepository;
    private final DetalleTarifarioJpaRepository detalleJpaRepository;
    private final TarifarioQueryRepository     queryRepository;
    private final ServicioSaludJpaRepository   servicioJpaRepository;

    public TarifarioServiceImpl(
            TarifarioJpaRepository jpaRepository,
            DetalleTarifarioJpaRepository detalleJpaRepository,
            TarifarioQueryRepository queryRepository,
            ServicioSaludJpaRepository servicioJpaRepository) {
        this.jpaRepository        = jpaRepository;
        this.detalleJpaRepository = detalleJpaRepository;
        this.queryRepository      = queryRepository;
        this.servicioJpaRepository = servicioJpaRepository;
    }

    @Override
    @Transactional
    public TarifarioResponseDto create(CreateTarifarioRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        if (queryRepository.existsByCode(request.getCode(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "El código de tarifario ya existe en la empresa");
        }

        TarifarioEntity entity = new TarifarioEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setCodigo(request.getCode());
        entity.setNombre(request.getName());
        entity.setDescripcion(request.getDescription());
        entity.setFecha_vigencia_desde(request.getValidFrom());
        entity.setFecha_vigencia_hasta(request.getValidUntil());
        entity.setUsuario_creacion(usuario_id);

        TarifarioEntity saved = jpaRepository.save(entity);
        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el tarifario creado"));
    }

    @Override
    public TarifarioResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Tarifario no encontrado"));
    }

    @Override
    @Transactional
    public TarifarioResponseDto update(Long id, UpdateTarifarioRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        TarifarioEntity entity = getValidEntity(id, empresa_id);
        entity.setNombre(request.getName());
        entity.setDescripcion(request.getDescription());
        entity.setFecha_vigencia_desde(request.getValidFrom());
        entity.setFecha_vigencia_hasta(request.getValidUntil());
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el tarifario actualizado"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        TarifarioEntity entity = getValidEntity(id, empresa_id);
        entity.setActivo(!Boolean.TRUE.equals(entity.getActivo()));
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);
        return entity.getActivo();
    }

    @Override
    public PageImpl<TarifarioTableDto> list(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.listTarifarios(pageable, empresa_id);
    }

    // ---------- Detalle ----------

    @Override
    @Transactional
    public DetalleTarifarioResponseDto upsertDetalle(Long tarifarioId, UpsertDetalleTarifarioRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();

        getValidEntity(tarifarioId, empresa_id);

        servicioJpaRepository.findById(request.getHealthServiceId())
                .filter(s -> empresa_id.equals(s.getEmpresa_id()) && s.getDeleted_at() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Servicio de salud no encontrado en la empresa"));

        if (queryRepository.existsDetalleByServicio(tarifarioId, request.getHealthServiceId(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "El servicio ya está en el tarifario");
        }

        DetalleTarifarioEntity entity = new DetalleTarifarioEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setTarifario_id(tarifarioId);
        entity.setServicio_salud_id(request.getHealthServiceId());
        entity.setValor(request.getValue());
        entity.setObservaciones(request.getObservations());

        DetalleTarifarioEntity saved = detalleJpaRepository.save(entity);
        return queryRepository.findDetalleById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el detalle creado"));
    }

    @Override
    public DetalleTarifarioResponseDto findDetalleById(Long tarifarioId, Long detalleId) {
        Long empresa_id = TenantContext.getEmpresaId();
        getValidEntity(tarifarioId, empresa_id);
        return queryRepository.findDetalleById(detalleId, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Detalle de tarifario no encontrado"));
    }

    @Override
    public List<DetalleTarifarioResponseDto> listDetalles(Long tarifarioId) {
        Long empresa_id = TenantContext.getEmpresaId();
        getValidEntity(tarifarioId, empresa_id);
        return queryRepository.listDetalles(tarifarioId, empresa_id);
    }

    @Override
    @Transactional
    public Boolean removeDetalle(Long tarifarioId, Long detalleId) {
        Long empresa_id = TenantContext.getEmpresaId();
        getValidEntity(tarifarioId, empresa_id);

        DetalleTarifarioEntity detalle = detalleJpaRepository.findById(detalleId)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Detalle de tarifario no encontrado"));
        if (!empresa_id.equals(detalle.getEmpresa_id()) || !tarifarioId.equals(detalle.getTarifario_id())) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Detalle de tarifario no encontrado");
        }
        detalle.setActivo(false);
        detalleJpaRepository.save(detalle);
        return true;
    }

    private TarifarioEntity getValidEntity(Long id, Long empresa_id) {
        TarifarioEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Tarifario no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Tarifario no encontrado");
        }
        return entity;
    }
}
