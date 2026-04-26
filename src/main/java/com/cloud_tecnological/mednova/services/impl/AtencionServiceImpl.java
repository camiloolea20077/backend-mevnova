package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.atencion.*;
import com.cloud_tecnological.mednova.entity.AtencionEntity;
import com.cloud_tecnological.mednova.repositories.atencion.AtencionJpaRepository;
import com.cloud_tecnological.mednova.repositories.atencion.AtencionQueryRepository;
import com.cloud_tecnological.mednova.services.AtencionService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class AtencionServiceImpl implements AtencionService {

    private static final Set<String> ESTADOS_BLOQUEADOS_TRIAGE = Set.of("EN_ATENCION", "CERRADA");

    private final AtencionJpaRepository atencionJpaRepository;
    private final AtencionQueryRepository atencionQueryRepository;

    public AtencionServiceImpl(AtencionJpaRepository atencionJpaRepository,
                               AtencionQueryRepository atencionQueryRepository) {
        this.atencionJpaRepository  = atencionJpaRepository;
        this.atencionQueryRepository = atencionQueryRepository;
    }

    @Override
    public AtencionResponseDto findByAdmisionId(Long admisionId) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        return atencionQueryRepository.findByAdmisionId(admisionId, empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada para la admisión"));
    }

    @Override
    @Transactional
    public AtencionResponseDto registrarTriage(Long id, RegistrarTriageRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        AtencionEntity entity = atencionJpaRepository.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada"));

        validateTenantAndNotDeleted(entity, empresaId, sedeId);

        String currentStatus = atencionQueryRepository.findEstadoAtencionCodigoById(entity.getEstado_atencion_id());
        if (!"EN_TRIAGE".equals(currentStatus)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                "El triage solo puede registrarse en atenciones en estado EN_TRIAGE");
        }

        entity.setNivel_triage(dto.getTriageLevel());
        entity.setMotivo_consulta(dto.getChiefComplaint());
        entity.setTension_sistolica(dto.getSystolicPressure());
        entity.setTension_diastolica(dto.getDiastolicPressure());
        entity.setFrecuencia_cardiaca(dto.getHeartRate());
        entity.setFrecuencia_respiratoria(dto.getRespiratoryRate());
        entity.setTemperatura(dto.getTemperature());
        entity.setSaturacion_oxigeno(dto.getOxygenSaturation());
        entity.setPeso(dto.getWeight());
        entity.setTalla(dto.getHeight());
        entity.setGlucometria(dto.getGlucometry());
        if (dto.getObservations() != null) {
            entity.setObservaciones(dto.getObservations());
        }

        // Avanzar estado: EN_TRIAGE → EN_SALA_ESPERA
        Long estadoSalaEsperaId = atencionQueryRepository.findEstadoAtencionIdByCodigo("EN_SALA_ESPERA");
        if (estadoSalaEsperaId == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Estado 'EN_SALA_ESPERA' no configurado en el sistema");
        }
        entity.setEstado_atencion_id(estadoSalaEsperaId);
        entity.setUsuario_modificacion(usuarioId);
        atencionJpaRepository.save(entity);

        return atencionQueryRepository.findActiveById(id, empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la atención"));
    }

    @Override
    @Transactional
    public AtencionResponseDto reclasificarTriage(Long id, ReclasificarTriageRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        AtencionEntity entity = atencionJpaRepository.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada"));

        validateTenantAndNotDeleted(entity, empresaId, sedeId);

        String currentStatus = atencionQueryRepository.findEstadoAtencionCodigoById(entity.getEstado_atencion_id());
        if (ESTADOS_BLOQUEADOS_TRIAGE.contains(currentStatus)) {
            throw new GlobalException(HttpStatus.CONFLICT,
                "No es posible reclasificar: el paciente ya está en atención o la atención está cerrada");
        }

        String justification = dto.getJustification();
        String existingObs   = entity.getObservaciones() != null ? entity.getObservaciones() : "";
        entity.setNivel_triage(dto.getNewTriageLevel());
        entity.setObservaciones(existingObs + "\n[Reclasificación] " + justification);
        entity.setUsuario_modificacion(usuarioId);
        atencionJpaRepository.save(entity);

        return atencionQueryRepository.findActiveById(id, empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la atención"));
    }

    @Override
    public List<ColaUrgenciasDto> getColaUrgencias() {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        return atencionQueryRepository.findColaUrgencias(empresaId, sedeId);
    }

    private void validateTenantAndNotDeleted(AtencionEntity entity, Long empresaId, Long sedeId) {
        if (entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada");
        }
        if (!entity.getEmpresa_id().equals(empresaId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada");
        }
        if (!entity.getSede_id().equals(sedeId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada");
        }
    }
}
