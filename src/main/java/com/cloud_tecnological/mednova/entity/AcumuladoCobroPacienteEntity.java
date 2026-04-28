package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "acumulado_cobro_paciente")
@Getter
@Setter
public class AcumuladoCobroPacienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "paciente_id", nullable = false)
    private Long paciente_id;

    @Column(name = "vigencia", nullable = false)
    private Integer vigencia;

    @Column(name = "tipo_cobro", nullable = false, length = 30)
    private String tipo_cobro;

    @Column(name = "valor_acumulado_evento", nullable = false, precision = 14, scale = 2)
    private BigDecimal valor_acumulado_evento;

    @Column(name = "valor_acumulado_anual", nullable = false, precision = 14, scale = 2)
    private BigDecimal valor_acumulado_anual;

    @Column(name = "tope_evento_aplicado", precision = 14, scale = 2)
    private BigDecimal tope_evento_aplicado;

    @Column(name = "tope_anual_aplicado", precision = 14, scale = 2)
    private BigDecimal tope_anual_aplicado;

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
        if (valor_acumulado_evento == null) valor_acumulado_evento = BigDecimal.ZERO;
        if (valor_acumulado_anual == null) valor_acumulado_anual = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
