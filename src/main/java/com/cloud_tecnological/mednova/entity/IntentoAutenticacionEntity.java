package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "intento_autenticacion")
@Getter
@Setter
public class IntentoAutenticacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id")
    private Long empresa_id;

    @Column(name = "usuario_id")
    private Long usuario_id;

    @Column(name = "paso", nullable = false, length = 20)
    private String paso;

    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "user_agent", length = 512)
    private String user_agent;

    @Column(name = "exitoso", nullable = false)
    private Boolean exitoso;

    @Column(name = "motivo_fallo", length = 100)
    private String motivo_fallo;

    // Campo de dominio: fecha exacta del intento
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    // Auditoría estándar v3
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "deleted_at")
    private LocalDateTime deleted_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (fecha == null) fecha = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
