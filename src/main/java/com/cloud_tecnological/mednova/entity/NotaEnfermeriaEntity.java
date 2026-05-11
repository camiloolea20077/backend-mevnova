package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "nota_enfermeria")
@Getter
@Setter
public class NotaEnfermeriaEntity {

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

    @Column(name = "tipo_nota", nullable = false, length = 30)
    private String tipo_nota;

    @Column(name = "turno", length = 10)
    private String turno;

    @Column(name = "fecha_nota", nullable = false)
    private LocalDateTime fecha_nota;

    @Column(name = "contenido", nullable = false, columnDefinition = "text")
    private String contenido;

    @Column(name = "tension_sistolica")
    private Integer tension_sistolica;

    @Column(name = "tension_diastolica")
    private Integer tension_diastolica;

    @Column(name = "frecuencia_cardiaca")
    private Integer frecuencia_cardiaca;

    @Column(name = "frecuencia_respiratoria")
    private Integer frecuencia_respiratoria;

    @Column(name = "temperatura", precision = 4, scale = 1)
    private BigDecimal temperatura;

    @Column(name = "saturacion_oxigeno")
    private Integer saturacion_oxigeno;

    @Column(name = "glucometria", precision = 5, scale = 1)
    private BigDecimal glucometria;

    @Column(name = "dolor_eva")
    private Integer dolor_eva;

    @Column(name = "firmada", nullable = false)
    private Boolean firmada;

    @Column(name = "fecha_firma")
    private LocalDateTime fecha_firma;

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
        if (firmada == null) firmada = false;
        if (fecha_nota == null) fecha_nota = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
