package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "concertacion_glosa")
@Getter
@Setter
public class ConcertacionGlosaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "glosa_id", nullable = false)
    private Long glosa_id;

    @Column(name = "fecha_concertacion", nullable = false)
    private LocalDate fecha_concertacion;

    @Column(name = "valor_glosa_inicial", nullable = false, precision = 18, scale = 2)
    private BigDecimal valor_glosa_inicial;

    @Column(name = "valor_aceptado_institucion", nullable = false, precision = 18, scale = 2)
    private BigDecimal valor_aceptado_institucion;

    @Column(name = "valor_aceptado_pagador", nullable = false, precision = 18, scale = 2)
    private BigDecimal valor_aceptado_pagador;

    @Column(name = "valor_recuperado", precision = 18, scale = 2, insertable = false, updatable = false)
    private BigDecimal valor_recuperado;

    @Column(name = "acta_url", length = 500)
    private String acta_url;

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
        if (valor_aceptado_institucion == null) valor_aceptado_institucion = BigDecimal.ZERO;
        if (valor_aceptado_pagador == null) valor_aceptado_pagador = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
