package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarifa_contrato")
@Getter
@Setter
public class TarifaContratoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "contrato_id", nullable = false)
    private Long contrato_id;

    @Column(name = "servicio_salud_id", nullable = false)
    private Long servicio_salud_id;

    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "porcentaje_descuento", precision = 5, scale = 2)
    private BigDecimal porcentaje_descuento;

    @Column(name = "fecha_vigencia_desde", nullable = false)
    private LocalDate fecha_vigencia_desde;

    @Column(name = "fecha_vigencia_hasta")
    private LocalDate fecha_vigencia_hasta;

    @Column(name = "vigente", nullable = false)
    private Boolean vigente;

    @Column(name = "observaciones", length = 300)
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

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
        if (vigente == null) vigente = true;
        if (porcentaje_descuento == null) porcentaje_descuento = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
