package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.sisbenpaciente.CreateSisbenPacienteRequestDto;
import com.cloud_tecnological.mednova.dto.sisbenpaciente.SisbenPacienteResponseDto;
import com.cloud_tecnological.mednova.entity.SisbenPacienteEntity;
import com.cloud_tecnological.mednova.repositories.paciente.PacienteJpaRepository;
import com.cloud_tecnological.mednova.repositories.sisbenpaciente.SisbenPacienteJpaRepository;
import com.cloud_tecnological.mednova.repositories.sisbenpaciente.SisbenPacienteQueryRepository;
import com.cloud_tecnological.mednova.services.SisbenPacienteService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SisbenPacienteServiceImpl implements SisbenPacienteService {

    private final SisbenPacienteJpaRepository   jpaRepository;
    private final SisbenPacienteQueryRepository queryRepository;
    private final PacienteJpaRepository         pacienteJpaRepository;

    public SisbenPacienteServiceImpl(
            SisbenPacienteJpaRepository jpaRepository,
            SisbenPacienteQueryRepository queryRepository,
            PacienteJpaRepository pacienteJpaRepository) {
        this.jpaRepository        = jpaRepository;
        this.queryRepository      = queryRepository;
        this.pacienteJpaRepository = pacienteJpaRepository;
    }

    @Override
    @Transactional
    public SisbenPacienteResponseDto create(Long patientId, CreateSisbenPacienteRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        validatePaciente(patientId, empresa_id);

        boolean setCurrent = request.getCurrent() == null || Boolean.TRUE.equals(request.getCurrent());

        if (setCurrent) {
            queryRepository.deactivateVigentesForPaciente(patientId, empresa_id, usuario_id);
        }

        SisbenPacienteEntity entity = new SisbenPacienteEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setPaciente_id(patientId);
        entity.setGrupo_sisben_id(request.getSisbenGroupId());
        entity.setPuntaje(request.getScore());
        entity.setFicha_sisben(request.getSisbenCard());
        entity.setFecha_encuesta(request.getSurveyDate());
        entity.setFecha_vigencia_desde(request.getValidFrom());
        entity.setFecha_vigencia_hasta(request.getValidUntil());
        entity.setVigente(setCurrent);
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);

        SisbenPacienteEntity saved = jpaRepository.save(entity);
        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el SISBEN creado"));
    }

    @Override
    public SisbenPacienteResponseDto findById(Long patientId, Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        validatePaciente(patientId, empresa_id);
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Registro SISBEN no encontrado"));
    }

    @Override
    public List<SisbenPacienteResponseDto> listByPaciente(Long patientId) {
        Long empresa_id = TenantContext.getEmpresaId();
        validatePaciente(patientId, empresa_id);
        return queryRepository.listByPaciente(patientId, empresa_id);
    }

    @Override
    @Transactional
    public Boolean remove(Long patientId, Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        validatePaciente(patientId, empresa_id);

        SisbenPacienteEntity entity = getValidEntity(id, patientId, empresa_id);
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

    private SisbenPacienteEntity getValidEntity(Long id, Long patientId, Long empresa_id) {
        SisbenPacienteEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Registro SISBEN no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || !patientId.equals(entity.getPaciente_id()) || !Boolean.TRUE.equals(entity.getActivo())) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Registro SISBEN no encontrado");
        }
        return entity;
    }
}
