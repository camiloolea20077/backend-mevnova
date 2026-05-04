package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento_inventario")
@Getter
@Setter
public class MovimientoInventarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "tipo_movimiento", nullable = false, length = 30)
    private String tipo_movimiento;

    @Column(name = "bodega_origen_id")
    private Long bodega_origen_id;

    @Column(name = "bodega_destino_id")
    private Long bodega_destino_id;

    @Column(name = "lote_id", nullable = false)
    private Long lote_id;

    @Column(name = "servicio_salud_id", nullable = false)
    private Long servicio_salud_id;

    @Column(name = "cantidad", nullable = false, precision = 15, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor_unitario;

    @Column(name = "valor_total", insertable = false, updatable = false, precision = 15, scale = 2)
    private BigDecimal valor_total;

    @Column(name = "referencia_tipo", length = 30)
    private String referencia_tipo;

    @Column(name = "referencia_id")
    private Long referencia_id;

    @Column(name = "motivo", length = 300)
    private String motivo;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDateTime fecha_movimiento;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "usuario_creacion")
    private Long usuario_creacion;

    @Column(name = "deleted_at")
    private LocalDateTime deleted_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
        if (fecha_movimiento == null) fecha_movimiento = LocalDateTime.now();
        if (valor_unitario == null) valor_unitario = BigDecimal.ZERO;
    }
}
