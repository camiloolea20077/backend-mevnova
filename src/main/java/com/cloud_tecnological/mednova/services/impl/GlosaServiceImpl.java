package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.glosa.CreateGlosaRequestDto;
import com.cloud_tecnological.mednova.dto.glosa.GlosaResponseDto;
import com.cloud_tecnological.mednova.dto.glosa.GlosaTableDto;
import com.cloud_tecnological.mednova.entity.FacturaEntity;
import com.cloud_tecnological.mednova.entity.GlosaEntity;
import com.cloud_tecnological.mednova.repositories.factura.FacturaJpaRepository;
import com.cloud_tecnological.mednova.repositories.glosa.GlosaJpaRepository;
import com.cloud_tecnological.mednova.repositories.glosa.GlosaQueryRepository;
import com.cloud_tecnological.mednova.services.GlosaService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Service
public class GlosaServiceImpl implements GlosaService {

    private static final int DIAS_HABILES_RESPUESTA = 15;
    private static final Set<String> ESTADOS_FACTURA_GLOSABLES = Set.of("RADICADA", "EN_AUDITORIA");

    private final GlosaJpaRepository    jpaRepository;
    private final GlosaQueryRepository  queryRepository;
    private final FacturaJpaRepository  facturaJpa;

    public GlosaServiceImpl(GlosaJpaRepository jpaRepository,
                            GlosaQueryRepository queryRepository,
                            FacturaJpaRepository facturaJpa) {
        this.jpaRepository   = jpaRepository;
        this.queryRepository = queryRepository;
        this.facturaJpa      = facturaJpa;
    }

    @Override
    @Transactional
    public GlosaResponseDto create(CreateGlosaRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        Map<String, Object> facturaRow = queryRepository.findFacturaSummaryById(request.getInvoiceId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Factura no encontrada"));

        String estadoCodigo = (String) facturaRow.get("estado_codigo");
        if (estadoCodigo == null || !ESTADOS_FACTURA_GLOSABLES.contains(estadoCodigo)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "Solo se puede glosar facturas en estado RADICADA o EN_AUDITORIA");
        }

        BigDecimal totalFactura = (BigDecimal) facturaRow.get("total_neto");
        if (totalFactura == null) totalFactura = BigDecimal.ZERO;

        BigDecimal yaGlosado = queryRepository.sumGlosadoByFactura(request.getInvoiceId(), empresa_id, null);
        BigDecimal totalAcumulado = yaGlosado.add(request.getTotalGlossedValue());
        if (totalAcumulado.compareTo(totalFactura) > 0) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "El valor glosado acumulado supera el total de la factura");
        }

        if (request.getOfficeDate().isAfter(request.getNotificationDate())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "La fecha del oficio no puede ser posterior a la fecha de notificación");
        }

        Long facturaSedeId = ((Number) facturaRow.get("sede_id")).longValue();
        Long radicacionId  = queryRepository.findRadicacionIdByFactura(request.getInvoiceId(), empresa_id).orElse(null);

        GlosaEntity entity = new GlosaEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setSede_id(facturaSedeId);
        entity.setFactura_id(request.getInvoiceId());
        entity.setRadicacion_id(radicacionId);
        entity.setNumero_oficio_pagador(request.getPayerOfficeNumber());
        entity.setFecha_oficio(request.getOfficeDate());
        entity.setFecha_notificacion(request.getNotificationDate());
        entity.setValor_total_glosado(request.getTotalGlossedValue());
        entity.setOficio_url(request.getOfficeUrl());
        entity.setFecha_limite_respuesta(addBusinessDays(request.getNotificationDate(), DIAS_HABILES_RESPUESTA));
        entity.setEstado_glosa("ABIERTA");
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);

        GlosaEntity saved = jpaRepository.save(entity);

        // Cambiar estado de la factura a GLOSADA si aún no lo está.
        if (!"GLOSADA".equals(estadoCodigo)) {
            Long estadoGlosadaId = queryRepository.findEstadoFacturaIdByCodigo("GLOSADA");
            if (estadoGlosadaId == null) {
                throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Estado de factura 'GLOSADA' no configurado en el sistema");
            }
            FacturaEntity factura = facturaJpa.findById(request.getInvoiceId())
                    .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Factura no encontrada"));
            factura.setEstado_factura_id(estadoGlosadaId);
            factura.setUsuario_modificacion(usuario_id);
            facturaJpa.save(factura);
        }

        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la glosa creada"));
    }

    @Override
    public GlosaResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Glosa no encontrada"));
    }

    @Override
    public PageImpl<GlosaTableDto> list(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return queryRepository.listGlosas(pageable, empresa_id, sede_id);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Suma N días hábiles (lunes a viernes) ignorando feriados nacionales.
     * Para una validación normativa estricta debería incluirse el calendario de festivos.
     */
    private LocalDate addBusinessDays(LocalDate from, int days) {
        LocalDate result = from;
        int added = 0;
        while (added < days) {
            result = result.plusDays(1);
            DayOfWeek dow = result.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                added++;
            }
        }
        return result;
    }
}
