package com.cloud_tecnological.mednova.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tercero")
@Getter
@Setter
public class TerceroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "tipo_tercero_id", nullable = false)
    private Long tipo_tercero_id;

    @Column(name = "tipo_documento_id", nullable = false)
    private Long tipo_documento_id;

    @Column(name = "numero_documento", nullable = false, length = 30)
    private String numero_documento;

    @Column(name = "digito_verificacion", length = 2)
    private String digito_verificacion;

    @Column(name = "primer_nombre", length = 100)
    private String primer_nombre;

    @Column(name = "segundo_nombre", length = 100)
    private String segundo_nombre;

    @Column(name = "primer_apellido", length = 100)
    private String primer_apellido;

    @Column(name = "segundo_apellido", length = 100)
    private String segundo_apellido;

    @Column(name = "razon_social", length = 300)
    private String razon_social;

    @Column(name = "nombre_completo", length = 400)
    private String nombre_completo;

    @Column(name = "fecha_nacimiento")
    private LocalDate fecha_nacimiento;

    @Column(name = "sexo_id")
    private Long sexo_id;

    @Column(name = "genero_id")
    private Long genero_id;

    @Column(name = "identidad_genero_id")
    private Long identidad_genero_id;

    @Column(name = "orientacion_sexual_id")
    private Long orientacion_sexual_id;

    @Column(name = "estado_civil_id")
    private Long estado_civil_id;

    @Column(name = "nivel_escolaridad_id")
    private Long nivel_escolaridad_id;

    @Column(name = "ocupacion_id")
    private Long ocupacion_id;

    @Column(name = "pertenencia_etnica_id")
    private Long pertenencia_etnica_id;

    @Column(name = "pais_nacimiento_id")
    private Long pais_nacimiento_id;

    @Column(name = "municipio_nacimiento_id")
    private Long municipio_nacimiento_id;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "deleted_at")
    private LocalDateTime deleted_at;

    @Column(name = "usuario_creacion")
    private Long usuario_creacion;

    @Column(name = "usuario_modificacion")
    private Long usuario_modificacion;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
