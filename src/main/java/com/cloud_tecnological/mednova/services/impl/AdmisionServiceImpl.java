package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.admision.*;
import com.cloud_tecnological.mednova.entity.AdmisionEntity;
import com.cloud_tecnological.mednova.entity.AtencionEntity;
import com.cloud_tecnological.mednova.repositories.admision.AdmisionJpaRepository;
import com.cloud_tecnological.mednova.repositories.admision.AdmisionQueryRepository;
import com.cloud_tecnological.mednova.repositories.atencion.AtencionJpaRepository;
import com.cloud_tecnological.mednova.repositories.atencion.AtencionQueryRepository;
import com.cloud_tecnological.mednova.services.AdmisionService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
public class AdmisionServiceImpl implements AdmisionService {

    // Máquina de estados: de -> conjunto de destinos permitidos
    private static final Map<String, Set<String>> ESTADO_TRANSITIONS = Map.of(
        "ADMITIDO",        Set.of("EN_ATENCION", "CANCELADO"),
        "EN_ATENCION",     Set.of("PENDIENTE_EGRESO"),
        "PENDIENTE_EGRESO", Set.of("EGRESADO")
    );

    private static final String CODIGO_URGENCIAS = "URGENCIAS";
    private static final String ESTADO_ADMITIDO  = "ADMITIDO";
    private static final String ESTADO_EGRESADO  = "EGRESADO";
    private static final String ESTADO_TRIAGE    = "EN_TRIAGE";
    private static final String ESTADO_PENDIENTE = "PENDIENTE";

    private final AdmisionJpaRepository admisionJpaRepository;
    private final AdmisionQueryRepository admisionQueryRepository;
    private final AtencionJpaRepository atencionJpaRepository;
    private final AtencionQueryRepository atencionQueryRepository;

    public AdmisionServiceImpl(
            AdmisionJpaRepository admisionJpaRepository,
            AdmisionQueryRepository admisionQueryRepository,
            AtencionJpaRepository atencionJpaRepository,
            AtencionQueryRepository atencionQueryRepository) {
        this.admisionJpaRepository  = admisionJpaRepository;
        this.admisionQueryRepository = admisionQueryRepository;
        this.atencionJpaRepository   = atencionJpaRepository;
        this.atencionQueryRepository = atencionQueryRepository;
    }

