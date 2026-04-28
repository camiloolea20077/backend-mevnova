package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento_cuenta_por_cobrar")
@Getter
@Setter
public class MovimientoCuentaPorCobrarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "cuenta_por_cobrar_id", nullable = false)
    private Long cuenta_por_cobrar_id;

    @Column(name = "tipo_movimiento", length = 20)
    private String tipo_movimiento;

    @Column(name = "fecha_movimiento")
    private LocalDateTime fecha_movimiento;

    @Column(name = "valor", precision = 18, scale = 2)
    private BigDecimal valor;

    @Column(name = "saldo_resultante", precision = 18, scale = 2)
    private BigDecimal saldo_resultante;

    @Column(name = "referencia", length = 100)
    private String referencia;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @Column(name = "usuario_creacion")
    private Long usuario_creacion;

    @PrePersist
    protected void onCreate() {
        if (fecha_movimiento == null) fecha_movimiento = LocalDateTime.now();
    }
}
