package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bodega")
@Getter
@Setter
public class BodegaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "tipo_bodega", nullable = false, length = 30)
    private String tipo_bodega;

    @Column(name = "responsable_id")
    private Long responsable_id;

    @Column(name = "ubicacion_fisica", length = 200)
    private String ubicacion_fisica;

    @Column(name = "es_principal", nullable = false)
    private Boolean es_principal;

    @Column(name = "permite_dispensar", nullable = false)
    private Boolean permite_dispensar;

    @Column(name = "permite_recibir", nullable = false)
    private Boolean permite_recibir;

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
        if (es_principal == null) es_principal = false;
        if (permite_dispensar == null) permite_dispensar = true;
        if (permite_recibir == null) permite_recibir = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
