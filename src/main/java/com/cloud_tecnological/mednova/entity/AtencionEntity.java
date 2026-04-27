package com.cloud_tecnological.mednova.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "atencion")
@Getter
@Setter
public class AtencionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "admision_id", nullable = false)
    private Long admision_id;

    @Column(name = "numero_atencion", nullable = false, length = 30)
    private String numero_atencion;

    @Column(name = "estado_atencion_id", nullable = false)
    private Long estado_atencion_id;

    @Column(name = "finalidad_atencion_id")
    private Long finalidad_atencion_id;

    @Column(name = "profesional_id")
    private Long profesional_id;

    @Column(name = "especialidad_id")
    private Long especialidad_id;

    @Column(name = "recurso_fisico_id")
    private Long recurso_fisico_id;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fecha_inicio;

    @Column(name = "fecha_cierre")
    private LocalDateTime fecha_cierre;

    @Column(name = "nivel_triage", length = 5)
    private String nivel_triage;

    @Column(name = "motivo_consulta", columnDefinition = "text")
    private String motivo_consulta;

    @Column(name = "enfermedad_actual", columnDefinition = "text")
    private String enfermedad_actual;

    @Column(name = "antecedentes", columnDefinition = "text")
    private String antecedentes;

    @Column(name = "examen_fisico", columnDefinition = "text")
    private String examen_fisico;

    @Column(name = "analisis", columnDefinition = "text")
    private String analisis;

    @Column(name = "plan", columnDefinition = "text")
    private String plan;

    @Column(name = "conducta", length = 50)
    private String conducta;

    @Column(name = "tension_sistolica")
    private Integer tension_sistolica;

    @Column(name = "tension_diastolica")
    private Integer tension_diastolica;

    @Column(name = "frecuencia_cardiaca")
    private Integer frecuencia_cardiaca;

    @Column(name = "frecuencia_respiratoria")
    private Integer frecuencia_respiratoria;

    @Column(name = "temperatura")
    private BigDecimal temperatura;

    @Column(name = "saturacion_oxigeno")
    private Integer saturacion_oxigeno;

    @Column(name = "peso")
    private BigDecimal peso;

    @Column(name = "talla")
    private BigDecimal talla;

    @Column(name = "glucometria")
    private BigDecimal glucometria;

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
        if (fecha_inicio == null) fecha_inicio = LocalDateTime.now();
        if (activo == null) activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
