package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_password")
@Getter
@Setter
public class HistorialPasswordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuario_id;

    @Column(name = "hash_password", nullable = false, length = 255)
    private String hash_password;

    @Column(name = "fecha_cambio", nullable = false, updatable = false)
    private LocalDateTime fecha_cambio;

    @PrePersist
    protected void onCreate() {
        fecha_cambio = LocalDateTime.now();
    }
}
