package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vacuna_paciente")
@Getter
@Setter
public class VacunaPacienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "paciente_id", nullable = false)
    private Long paciente_id;

    @Column(name = "nombre_vacuna", nullable = false, length = 150)
    private String nombre_vacuna;

    @Column(name = "codigo_vacuna", length = 30)
    private String codigo_vacuna;

    @Column(name = "dosis", nullable = false)
    private Integer dosis;

    @Column(name = "total_dosis_esquema")
    private Integer total_dosis_esquema;

    @Column(name = "fecha_aplicacion", nullable = false)
    private LocalDate fecha_aplicacion;

    @Column(name = "fecha_proxima_dosis")
    private LocalDate fecha_proxima_dosis;

    @Column(name = "laboratorio", length = 100)
    private String laboratorio;

    @Column(name = "numero_lote", length = 50)
    private String numero_lote;

    @Column(name = "via_administracion_id")
    private Long via_administracion_id;

    @Column(name = "profesional_aplica_id")
    private Long profesional_aplica_id;

    @Column(name = "institucion_aplica", length = 200)
    private String institucion_aplica;

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
