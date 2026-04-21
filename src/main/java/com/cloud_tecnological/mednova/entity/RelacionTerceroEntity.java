package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "relacion_tercero")
@Getter
@Setter
public class RelacionTerceroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "tercero_origen_id", nullable = false)
    private Long tercero_origen_id;

    @Column(name = "tercero_destino_id", nullable = false)
    private Long tercero_destino_id;

    @Column(name = "tipo_relacion_id", nullable = false)
    private Long tipo_relacion_id;

    @Column(name = "es_responsable", nullable = false)
    private Boolean es_responsable;

    @Column(name = "es_contacto_emergencia", nullable = false)
    private Boolean es_contacto_emergencia;

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
        if (es_responsable == null) es_responsable = false;
        if (es_contacto_emergencia == null) es_contacto_emergencia = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
