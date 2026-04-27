package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_prescripcion")
@Getter
@Setter
public class DetallePrescripcionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "prescripcion_id", nullable = false)
    private Long prescripcion_id;

    @Column(name = "servicio_salud_id", nullable = false)
    private Long servicio_salud_id;

    @Column(name = "dosis", nullable = false)
    private BigDecimal dosis;

    @Column(name = "unidad_dosis", nullable = false, length = 20)
    private String unidad_dosis;

    @Column(name = "via_administracion_id", nullable = false)
    private Long via_administracion_id;

    @Column(name = "frecuencia_dosis_id", nullable = false)
    private Long frecuencia_dosis_id;

    @Column(name = "duracion_dias")
    private Integer duracion_dias;

    @Column(name = "cantidad_despachar")
    private BigDecimal cantidad_despachar;

    @Column(name = "indicaciones", columnDefinition = "text")
    private String indicaciones;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
    }
}
