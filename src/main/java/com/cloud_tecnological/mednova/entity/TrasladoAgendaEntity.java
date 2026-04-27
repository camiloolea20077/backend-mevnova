package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "traslado_agenda")
@Getter
@Setter
public class TrasladoAgendaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "sede_id", nullable = false)
    private Long sede_id;

    @Column(name = "agenda_origen_id", nullable = false)
    private Long agenda_origen_id;

    @Column(name = "fecha_origen", nullable = false)
    private LocalDate fecha_origen;

    @Column(name = "agenda_destino_id")
    private Long agenda_destino_id;

    @Column(name = "fecha_destino")
    private LocalDate fecha_destino;

    @Column(name = "motivo", nullable = false, length = 300)
    private String motivo;

    @Column(name = "total_citas", nullable = false)
    private Integer total_citas;

    @Column(name = "citas_trasladadas", nullable = false)
    private Integer citas_trasladadas;

    @Column(name = "citas_fallidas", nullable = false)
    private Integer citas_fallidas;

    @Column(name = "fecha_ejecucion", nullable = false, updatable = false)
    private LocalDateTime fecha_ejecucion;

    @Column(name = "usuario_creacion")
    private Long usuario_creacion;

    @PrePersist
    protected void onCreate() {
        fecha_ejecucion = LocalDateTime.now();
        if (total_citas       == null) total_citas       = 0;
        if (citas_trasladadas == null) citas_trasladadas = 0;
        if (citas_fallidas    == null) citas_fallidas    = 0;
    }
}
