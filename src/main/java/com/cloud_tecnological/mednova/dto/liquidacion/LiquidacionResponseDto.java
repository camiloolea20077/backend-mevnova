package com.cloud_tecnological.mednova.dto.liquidacion;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class LiquidacionResponseDto {

    private Long id;
    private Long empresa_id;
    private Long paciente_id;
    private String patient_name;
    private Long admision_id;
    private Long atencion_id;
    private Long factura_id;
    private String invoice_number;
    private String tipo_cobro;
    private Long servicio_salud_id;
    private String service_name;
    private Long regla_cobro_paciente_id;
    private BigDecimal base_calculo;
    private BigDecimal porcentaje_aplicado;
    private BigDecimal valor_calculado;
    private BigDecimal valor_cobrado;
    private Boolean aplica_exencion;
    private String motivo_exencion;
    private LocalDateTime fecha_liquidacion;
    private String estado_recaudo;
    private String observaciones;
    private Boolean activo;
}
