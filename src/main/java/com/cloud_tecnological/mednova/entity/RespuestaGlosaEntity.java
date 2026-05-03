package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "respuesta_glosa")
@Getter
@Setter
public class RespuestaGlosaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "glosa_id", nullable = false)
    private Long glosa_id;

    @Column(name = "detalle_glosa_id", nullable = false)
    private Long detalle_glosa_id;

    @Column(name = "tipo_respuesta", nullable = false, length = 20)
    private String tipo_respuesta;

    @Column(name = "valor_aceptado", nullable = false, precision = 18, scale = 2)
    private BigDecimal valor_aceptado;

    @Column(name = "argumentacion", nullable = false, columnDefinition = "text")
    private String argumentacion;

    @Column(name = "soporte_url", length = 500)
    private String soporte_url;

    @Column(name = "profesional_respuesta_id")
    private Long profesional_respuesta_id;

    @Column(name = "fecha_respuesta", nullable = false)
    private LocalDateTime fecha_respuesta;

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
        if (fecha_respuesta == null) fecha_respuesta = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
