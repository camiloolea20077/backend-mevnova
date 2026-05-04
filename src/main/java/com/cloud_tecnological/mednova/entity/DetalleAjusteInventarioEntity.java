package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_ajuste_inventario")
@Getter
@Setter
public class DetalleAjusteInventarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "ajuste_id", nullable = false)
    private Long ajuste_id;

    @Column(name = "lote_id", nullable = false)
    private Long lote_id;

    @Column(name = "servicio_salud_id", nullable = false)
    private Long servicio_salud_id;

    @Column(name = "cantidad_sistema", nullable = false, precision = 15, scale = 3)
    private BigDecimal cantidad_sistema;

    @Column(name = "cantidad_real", nullable = false, precision = 15, scale = 3)
    private BigDecimal cantidad_real;

    @Column(name = "diferencia", insertable = false, updatable = false, precision = 15, scale = 3)
    private BigDecimal diferencia;

    @Column(name = "valor_unitario", precision = 15, scale = 2)
    private BigDecimal valor_unitario;

    @Column(name = "valor_diferencia", precision = 15, scale = 2)
    private BigDecimal valor_diferencia;

    @Column(name = "observaciones", length = 300)
    private String observaciones;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "deleted_at")
    private LocalDateTime deleted_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
    }
}
