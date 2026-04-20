package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Getter
@Setter
public class AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id")
    private Long empresa_id;

    @Column(name = "sede_id")
    private Long sede_id;

    @Column(name = "usuario_id")
    private Long usuario_id;

    @Column(name = "tabla_afectada", nullable = false, length = 100)
    private String tabla_afectada;

    @Column(name = "registro_id", length = 100)
    private String registro_id;

    @Column(name = "accion", nullable = false, length = 20)
    private String accion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_antes", columnDefinition = "jsonb")
    private String datos_antes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_despues", columnDefinition = "jsonb")
    private String datos_despues;

    @Column(name = "ip_origen", length = 45)
    private String ip_origen;

    @Column(name = "user_agent", length = 500)
    private String user_agent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
    }
}
