package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicacion_habitual")
@Getter
@Setter
public class MedicacionHabitualEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "paciente_id", nullable = false)
    private Long paciente_id;

    @Column(name = "servicio_salud_id")
    private Long servicio_salud_id;

    @Column(name = "nombre_medicamento", nullable = false, length = 200)
    private String nombre_medicamento;

    @Column(name = "dosis", length = 50)
    private String dosis;

    @Column(name = "via_administracion_id")
    private Long via_administracion_id;

    @Column(name = "frecuencia_dosis_id")
    private Long frecuencia_dosis_id;

    @Column(name = "fecha_inicio")
    private LocalDate fecha_inicio;

    @Column(name = "fecha_fin")
    private LocalDate fecha_fin;

    @Column(name = "indicacion", length = 300)
    private String indicacion;

    @Column(name = "profesional_prescriptor", length = 200)
    private String profesional_prescriptor;

    @Column(name = "es_activo", nullable = false)
    private Boolean es_activo;

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
        if (es_activo == null) es_activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
