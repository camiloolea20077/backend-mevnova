package com.cloud_tecnological.mednova.dto.ordenclinica;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrdenClinicaResponseDto {
    private Long id;
    private Long atencionId;
    private Long empresaId;
    private Long sedeId;
    private String numeroOrden;
    private String tipoOrden;
    private String tipoOrdenNombre;
    private String estadoOrden;
    private String estadoOrdenNombre;
    private Long profesionalId;
    private String profesionalNombre;
    private LocalDateTime fechaOrden;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime createdAt;
    private List<DetalleOrdenResponseDto> items;

    @Getter
    @Setter
    public static class DetalleOrdenResponseDto {
        private Long id;
        private Long servicioSaludId;
        private String servicioNombre;
        private String codigoInterno;
        private java.math.BigDecimal cantidad;
        private String indicaciones;
        private String urgencia;
    }
}
