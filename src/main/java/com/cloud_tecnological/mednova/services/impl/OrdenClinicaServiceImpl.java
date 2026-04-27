package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.ordenclinica.CreateOrdenClinicaRequestDto;
import com.cloud_tecnological.mednova.dto.ordenclinica.DetalleOrdenRequestDto;
import com.cloud_tecnological.mednova.dto.ordenclinica.OrdenClinicaResponseDto;
import com.cloud_tecnological.mednova.entity.AtencionEntity;
import com.cloud_tecnological.mednova.entity.DetalleOrdenClinicaEntity;
import com.cloud_tecnological.mednova.entity.OrdenClinicaEntity;
import com.cloud_tecnological.mednova.repositories.atencion.AtencionJpaRepository;
import com.cloud_tecnological.mednova.repositories.ordenclinica.DetalleOrdenClinicaJpaRepository;
import com.cloud_tecnological.mednova.repositories.ordenclinica.OrdenClinicaJpaRepository;
import com.cloud_tecnological.mednova.repositories.ordenclinica.OrdenClinicaQueryRepository;
import com.cloud_tecnological.mednova.services.OrdenClinicaService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrdenClinicaServiceImpl implements OrdenClinicaService {

    private final OrdenClinicaJpaRepository ordenJpaRepository;
    private final DetalleOrdenClinicaJpaRepository detalleJpaRepository;
    private final OrdenClinicaQueryRepository ordenQueryRepository;
    private final AtencionJpaRepository atencionJpaRepository;

    public OrdenClinicaServiceImpl(OrdenClinicaJpaRepository ordenJpaRepository,
                                   DetalleOrdenClinicaJpaRepository detalleJpaRepository,
                                   OrdenClinicaQueryRepository ordenQueryRepository,
                                   AtencionJpaRepository atencionJpaRepository) {
        this.ordenJpaRepository    = ordenJpaRepository;
        this.detalleJpaRepository  = detalleJpaRepository;
        this.ordenQueryRepository  = ordenQueryRepository;
        this.atencionJpaRepository = atencionJpaRepository;
    }

    @Override
    @Transactional
    public OrdenClinicaResponseDto create(CreateOrdenClinicaRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        AtencionEntity atencion = atencionJpaRepository.findById(dto.getAtencionId())
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada"));
        validateAtencionTenant(atencion, empresaId, sedeId);

        if (!ordenQueryRepository.existsTipoOrdenClinica(dto.getTipoOrden())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Tipo de orden clínica no válido");
        }

        for (DetalleOrdenRequestDto item : dto.getItems()) {
            if (!ordenQueryRepository.existsServicioSalud(item.getServicioSaludId(), empresaId)) {
                throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "El servicio " + item.getServicioSaludId() + " no existe en su empresa");
            }
        }

        Long profesionalId = ordenQueryRepository.findProfesionalByUsuario(usuarioId, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST,
                "El usuario no está registrado como profesional de salud en esta empresa"));

        String numeroOrden = ordenQueryRepository.generateNumeroOrden(empresaId);

        OrdenClinicaEntity orden = new OrdenClinicaEntity();
        orden.setEmpresa_id(empresaId);
        orden.setSede_id(sedeId);
        orden.setAtencion_id(dto.getAtencionId());
        orden.setNumero_orden(numeroOrden);
        orden.setTipo_orden(dto.getTipoOrden());
        orden.setEstado_orden("PENDIENTE");
        orden.setProfesional_id(profesionalId);
        orden.setObservaciones(dto.getObservaciones());
        orden.setUsuario_creacion(usuarioId);
        OrdenClinicaEntity savedOrden = ordenJpaRepository.save(orden);

        for (DetalleOrdenRequestDto item : dto.getItems()) {
            DetalleOrdenClinicaEntity detalle = new DetalleOrdenClinicaEntity();
            detalle.setEmpresa_id(empresaId);
            detalle.setOrden_clinica_id(savedOrden.getId());
            detalle.setServicio_salud_id(item.getServicioSaludId());
            if (item.getCantidad() != null) detalle.setCantidad(item.getCantidad());
            detalle.setIndicaciones(item.getIndicaciones());
            if (item.getUrgencia() != null) detalle.setUrgencia(item.getUrgencia());
            detalleJpaRepository.save(detalle);
        }

        return ordenQueryRepository.findByAtencionId(dto.getAtencionId(), empresaId, sedeId)
            .stream()
            .filter(o -> o.getId().equals(savedOrden.getId()))
            .findFirst()
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la orden"));
    }

    @Override
    public List<OrdenClinicaResponseDto> findByAtencionId(Long atencionId) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();

        atencionJpaRepository.findById(atencionId)
            .filter(a -> a.getEmpresa_id().equals(empresaId)
                      && a.getSede_id().equals(sedeId)
                      && a.getDeleted_at() == null)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada"));

        return ordenQueryRepository.findByAtencionId(atencionId, empresaId, sedeId);
    }

    @Override
    public List<OrdenClinicaResponseDto> findActiveByAtencionId(Long atencionId) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();

        atencionJpaRepository.findById(atencionId)
            .filter(a -> a.getEmpresa_id().equals(empresaId)
                      && a.getSede_id().equals(sedeId)
                      && a.getDeleted_at() == null)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada"));

        return ordenQueryRepository.findActiveByAtencionId(atencionId, empresaId, sedeId);
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        OrdenClinicaEntity entity = ordenJpaRepository.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Orden clínica no encontrada"));

        if (!entity.getEmpresa_id().equals(empresaId) || !entity.getSede_id().equals(sedeId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Orden clínica no encontrada");
        }
        if (entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "La orden ya fue eliminada");
        }

        entity.setDeleted_at(java.time.LocalDateTime.now());
        entity.setUsuario_modificacion(usuarioId);
        ordenJpaRepository.save(entity);
        return true;
    }

    private void validateAtencionTenant(AtencionEntity atencion, Long empresaId, Long sedeId) {
        if (atencion.getDeleted_at() != null
                || !atencion.getEmpresa_id().equals(empresaId)
                || !atencion.getSede_id().equals(sedeId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada");
        }
    }
}
