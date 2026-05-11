package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "escala_clinica")
@Getter
@Setter
public class EscalaClinicaEntity {

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

    @Column(name = "profesional_id", nullable = false)
    private Long profesional_id;

    @Column(name = "tipo_escala", nullable = false, length = 30)
    private String tipo_escala;

    @Column(name = "fecha_aplicacion", nullable = false)
    private LocalDateTime fecha_aplicacion;

    @Column(name = "puntaje_total", nullable = false)
    private Integer puntaje_total;

    @Column(name = "interpretacion", length = 200)
    private String interpretacion;

    @Column(name = "riesgo", length = 20)
    private String riesgo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detalle_escala", columnDefinition = "jsonb")
    private String detalle_escala;

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
        if (fecha_aplicacion == null) fecha_aplicacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
