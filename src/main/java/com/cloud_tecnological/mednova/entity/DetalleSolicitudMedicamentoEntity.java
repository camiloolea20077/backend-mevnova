package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_solicitud_medicamento")
@Getter
@Setter
public class DetalleSolicitudMedicamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "solicitud_id", nullable = false)
    private Long solicitud_id;

    @Column(name = "servicio_salud_id", nullable = false)
    private Long servicio_salud_id;

    @Column(name = "cantidad_solicitada", nullable = false, precision = 15, scale = 3)
    private BigDecimal cantidad_solicitada;

    @Column(name = "cantidad_despachada", nullable = false, precision = 15, scale = 3)
    private BigDecimal cantidad_despachada;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "motivo_rechazo", length = 300)
    private String motivo_rechazo;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "deleted_at")
    private LocalDateTime deleted_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
        if (estado == null) estado = "PENDIENTE";
        if (cantidad_despachada == null) cantidad_despachada = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
