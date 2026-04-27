package com.cloud_tecnological.mednova.dto.prescripcion;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PrescripcionResponseDto {
    private Long id;
    private Long atencionId;
    private Long empresaId;
    private Long sedeId;
    private String numeroPrescripcion;
    private Long estadoPrescripcionId;
    private String estadoPrescripcionNombre;
    private Long profesionalId;
    private String profesionalNombre;
    private LocalDateTime fechaPrescripcion;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime createdAt;
    private List<DetallePrescripcionResponseDto> items;

    @Getter
    @Setter
    public static class DetallePrescripcionResponseDto {
        private Long id;
        private Long servicioSaludId;
        private String medicamentoNombre;
        private BigDecimal dosis;
        private String unidadDosis;
        private Long viaAdministracionId;
        private String viaNombre;
        private Long frecuenciaDosisId;
        private String frecuenciaNombre;
        private Integer duracionDias;
        private BigDecimal cantidadDespachar;
        private String indicaciones;
    }
}
