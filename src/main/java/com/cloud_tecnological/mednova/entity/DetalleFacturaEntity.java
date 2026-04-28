package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_factura")
@Getter
@Setter
public class DetalleFacturaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "factura_id", nullable = false)
    private Long factura_id;

    @Column(name = "servicio_salud_id", nullable = false)
    private Long servicio_salud_id;

    @Column(name = "atencion_id")
    private Long atencion_id;

    @Column(name = "cantidad", precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "valor_unitario", precision = 15, scale = 2)
    private BigDecimal valor_unitario;

    @Column(name = "porcentaje_iva", precision = 5, scale = 2)
    private BigDecimal porcentaje_iva;

    @Column(name = "valor_iva", precision = 15, scale = 2)
    private BigDecimal valor_iva;

    @Column(name = "valor_descuento", precision = 15, scale = 2)
    private BigDecimal valor_descuento;

    @Column(name = "valor_copago", precision = 15, scale = 2)
    private BigDecimal valor_copago;

    @Column(name = "valor_cuota_moderadora", precision = 15, scale = 2)
    private BigDecimal valor_cuota_moderadora;

    @Column(name = "subtotal", precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "total", precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "diagnostico_id")
    private Long diagnostico_id;

    @Column(name = "observaciones", length = 300)
    private String observaciones;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
        if (cantidad == null) cantidad = BigDecimal.ONE;
        if (porcentaje_iva == null) porcentaje_iva = BigDecimal.ZERO;
        if (valor_iva == null) valor_iva = BigDecimal.ZERO;
        if (valor_descuento == null) valor_descuento = BigDecimal.ZERO;
        if (valor_copago == null) valor_copago = BigDecimal.ZERO;
        if (valor_cuota_moderadora == null) valor_cuota_moderadora = BigDecimal.ZERO;
    }
}
