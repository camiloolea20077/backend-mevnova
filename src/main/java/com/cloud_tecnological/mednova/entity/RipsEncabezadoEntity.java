package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rips_encabezado")
@Getter
@Setter
public class RipsEncabezadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "factura_id", nullable = false)
    private Long factura_id;

    @Column(name = "pagador_id")
    private Long pagador_id;

    @Column(name = "fecha_generacion")
    private LocalDateTime fecha_generacion;

    @Column(name = "estado", length = 20)
    private String estado;

    @Column(name = "version_norma", length = 20)
    private String version_norma;

    @Column(name = "observaciones", columnDefinition = "text")
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
        if (fecha_generacion == null) fecha_generacion = LocalDateTime.now();
        if (estado == null) estado = "GENERADO";
        if (version_norma == null) version_norma = "RES_3374";
    }
}
