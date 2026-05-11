package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "balance_liquidos")
@Getter
@Setter
public class BalanceLiquidosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "atencion_id", nullable = false)
    private Long atencion_id;

    @Column(name = "paciente_id", nullable = false)
    private Long paciente_id;

    @Column(name = "profesional_id", nullable = false)
    private Long profesional_id;

    @Column(name = "fecha_balance", nullable = false)
    private LocalDate fecha_balance;

    @Column(name = "turno", length = 10)
    private String turno;

    @Column(name = "total_ingresos", nullable = false, precision = 10, scale = 2)
    private BigDecimal total_ingresos;

    @Column(name = "total_egresos", nullable = false, precision = 10, scale = 2)
    private BigDecimal total_egresos;

    /** Columna GENERATED ALWAYS AS (total_ingresos - total_egresos) STORED. Solo lectura. */
    @Column(name = "balance", insertable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal balance;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "deleted_at")
    private LocalDateTime deleted_at;

    @Column(name = "usuario_creacion")
    private Long usuario_creacion;

    @Column(name = "usuario_modificacion")
    private Long usuario_modificacion;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
        if (fecha_balance == null) fecha_balance = LocalDate.now();
        if (total_ingresos == null) total_ingresos = BigDecimal.ZERO;
        if (total_egresos == null) total_egresos = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
