package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "interconsulta")
@Getter
@Setter
public class InterconsultaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "atencion_origen_id", nullable = false)
    private Long atencion_origen_id;

    @Column(name = "atencion_respuesta_id")
    private Long atencion_respuesta_id;

    @Column(name = "numero_interconsulta", nullable = false, length = 30)
    private String numero_interconsulta;

    @Column(name = "profesional_solicita_id", nullable = false)
    private Long profesional_solicita_id;

    @Column(name = "profesional_responde_id")
    private Long profesional_responde_id;

    @Column(name = "especialidad_destino_id", nullable = false)
    private Long especialidad_destino_id;

    @Column(name = "motivo", nullable = false, columnDefinition = "text")
    private String motivo;

    @Column(name = "impresion_diagnostica", columnDefinition = "text")
    private String impresion_diagnostica;

    @Column(name = "pregunta_clinica", columnDefinition = "text")
    private String pregunta_clinica;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "prioridad", length = 20)
    private String prioridad;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fecha_solicitud;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fecha_respuesta;

    @Column(name = "respuesta", columnDefinition = "text")
    private String respuesta;

    @Column(name = "recomendaciones", columnDefinition = "text")
    private String recomendaciones;

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
        if (estado == null) estado = "PENDIENTE";
        if (prioridad == null) prioridad = "NORMAL";
        if (fecha_solicitud == null) fecha_solicitud = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
