package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_compra")
@Getter
@Setter
public class DetalleCompraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "compra_id", nullable = false)
    private Long compra_id;

    @Column(name = "servicio_salud_id", nullable = false)
    private Long servicio_salud_id;

    @Column(name = "lote_id", nullable = false)
    private Long lote_id;

    @Column(name = "cantidad", nullable = false, precision = 15, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor_unitario;

    @Column(name = "porcentaje_iva", precision = 5, scale = 2)
    private BigDecimal porcentaje_iva;

    @Column(name = "valor_iva", precision = 15, scale = 2)
    private BigDecimal valor_iva;

    @Column(name = "porcentaje_descuento", precision = 5, scale = 2)
    private BigDecimal porcentaje_descuento;

    @Column(name = "valor_descuento", precision = 15, scale = 2)
    private BigDecimal valor_descuento;

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "total", nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "observaciones", length = 300)
    private String observaciones;

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
        if (porcentaje_iva == null) porcentaje_iva = BigDecimal.ZERO;
        if (valor_iva == null) valor_iva = BigDecimal.ZERO;
        if (porcentaje_descuento == null) porcentaje_descuento = BigDecimal.ZERO;
        if (valor_descuento == null) valor_descuento = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
