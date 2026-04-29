package com.cloud_tecnological.mednova.repositories.rips;

import com.cloud_tecnological.mednova.dto.rips.RipsLineaDto;
import com.cloud_tecnological.mednova.dto.rips.RipsResponseDto;
import com.cloud_tecnological.mednova.util.MapperRepository;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class RipsQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public RipsQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<RipsResponseDto> findByFactura(Long facturaId, Long empresaId) {
        String sql = """
            SELECT
                r.id,
                r.empresa_id,
                r.factura_id,
                f.numero                 AS invoice_number,
                r.pagador_id,
                t.nombre_completo        AS payer_name,
                r.fecha_generacion,
                r.estado,
                r.version_norma,
                r.observaciones,
                r.activo,
                r.created_at
            FROM rips_encabezado r
            INNER JOIN factura f    ON f.id = r.factura_id
            INNER JOIN pagador pag  ON pag.id = r.pagador_id
            INNER JOIN tercero t    ON t.id = pag.tercero_id
            WHERE r.factura_id = :factura_id
              AND r.empresa_id = :empresa_id
              AND r.activo = true
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("factura_id", facturaId)
            .addValue("empresa_id", empresaId);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(MapperRepository.mapListToDtoListNull(rows, RipsResponseDto.class).get(0));
    }

    public Optional<RipsResponseDto> findById(Long id, Long empresaId) {
        String sql = """
            SELECT
                r.id,
                r.empresa_id,
                r.factura_id,
                f.numero                 AS invoice_number,
                r.pagador_id,
                t.nombre_completo        AS payer_name,
                r.fecha_generacion,
                r.estado,
                r.version_norma,
                r.observaciones,
                r.activo,
                r.created_at
            FROM rips_encabezado r
            INNER JOIN factura f    ON f.id = r.factura_id
            INNER JOIN pagador pag  ON pag.id = r.pagador_id
            INNER JOIN tercero t    ON t.id = pag.tercero_id
            WHERE r.id = :id
              AND r.empresa_id = :empresa_id
              AND r.activo = true
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("empresa_id", empresaId);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(MapperRepository.mapListToDtoListNull(rows, RipsResponseDto.class).get(0));
    }

    public List<RipsLineaDto> findLineas(Long encabezadoId, Long empresaId) {
        String sql = """
            SELECT id, rips_encabezado_id, tipo_archivo, linea_datos, created_at
            FROM rips_detalle
            WHERE rips_encabezado_id = :encabezado_id
              AND empresa_id = :empresa_id
            ORDER BY id
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("encabezado_id", encabezadoId)
            .addValue("empresa_id", empresaId);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return MapperRepository.mapListToDtoListNull(rows, RipsLineaDto.class);
    }

    // Carga los ítems de la factura con datos necesarios para generar las líneas RIPS
    public List<Map<String, Object>> findItemsParaRips(Long facturaId, Long empresaId) {
        String sql = """
            SELECT
                df.id                          AS detalle_id,
                df.servicio_salud_id,
                COALESCE(ss.codigo_cups, ss.codigo_interno, '') AS service_code,
                ss.nombre                      AS service_name,
                df.cantidad,
                df.valor_unitario,
                df.valor_cuota_moderadora,
                df.total,
                df.atencion_id,
                COALESCE(tdoc.codigo, '')       AS tipo_doc_paciente,
                ter.numero_documento            AS num_doc_paciente,
                COALESCE(at2.fecha_inicio::text, f.fecha_factura::text) AS fecha_atencion,
                COALESCE(cd.codigo, 'Z00')      AS diagnostico_codigo
            FROM detalle_factura df
            INNER JOIN factura f              ON f.id = df.factura_id
            INNER JOIN paciente p             ON p.id = f.paciente_id
            INNER JOIN tercero ter            ON ter.id = p.tercero_id
            LEFT JOIN tipo_documento tdoc     ON tdoc.id = ter.tipo_documento_id
            LEFT JOIN atencion at2            ON at2.id = df.atencion_id
            LEFT JOIN diagnostico_atencion da ON da.atencion_id = at2.id
                                              AND da.tipo_diagnostico = 'PRINCIPAL'
                                              AND da.activo = true
            LEFT JOIN catalogo_diagnostico cd ON cd.id = da.catalogo_diagnostico_id
            LEFT JOIN servicio_salud ss       ON ss.id = df.servicio_salud_id
            WHERE df.factura_id = :factura_id
              AND df.empresa_id = :empresa_id
              AND df.activo = true
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("factura_id", facturaId)
            .addValue("empresa_id", empresaId);
        return jdbc.query(sql, params, new ColumnMapRowMapper());
    }
}
