package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_calendario_cita")
@Getter
@Setter
public class DetalleCalendarioCitaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "calendario_id", nullable = false)
    private Long calendario_id;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "es_habil", nullable = false)
    private Boolean es_habil;

    @Column(name = "es_festivo", nullable = false)
    private Boolean es_festivo;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (es_habil  == null) es_habil  = true;
        if (es_festivo == null) es_festivo = false;
    }
}
