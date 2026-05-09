package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "plan_cuidados_enfermeria")
@Getter
@Setter
public class PlanCuidadosEnfermeriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "atencion_id", nullable = false)
    private Long atencion_id;

    @Column(name = "paciente_id", nullable = false)
    private Long paciente_id;

    @Column(name = "profesional_id", nullable = false)
    private Long profesional_id;

    @Column(name = "fecha_plan", nullable = false)
    private LocalDate fecha_plan;

    @Column(name = "diagnostico_enfermeria", nullable = false, columnDefinition = "text")
    private String diagnostico_enfermeria;

    @Column(name = "objetivos", nullable = false, columnDefinition = "text")
    private String objetivos;

    @Column(name = "intervenciones", nullable = false, columnDefinition = "text")
    private String intervenciones;

    @Column(name = "evaluacion", columnDefinition = "text")
    private String evaluacion;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

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
        if (estado == null) estado = "ACTIVO";
        if (fecha_plan == null) fecha_plan = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
