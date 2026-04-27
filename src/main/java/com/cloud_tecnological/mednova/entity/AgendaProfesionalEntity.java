package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "agenda_profesional")
@Getter
@Setter
public class AgendaProfesionalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "profesional_id", nullable = false)
    private Long profesional_id;

    @Column(name = "especialidad_id", nullable = false)
    private Long especialidad_id;

    @Column(name = "recurso_fisico_id")
    private Long recurso_fisico_id;

    @Column(name = "calendario_id", nullable = false)
    private Long calendario_id;

    @Column(name = "estado_agenda_id", nullable = false)
    private Long estado_agenda_id;

    @Column(name = "duracion_cita_minutos", nullable = false)
    private Integer duracion_cita_minutos;

    @Column(name = "fecha_vigencia_desde", nullable = false)
    private LocalDate fecha_vigencia_desde;

    @Column(name = "fecha_vigencia_hasta")
    private LocalDate fecha_vigencia_hasta;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "usuario_creacion")
    private Long usuario_creacion;

    @Column(name = "usuario_modificacion")
    private Long usuario_modificacion;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
        if (duracion_cita_minutos == null) duracion_cita_minutos = 20;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
