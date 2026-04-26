package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.catalogo.CatalogoResponseDto;
import com.cloud_tecnological.mednova.services.CatalogoService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogoServiceImpl implements CatalogoService {

    private final JdbcTemplate jdbcTemplate;

    private static final List<String> CATALOGOS_VALIDOS = List.of(
            "tipo_tercero", "tipo_documento", "sexo", "genero", "orientacion_sexual",
            "identidad_genero", "estado_civil", "nivel_escolaridad", "ocupacion",
            "discapacidad", "grupo_sanguineo", "factor_rh", "grupo_sisben",
            "pertenencia_etnica", "grupo_atencion", "tipo_contacto", "tipo_relacion",
            "zona_residencia", "pais", "departamento", "municipio", "tipo_cliente",
            "regimen", "categoria_afiliacion", "tipo_afiliacion", "tipo_admision",
            "estado_admision", "origen_atencion", "estado_atencion", "estado_cita",
            "tipo_cita", "motivo_cancelacion_cita", "motivo_reprogramacion_cita",
            "estado_agenda", "estado_disponibilidad", "tipo_orden_clinica",
            "finalidad_atencion", "estado_prescripcion", "estado_orden", "especialidad",
            "via_administracion", "frecuencia_dosis", "categoria_servicio_salud",
            "tipo_pagador", "modalidad_pago", "estado_factura", "estado_radicacion",
            "estado_glosa", "estado_cartera", "medio_pago"
    );

    public CatalogoServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<CatalogoResponseDto> findAll(String tableName, Boolean activo) {
        String normalizedTableName = tableName.toLowerCase();
        
        if (!CATALOGOS_VALIDOS.contains(normalizedTableName)) {
            throw new IllegalArgumentException("Catálogo no válido: " + tableName);
        }

        List<String> columnas = obtenerColumnas(normalizedTableName);
        String selectColumns = String.join(", ", columnas);
        String sql;
        
        if (activo != null) {
            sql = "SELECT " + selectColumns + " FROM " + normalizedTableName + " WHERE activo = ? ORDER BY codigo";
        } else {
            sql = "SELECT " + selectColumns + " FROM " + normalizedTableName + " ORDER BY codigo";
        }

        List<CatalogoResponseDto> resultados;
        if (activo != null) {
            resultados = jdbcTemplate.query(sql, new Object[]{activo}, (rs, rowNum) -> mapRow(rs, rowNum, columnas));
        } else {
            resultados = jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs, rowNum, columnas));
        }

        return resultados.stream()
                .peek(dto -> dto.setLabel((dto.getCodigo() + " - " + dto.getNombre()).toUpperCase()))
                .collect(Collectors.toList());
    }

    private List<String> obtenerColumnas(String tableName) {
        List<String> columnasBase = List.of("id", "codigo", "nombre", "descripcion", "activo");
        
        if (tableName.equals("pais")) {
            return List.of("id", "codigo", "nombre", "codigo_iso", "activo");
        }
        if (tableName.equals("departamento")) {
            return List.of("id", "codigo", "nombre", "pais_id", "activo");
        }
        if (tableName.equals("municipio")) {
            return List.of("id", "codigo", "nombre", "departamento_id", "activo");
        }
        
        return columnasBase;
    }

    private CatalogoResponseDto mapRow(ResultSet rs, int rowNum, List<String> columnas) throws SQLException {
        CatalogoResponseDto dto = new CatalogoResponseDto();
        dto.setId(rs.getLong("id"));
        dto.setCodigo(rs.getString("codigo"));
        dto.setNombre(rs.getString("nombre"));
        
        if (columnas.contains("descripcion")) {
            dto.setDescripcion(rs.getString("descripcion"));
        }
        
        dto.setActivo(rs.getBoolean("activo"));
        return dto;
    }
}