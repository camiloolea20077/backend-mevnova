package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Detalle inmutable de balance de líquidos.
 * DDL: sin updated_at, sin usuario_modificacion. Los registros se crean y se eliminan lógicamente.
 */
@Entity
@Table(name = "detalle_balance_liquidos")
@Getter
@Setter
public class DetalleBalanceLiquidosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "balance_id", nullable = false)
    private Long balance_id;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "via", nullable = false, length = 30)
    private String via;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @Column(name = "cantidad_ml", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad_ml;

    @Column(name = "hora_registro", nullable = false)
    private LocalTime hora_registro;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "deleted_at")
    private LocalDateTime deleted_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
    }
}
