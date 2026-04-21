package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "paciente")
@Getter
@Setter
public class PacienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "tercero_id", nullable = false)
    private Long tercero_id;

    @Column(name = "grupo_sanguineo_id")
    private Long grupo_sanguineo_id;

    @Column(name = "factor_rh_id")
    private Long factor_rh_id;

    @Column(name = "discapacidad_id")
    private Long discapacidad_id;

    @Column(name = "grupo_atencion_id")
    private Long grupo_atencion_id;

    @Column(name = "alergias_conocidas", columnDefinition = "text")
    private String alergias_conocidas;

    @Column(name = "observaciones_clinicas", columnDefinition = "text")
    private String observaciones_clinicas;

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
