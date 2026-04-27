package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_traslado_agenda")
@Getter
@Setter
public class DetalleTrasladoAgendaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "traslado_id", nullable = false)
    private Long traslado_id;

    @Column(name = "cita_id", nullable = false)
    private Long cita_id;

    // CHECK: TRASLADADA | FALLIDA | OMITIDA | EN_LISTA_ESPERA
    @Column(name = "resultado", nullable = false, length = 20)
    private String resultado;

    @Column(name = "observacion", length = 300)
    private String observacion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
    }
}
