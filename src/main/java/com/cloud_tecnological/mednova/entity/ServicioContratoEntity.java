package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "servicio_contrato")
@Getter
@Setter
public class ServicioContratoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "contrato_id", nullable = false)
    private Long contrato_id;

    @Column(name = "servicio_salud_id", nullable = false)
    private Long servicio_salud_id;

    @Column(name = "requiere_autorizacion", nullable = false)
    private Boolean requiere_autorizacion;

    @Column(name = "cantidad_maxima")
    private Integer cantidad_maxima;

    @Column(name = "observaciones", length = 300)
    private String observaciones;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "usuario_creacion")
    private Long usuario_creacion;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
        if (requiere_autorizacion == null) requiere_autorizacion = false;
    }
}
