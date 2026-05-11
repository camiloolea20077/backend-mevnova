package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdministerDoseRequestDto;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdministracionMedicamentoFilterParams;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdministracionMedicamentoResponseDto;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdministracionMedicamentoTableDto;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdverseReactionRequestDto;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.OmitDoseRequestDto;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.ScheduleAdministracionRequestDto;
import com.cloud_tecnological.mednova.entity.AdministracionMedicamentoEntity;
import com.cloud_tecnological.mednova.repositories.administracionmedicamento.AdministracionMedicamentoJpaRepository;
import com.cloud_tecnological.mednova.repositories.administracionmedicamento.AdministracionMedicamentoQueryRepository;
import com.cloud_tecnological.mednova.services.AdministracionMedicamentoService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdministracionMedicamentoServiceImpl implements AdministracionMedicamentoService {

    private static final String ESTADO_PROGRAMADA   = "PROGRAMADA";
    private static final String ESTADO_ADMINISTRADA = "ADMINISTRADA";
    private static final String ESTADO_OMITIDA      = "OMITIDA";
    private static final String ESTADO_RECHAZADA    = "RECHAZADA";
    private static final String ESTADO_SUSPENDIDA   = "SUSPENDIDA";

    private final AdministracionMedicamentoJpaRepository jpa;
    private final AdministracionMedicamentoQueryRepository query;

    public AdministracionMedicamentoServiceImpl(AdministracionMedicamentoJpaRepository jpa,
                                                AdministracionMedicamentoQueryRepository query) {
        this.jpa   = jpa;
        this.query = query;
    }

    // ── CA1: programar dosis ───────────────────────────────────────────────

    @Override
    @Transactional
    public List<AdministracionMedicamentoResponseDto> schedule(ScheduleAdministracionRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        validatePaciente(empresa_id, request.getPatientId());
        validateAtencion(empresa_id, sede_id, request.getEncounterId());
        validateAtencionMatchesPaciente(request.getEncounterId(), request.getPatientId());
        validateProfesional(empresa_id, request.getProfessionalId());
        validateDetallePrescripcion(empresa_id, request.getPrescriptionDetailId());

        List<AdministracionMedicamentoResponseDto> created = new ArrayList<>();
        LocalDateTime nextDose = request.getFirstDoseAt();
        for (int i = 0; i < request.getTotalDoses(); i++) {
            AdministracionMedicamentoEntity entity = new AdministracionMedicamentoEntity();
            entity.setEmpresa_id(empresa_id);
            entity.setSede_id(sede_id);
            entity.setAtencion_id(request.getEncounterId());
            entity.setPaciente_id(request.getPatientId());
            entity.setDetalle_prescripcion_id(request.getPrescriptionDetailId());
            entity.setProfesional_id(request.getProfessionalId());
            entity.setFecha_programada(nextDose);
            entity.setEstado(ESTADO_PROGRAMADA);
            entity.setUsuario_creacion(usuario_id);

            AdministracionMedicamentoEntity saved = jpa.save(entity);
            created.add(query.findActiveById(saved.getId(), empresa_id, sede_id)
                    .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error al recuperar la dosis programada")));

            nextDose = nextDose.plusHours(request.getIntervalHours());
        }
        return created;
    }

    // ── CA2: administrar dosis ─────────────────────────────────────────────

    @Override
    @Transactional
    public AdministracionMedicamentoResponseDto administer(Long id, AdministerDoseRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        AdministracionMedicamentoEntity entity = getValidEntity(id, empresa_id, sede_id);
        if (!ESTADO_PROGRAMADA.equals(entity.getEstado())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Solo se pueden administrar dosis en estado PROGRAMADA. Estado actual: " + entity.getEstado());
        }

        if (request.getDispensationId() != null) {
            validateDispensacion(empresa_id, sede_id, request.getDispensationId());
        }
        if (request.getLoteId() != null) {
            validateLote(empresa_id, request.getLoteId());
        }
        if (request.getRouteOfAdministrationId() != null) {
            validateVia(request.getRouteOfAdministrationId());
        }

        entity.setEstado(ESTADO_ADMINISTRADA);
        entity.setFecha_administracion(request.getAdministrationTime());
        entity.setDosis_administrada(request.getAdministeredDose());
        entity.setVia_administracion_id(request.getRouteOfAdministrationId());
        entity.setDispensacion_id(request.getDispensationId());
        entity.setLote_id(request.getLoteId());
        if (request.getAdverseReaction() != null && !request.getAdverseReaction().isBlank()) {
            entity.setReaccion_adversa(request.getAdverseReaction());
        }
        if (request.getObservations() != null && !request.getObservations().isBlank()) {
            entity.setObservaciones(request.getObservations());
        }
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la administración"));
    }

    // ── CA3: omitir / rechazar dosis ───────────────────────────────────────

    @Override
    @Transactional
    public AdministracionMedicamentoResponseDto omit(Long id, OmitDoseRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        AdministracionMedicamentoEntity entity = getValidEntity(id, empresa_id, sede_id);
        if (!ESTADO_PROGRAMADA.equals(entity.getEstado())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Solo se pueden omitir dosis en estado PROGRAMADA. Estado actual: " + entity.getEstado());
        }

        String finalStatus = ESTADO_RECHAZADA.equals(request.getStatus()) ? ESTADO_RECHAZADA : ESTADO_OMITIDA;
        entity.setEstado(finalStatus);
        entity.setMotivo_omision(request.getOmissionReason());
        if (request.getObservations() != null && !request.getObservations().isBlank()) {
            entity.setObservaciones(request.getObservations());
        }
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la administración"));
    }

    // ── Suspender ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AdministracionMedicamentoResponseDto suspend(Long id, String reason) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        AdministracionMedicamentoEntity entity = getValidEntity(id, empresa_id, sede_id);
        if (!ESTADO_PROGRAMADA.equals(entity.getEstado())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Solo se pueden suspender dosis en estado PROGRAMADA. Estado actual: " + entity.getEstado());
        }

        entity.setEstado(ESTADO_SUSPENDIDA);
        if (reason != null && !reason.isBlank()) {
            entity.setMotivo_omision(reason);
        }
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la administración"));
    }

    // ── CA4: reacción adversa ──────────────────────────────────────────────

    @Override
    @Transactional
    public AdministracionMedicamentoResponseDto registerAdverseReaction(Long id, AdverseReactionRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        AdministracionMedicamentoEntity entity = getValidEntity(id, empresa_id, sede_id);
        if (!ESTADO_ADMINISTRADA.equals(entity.getEstado())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Solo se pueden registrar reacciones adversas en dosis ADMINISTRADAS.");
        }

        entity.setReaccion_adversa(request.getAdverseReaction());
        if (request.getObservations() != null && !request.getObservations().isBlank()) {
            entity.setObservaciones(request.getObservations());
        }
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la administración"));
    }

    // ── Lecturas y soft delete ─────────────────────────────────────────────

    @Override
    public AdministracionMedicamentoResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Administración de medicamento no encontrada"));
    }

    @Override
    public PageImpl<AdministracionMedicamentoTableDto> list(PageableDto<AdministracionMedicamentoFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return query.listAdministraciones(pageable, empresa_id, sede_id);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        AdministracionMedicamentoEntity entity = getValidEntity(id, empresa_id, sede_id);
        if (ESTADO_ADMINISTRADA.equals(entity.getEstado())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "No se puede eliminar una dosis ya administrada. Use registro de reacción adversa o auditoría.");
        }
        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AdministracionMedicamentoEntity getValidEntity(Long id, Long empresa_id, Long sede_id) {
        AdministracionMedicamentoEntity entity = jpa.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Administración de medicamento no encontrada"));
        if (!empresa_id.equals(entity.getEmpresa_id())
                || !sede_id.equals(entity.getSede_id())
                || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Administración de medicamento no encontrada");
        }
        return entity;
    }

    private void validatePaciente(Long empresa_id, Long paciente_id) {
        if (!query.pacienteExistsInEmpresa(paciente_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }
    }

    private void validateAtencion(Long empresa_id, Long sede_id, Long atencion_id) {
        if (!query.atencionExistsInTenant(atencion_id, empresa_id, sede_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada");
        }
    }

    private void validateAtencionMatchesPaciente(Long atencion_id, Long paciente_id) {
        if (!query.atencionMatchesPaciente(atencion_id, paciente_id)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "La atención no corresponde al paciente indicado");
        }
    }

    private void validateProfesional(Long empresa_id, Long profesional_id) {
        if (!query.profesionalActivoInEmpresa(profesional_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Profesional no encontrado");
        }
    }

    private void validateDetallePrescripcion(Long empresa_id, Long detalle_id) {
        if (!query.detallePrescripcionExistsInEmpresa(detalle_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Detalle de prescripción no encontrado");
        }
    }

    private void validateDispensacion(Long empresa_id, Long sede_id, Long dispensacion_id) {
        if (!query.dispensacionExistsInTenant(dispensacion_id, empresa_id, sede_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Dispensación no encontrada");
        }
    }

    private void validateLote(Long empresa_id, Long lote_id) {
        if (!query.loteExistsInEmpresa(lote_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Lote no encontrado");
        }
    }

    private void validateVia(Long via_id) {
        if (!query.viaAdministracionExists(via_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Vía de administración no encontrada");
        }
    }
}
