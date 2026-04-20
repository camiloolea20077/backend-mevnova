package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Getter
@Setter
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "nombre_usuario", nullable = false, length = 100)
    private String nombre_usuario;

    @Column(name = "hash_password", nullable = false)
    private String hash_password;

    @Column(name = "nombre_completo", length = 200)
    private String nombre_completo;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "bloqueado", nullable = false)
    private Boolean bloqueado;

    @Column(name = "intentos_fallidos", nullable = false)
    private Integer intentos_fallidos;

    @Column(name = "requiere_cambio_password", nullable = false)
    private Boolean requiere_cambio_password;

    @Column(name = "fecha_ultimo_ingreso")
    private LocalDateTime fecha_ultimo_ingreso;

    @Column(name = "ip_ultimo_ingreso", length = 45)
    private String ip_ultimo_ingreso;

    @Column(name = "fecha_bloqueo")
    private LocalDateTime fecha_bloqueo;

    @Column(name = "motivo_bloqueo", length = 100)
    private String motivo_bloqueo;

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
        if (bloqueado == null) bloqueado = false;
        if (intentos_fallidos == null) intentos_fallidos = 0;
        if (requiere_cambio_password == null) requiere_cambio_password = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
