package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "orden_clinica")
@Getter
@Setter
public class OrdenClinicaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "atencion_id", nullable = false)
    private Long atencion_id;

    @Column(name = "numero_orden", nullable = false, length = 30)
    private String numero_orden;

    @Column(name = "tipo_orden", nullable = false, length = 30)
    private String tipo_orden;

    @Column(name = "estado_orden", nullable = false, length = 30)
    private String estado_orden;

    @Column(name = "profesional_id", nullable = false)
    private Long profesional_id;

    @Column(name = "fecha_orden", nullable = false)
    private LocalDateTime fecha_orden;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @Column(nullable = false)
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
        if (fecha_orden == null) fecha_orden = LocalDateTime.now();
        if (activo == null) activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
