package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.rips.RipsLineaDto;
import com.cloud_tecnological.mednova.dto.rips.RipsResponseDto;
import com.cloud_tecnological.mednova.entity.RipsDetalleEntity;
import com.cloud_tecnological.mednova.entity.RipsEncabezadoEntity;
import com.cloud_tecnological.mednova.repositories.factura.FacturaQueryRepository;
import com.cloud_tecnological.mednova.repositories.rips.RipsDetalleJpaRepository;
import com.cloud_tecnological.mednova.repositories.rips.RipsEncabezadoJpaRepository;
import com.cloud_tecnological.mednova.repositories.rips.RipsQueryRepository;
import com.cloud_tecnological.mednova.services.RipsService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class RipsServiceImpl implements RipsService {

    private final RipsEncabezadoJpaRepository encabezadoJpa;
    private final RipsDetalleJpaRepository detalleJpa;
    private final RipsQueryRepository ripsQuery;
    private final FacturaQueryRepository facturaQuery;

    public RipsServiceImpl(
            RipsEncabezadoJpaRepository encabezadoJpa,
            RipsDetalleJpaRepository detalleJpa,
            RipsQueryRepository ripsQuery,
            FacturaQueryRepository facturaQuery) {
        this.encabezadoJpa = encabezadoJpa;
        this.detalleJpa    = detalleJpa;
        this.ripsQuery     = ripsQuery;
        this.facturaQuery  = facturaQuery;
    }

    @Override
    @Transactional
    public RipsResponseDto generarDesdeFactura(Long facturaId, String observaciones) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        // Validar que la factura existe y está APROBADA
        String estadoCodigo = facturaQuery.findEstadoFacturaCodigoById(
            facturaQuery.findActiveById(facturaId, empresaId, sedeId)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Factura no encontrada"))
                .getEstado_factura_id()
        );
        if (!"APROBADA".equals(estadoCodigo)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Solo se pueden generar RIPS para facturas APROBADAS");
        }

        // Evitar duplicados
        if (encabezadoJpa.findByFacturaIdAndEmpresaId(facturaId, empresaId).isPresent()) {
            throw new GlobalException(HttpStatus.CONFLICT, "Ya existe un RIPS generado para esta factura");
        }

        // Obtener pagador_id desde la factura
        Long pagadorId = facturaQuery.findActiveById(facturaId, empresaId, sedeId)
            .map(f -> f.getPagador_id()).orElse(null);

        // Crear encabezado
        RipsEncabezadoEntity encabezado = new RipsEncabezadoEntity();
        encabezado.setEmpresa_id(empresaId);
        encabezado.setFactura_id(facturaId);
        encabezado.setPagador_id(pagadorId);
        encabezado.setObservaciones(observaciones);
        encabezado.setUsuario_creacion(usuarioId);
        RipsEncabezadoEntity saved = encabezadoJpa.save(encabezado);

        // Generar líneas AC desde ítems de la factura
        List<Map<String, Object>> items = ripsQuery.findItemsParaRips(facturaId, empresaId);
        for (Map<String, Object> item : items) {
            RipsDetalleEntity linea = new RipsDetalleEntity();
            linea.setEmpresa_id(empresaId);
            linea.setRips_encabezado_id(saved.getId());
            linea.setTipo_archivo("AC");
            linea.setLinea_datos(buildLineaAC(item));
            detalleJpa.save(linea);
        }

        return ripsQuery.findById(saved.getId(), empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar RIPS generado"));
    }

    @Override
    public RipsResponseDto findById(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        return ripsQuery.findById(id, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "RIPS no encontrado"));
    }

    @Override
    public RipsResponseDto findByFactura(Long facturaId) {
        Long empresaId = TenantContext.getEmpresaId();
        return ripsQuery.findByFactura(facturaId, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "No existe RIPS para esta factura"));
    }

    @Override
    public List<RipsLineaDto> findLineas(Long ripsId) {
        Long empresaId = TenantContext.getEmpresaId();
        ripsQuery.findById(ripsId, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "RIPS no encontrado"));
        return ripsQuery.findLineas(ripsId, empresaId);
    }

    private String buildLineaAC(Map<String, Object> item) {
        String tipoDoc    = str(item.get("tipo_doc_paciente"));
        String numDoc     = str(item.get("num_doc_paciente"));
        String fecha      = str(item.get("fecha_atencion"));
        String codServicio = str(item.get("service_code"));
        String diagnostico = str(item.get("diagnostico_codigo"));
        BigDecimal valor  = toBigDecimal(item.get("valor_unitario"));
        BigDecimal cuota  = toBigDecimal(item.get("valor_cuota_moderadora"));

        return String.format("%s|%s|%s|%s|%s|1|99|%s|1|%.2f|%.2f|%.2f",
            tipoDoc, numDoc, fecha, "NA", codServicio,
            diagnostico, valor, cuota,
            valor.subtract(cuota.compareTo(BigDecimal.ZERO) >= 0 ? cuota : BigDecimal.ZERO));
    }

    private String str(Object v) {
        return v == null ? "" : v.toString();
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        return BigDecimal.valueOf(((Number) v).doubleValue());
    }
}
