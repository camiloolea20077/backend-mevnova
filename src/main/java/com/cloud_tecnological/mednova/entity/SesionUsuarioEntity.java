package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sesion_usuario")
@Getter
@Setter
public class SesionUsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jti", nullable = false, unique = true)
    private String jti;

    // Vincula el refresh token al access token que lo originó
    @Column(name = "parent_jti")
    private String parent_jti;

    @Column(name = "tipo_token", nullable = false, length = 20)
    private String tipo_token;

    @Column(name = "empresa_id")
    private Long empresa_id;

    @Column(name = "usuario_id")
    private Long usuario_id;

    @Column(name = "sede_id")
    private Long sede_id;

    @Column(name = "usado", nullable = false)
    private Boolean usado;

    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fecha_expiracion;

    @Column(name = "fecha_uso")
    private LocalDateTime fecha_uso;

    @Column(name = "fecha_revocacion")
    private LocalDateTime fecha_revocacion;

    // Auditoría estándar v3
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "deleted_at")
    private LocalDateTime deleted_at;

    @PrePersist
    protected void onCreate() {
        if (usado == null) usado = false;
        created_at = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
