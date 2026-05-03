package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.impactoglosa.ImpactoGlosaCarteraDto;
import com.cloud_tecnological.mednova.entity.ConcertacionGlosaEntity;

public interface ImpactoGlosaCarteraService {

    /**
     * HU-FASE2-066. Aplica el impacto financiero de una concertación de glosa
     * recién cerrada sobre la cuenta por cobrar de la factura.
     *
     * Se invoca automáticamente desde {@code ConcertacionGlosaService.create(...)}
     * dentro de la misma transacción.
     */
    ImpactoGlosaCarteraDto aplicarImpacto(ConcertacionGlosaEntity concertacion);

    /** Consulta del movimiento generado por una glosa cerrada. */
    ImpactoGlosaCarteraDto consultarImpacto(Long glossId);
}
