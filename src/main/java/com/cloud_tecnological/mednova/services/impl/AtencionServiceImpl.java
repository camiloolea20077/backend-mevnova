package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.atencion.*;
import com.cloud_tecnological.mednova.entity.AdmisionEntity;
import com.cloud_tecnological.mednova.entity.AtencionEntity;
import com.cloud_tecnological.mednova.repositories.admision.AdmisionJpaRepository;
import com.cloud_tecnological.mednova.repositories.atencion.AtencionJpaRepository;
import com.cloud_tecnological.mednova.repositories.atencion.AtencionQueryRepository;
import com.cloud_tecnological.mednova.services.AtencionService;
import com.cloud_tecnological.mednova.services.DiagnosticoService;
import com.cloud_tecnological.mednova.services.OrdenClinicaService;
import com.cloud_tecnological.mednova.services.PrescripcionService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class AtencionServiceImpl implements AtencionService {

    private static final Set<String> ESTADOS_BLOQUEADOS_TRIAGE = Set.of("EN_ATENCION", "CERRADA");

    private final AtencionJpaRepository atencionJpaRepository;
    private final AtencionQueryRepository atencionQueryRepository;
    private final AdmisionJpaRepository admisionJpaRepository;
    private final DiagnosticoService diagnosticoService;
    private final OrdenClinicaService ordenClinicaService;
    private final PrescripcionService prescripcionService;

    public AtencionServiceImpl(AtencionJpaRepository atencionJpaRepository,
                               AtencionQueryRepository atencionQueryRepository,
                               AdmisionJpaRepository admisionJpaRepository,
                               DiagnosticoService diagnosticoService,
                               OrdenClinicaService ordenClinicaService,
                               PrescripcionService prescripcionService) {
        this.atencionJpaRepository  = atencionJpaRepository;
        this.atencionQueryRepository = atencionQueryRepository;
        this.admisionJpaRepository  = admisionJpaRepository;
        this.diagnosticoService     = diagnosticoService;
        this.ordenClinicaService    = ordenClinicaService;
        this.prescripcionService    = prescripcionService;
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

        Long estadoSalaEsperaId = atencionQueryRepository.findEstadoAtencionIdByCodigo("PENDIENTE");
        if (estadoSalaEsperaId == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Estado 'PENDIENTE' no configurado en el sistema");
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

    @Override
    public ConsolaAtencionDto getConsola(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();

        ConsolaAtencionDto consola = atencionQueryRepository.findConsolaData(id, empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada"));

        consola.setDiagnosticos(diagnosticoService.findByAtencionId(id));
        consola.setOrdenes(ordenClinicaService.findByAtencionId(id));
        consola.setPrescripciones(prescripcionService.findByAtencionId(id));

        return consola;
    }

    @Override
    @Transactional
    public AtencionResponseDto startAttention(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        AtencionEntity entity = atencionJpaRepository.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada"));

        validateTenantAndNotDeleted(entity, empresaId, sedeId);

        String currentStatus = atencionQueryRepository.findEstadoAtencionCodigoById(entity.getEstado_atencion_id());
        if (!"PENDIENTE".equals(currentStatus)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                "La atención debe estar en estado PENDIENTE para ser iniciada");
        }

        atencionQueryRepository.findProfesionalByUsuario(usuarioId, empresaId)
            .ifPresent(entity::setProfesional_id);

        Long estadoEnAtencionId = atencionQueryRepository.findEstadoAtencionIdByCodigo("EN_ATENCION");
        if (estadoEnAtencionId == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Estado 'EN_ATENCION' no configurado en el sistema");
        }
        entity.setEstado_atencion_id(estadoEnAtencionId);
        entity.setUsuario_modificacion(usuarioId);
        atencionJpaRepository.save(entity);

        return atencionQueryRepository.findActiveById(id, empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la atención"));
    }

    @Override
    @Transactional
    public AtencionResponseDto actualizarNotas(Long id, ActualizarNotasRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        AtencionEntity entity = atencionJpaRepository.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada"));

        validateTenantAndNotDeleted(entity, empresaId, sedeId);

        String currentStatus = atencionQueryRepository.findEstadoAtencionCodigoById(entity.getEstado_atencion_id());
        if (!"EN_ATENCION".equals(currentStatus)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                "Las notas clínicas solo pueden editarse en atenciones en estado EN_ATENCION");
        }

        if (dto.getChiefComplaint() != null) entity.setMotivo_consulta(dto.getChiefComplaint());
        if (dto.getCurrentIllness() != null) entity.setEnfermedad_actual(dto.getCurrentIllness());
        if (dto.getMedicalHistory() != null) entity.setAntecedentes(dto.getMedicalHistory());
        if (dto.getPhysicalExam()   != null) entity.setExamen_fisico(dto.getPhysicalExam());
        if (dto.getAnalysis()       != null) entity.setAnalisis(dto.getAnalysis());
        if (dto.getPlan()           != null) entity.setPlan(dto.getPlan());
        if (dto.getObservations()   != null) entity.setObservaciones(dto.getObservations());
        entity.setUsuario_modificacion(usuarioId);
        atencionJpaRepository.save(entity);

        return atencionQueryRepository.findActiveById(id, empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la atención"));
    }

    @Override
    @Transactional
    public AtencionResponseDto cerrarAtencion(Long id, CerrarAtencionRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        AtencionEntity entity = atencionJpaRepository.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada"));

        validateTenantAndNotDeleted(entity, empresaId, sedeId);

        String currentStatus = atencionQueryRepository.findEstadoAtencionCodigoById(entity.getEstado_atencion_id());
        if (!"EN_ATENCION".equals(currentStatus)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                "Solo puede cerrarse una atención en estado EN_ATENCION");
        }

        if (!atencionQueryRepository.existsPrincipalDiagnosis(id, empresaId)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                "La atención debe tener al menos un diagnóstico PRINCIPAL antes de cerrarse");
        }

        Long estadoCerradaId = atencionQueryRepository.findEstadoAtencionIdByCodigo("CERRADA");
        if (estadoCerradaId == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Estado 'CERRADA' no configurado en el sistema");
        }

        entity.setConducta(dto.getConduct());
        if (dto.getPlan()         != null) entity.setPlan(dto.getPlan());
        if (dto.getAnalysis()     != null) entity.setAnalisis(dto.getAnalysis());
        if (dto.getObservations() != null) entity.setObservaciones(dto.getObservations());
        entity.setFecha_cierre(LocalDateTime.now());
        entity.setEstado_atencion_id(estadoCerradaId);
        entity.setUsuario_modificacion(usuarioId);
        atencionJpaRepository.save(entity);

        return atencionQueryRepository.findActiveById(id, empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la atención"));
    }

    @Override
    @Transactional
    public AtencionResponseDto solicitarHospitalizacion(Long id, SolicitarHospitalizacionRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        AtencionEntity entity = atencionJpaRepository.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada"));

        validateTenantAndNotDeleted(entity, empresaId, sedeId);

        String currentStatus = atencionQueryRepository.findEstadoAtencionCodigoById(entity.getEstado_atencion_id());
        if (!"EN_ATENCION".equals(currentStatus)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                "Solo puede solicitar hospitalización en atenciones en estado EN_ATENCION");
        }

        if (!atencionQueryRepository.existsConfirmedPrincipalDiagnosis(id, empresaId)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                "La atención debe tener un diagnóstico PRINCIPAL confirmado para solicitar hospitalización");
        }

        Long estadoCerradaId = atencionQueryRepository.findEstadoAtencionIdByCodigo("CERRADA");
        if (estadoCerradaId == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Estado 'CERRADA' no configurado en el sistema");
        }

        Long estadoPendienteHospId = atencionQueryRepository.findEstadoAdmisionIdByCodigo("PENDIENTE_HOSPITALIZACION");
        if (estadoPendienteHospId == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Estado 'PENDIENTE_HOSPITALIZACION' de admisión no configurado");
        }

        entity.setConducta("HOSPITALIZACION");
        entity.setPlan("Servicio destino: " + dto.getTargetService() + "\n\n" + dto.getJustification());
        if (dto.getObservations() != null) entity.setObservaciones(dto.getObservations());
        entity.setFecha_cierre(LocalDateTime.now());
        entity.setEstado_atencion_id(estadoCerradaId);
        entity.setUsuario_modificacion(usuarioId);
        atencionJpaRepository.save(entity);

        AdmisionEntity admision = admisionJpaRepository.findById(entity.getAdmision_id())
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Admisión no encontrada"));
        admision.setEstado_admision_id(estadoPendienteHospId);
        admision.setUsuario_modificacion(usuarioId);
        admisionJpaRepository.save(admision);

        return atencionQueryRepository.findActiveById(id, empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la atención"));
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
