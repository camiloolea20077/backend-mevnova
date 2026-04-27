package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "cita")
@Getter
@Setter
public class CitaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "numero_cita", nullable = false, length = 30)
    private String numero_cita;

    @Column(name = "disponibilidad_id", nullable = false)
    private Long disponibilidad_id;

    @Column(name = "agenda_id", nullable = false)
    private Long agenda_id;

    @Column(name = "paciente_id", nullable = false)
    private Long paciente_id;

    @Column(name = "servicio_salud_id")
    private Long servicio_salud_id;

    @Column(name = "tipo_cita_id", nullable = false)
    private Long tipo_cita_id;

    @Column(name = "estado_cita_id", nullable = false)
    private Long estado_cita_id;

    @Column(name = "especialidad_id")
    private Long especialidad_id;

    @Column(name = "fecha_cita", nullable = false)
    private LocalDateTime fecha_cita;

    @Column(name = "motivo", columnDefinition = "text")
    private String motivo;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fecha_asignacion;

    @Column(name = "fecha_atencion")
    private LocalDateTime fecha_atencion;

    @Column(name = "motivo_cancelacion_id")
    private Long motivo_cancelacion_id;

    @Column(name = "motivo_reprogramacion_id")
    private Long motivo_reprogramacion_id;

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
        if (fecha_asignacion == null) fecha_asignacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
