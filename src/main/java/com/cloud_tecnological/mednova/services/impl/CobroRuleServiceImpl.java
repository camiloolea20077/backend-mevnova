package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.cobrorule.*;
import com.cloud_tecnological.mednova.entity.ReglaCobroPacienteEntity;
import com.cloud_tecnological.mednova.entity.ServicioExentoCobroEntity;
import com.cloud_tecnological.mednova.repositories.cobrorule.*;
import com.cloud_tecnological.mednova.services.CobroRuleService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CobroRuleServiceImpl implements CobroRuleService {

    private final ReglaCobroPacienteJpaRepository reglaJpa;
    private final ReglaCobroPacienteQueryRepository reglaQuery;
    private final ServicioExentoCobroJpaRepository exencionJpa;
    private final ServicioExentoCobroQueryRepository exencionQuery;

    public CobroRuleServiceImpl(
            ReglaCobroPacienteJpaRepository reglaJpa,
            ReglaCobroPacienteQueryRepository reglaQuery,
            ServicioExentoCobroJpaRepository exencionJpa,
            ServicioExentoCobroQueryRepository exencionQuery) {
        this.reglaJpa     = reglaJpa;
        this.reglaQuery   = reglaQuery;
        this.exencionJpa  = exencionJpa;
        this.exencionQuery = exencionQuery;
    }

    @Override
    @Transactional
    public CobroRuleResponseDto create(CreateCobroRuleRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long usuarioId = TenantContext.getUsuarioId();

        if (reglaQuery.existsDuplicateActiveRule(empresaId, dto.getVigencia(),
                dto.getRegimenId(), dto.getTipoCobro(), null)) {
            throw new GlobalException(HttpStatus.CONFLICT,
                "Ya existe una regla activa para la misma vigencia, régimen y tipo de cobro");
        }

        ReglaCobroPacienteEntity entity = new ReglaCobroPacienteEntity();
        entity.setEmpresa_id(empresaId);
        entity.setVigencia(dto.getVigencia());
        entity.setRegimen_id(dto.getRegimenId());
        entity.setTipo_cobro(dto.getTipoCobro());
        entity.setRango_ingreso_desde(dto.getRangoIngresoDesde());
        entity.setRango_ingreso_hasta(dto.getRangoIngresoHasta());
        entity.setCategoria_sisben_id(dto.getCategoriaSisbenId());
        entity.setPorcentaje_cobro(dto.getPorcentajeCobro());
        entity.setValor_fijo(dto.getValorFijo());
        entity.setTope_evento(dto.getTopeEvento());
        entity.setTope_anual(dto.getTopeAnual());
        entity.setUnidad_valor(dto.getUnidadValor());
        entity.setObservaciones(dto.getObservations());
        entity.setUsuario_creacion(usuarioId);

        ReglaCobroPacienteEntity saved = reglaJpa.save(entity);

        return reglaQuery.findActiveById(saved.getId(), empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la regla creada"));
    }

    @Override
    public CobroRuleResponseDto findById(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        return reglaQuery.findActiveById(id, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Regla de cobro no encontrada"));
    }

    @Override
    @Transactional
    public Boolean update(Long id, UpdateCobroRuleRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long usuarioId = TenantContext.getUsuarioId();

        ReglaCobroPacienteEntity entity = reglaJpa.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Regla de cobro no encontrada"));

        validateTenant(entity, empresaId);

        if (dto.getRangoIngresoDesde() != null) entity.setRango_ingreso_desde(dto.getRangoIngresoDesde());
        if (dto.getRangoIngresoHasta() != null) entity.setRango_ingreso_hasta(dto.getRangoIngresoHasta());
        if (dto.getCategoriaSisbenId() != null)  entity.setCategoria_sisben_id(dto.getCategoriaSisbenId());
        if (dto.getPorcentajeCobro() != null)    entity.setPorcentaje_cobro(dto.getPorcentajeCobro());
        if (dto.getValorFijo() != null)          entity.setValor_fijo(dto.getValorFijo());
        if (dto.getTopeEvento() != null)         entity.setTope_evento(dto.getTopeEvento());
        if (dto.getTopeAnual() != null)          entity.setTope_anual(dto.getTopeAnual());
        if (dto.getUnidadValor() != null)        entity.setUnidad_valor(dto.getUnidadValor());
        if (dto.getObservations() != null)       entity.setObservaciones(dto.getObservations());
        if (dto.getActive() != null)             entity.setActivo(dto.getActive());

        entity.setUsuario_modificacion(usuarioId);
        reglaJpa.save(entity);
        return true;
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long usuarioId = TenantContext.getUsuarioId();

        ReglaCobroPacienteEntity entity = reglaJpa.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Regla de cobro no encontrada"));

        validateTenant(entity, empresaId);

        entity.setDeleted_at(LocalDateTime.now());
        entity.setUsuario_modificacion(usuarioId);
        reglaJpa.save(entity);
        return true;
    }

    @Override
    public PageImpl<CobroRuleTableDto> listActive(PageableDto<?> request) {
        Long empresaId = TenantContext.getEmpresaId();
        return reglaQuery.listActive(request, empresaId);
    }

    @Override
    @Transactional
    public ServiceExemptionResponseDto createExemption(CreateServiceExemptionRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long usuarioId = TenantContext.getUsuarioId();

        ServicioExentoCobroEntity entity = new ServicioExentoCobroEntity();
        entity.setEmpresa_id(empresaId);
        entity.setServicio_salud_id(dto.getHealthServiceId());
        entity.setTipo_cobro(dto.getTipoCobro());
        entity.setMotivo_exencion(dto.getMotivoExencion());
        entity.setVigencia_desde(dto.getVigenciaDesde());
        entity.setVigencia_hasta(dto.getVigenciaHasta());
        entity.setUsuario_creacion(usuarioId);

        ServicioExentoCobroEntity saved = exencionJpa.save(entity);

        return exencionQuery.findActiveById(saved.getId(), empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la exención creada"));
    }

    @Override
    public ServiceExemptionResponseDto findExemptionById(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        return exencionQuery.findActiveById(id, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Exención no encontrada"));
    }

    @Override
    @Transactional
    public Boolean deleteExemption(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long usuarioId = TenantContext.getUsuarioId();

        ServicioExentoCobroEntity entity = exencionJpa.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Exención no encontrada"));

        if (!entity.getEmpresa_id().equals(empresaId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Exención no encontrada");
        }

        entity.setDeleted_at(LocalDateTime.now());
        entity.setUsuario_modificacion(usuarioId);
        exencionJpa.save(entity);
        return true;
    }

    @Override
    public PageImpl<ServiceExemptionTableDto> listExemptions(PageableDto<?> request) {
        Long empresaId = TenantContext.getEmpresaId();
        return exencionQuery.listActive(request, empresaId);
    }

    private void validateTenant(ReglaCobroPacienteEntity entity, Long empresaId) {
        if (entity.getDeleted_at() != null || !entity.getEmpresa_id().equals(empresaId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Regla de cobro no encontrada");
        }
    }
}
