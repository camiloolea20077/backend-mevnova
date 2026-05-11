package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "epicrisis")
@Getter
@Setter
public class EpicrisisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "admision_id", nullable = false)
    private Long admision_id;

    @Column(name = "paciente_id", nullable = false)
    private Long paciente_id;

    @Column(name = "profesional_id", nullable = false)
    private Long profesional_id;

    @Column(name = "fecha_egreso", nullable = false)
    private LocalDateTime fecha_egreso;

    @Column(name = "motivo_ingreso", nullable = false, columnDefinition = "text")
    private String motivo_ingreso;

    @Column(name = "diagnostico_ingreso", nullable = false, columnDefinition = "text")
    private String diagnostico_ingreso;

    @Column(name = "diagnostico_egreso", nullable = false, columnDefinition = "text")
    private String diagnostico_egreso;

    @Column(name = "procedimientos_realizados", columnDefinition = "text")
    private String procedimientos_realizados;

    @Column(name = "evolucion_resumen", nullable = false, columnDefinition = "text")
    private String evolucion_resumen;

    @Column(name = "complicaciones", columnDefinition = "text")
    private String complicaciones;

    @Column(name = "plan_seguimiento", nullable = false, columnDefinition = "text")
    private String plan_seguimiento;

    @Column(name = "medicamentos_egreso", columnDefinition = "text")
    private String medicamentos_egreso;

    @Column(name = "recomendaciones", nullable = false, columnDefinition = "text")
    private String recomendaciones;

    @Column(name = "indicaciones_dieta", columnDefinition = "text")
    private String indicaciones_dieta;

    @Column(name = "indicaciones_actividad", columnDefinition = "text")
    private String indicaciones_actividad;

    @Column(name = "fecha_proximo_control")
    private LocalDate fecha_proximo_control;

    @Column(name = "firmada", nullable = false)
    private Boolean firmada;

    @Column(name = "fecha_firma")
    private LocalDateTime fecha_firma;

    @Column(name = "pdf_url", length = 500)
    private String pdf_url;

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
        if (firmada == null) firmada = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
