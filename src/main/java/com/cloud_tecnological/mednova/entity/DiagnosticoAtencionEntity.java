package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "diagnostico_atencion")
@Getter
@Setter
public class DiagnosticoAtencionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "atencion_id", nullable = false)
    private Long atencion_id;

    @Column(name = "catalogo_diagnostico_id", nullable = false)
    private Long catalogo_diagnostico_id;

    @Column(name = "tipo_diagnostico", nullable = false, length = 20)
    private String tipo_diagnostico;

    @Column(name = "es_confirmado", nullable = false)
    private Boolean es_confirmado;

    @Column(name = "observaciones", length = 300)
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
        if (es_confirmado == null) es_confirmado = false;
    }
}
