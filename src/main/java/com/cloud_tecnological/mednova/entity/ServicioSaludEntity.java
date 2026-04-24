package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "servicio_salud")
@Getter
@Setter
public class ServicioSaludEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "codigo_interno", nullable = false, length = 30)
    private String codigo_interno;

    @Column(name = "codigo_cups", length = 20)
    private String codigo_cups;

    @Column(name = "nombre", nullable = false, length = 300)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;

    @Column(name = "categoria_servicio_salud_id", nullable = false)
    private Long categoria_servicio_salud_id;

    @Column(name = "centro_costo_id")
    private Long centro_costo_id;

    @Column(name = "unidad_medida", length = 30)
    private String unidad_medida;

    @Column(name = "requiere_autorizacion", nullable = false)
    private Boolean requiere_autorizacion;

    @Column(name = "requiere_diagnostico", nullable = false)
    private Boolean requiere_diagnostico;

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
        if (requiere_autorizacion == null) requiere_autorizacion = false;
        if (requiere_diagnostico == null) requiere_diagnostico = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
