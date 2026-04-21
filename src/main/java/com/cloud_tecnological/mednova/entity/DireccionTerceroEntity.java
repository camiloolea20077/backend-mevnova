package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "direccion_tercero")
@Getter
@Setter
public class DireccionTerceroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "tercero_id", nullable = false)
    private Long tercero_id;

    @Column(name = "tipo_direccion", nullable = false, length = 30)
    private String tipo_direccion;

    @Column(name = "zona_residencia_id")
    private Long zona_residencia_id;

    @Column(name = "pais_id", nullable = false)
    private Long pais_id;

    @Column(name = "departamento_id", nullable = false)
    private Long departamento_id;

    @Column(name = "municipio_id", nullable = false)
    private Long municipio_id;

    @Column(name = "direccion", nullable = false, length = 300)
    private String direccion;

    @Column(name = "barrio", length = 150)
    private String barrio;

    @Column(name = "codigo_postal", length = 20)
    private String codigo_postal;

    @Column(name = "referencia", length = 300)
    private String referencia;

    @Column(name = "latitud", precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 10, scale = 7)
    private BigDecimal longitud;

    @Column(name = "es_principal", nullable = false)
    private Boolean es_principal;

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
        if (es_principal == null) es_principal = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
