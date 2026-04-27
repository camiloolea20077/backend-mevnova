package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lista_espera_cita")
@Getter
@Setter
public class ListaEsperaCitaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "paciente_id", nullable = false)
    private Long paciente_id;

    @Column(name = "especialidad_id")
    private Long especialidad_id;

    @Column(name = "servicio_salud_id")
    private Long servicio_salud_id;

    // 1=urgente … 4=baja
    @Column(name = "prioridad", nullable = false)
    private Integer prioridad;

    @Column(name = "fecha_preferida_desde")
    private LocalDate fecha_preferida_desde;

    @Column(name = "fecha_preferida_hasta")
    private LocalDate fecha_preferida_hasta;

    // CHECK: ACTIVA | ASIGNADA | VENCIDA | CANCELADA
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "usuario_creacion")
    private Long usuario_creacion;

    @Column(name = "usuario_modificacion")
    private Long usuario_modificacion;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo   == null) activo   = true;
        if (prioridad == null) prioridad = 3;
        if (estado   == null) estado   = "ACTIVA";
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
