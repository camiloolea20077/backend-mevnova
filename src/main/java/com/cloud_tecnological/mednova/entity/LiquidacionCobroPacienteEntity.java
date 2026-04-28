package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "liquidacion_cobro_paciente")
@Getter
@Setter
public class LiquidacionCobroPacienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "paciente_id", nullable = false)
    private Long paciente_id;

    @Column(name = "admision_id")
    private Long admision_id;

    @Column(name = "atencion_id")
    private Long atencion_id;

    @Column(name = "factura_id")
    private Long factura_id;

    @Column(name = "tipo_cobro", nullable = false, length = 30)
    private String tipo_cobro;

    @Column(name = "servicio_salud_id")
    private Long servicio_salud_id;

    @Column(name = "regla_cobro_paciente_id")
    private Long regla_cobro_paciente_id;

    @Column(name = "base_calculo", precision = 14, scale = 2)
    private BigDecimal base_calculo;

    @Column(name = "porcentaje_aplicado", precision = 7, scale = 2)
    private BigDecimal porcentaje_aplicado;

    @Column(name = "valor_calculado", nullable = false, precision = 14, scale = 2)
    private BigDecimal valor_calculado;

    @Column(name = "valor_cobrado", nullable = false, precision = 14, scale = 2)
    private BigDecimal valor_cobrado;

    @Column(name = "aplica_exencion", nullable = false)
    private Boolean aplica_exencion;

    @Column(name = "motivo_exencion", length = 300)
    private String motivo_exencion;

    @Column(name = "fecha_liquidacion", nullable = false)
    private LocalDateTime fecha_liquidacion;

    @Column(name = "estado_recaudo", nullable = false, length = 20)
    private String estado_recaudo;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "usuario_creacion")
    private Long usuario_creacion;

    @Column(name = "deleted_at")
    private LocalDateTime deleted_at;

    @Column(name = "usuario_modificacion")
    private Long usuario_modificacion;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
        if (valor_calculado == null) valor_calculado = BigDecimal.ZERO;
        if (valor_cobrado == null) valor_cobrado = BigDecimal.ZERO;
        if (aplica_exencion == null) aplica_exencion = false;
        if (estado_recaudo == null) estado_recaudo = "PENDIENTE";
        if (fecha_liquidacion == null) fecha_liquidacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
