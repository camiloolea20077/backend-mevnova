package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_glosa")
@Getter
@Setter
public class DetalleGlosaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "glosa_id", nullable = false)
    private Long glosa_id;

    @Column(name = "detalle_factura_id", nullable = false)
    private Long detalle_factura_id;

    @Column(name = "motivo_glosa_id")
    private Long motivo_glosa_id;

    @Column(name = "valor_glosado", nullable = false, precision = 18, scale = 2)
    private BigDecimal valor_glosado;

    @Column(name = "observacion_pagador", columnDefinition = "text")
    private String observacion_pagador;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
