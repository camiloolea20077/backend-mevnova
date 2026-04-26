package com.cloud_tecnological.mednova.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "admision")
@Getter
@Setter
public class AdmisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "numero_admision", nullable = false, length = 30)
    private String numero_admision;

    @Column(name = "paciente_id", nullable = false)
    private Long paciente_id;

    @Column(name = "tipo_admision_id", nullable = false)
    private Long tipo_admision_id;

    @Column(name = "estado_admision_id", nullable = false)
    private Long estado_admision_id;

    @Column(name = "origen_atencion_id", nullable = false)
    private Long origen_atencion_id;

    @Column(name = "pagador_id", nullable = false)
    private Long pagador_id;

    @Column(name = "contrato_id")
    private Long contrato_id;

    @Column(name = "acompanante_id")
    private Long acompanante_id;

    @Column(name = "motivo_ingreso", columnDefinition = "text")
    private String motivo_ingreso;

    @Column(name = "fecha_admision", nullable = false)
    private LocalDateTime fecha_admision;

    @Column(name = "fecha_egreso")
    private LocalDateTime fecha_egreso;

    @Column(name = "tipo_egreso", length = 30)
    private String tipo_egreso;

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
        if (fecha_admision == null) fecha_admision = LocalDateTime.now();
        if (activo == null) activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
