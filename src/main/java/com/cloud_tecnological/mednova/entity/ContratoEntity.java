package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contrato")
@Getter
@Setter
public class ContratoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @Column(name = "pagador_id", nullable = false)
    private Long pagador_id;

    @Column(name = "modalidad_pago_id", nullable = false)
    private Long modalidad_pago_id;

    @Column(name = "tarifario_id")
    private Long tarifario_id;

    @Column(name = "objeto", columnDefinition = "text")
    private String objeto;

    @Column(name = "fecha_vigencia_desde", nullable = false)
    private LocalDate fecha_vigencia_desde;

    @Column(name = "fecha_vigencia_hasta")
    private LocalDate fecha_vigencia_hasta;

    @Column(name = "valor_contrato", precision = 18, scale = 2)
    private BigDecimal valor_contrato;

    @Column(name = "techo_mensual", precision = 18, scale = 2)
    private BigDecimal techo_mensual;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