    @Override
    @Transactional
    public AdmisionResponseDto create(CreateAdmisionRequestDto dto) {
        Long empresaId  = TenantContext.getEmpresaId();
        Long sedeId     = TenantContext.getSedeId();
        Long usuarioId  = TenantContext.getUsuarioId();

        // Validar que el paciente pertenezca a la empresa
        if (!admisionQueryRepository.existsPacienteInEmpresa(dto.getPatientId(), empresaId)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Paciente no encontrado en la empresa");
        }

        // Validar pagador
        if (!admisionQueryRepository.existsPagadorInEmpresa(dto.getPayerId(), empresaId)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Pagador no encontrado en la empresa");
        }

        // Validar contrato (opcional)
        if (dto.getContractId() != null && !admisionQueryRepository.existsContratoInEmpresa(dto.getContractId(), empresaId)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Contrato no encontrado en la empresa");
        }

        // Validar acompañante (opcional)
        if (dto.getCompanionId() != null && !admisionQueryRepository.existsTerceroInEmpresa(dto.getCompanionId(), empresaId)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Acompañante no encontrado en la empresa");
        }

        // Validar admisión duplicada abierta
        if (admisionQueryRepository.existsAdmisionAbierta(dto.getPatientId(), dto.getAdmissionTypeId(), sedeId, empresaId)) {
            throw new GlobalException(HttpStatus.CONFLICT,
                "El paciente ya tiene una admisión activa del mismo tipo en esta sede");
        }

        // Obtener el ID del estado inicial
        Long estadoAdmitidoId = admisionQueryRepository.findEstadoAdmisionIdByCodigo(ESTADO_ADMITIDO);
        if (estadoAdmitidoId == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Estado de admisión 'ADMITIDO' no configurado en el sistema");
        }

        // Generar número de admisión consecutivo
        String numeroAdmision = admisionQueryRepository.generateNumeroAdmision(empresaId, sedeId);

        // Crear la admisión
        AdmisionEntity admision = new AdmisionEntity();
        admision.setEmpresa_id(empresaId);
        admision.setSede_id(sedeId);
        admision.setNumero_admision(numeroAdmision);
        admision.setPaciente_id(dto.getPatientId());
        admision.setTipo_admision_id(dto.getAdmissionTypeId());
        admision.setEstado_admision_id(estadoAdmitidoId);
        admision.setOrigen_atencion_id(dto.getCareOriginId());
        admision.setPagador_id(dto.getPayerId());
        admision.setContrato_id(dto.getContractId());
        admision.setAcompanante_id(dto.getCompanionId());
        admision.setMotivo_ingreso(dto.getEntryReason());
        admision.setObservaciones(dto.getObservations());
        admision.setUsuario_creacion(usuarioId);

        AdmisionEntity savedAdmision = admisionJpaRepository.save(admision);

        // HU-021: Apertura automática de atención dentro de la misma transacción
        String tipoAdmisionCodigo = admisionQueryRepository.findTipoAdmisionCodigoById(dto.getAdmissionTypeId());
        boolean esUrgencias = CODIGO_URGENCIAS.equalsIgnoreCase(tipoAdmisionCodigo);

        String estadoAtencionCodigo = esUrgencias ? ESTADO_TRIAGE : ESTADO_PENDIENTE;
        Long estadoAtencionId = atencionQueryRepository.findEstadoAtencionIdByCodigo(estadoAtencionCodigo);
        if (estadoAtencionId == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Estado de atención '" + estadoAtencionCodigo + "' no configurado en el sistema");
        }

        String numeroAtencion = atencionQueryRepository.generateNumeroAtencion(empresaId, sedeId);

        AtencionEntity atencion = new AtencionEntity();
        atencion.setEmpresa_id(empresaId);
        atencion.setSede_id(sedeId);
        atencion.setAdmision_id(savedAdmision.getId());
        atencion.setNumero_atencion(numeroAtencion);
        atencion.setEstado_atencion_id(estadoAtencionId);
        atencion.setUsuario_creacion(usuarioId);

        atencionJpaRepository.save(atencion);

        return admisionQueryRepository.findActiveById(savedAdmision.getId(), empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la admisión creada"));
    }

    @Override
    public AdmisionResponseDto findById(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        return admisionQueryRepository.findActiveById(id, empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Admisión no encontrada"));
    }

    @Override
    public PageImpl<AdmisionTableDto> listActive(PageableDto<?> request) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        return admisionQueryRepository.listActive(request, empresaId, sedeId);
    }

    @Override
    @Transactional
    public Boolean changeStatus(Long id, ChangeAdmisionStatusRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        AdmisionEntity entity = admisionJpaRepository.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Admisión no encontrada"));

        validateTenantAndNotDeleted(entity, empresaId, sedeId);

        String currentCode = admisionQueryRepository.findEstadoAdmisionCodigoById(entity.getEstado_admision_id());
        String targetCode  = dto.getNewStatusCode().toUpperCase();

        Set<String> allowed = ESTADO_TRANSITIONS.getOrDefault(currentCode, Set.of());
        if (!allowed.contains(targetCode)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                "Transición de estado no permitida: " + currentCode + " → " + targetCode);
        }

        Long newEstadoId = admisionQueryRepository.findEstadoAdmisionIdByCodigo(targetCode);
        if (newEstadoId == null) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Estado de admisión '" + targetCode + "' no existe");
        }

        entity.setEstado_admision_id(newEstadoId);
        if (dto.getObservations() != null && !dto.getObservations().isBlank()) {
            entity.setObservaciones(dto.getObservations());
        }
        entity.setUsuario_modificacion(usuarioId);
        admisionJpaRepository.save(entity);
        return true;
    }

    @Override
    @Transactional
    public Boolean egreso(Long id, EgresoAdmisionRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        AdmisionEntity entity = admisionJpaRepository.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Admisión no encontrada"));

        validateTenantAndNotDeleted(entity, empresaId, sedeId);

        String currentCode = admisionQueryRepository.findEstadoAdmisionCodigoById(entity.getEstado_admision_id());
        if ("EGRESADO".equals(currentCode) || "CANCELADO".equals(currentCode)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "La admisión ya está cerrada");
        }

        // No permitir egreso con atenciones abiertas
        if (admisionQueryRepository.hasOpenAttentions(id)) {
            throw new GlobalException(HttpStatus.CONFLICT,
                "No se puede egresar: existen atenciones abiertas asociadas a esta admisión");
        }

        Long estadoEgresadoId = admisionQueryRepository.findEstadoAdmisionIdByCodigo(ESTADO_EGRESADO);
        if (estadoEgresadoId == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Estado 'EGRESADO' no configurado en el sistema");
        }

        entity.setEstado_admision_id(estadoEgresadoId);
        entity.setFecha_egreso(LocalDateTime.now());
        entity.setTipo_egreso(dto.getDischargeType());
        if (dto.getObservations() != null) {
            entity.setObservaciones(dto.getObservations());
        }
        entity.setUsuario_modificacion(usuarioId);
        admisionJpaRepository.save(entity);
        return true;
    }

    private void validateTenantAndNotDeleted(AdmisionEntity entity, Long empresaId, Long sedeId) {
        if (entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Admisión no encontrada");
        }
        if (!entity.getEmpresa_id().equals(empresaId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Admisión no encontrada");
        }
        if (!entity.getSede_id().equals(sedeId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Admisión no encontrada");
        }
    }
}
