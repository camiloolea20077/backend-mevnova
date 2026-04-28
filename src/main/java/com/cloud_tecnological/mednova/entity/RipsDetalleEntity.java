package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rips_detalle")
@Getter
@Setter
public class RipsDetalleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "rips_encabezado_id", nullable = false)
    private Long rips_encabezado_id;

    @Column(name = "tipo_archivo", length = 5)
    private String tipo_archivo;

    @Column(name = "linea_datos", columnDefinition = "text")
    private String linea_datos;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
    }
}
