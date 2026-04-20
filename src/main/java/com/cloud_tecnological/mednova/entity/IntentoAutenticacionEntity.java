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

    @Column(name = "nombre_usuario", length = 100)
    private String nombre_usuario;

    @Column(name = "paso", nullable = false, length = 20)
    private String paso;

    @Column(name = "exitoso", nullable = false)
    private Boolean exitoso;

    @Column(name = "motivo_fallo", length = 200)
    private String motivo_fallo;

    @Column(name = "ip_origen", length = 45)
    private String ip_origen;

    @Column(name = "user_agent", length = 500)
    private String user_agent;

    @Column(name = "fecha_intento", nullable = false)
    private LocalDateTime fecha_intento;

    @PrePersist
    protected void onCreate() {
        if (fecha_intento == null) fecha_intento = LocalDateTime.now();
    }
}
