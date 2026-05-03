package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "glosa")
@Getter
@Setter
public class GlosaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "factura_id", nullable = false)
    private Long factura_id;

    @Column(name = "radicacion_id")
    private Long radicacion_id;

    @Column(name = "numero_oficio_pagador", nullable = false, length = 50)
    private String numero_oficio_pagador;

    @Column(name = "fecha_oficio", nullable = false)
    private LocalDate fecha_oficio;

    @Column(name = "fecha_notificacion", nullable = false)
    private LocalDate fecha_notificacion;

    @Column(name = "valor_total_glosado", nullable = false, precision = 18, scale = 2)
    private BigDecimal valor_total_glosado;

    @Column(name = "oficio_url", length = 500)
    private String oficio_url;

    @Column(name = "fecha_limite_respuesta")
    private LocalDate fecha_limite_respuesta;

    @Column(name = "estado_glosa", nullable = false, length = 20)
    private String estado_glosa;

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
        if (estado_glosa == null) estado_glosa = "ABIERTA";
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
