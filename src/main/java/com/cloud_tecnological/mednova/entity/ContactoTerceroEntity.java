package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "contacto_tercero")
@Getter
@Setter
public class ContactoTerceroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "tercero_id", nullable = false)
    private Long tercero_id;

    @Column(name = "tipo_contacto_id", nullable = false)
    private Long tipo_contacto_id;

    @Column(name = "valor", nullable = false, length = 200)
    private String valor;

    @Column(name = "es_principal", nullable = false)
    private Boolean es_principal;

    @Column(name = "acepta_notificaciones", nullable = false)
    private Boolean acepta_notificaciones;

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
        if (es_principal == null) es_principal = false;
        if (acepta_notificaciones == null) acepta_notificaciones = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
