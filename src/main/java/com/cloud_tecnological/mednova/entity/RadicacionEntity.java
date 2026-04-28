package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "radicacion")
@Getter
@Setter
public class RadicacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "factura_id", nullable = false)
    private Long factura_id;

    @Column(name = "pagador_id", nullable = false)
    private Long pagador_id;

    @Column(name = "estado_radicacion_id", nullable = false)
    private Long estado_radicacion_id;

    @Column(name = "numero_radicado", length = 50)
    private String numero_radicado;

    @Column(name = "fecha_radicacion")
    private LocalDate fecha_radicacion;

    @Column(name = "fecha_limite_respuesta")
    private LocalDate fecha_limite_respuesta;

    @Column(name = "fecha_respuesta")
    private LocalDate fecha_respuesta;

    @Column(name = "soporte_url", length = 500)
    private String soporte_url;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @Column(name = "activo", nullable = false)
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
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
