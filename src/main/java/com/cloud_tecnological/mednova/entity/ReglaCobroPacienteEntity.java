package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "regla_cobro_paciente")
@Getter
@Setter
public class ReglaCobroPacienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "vigencia", nullable = false)
    private Integer vigencia;

    @Column(name = "regimen_id", nullable = false)
    private Long regimen_id;

    @Column(name = "tipo_cobro", nullable = false, length = 30)
    private String tipo_cobro;

    @Column(name = "rango_ingreso_desde", precision = 14, scale = 2)
    private BigDecimal rango_ingreso_desde;

    @Column(name = "rango_ingreso_hasta", precision = 14, scale = 2)
    private BigDecimal rango_ingreso_hasta;

    @Column(name = "categoria_sisben_id")
    private Long categoria_sisben_id;

    @Column(name = "porcentaje_cobro", precision = 7, scale = 2)
    private BigDecimal porcentaje_cobro;

    @Column(name = "valor_fijo", precision = 14, scale = 2)
    private BigDecimal valor_fijo;

    @Column(name = "tope_evento", precision = 14, scale = 2)
    private BigDecimal tope_evento;

    @Column(name = "tope_anual", precision = 14, scale = 2)
    private BigDecimal tope_anual;

    @Column(name = "unidad_valor", length = 20)
    private String unidad_valor;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "usuario_creacion")
    private Long usuario_creacion;

    @Column(name = "deleted_at")
    private LocalDateTime deleted_at;

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
