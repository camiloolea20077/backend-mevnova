package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_tarifario")
@Getter
@Setter
public class DetalleTarifarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "tarifario_id", nullable = false)
    private Long tarifario_id;

    @Column(name = "servicio_salud_id", nullable = false)
    private Long servicio_salud_id;

    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "observaciones", length = 300)
    private String observaciones;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
    }
}
