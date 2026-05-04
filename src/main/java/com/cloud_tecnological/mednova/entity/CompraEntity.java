package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "compra")
@Getter
@Setter
public class CompraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "bodega_id", nullable = false)
    private Long bodega_id;

    @Column(name = "proveedor_id", nullable = false)
    private Long proveedor_id;

    @Column(name = "numero_compra", nullable = false, length = 30)
    private String numero_compra;

    @Column(name = "numero_factura_proveedor", length = 50)
    private String numero_factura_proveedor;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDate fecha_compra;

    @Column(name = "fecha_recepcion")
    private LocalDate fecha_recepcion;

    @Column(name = "estado_compra", nullable = false, length = 20)
    private String estado_compra;

    @Column(name = "subtotal", nullable = false, precision = 18, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "total_iva", nullable = false, precision = 18, scale = 2)
    private BigDecimal total_iva;

    @Column(name = "total_descuento", nullable = false, precision = 18, scale = 2)
    private BigDecimal total_descuento;

    @Column(name = "total", nullable = false, precision = 18, scale = 2)
    private BigDecimal total;

    @Column(name = "soporte_url", length = 500)
    private String soporte_url;

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
        if (estado_compra == null) estado_compra = "BORRADOR";
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (total_iva == null) total_iva = BigDecimal.ZERO;
        if (total_descuento == null) total_descuento = BigDecimal.ZERO;
        if (total == null) total = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
