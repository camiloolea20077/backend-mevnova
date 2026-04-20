package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sede")
@Getter
@Setter
public class SedeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "codigo_habilitacion_reps", length = 20)
    private String codigo_habilitacion_reps;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "pais_id", nullable = false)
    private Long pais_id;

    @Column(name = "departamento_id", nullable = false)
    private Long departamento_id;

    @Column(name = "municipio_id", nullable = false)
    private Long municipio_id;

    @Column(name = "direccion", length = 300)
    private String direccion;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "correo", length = 150)
    private String correo;

    @Column(name = "es_principal", nullable = false)
    private Boolean es_principal;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
