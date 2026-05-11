package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "administracion_medicamento")
@Getter
@Setter
public class AdministracionMedicamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "atencion_id", nullable = false)
    private Long atencion_id;

    @Column(name = "paciente_id", nullable = false)
    private Long paciente_id;

    @Column(name = "detalle_prescripcion_id", nullable = false)
    private Long detalle_prescripcion_id;

    @Column(name = "dispensacion_id")
    private Long dispensacion_id;

    @Column(name = "lote_id")
    private Long lote_id;

    @Column(name = "profesional_id", nullable = false)
    private Long profesional_id;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDateTime fecha_programada;

    @Column(name = "fecha_administracion")
    private LocalDateTime fecha_administracion;

    @Column(name = "dosis_administrada", precision = 10, scale = 2)
    private BigDecimal dosis_administrada;

    @Column(name = "via_administracion_id")
    private Long via_administracion_id;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "motivo_omision", length = 300)
    private String motivo_omision;

    @Column(name = "reaccion_adversa", columnDefinition = "text")
    private String reaccion_adversa;

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
        if (estado == null) estado = "PROGRAMADA";
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
