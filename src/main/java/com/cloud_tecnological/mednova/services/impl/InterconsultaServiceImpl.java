package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.interconsulta.CreateInterconsultaRequestDto;
import com.cloud_tecnological.mednova.dto.interconsulta.InterconsultaFilterParams;
import com.cloud_tecnological.mednova.dto.interconsulta.InterconsultaResponseDto;
import com.cloud_tecnological.mednova.dto.interconsulta.InterconsultaTableDto;
import com.cloud_tecnological.mednova.dto.interconsulta.RespondInterconsultaRequestDto;
import com.cloud_tecnological.mednova.dto.interconsulta.UpdateInterconsultaRequestDto;
import com.cloud_tecnological.mednova.entity.InterconsultaEntity;
import com.cloud_tecnological.mednova.repositories.interconsulta.InterconsultaJpaRepository;
import com.cloud_tecnological.mednova.repositories.interconsulta.InterconsultaQueryRepository;
import com.cloud_tecnological.mednova.services.InterconsultaService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InterconsultaServiceImpl implements InterconsultaService {

    private static final String ESTADO_PENDIENTE  = "PENDIENTE";
    private static final String ESTADO_EN_PROCESO = "EN_PROCESO";
    private static final String ESTADO_RESPONDIDA = "RESPONDIDA";
    private static final String ESTADO_ANULADA    = "ANULADA";

    private final InterconsultaJpaRepository jpa;
    private final InterconsultaQueryRepository query;

    public InterconsultaServiceImpl(InterconsultaJpaRepository jpa,
                                    InterconsultaQueryRepository query) {
        this.jpa = jpa;
        this.query = query;
    }

    @Override
    @Transactional
    public InterconsultaResponseDto create(CreateInterconsultaRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        validateAtencion(empresa_id, sede_id, request.getOriginEncounterId());
        validateProfesional(empresa_id, request.getRequestingProfessionalId());
        validateEspecialidad(request.getDestinationSpecialtyId());

        InterconsultaEntity entity = new InterconsultaEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setSede_id(sede_id);
        entity.setAtencion_origen_id(request.getOriginEncounterId());
        entity.setProfesional_solicita_id(request.getRequestingProfessionalId());
        entity.setEspecialidad_destino_id(request.getDestinationSpecialtyId());
        entity.setMotivo(request.getReason());
        entity.setImpresion_diagnostica(request.getDiagnosticImpression());
        entity.setPregunta_clinica(request.getClinicalQuestion());
        entity.setPrioridad(request.getPriority() == null ? "NORMAL" : request.getPriority());
        entity.setEstado(ESTADO_PENDIENTE);
        entity.setNumero_interconsulta(query.generateNextNumeroInterconsulta(empresa_id));
        entity.setUsuario_creacion(usuario_id);

        InterconsultaEntity saved = jpa.save(entity);
        return query.findActiveById(saved.getId(), empresa_id, sede_id, false)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la interconsulta creada"));
    }

    @Override
    @Transactional
    public InterconsultaResponseDto update(Long id, UpdateInterconsultaRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        InterconsultaEntity entity = getValidEntity(id, empresa_id, sede_id, false);
        if (!ESTADO_PENDIENTE.equals(entity.getEstado())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Solo se puede editar una interconsulta en estado PENDIENTE.");
        }
        validateEspecialidad(request.getDestinationSpecialtyId());

        entity.setEspecialidad_destino_id(request.getDestinationSpecialtyId());
        entity.setMotivo(request.getReason());
        entity.setImpresion_diagnostica(request.getDiagnosticImpression());
        entity.setPregunta_clinica(request.getClinicalQuestion());
        if (request.getPriority() != null && !request.getPriority().isBlank()) {
            entity.setPrioridad(request.getPriority());
        }
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id, false)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la interconsulta"));
    }

    @Override
    @Transactional
    public InterconsultaResponseDto respond(Long id, RespondInterconsultaRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        // Respuesta es cross-sede: el especialista puede estar en otra sede de la misma empresa.
        InterconsultaEntity entity = getValidEntity(id, empresa_id, sede_id, true);
        if (ESTADO_RESPONDIDA.equals(entity.getEstado()) || ESTADO_ANULADA.equals(entity.getEstado())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "La interconsulta no puede ser respondida en estado " + entity.getEstado() + ".");
        }
        validateProfesional(empresa_id, request.getRespondingProfessionalId());
        if (request.getResponseEncounterId() != null
                && !query.atencionExistsInEmpresa(request.getResponseEncounterId(), empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Atención de respuesta no encontrada");
        }

        entity.setEstado(ESTADO_RESPONDIDA);
        entity.setProfesional_responde_id(request.getRespondingProfessionalId());
        entity.setRespuesta(request.getResponse());
        entity.setRecomendaciones(request.getRecommendations());
        entity.setFecha_respuesta(LocalDateTime.now());
        entity.setAtencion_respuesta_id(request.getResponseEncounterId());
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id, true)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la interconsulta"));
    }

    @Override
    @Transactional
    public InterconsultaResponseDto markInProgress(Long id, Long respondingProfessionalId) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        InterconsultaEntity entity = getValidEntity(id, empresa_id, sede_id, true);
        if (!ESTADO_PENDIENTE.equals(entity.getEstado())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Solo se puede tomar una interconsulta PENDIENTE. Estado actual: " + entity.getEstado());
        }
        validateProfesional(empresa_id, respondingProfessionalId);

        entity.setEstado(ESTADO_EN_PROCESO);
        entity.setProfesional_responde_id(respondingProfessionalId);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id, true)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la interconsulta"));
    }

    @Override
    @Transactional
    public InterconsultaResponseDto cancel(Long id, String reason) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        InterconsultaEntity entity = getValidEntity(id, empresa_id, sede_id, false);
        if (ESTADO_RESPONDIDA.equals(entity.getEstado()) || ESTADO_ANULADA.equals(entity.getEstado())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "La interconsulta no puede anularse en estado " + entity.getEstado() + ".");
        }

        entity.setEstado(ESTADO_ANULADA);
        if (reason != null && !reason.isBlank()) {
            String prev = entity.getRecomendaciones();
            entity.setRecomendaciones(prev == null ? "Anulada: " + reason : prev + " | Anulada: " + reason);
        }
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id, false)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la interconsulta"));
    }

    @Override
    public InterconsultaResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        // Permitir lectura cross-sede dentro de empresa: el especialista pueda ver la solicitud.
        return query.findActiveById(id, empresa_id, sede_id, true)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Interconsulta no encontrada"));
    }

    @Override
    public PageImpl<InterconsultaTableDto> list(PageableDto<InterconsultaFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return query.listInterconsultas(pageable, empresa_id, sede_id);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        InterconsultaEntity entity = getValidEntity(id, empresa_id, sede_id, false);
        if (ESTADO_RESPONDIDA.equals(entity.getEstado())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "No se puede eliminar una interconsulta ya respondida.");
        }
        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private InterconsultaEntity getValidEntity(Long id, Long empresa_id, Long sede_id, boolean crossSede) {
        InterconsultaEntity entity = jpa.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Interconsulta no encontrada"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Interconsulta no encontrada");
        }
        if (!crossSede && !sede_id.equals(entity.getSede_id())) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Interconsulta no encontrada");
        }
        return entity;
    }

    private void validateAtencion(Long empresa_id, Long sede_id, Long atencion_id) {
        if (!query.atencionExistsInTenant(atencion_id, empresa_id, sede_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada");
        }
    }

    private void validateProfesional(Long empresa_id, Long profesional_id) {
        if (!query.profesionalActivoInEmpresa(profesional_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Profesional no encontrado");
        }
    }

    private void validateEspecialidad(Long especialidad_id) {
        if (!query.especialidadExists(especialidad_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Especialidad no encontrada");
        }
    }
}
