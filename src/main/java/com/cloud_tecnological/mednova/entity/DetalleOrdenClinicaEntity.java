package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_orden_clinica")
@Getter
@Setter
public class DetalleOrdenClinicaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "orden_clinica_id", nullable = false)
    private Long orden_clinica_id;

    @Column(name = "servicio_salud_id", nullable = false)
    private Long servicio_salud_id;

    @Column(name = "cantidad")
    private BigDecimal cantidad;

    @Column(name = "indicaciones", columnDefinition = "text")
    private String indicaciones;

    @Column(name = "urgencia", length = 20)
    private String urgencia;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
        if (cantidad == null) cantidad = BigDecimal.ONE;
        if (urgencia == null) urgencia = "NORMAL"; // CHECK: NORMAL | URGENTE | PRIORITARIA
    }
}
