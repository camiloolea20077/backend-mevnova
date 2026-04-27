package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "bloque_agenda")
@Getter
@Setter
public class BloqueAgendaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "agenda_id", nullable = false)
    private Long agenda_id;

    // 1=Lunes ... 7=Domingo (ISO 8601)
    @Column(name = "dia_semana", nullable = false)
    private Integer dia_semana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime hora_inicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime hora_fin;

    @Column(name = "cupos", nullable = false)
    private Integer cupos;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (cupos == null) cupos = 1;
    }
}
