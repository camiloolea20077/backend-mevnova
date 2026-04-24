package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "seguridad_social_paciente")
@Getter
@Setter
public class SeguridadSocialPacienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "paciente_id", nullable = false)
    private Long paciente_id;

    @Column(name = "pagador_id", nullable = false)
    private Long pagador_id;

    @Column(name = "regimen_id")
    private Long regimen_id;

    @Column(name = "categoria_afiliacion_id")
    private Long categoria_afiliacion_id;

    @Column(name = "tipo_afiliacion_id")
    private Long tipo_afiliacion_id;

    @Column(name = "numero_afiliacion", length = 50)
    private String numero_afiliacion;

    @Column(name = "tercero_cotizante_id")
    private Long tercero_cotizante_id;

    @Column(name = "fecha_afiliacion")
    private LocalDate fecha_afiliacion;

    @Column(name = "fecha_vigencia_desde", nullable = false)
    private LocalDate fecha_vigencia_desde;

    @Column(name = "fecha_vigencia_hasta")
    private LocalDate fecha_vigencia_hasta;

    @Column(name = "vigente", nullable = false)
    private Boolean vigente;

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

    @Column(name = "usuario_modificacion")
    private Long usuario_modificacion;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
        if (vigente == null) vigente = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
