package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.concertacionglosa.ConcertacionGlosaResponseDto;
import com.cloud_tecnological.mednova.dto.concertacionglosa.CreateConcertacionGlosaRequestDto;
import com.cloud_tecnological.mednova.entity.ConcertacionGlosaEntity;
import com.cloud_tecnological.mednova.repositories.concertacionglosa.ConcertacionGlosaJpaRepository;
import com.cloud_tecnological.mednova.repositories.concertacionglosa.ConcertacionGlosaQueryRepository;
import com.cloud_tecnological.mednova.services.ConcertacionGlosaService;
import com.cloud_tecnological.mednova.services.ImpactoGlosaCarteraService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@Service
public class ConcertacionGlosaServiceImpl implements ConcertacionGlosaService {

    private static final String ESTADO_RESPONDIDA = "RESPONDIDA";
    private static final String ESTADO_RATIFICADA = "RATIFICADA";
    private static final String ESTADO_CERRADA    = "CERRADA";

    private static final Set<String> ESTADOS_CONCILIABLES = Set.of(ESTADO_RESPONDIDA, ESTADO_RATIFICADA);

    private final ConcertacionGlosaJpaRepository   jpaRepository;
    private final ConcertacionGlosaQueryRepository queryRepository;
    private final ImpactoGlosaCarteraService       impactoGlosaCarteraService;

    public ConcertacionGlosaServiceImpl(ConcertacionGlosaJpaRepository jpaRepository,
                                        ConcertacionGlosaQueryRepository queryRepository,
                                        ImpactoGlosaCarteraService impactoGlosaCarteraService) {
        this.jpaRepository              = jpaRepository;
        this.queryRepository            = queryRepository;
        this.impactoGlosaCarteraService = impactoGlosaCarteraService;
    }

    @Override
    @Transactional
    public ConcertacionGlosaResponseDto create(CreateConcertacionGlosaRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        Map<String, Object> glosa = queryRepository.findGlosaSummary(request.getGlossId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Glosa no encontrada"));

        String estado = (String) glosa.get("estado_glosa");
        if (!ESTADOS_CONCILIABLES.contains(estado)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "Solo se concilia una glosa cuando está RESPONDIDA o RATIFICADA");
        }

        if (queryRepository.existsByGlosa(request.getGlossId(), empresa_id)) {
            throw new GlobalException(HttpStatus.CONFLICT, "La glosa ya tiene una concertación registrada");
        }

        BigDecimal valorGlosaInicial = (BigDecimal) glosa.get("valor_total_glosado");
        if (valorGlosaInicial == null) valorGlosaInicial = BigDecimal.ZERO;

        BigDecimal aceptadoInst   = request.getInstitutionAcceptedValue();
        BigDecimal aceptadoPag    = request.getPayerAcceptedValue();

        if (aceptadoInst.compareTo(valorGlosaInicial) > 0) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "El valor aceptado por la institución no puede superar el valor inicial de la glosa");
        }

        // Suma cuadrada: aceptado_institucion + aceptado_pagador = valor_glosa_inicial
        BigDecimal suma = aceptadoInst.add(aceptadoPag);
        if (suma.compareTo(valorGlosaInicial) != 0) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "La suma de valor aceptado por institución y pagador debe ser igual al valor inicial de la glosa");
        }

        ConcertacionGlosaEntity entity = new ConcertacionGlosaEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setGlosa_id(request.getGlossId());
        entity.setFecha_concertacion(request.getConcertationDate());
        entity.setValor_glosa_inicial(valorGlosaInicial);
        entity.setValor_aceptado_institucion(aceptadoInst);
        entity.setValor_aceptado_pagador(aceptadoPag);
        entity.setActa_url(request.getActaUrl());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);

        ConcertacionGlosaEntity saved = jpaRepository.save(entity);

        // Cerrar glosa.
        queryRepository.updateEstadoGlosa(request.getGlossId(), empresa_id, ESTADO_CERRADA, usuario_id);

        // HU-FASE2-066: impacto automático en cuenta por cobrar (misma transacción).
        impactoGlosaCarteraService.aplicarImpacto(saved);

        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la concertación creada"));
    }

    @Override
    public ConcertacionGlosaResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Concertación no encontrada"));
    }

    @Override
    public ConcertacionGlosaResponseDto findByGloss(Long glossId) {
        Long empresa_id = TenantContext.getEmpresaId();
        // Validar que la glosa exista y sea de mi empresa.
        queryRepository.findGlosaSummary(glossId, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Glosa no encontrada"));
        return queryRepository.findByGlosa(glossId, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "La glosa aún no tiene concertación"));
    }
}
