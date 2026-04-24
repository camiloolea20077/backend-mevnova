package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.tarifacontrato.CreateTarifaContratoRequestDto;
import com.cloud_tecnological.mednova.dto.tarifacontrato.TarifaContratoResponseDto;
import com.cloud_tecnological.mednova.dto.tarifacontrato.UpdateTarifaContratoRequestDto;
import com.cloud_tecnological.mednova.entity.TarifaContratoEntity;
import com.cloud_tecnological.mednova.repositories.contrato.ContratoJpaRepository;
import com.cloud_tecnological.mednova.repositories.serviciosalud.ServicioSaludJpaRepository;
import com.cloud_tecnological.mednova.repositories.tarifacontrato.TarifaContratoJpaRepository;
import com.cloud_tecnological.mednova.repositories.tarifacontrato.TarifaContratoQueryRepository;
import com.cloud_tecnological.mednova.services.TarifaContratoService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TarifaContratoServiceImpl implements TarifaContratoService {

    private final TarifaContratoJpaRepository   jpaRepository;
    private final TarifaContratoQueryRepository queryRepository;
    private final ContratoJpaRepository         contratoJpaRepository;
    private final ServicioSaludJpaRepository    servicioJpaRepository;

    public TarifaContratoServiceImpl(
            TarifaContratoJpaRepository jpaRepository,
            TarifaContratoQueryRepository queryRepository,
            ContratoJpaRepository contratoJpaRepository,
            ServicioSaludJpaRepository servicioJpaRepository) {
        this.jpaRepository         = jpaRepository;
        this.queryRepository       = queryRepository;
        this.contratoJpaRepository  = contratoJpaRepository;
        this.servicioJpaRepository  = servicioJpaRepository;
    }

    @Override
    @Transactional
    public TarifaContratoResponseDto create(Long contratoId, CreateTarifaContratoRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        validateContrato(contratoId, empresa_id);

        servicioJpaRepository.findById(request.getHealthServiceId())
                .filter(s -> empresa_id.equals(s.getEmpresa_id()) && s.getDeleted_at() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Servicio de salud no encontrado en la empresa"));

        if (queryRepository.existsByServicio(contratoId, request.getHealthServiceId(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "El servicio ya tiene una tarifa registrada en este contrato");
        }

        TarifaContratoEntity entity = new TarifaContratoEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setContrato_id(contratoId);
        entity.setServicio_salud_id(request.getHealthServiceId());
        entity.setValor(request.getValue());
        entity.setPorcentaje_descuento(request.getDiscountPercentage());
        entity.setFecha_vigencia_desde(request.getValidFrom());
        entity.setFecha_vigencia_hasta(request.getValidUntil());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);

        TarifaContratoEntity saved = jpaRepository.save(entity);
        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la tarifa creada"));
    }

    @Override
    public TarifaContratoResponseDto findById(Long contratoId, Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        validateContrato(contratoId, empresa_id);
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Tarifa de contrato no encontrada"));
    }

    @Override
    @Transactional
    public TarifaContratoResponseDto update(Long contratoId, Long id, UpdateTarifaContratoRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        validateContrato(contratoId, empresa_id);

        TarifaContratoEntity entity = getValidEntity(id, contratoId, empresa_id);
        entity.setValor(request.getValue());
        entity.setPorcentaje_descuento(request.getDiscountPercentage());
        entity.setFecha_vigencia_desde(request.getValidFrom());
        entity.setFecha_vigencia_hasta(request.getValidUntil());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la tarifa actualizada"));
    }

    @Override
    @Transactional
    public Boolean remove(Long contratoId, Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        validateContrato(contratoId, empresa_id);

        TarifaContratoEntity entity = getValidEntity(id, contratoId, empresa_id);
        entity.setActivo(false);
        entity.setUsuario_modificacion(TenantContext.getUsuarioId());
        jpaRepository.save(entity);
        return true;
    }

    @Override
    public List<TarifaContratoResponseDto> listByContrato(Long contratoId) {
        Long empresa_id = TenantContext.getEmpresaId();
        validateContrato(contratoId, empresa_id);
        return queryRepository.listByContrato(contratoId, empresa_id);
    }

    private void validateContrato(Long contratoId, Long empresa_id) {
        contratoJpaRepository.findById(contratoId)
                .filter(c -> empresa_id.equals(c.getEmpresa_id()) && c.getDeleted_at() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Contrato no encontrado en la empresa"));
    }

    private TarifaContratoEntity getValidEntity(Long id, Long contratoId, Long empresa_id) {
        TarifaContratoEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Tarifa de contrato no encontrada"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || !contratoId.equals(entity.getContrato_id()) || !Boolean.TRUE.equals(entity.getActivo())) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Tarifa de contrato no encontrada");
        }
        return entity;
    }
}
