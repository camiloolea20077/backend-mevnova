package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ajuste_inventario")
@Getter
@Setter
public class AjusteInventarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "bodega_id", nullable = false)
    private Long bodega_id;

    @Column(name = "numero_ajuste", nullable = false, length = 30)
    private String numero_ajuste;

    @Column(name = "tipo_ajuste", nullable = false, length = 30)
    private String tipo_ajuste;

    @Column(name = "fecha_ajuste", nullable = false)
    private LocalDate fecha_ajuste;

    @Column(name = "motivo", nullable = false, columnDefinition = "text")
    private String motivo;

    @Column(name = "valor_total_ajuste", nullable = false, precision = 18, scale = 2)
    private BigDecimal valor_total_ajuste;

    @Column(name = "aprobado_por_id")
    private Long aprobado_por_id;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fecha_aprobacion;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

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
        if (estado == null) estado = "BORRADOR";
        if (valor_total_ajuste == null) valor_total_ajuste = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
