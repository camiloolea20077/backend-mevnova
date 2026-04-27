package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "disponibilidad_cita")
@Getter
@Setter
public class DisponibilidadCitaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "agenda_id", nullable = false)
    private Long agenda_id;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime hora_inicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime hora_fin;

    @Column(name = "cupos_totales", nullable = false)
    private Integer cupos_totales;

    @Column(name = "cupos_ocupados", nullable = false)
    private Integer cupos_ocupados;

    @Column(name = "estado_disponibilidad_id", nullable = false)
    private Long estado_disponibilidad_id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (cupos_ocupados == null) cupos_ocupados = 0;
    }
}
