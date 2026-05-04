package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_lote")
@Getter
@Setter
public class StockLoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "bodega_id", nullable = false)
    private Long bodega_id;

    @Column(name = "lote_id", nullable = false)
    private Long lote_id;

    @Column(name = "cantidad_disponible", nullable = false, precision = 15, scale = 3)
    private BigDecimal cantidad_disponible;

    @Column(name = "cantidad_reservada", nullable = false, precision = 15, scale = 3)
    private BigDecimal cantidad_reservada;

    @Column(name = "cantidad_total", insertable = false, updatable = false, precision = 15, scale = 3)
    private BigDecimal cantidad_total;

    @Column(name = "ultimo_movimiento_at")
    private LocalDateTime ultimo_movimiento_at;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "deleted_at")
    private LocalDateTime deleted_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
        if (cantidad_disponible == null) cantidad_disponible = BigDecimal.ZERO;
        if (cantidad_reservada == null) cantidad_reservada = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
