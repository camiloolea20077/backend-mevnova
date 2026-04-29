package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cuenta_por_cobrar")
@Getter
@Setter
public class CuentaPorCobrarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "factura_id", nullable = false)
    private Long factura_id;

    @Column(name = "pagador_id", nullable = false)
    private Long pagador_id;

    @Column(name = "estado_cartera_id", nullable = false)
    private Long estado_cartera_id;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fecha_inicio;

    @Column(name = "fecha_causacion")
    private LocalDate fecha_causacion;

    @Column(name = "fecha_vencimiento")
    private LocalDate fecha_vencimiento;

    @Column(name = "valor_inicial", precision = 18, scale = 2)
    private BigDecimal valor_inicial;

    @Column(name = "saldo", precision = 18, scale = 2)
    private BigDecimal saldo_actual;

    @Column(name = "dias_mora")
    private Integer dias_mora;

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

    @Column(name = "usuario_modificacion")
    private Long usuario_modificacion;

    @Column(name = "deleted_at")
    private LocalDateTime deleted_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
        if (dias_mora == null) dias_mora = 0;
        if (fecha_inicio == null) fecha_inicio = java.time.LocalDate.now();
        if (saldo_actual == null) saldo_actual = java.math.BigDecimal.ZERO;
        if (valor_inicial == null) valor_inicial = java.math.BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
