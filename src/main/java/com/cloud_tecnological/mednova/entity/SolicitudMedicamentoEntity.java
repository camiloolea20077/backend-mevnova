package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_medicamento")
@Getter
@Setter
public class SolicitudMedicamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "numero_solicitud", nullable = false, length = 30)
    private String numero_solicitud;

    @Column(name = "bodega_origen_id", nullable = false)
    private Long bodega_origen_id;

    @Column(name = "bodega_destino_id", nullable = false)
    private Long bodega_destino_id;

    @Column(name = "profesional_solicitante_id")
    private Long profesional_solicitante_id;

    @Column(name = "estado_solicitud", nullable = false, length = 20)
    private String estado_solicitud;

    @Column(name = "prioridad", nullable = false, length = 20)
    private String prioridad;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fecha_solicitud;

    @Column(name = "fecha_despacho")
    private LocalDateTime fecha_despacho;

    @Column(name = "motivo", columnDefinition = "text")
    private String motivo;

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
        if (estado_solicitud == null) estado_solicitud = "PENDIENTE";
        if (prioridad == null) prioridad = "NORMAL";
        if (fecha_solicitud == null) fecha_solicitud = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
