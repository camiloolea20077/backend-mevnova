package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "adjunto_clinico")
@Getter
@Setter
public class AdjuntoClinicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "paciente_id", nullable = false)
    private Long paciente_id;

    @Column(name = "atencion_id")
    private Long atencion_id;

    @Column(name = "tipo_documento", nullable = false, length = 30)
    private String tipo_documento;

    @Column(name = "nombre_archivo", nullable = false, length = 300)
    private String nombre_archivo;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "url_archivo", nullable = false, length = 500)
    private String url_archivo;

    @Column(name = "mime_type", length = 100)
    private String mime_type;

    @Column(name = "tamano_bytes")
    private Long tamano_bytes;

    @Column(name = "profesional_carga_id")
    private Long profesional_carga_id;

    @Column(name = "fecha_documento")
    private LocalDate fecha_documento;

    @Column(name = "es_confidencial", nullable = false)
    private Boolean es_confidencial;

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
        if (es_confidencial == null) es_confidencial = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
