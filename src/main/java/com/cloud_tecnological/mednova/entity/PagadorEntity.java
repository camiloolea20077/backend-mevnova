package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pagador")
@Getter
@Setter
public class PagadorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "tercero_id", nullable = false)
    private Long tercero_id;

    @Column(name = "codigo", nullable = false, length = 30)
    private String codigo;

    @Column(name = "tipo_pagador_id", nullable = false)
    private Long tipo_pagador_id;

    @Column(name = "tipo_cliente_id")
    private Long tipo_cliente_id;

    @Column(name = "codigo_eps", length = 20)
    private String codigo_eps;

    @Column(name = "codigo_administradora", length = 20)
    private String codigo_administradora;

    @Column(name = "dias_radicacion")
    private Integer dias_radicacion;

    @Column(name = "dias_respuesta_glosa")
    private Integer dias_respuesta_glosa;

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
        if (dias_radicacion == null) dias_radicacion = 30;
        if (dias_respuesta_glosa == null) dias_respuesta_glosa = 15;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
