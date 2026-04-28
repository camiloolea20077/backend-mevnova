package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "factura")
@Getter
@Setter
public class FacturaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "prefijo", length = 10)
    private String prefijo;

    @Column(name = "numero", nullable = false, length = 30)
    private String numero;

    @Column(name = "admision_id")
    private Long admision_id;

    @Column(name = "paciente_id")
    private Long paciente_id;

    @Column(name = "pagador_id")
    private Long pagador_id;

    @Column(name = "contrato_id")
    private Long contrato_id;

    @Column(name = "estado_factura_id")
    private Long estado_factura_id;

    @Column(name = "fecha_factura")
    private LocalDate fecha_factura;

    @Column(name = "fecha_vencimiento")
    private LocalDate fecha_vencimiento;

    @Column(name = "subtotal", precision = 18, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "total_iva", precision = 18, scale = 2)
    private BigDecimal total_iva;

    @Column(name = "total_descuento", precision = 18, scale = 2)
    private BigDecimal total_descuento;

    @Column(name = "total_copago", precision = 18, scale = 2)
    private BigDecimal total_copago;

    @Column(name = "total_cuota_moderadora", precision = 18, scale = 2)
    private BigDecimal total_cuota_moderadora;

    @Column(name = "total_neto", precision = 18, scale = 2)
    private BigDecimal total_neto;

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
        if (fecha_factura == null) fecha_factura = LocalDate.now();
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (total_iva == null) total_iva = BigDecimal.ZERO;
        if (total_descuento == null) total_descuento = BigDecimal.ZERO;
        if (total_copago == null) total_copago = BigDecimal.ZERO;
        if (total_cuota_moderadora == null) total_cuota_moderadora = BigDecimal.ZERO;
        if (total_neto == null) total_neto = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
