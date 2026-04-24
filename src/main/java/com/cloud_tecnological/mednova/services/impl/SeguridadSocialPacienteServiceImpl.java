package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.seguridadsocialpaciente.CreateSeguridadSocialPacienteRequestDto;
import com.cloud_tecnological.mednova.dto.seguridadsocialpaciente.SeguridadSocialPacienteResponseDto;
import com.cloud_tecnological.mednova.entity.SeguridadSocialPacienteEntity;
import com.cloud_tecnological.mednova.repositories.paciente.PacienteJpaRepository;
import com.cloud_tecnological.mednova.repositories.pagador.PagadorJpaRepository;
import com.cloud_tecnological.mednova.repositories.seguridadsocialpaciente.SeguridadSocialPacienteJpaRepository;
import com.cloud_tecnological.mednova.repositories.seguridadsocialpaciente.SeguridadSocialPacienteQueryRepository;
import com.cloud_tecnological.mednova.repositories.tercero.TerceroJpaRepository;
import com.cloud_tecnological.mednova.services.SeguridadSocialPacienteService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeguridadSocialPacienteServiceImpl implements SeguridadSocialPacienteService {

    private final SeguridadSocialPacienteJpaRepository   jpaRepository;
    private final SeguridadSocialPacienteQueryRepository queryRepository;
    private final PacienteJpaRepository                  pacienteJpaRepository;
    private final PagadorJpaRepository                   pagadorJpaRepository;
    private final TerceroJpaRepository                   terceroJpaRepository;

    public SeguridadSocialPacienteServiceImpl(
            SeguridadSocialPacienteJpaRepository jpaRepository,
            SeguridadSocialPacienteQueryRepository queryRepository,
            PacienteJpaRepository pacienteJpaRepository,
            PagadorJpaRepository pagadorJpaRepository,
            TerceroJpaRepository terceroJpaRepository) {
        this.jpaRepository        = jpaRepository;
        this.queryRepository      = queryRepository;
        this.pacienteJpaRepository = pacienteJpaRepository;
        this.pagadorJpaRepository  = pagadorJpaRepository;
        this.terceroJpaRepository  = terceroJpaRepository;
    }

    @Override
    @Transactional
    public SeguridadSocialPacienteResponseDto create(Long patientId, CreateSeguridadSocialPacienteRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        validatePaciente(patientId, empresa_id);

        pagadorJpaRepository.findById(request.getPayerId())
                .filter(p -> empresa_id.equals(p.getEmpresa_id()) && p.getDeleted_at() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Pagador no encontrado en la empresa"));

        if (Boolean.TRUE.equals(request.getIsBeneficiary())) {
            if (request.getCotizanteThirdPartyId() == null) {
                throw new GlobalException(HttpStatus.BAD_REQUEST, "El cotizante es obligatorio para beneficiarios");
            }
            terceroJpaRepository.findById(request.getCotizanteThirdPartyId())
                    .filter(t -> empresa_id.equals(t.getEmpresa_id()) && t.getDeleted_at() == null)
                    .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Tercero cotizante no encontrado en la empresa"));
        }

        boolean setCurrent = request.getCurrent() == null || Boolean.TRUE.equals(request.getCurrent());

        if (setCurrent) {
            queryRepository.deactivateVigentesForPaciente(patientId, empresa_id, usuario_id);
        }

        SeguridadSocialPacienteEntity entity = new SeguridadSocialPacienteEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setPaciente_id(patientId);
        entity.setPagador_id(request.getPayerId());
        entity.setRegimen_id(request.getRegimenId());
        entity.setCategoria_afiliacion_id(request.getAffiliationCategoryId());
        entity.setTipo_afiliacion_id(request.getAffiliationTypeId());
        entity.setNumero_afiliacion(request.getAffiliationNumber());
        entity.setTercero_cotizante_id(request.getCotizanteThirdPartyId());
        entity.setFecha_afiliacion(request.getAffiliationDate());
        entity.setFecha_vigencia_desde(request.getValidFrom());
        entity.setFecha_vigencia_hasta(request.getValidUntil());
        entity.setVigente(setCurrent);
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);

        SeguridadSocialPacienteEntity saved = jpaRepository.save(entity);
        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la seguridad social creada"));
    }

    @Override
    public SeguridadSocialPacienteResponseDto findById(Long patientId, Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        validatePaciente(patientId, empresa_id);
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Registro de seguridad social no encontrado"));
    }

    @Override
    public List<SeguridadSocialPacienteResponseDto> listByPaciente(Long patientId) {
        Long empresa_id = TenantContext.getEmpresaId();
        validatePaciente(patientId, empresa_id);
        return queryRepository.listByPaciente(patientId, empresa_id);
    }

    @Override
    @Transactional
    public Boolean remove(Long patientId, Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        validatePaciente(patientId, empresa_id);

        SeguridadSocialPacienteEntity entity = getValidEntity(id, patientId, empresa_id);
        entity.setActivo(false);
        entity.setUsuario_modificacion(TenantContext.getUsuarioId());
        jpaRepository.save(entity);
        return true;
    }

    private void validatePaciente(Long patientId, Long empresa_id) {
        pacienteJpaRepository.findById(patientId)
                .filter(p -> empresa_id.equals(p.getEmpresa_id()) && p.getDeleted_at() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado en la empresa"));
    }

    private SeguridadSocialPacienteEntity getValidEntity(Long id, Long patientId, Long empresa_id) {
        SeguridadSocialPacienteEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Registro de seguridad social no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || !patientId.equals(entity.getPaciente_id()) || !Boolean.TRUE.equals(entity.getActivo())) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Registro de seguridad social no encontrado");
        }
        return entity;
    }
}
