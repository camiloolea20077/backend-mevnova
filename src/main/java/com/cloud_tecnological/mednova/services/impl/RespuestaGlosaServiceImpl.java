package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.respuestaglosa.CreateRespuestaGlosaRequestDto;
import com.cloud_tecnological.mednova.dto.respuestaglosa.RespuestaGlosaResponseDto;
import com.cloud_tecnological.mednova.dto.respuestaglosa.UpdateRespuestaGlosaRequestDto;
import com.cloud_tecnological.mednova.entity.RespuestaGlosaEntity;
import com.cloud_tecnological.mednova.repositories.respuestaglosa.RespuestaGlosaJpaRepository;
import com.cloud_tecnological.mednova.repositories.respuestaglosa.RespuestaGlosaQueryRepository;
import com.cloud_tecnological.mednova.services.RespuestaGlosaService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RespuestaGlosaServiceImpl implements RespuestaGlosaService {

    private static final String TIPO_ACEPTA_TOTAL   = "ACEPTA_TOTAL";
    private static final String TIPO_ACEPTA_PARCIAL = "ACEPTA_PARCIAL";
    private static final String TIPO_NO_ACEPTA      = "NO_ACEPTA";

    private static final String ESTADO_ABIERTA      = "ABIERTA";
    private static final String ESTADO_EN_RESPUESTA = "EN_RESPUESTA";
    private static final String ESTADO_RESPONDIDA   = "RESPONDIDA";

    private static final Set<String> ESTADOS_PERMITEN_RESPUESTA =
            Set.of(ESTADO_ABIERTA, ESTADO_EN_RESPUESTA);

    private final RespuestaGlosaJpaRepository   jpaRepository;
    private final RespuestaGlosaQueryRepository queryRepository;

    public RespuestaGlosaServiceImpl(RespuestaGlosaJpaRepository jpaRepository,
                                     RespuestaGlosaQueryRepository queryRepository) {
        this.jpaRepository   = jpaRepository;
        this.queryRepository = queryRepository;
    }

    @Override
    @Transactional
    public RespuestaGlosaResponseDto create(CreateRespuestaGlosaRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        Map<String, Object> detalle = queryRepository.findDetalleConGlosa(request.getGlossDetailId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Detalle de glosa no encontrado"));

        String estadoGlosa = (String) detalle.get("estado_glosa");
        if (!ESTADOS_PERMITEN_RESPUESTA.contains(estadoGlosa)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "Solo se puede responder mientras la glosa esté ABIERTA o EN_RESPUESTA");
        }

        if (queryRepository.existsByDetalleGlosa(request.getGlossDetailId(), empresa_id)) {
            throw new GlobalException(HttpStatus.CONFLICT, "El ítem ya tiene una respuesta registrada");
        }

        BigDecimal valorGlosado = (BigDecimal) detalle.get("valor_glosado");
        BigDecimal valorAceptado = normalizeAcceptedValue(request.getResponseType(), request.getAcceptedValue(), valorGlosado);

        if (request.getProfessionalId() != null
                && !queryRepository.existsProfesionalByIdAndEmpresa(request.getProfessionalId(), empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Profesional no encontrado");
        }

        Long glosaId = ((Number) detalle.get("glosa_id")).longValue();

        RespuestaGlosaEntity entity = new RespuestaGlosaEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setGlosa_id(glosaId);
        entity.setDetalle_glosa_id(request.getGlossDetailId());
        entity.setTipo_respuesta(request.getResponseType());
        entity.setValor_aceptado(valorAceptado);
        entity.setArgumentacion(request.getArgumentation());
        entity.setSoporte_url(request.getSupportUrl());
        entity.setProfesional_respuesta_id(request.getProfessionalId());
        entity.setFecha_respuesta(LocalDateTime.now());
        entity.setUsuario_creacion(usuario_id);

        RespuestaGlosaEntity saved = jpaRepository.save(entity);

        // Avance de estado de la glosa.
        advanceGlossStatus(glosaId, empresa_id, estadoGlosa, usuario_id);

        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la respuesta creada"));
    }

    @Override
    public RespuestaGlosaResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Respuesta de glosa no encontrada"));
    }

    @Override
    public RespuestaGlosaResponseDto findByGlossDetail(Long glossDetailId) {
        Long empresa_id = TenantContext.getEmpresaId();
        // Validar que el detalle existe y es de mi empresa.
        queryRepository.findDetalleConGlosa(glossDetailId, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Detalle de glosa no encontrado"));
        return queryRepository.findByDetalleGlosa(glossDetailId, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "El ítem aún no tiene respuesta"));
    }

    @Override
    public List<RespuestaGlosaResponseDto> listByGloss(Long glossId) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.listByGlosa(glossId, empresa_id);
    }

    @Override
    @Transactional
    public RespuestaGlosaResponseDto update(Long id, UpdateRespuestaGlosaRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        RespuestaGlosaEntity entity = getValidEntity(id, empresa_id);
        Map<String, Object> detalle = queryRepository.findDetalleConGlosa(entity.getDetalle_glosa_id(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Detalle de glosa no encontrado"));

        String estadoGlosa = (String) detalle.get("estado_glosa");
        if (!ESTADOS_PERMITEN_RESPUESTA.contains(estadoGlosa)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "Solo se puede modificar la respuesta mientras la glosa esté ABIERTA o EN_RESPUESTA");
        }

        BigDecimal valorGlosado = (BigDecimal) detalle.get("valor_glosado");
        BigDecimal valorAceptado = normalizeAcceptedValue(request.getResponseType(), request.getAcceptedValue(), valorGlosado);

        if (request.getProfessionalId() != null
                && !queryRepository.existsProfesionalByIdAndEmpresa(request.getProfessionalId(), empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Profesional no encontrado");
        }

        entity.setTipo_respuesta(request.getResponseType());
        entity.setValor_aceptado(valorAceptado);
        entity.setArgumentacion(request.getArgumentation());
        entity.setSoporte_url(request.getSupportUrl());
        entity.setProfesional_respuesta_id(request.getProfessionalId());
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la respuesta actualizada"));
    }

    @Override
    @Transactional
    public Boolean softDelete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        RespuestaGlosaEntity entity = getValidEntity(id, empresa_id);
        Map<String, Object> detalle = queryRepository.findDetalleConGlosa(entity.getDetalle_glosa_id(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Detalle de glosa no encontrado"));

        String estadoGlosa = (String) detalle.get("estado_glosa");
        if (!ESTADOS_PERMITEN_RESPUESTA.contains(estadoGlosa)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "Solo se puede eliminar la respuesta mientras la glosa esté ABIERTA o EN_RESPUESTA");
        }

        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        // Recalcular estado: si ya no hay respuestas, vuelve a ABIERTA.
        Long glosaId = entity.getGlosa_id();
        long respuestas = queryRepository.countRespuestasByGlosa(glosaId, empresa_id);
        if (respuestas == 0L && ESTADO_EN_RESPUESTA.equals(estadoGlosa)) {
            queryRepository.updateEstadoGlosa(glosaId, empresa_id, ESTADO_ABIERTA, usuario_id);
        } else if (ESTADO_RESPONDIDA.equals(estadoGlosa)) {
            queryRepository.updateEstadoGlosa(glosaId, empresa_id, ESTADO_EN_RESPUESTA, usuario_id);
        }
        return true;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private BigDecimal normalizeAcceptedValue(String tipo, BigDecimal valor, BigDecimal valorGlosado) {
        if (valorGlosado == null) valorGlosado = BigDecimal.ZERO;
        switch (tipo) {
            case TIPO_ACEPTA_TOTAL:
                return valorGlosado;
            case TIPO_NO_ACEPTA:
                return BigDecimal.ZERO;
            case TIPO_ACEPTA_PARCIAL:
                if (valor == null
                        || valor.compareTo(BigDecimal.ZERO) <= 0
                        || valor.compareTo(valorGlosado) >= 0) {
                    throw new GlobalException(HttpStatus.BAD_REQUEST,
                            "Para ACEPTA_PARCIAL el valor aceptado debe ser mayor que cero y menor que el valor glosado");
                }
                return valor;
            default:
                throw new GlobalException(HttpStatus.BAD_REQUEST, "Tipo de respuesta inválido");
        }
    }

    private void advanceGlossStatus(Long glosaId, Long empresa_id, String estadoActual, Long usuario_id) {
        long totalDetalles = queryRepository.countDetallesByGlosa(glosaId, empresa_id);
        long totalRespuestas = queryRepository.countRespuestasByGlosa(glosaId, empresa_id);

        if (totalDetalles > 0 && totalRespuestas >= totalDetalles) {
            queryRepository.updateEstadoGlosa(glosaId, empresa_id, ESTADO_RESPONDIDA, usuario_id);
        } else if (ESTADO_ABIERTA.equals(estadoActual)) {
            queryRepository.updateEstadoGlosa(glosaId, empresa_id, ESTADO_EN_RESPUESTA, usuario_id);
        }
    }

    private RespuestaGlosaEntity getValidEntity(Long id, Long empresa_id) {
        RespuestaGlosaEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Respuesta de glosa no encontrada"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Respuesta de glosa no encontrada");
        }
        return entity;
    }
}
