package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.prescripcion.CreatePrescripcionRequestDto;
import com.cloud_tecnological.mednova.dto.prescripcion.DetallePrescripcionRequestDto;
import com.cloud_tecnological.mednova.dto.prescripcion.PrescripcionResponseDto;
import com.cloud_tecnological.mednova.entity.AtencionEntity;
import com.cloud_tecnological.mednova.entity.DetallePrescripcionEntity;
import com.cloud_tecnological.mednova.entity.PrescripcionEntity;
import com.cloud_tecnological.mednova.repositories.atencion.AtencionJpaRepository;
import com.cloud_tecnological.mednova.repositories.prescripcion.DetallePrescripcionJpaRepository;
import com.cloud_tecnological.mednova.repositories.prescripcion.PrescripcionJpaRepository;
import com.cloud_tecnological.mednova.repositories.prescripcion.PrescripcionQueryRepository;
import com.cloud_tecnological.mednova.services.PrescripcionService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrescripcionServiceImpl implements PrescripcionService {

    private final PrescripcionJpaRepository prescripcionJpaRepository;
    private final DetallePrescripcionJpaRepository detalleJpaRepository;
    private final PrescripcionQueryRepository prescripcionQueryRepository;
    private final AtencionJpaRepository atencionJpaRepository;

    public PrescripcionServiceImpl(PrescripcionJpaRepository prescripcionJpaRepository,
                                   DetallePrescripcionJpaRepository detalleJpaRepository,
                                   PrescripcionQueryRepository prescripcionQueryRepository,
                                   AtencionJpaRepository atencionJpaRepository) {
        this.prescripcionJpaRepository  = prescripcionJpaRepository;
        this.detalleJpaRepository       = detalleJpaRepository;
        this.prescripcionQueryRepository = prescripcionQueryRepository;
        this.atencionJpaRepository      = atencionJpaRepository;
    }

    @Override
    @Transactional
    public PrescripcionResponseDto create(CreatePrescripcionRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        AtencionEntity atencion = atencionJpaRepository.findById(dto.getAtencionId())
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada"));
        validateAtencionTenant(atencion, empresaId, sedeId);

        for (DetallePrescripcionRequestDto item : dto.getItems()) {
            if (!prescripcionQueryRepository.existsServicioSalud(item.getServicioSaludId(), empresaId)) {
                throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "El medicamento/servicio " + item.getServicioSaludId() + " no existe en su empresa");
            }
            if (!prescripcionQueryRepository.existsViaAdministracion(item.getViaAdministracionId())) {
                throw new GlobalException(HttpStatus.BAD_REQUEST, "Vía de administración no válida");
            }
            if (!prescripcionQueryRepository.existsFrecuenciaDosis(item.getFrecuenciaDosisId())) {
                throw new GlobalException(HttpStatus.BAD_REQUEST, "Frecuencia de dosis no válida");
            }
        }

        Long profesionalId = prescripcionQueryRepository.findProfesionalByUsuario(usuarioId, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST,
                "El usuario no está registrado como profesional de salud en esta empresa"));

        Long estadoId = prescripcionQueryRepository.findEstadoPrescripcionIdByCodigo("ACTIVA");
        if (estadoId == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Estado 'ACTIVA' de prescripción no configurado");
        }

        String numeroPrescripcion = prescripcionQueryRepository.generateNumeroPrescripcion(empresaId);

        PrescripcionEntity prescripcion = new PrescripcionEntity();
        prescripcion.setEmpresa_id(empresaId);
        prescripcion.setSede_id(sedeId);
        prescripcion.setAtencion_id(dto.getAtencionId());
        prescripcion.setNumero_prescripcion(numeroPrescripcion);
        prescripcion.setEstado_prescripcion_id(estadoId);
        prescripcion.setProfesional_id(profesionalId);
        prescripcion.setObservaciones(dto.getObservaciones());
        prescripcion.setUsuario_creacion(usuarioId);
        PrescripcionEntity saved = prescripcionJpaRepository.save(prescripcion);

        for (DetallePrescripcionRequestDto item : dto.getItems()) {
            DetallePrescripcionEntity detalle = new DetallePrescripcionEntity();
            detalle.setEmpresa_id(empresaId);
            detalle.setPrescripcion_id(saved.getId());
            detalle.setServicio_salud_id(item.getServicioSaludId());
            detalle.setDosis(item.getDosis());
            detalle.setUnidad_dosis(item.getUnidadDosis());
            detalle.setVia_administracion_id(item.getViaAdministracionId());
            detalle.setFrecuencia_dosis_id(item.getFrecuenciaDosisId());
            detalle.setDuracion_dias(item.getDuracionDias());
            detalle.setCantidad_despachar(item.getCantidadDespachar());
            detalle.setIndicaciones(item.getIndicaciones());
            detalleJpaRepository.save(detalle);
        }

        return prescripcionQueryRepository.findByAtencionId(dto.getAtencionId(), empresaId, sedeId)
            .stream()
            .filter(p -> p.getId().equals(saved.getId()))
            .findFirst()
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la prescripción"));
    }

    @Override
    public List<PrescripcionResponseDto> findByAtencionId(Long atencionId) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();

        atencionJpaRepository.findById(atencionId)
            .filter(a -> a.getEmpresa_id().equals(empresaId)
                      && a.getSede_id().equals(sedeId)
                      && a.getDeleted_at() == null)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada"));

        return prescripcionQueryRepository.findByAtencionId(atencionId, empresaId, sedeId);
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        PrescripcionEntity entity = prescripcionJpaRepository.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Prescripción no encontrada"));

        if (!entity.getEmpresa_id().equals(empresaId) || !entity.getSede_id().equals(sedeId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Prescripción no encontrada");
        }
        if (entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "La prescripción ya fue eliminada");
        }

        entity.setDeleted_at(java.time.LocalDateTime.now());
        entity.setUsuario_modificacion(usuarioId);
        prescripcionJpaRepository.save(entity);
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
