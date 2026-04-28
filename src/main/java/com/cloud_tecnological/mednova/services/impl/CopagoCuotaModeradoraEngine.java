package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.entity.AcumuladoCobroPacienteEntity;
import com.cloud_tecnological.mednova.entity.LiquidacionCobroPacienteEntity;
import com.cloud_tecnological.mednova.repositories.cobrorule.ReglaCobroPacienteQueryRepository;
import com.cloud_tecnological.mednova.repositories.cobrorule.ServicioExentoCobroQueryRepository;
import com.cloud_tecnological.mednova.repositories.liquidacion.AcumuladoCobroPacienteJpaRepository;
import com.cloud_tecnological.mednova.repositories.liquidacion.AcumuladoCobroPacienteQueryRepository;
import com.cloud_tecnological.mednova.repositories.liquidacion.LiquidacionCobroPacienteJpaRepository;
import com.cloud_tecnological.mednova.repositories.liquidacion.LiquidacionCobroPacienteQueryRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Component
public class CopagoCuotaModeradoraEngine {

    private final ReglaCobroPacienteQueryRepository reglaQuery;
    private final ServicioExentoCobroQueryRepository exencionQuery;
    private final LiquidacionCobroPacienteJpaRepository liquidacionJpa;
    private final AcumuladoCobroPacienteJpaRepository acumuladoJpa;
    private final AcumuladoCobroPacienteQueryRepository acumuladoQuery;
    private final LiquidacionCobroPacienteQueryRepository liquidacionQuery;

    public CopagoCuotaModeradoraEngine(
            ReglaCobroPacienteQueryRepository reglaQuery,
            ServicioExentoCobroQueryRepository exencionQuery,
            LiquidacionCobroPacienteJpaRepository liquidacionJpa,
            AcumuladoCobroPacienteJpaRepository acumuladoJpa,
            AcumuladoCobroPacienteQueryRepository acumuladoQuery,
            LiquidacionCobroPacienteQueryRepository liquidacionQuery) {
        this.reglaQuery = reglaQuery;
        this.exencionQuery = exencionQuery;
        this.liquidacionJpa = liquidacionJpa;
        this.acumuladoJpa = acumuladoJpa;
        this.acumuladoQuery = acumuladoQuery;
        this.liquidacionQuery = liquidacionQuery;
    }

    /**
     * Calcula y persiste la liquidación de copago o cuota moderadora para un servicio.
     * Retorna el valor a cobrar al paciente (0 si exento).
     */
    @Transactional
    public BigDecimal calcular(
            Long empresaId,
            Long pacienteId,
            Long admisionId,
            Long atencionId,
            Long facturaId,
            Long servicioSaludId,
            String tipoCobro,
            BigDecimal valorServicio,
            Long regimenId,
            Long categoriaSisbenId,
            Long usuarioId) {

        int vigencia = LocalDate.now().getYear();
        LocalDate hoy = LocalDate.now();

        // 1. Verificar exención
        boolean esExento = exencionQuery.isServiceExempt(servicioSaludId, tipoCobro, empresaId, hoy);
        if (esExento) {
            persistirLiquidacion(empresaId, pacienteId, admisionId, atencionId, facturaId,
                tipoCobro, servicioSaludId, null, valorServicio, null, BigDecimal.ZERO,
                true, "Servicio exento de cobro", "EXENTO", usuarioId);
            return BigDecimal.ZERO;
        }

        // 2. Buscar regla activa
        Optional<Map<String, Object>> reglaOpt = reglaQuery.findActiveRule(
            empresaId, vigencia, regimenId, tipoCobro, null, categoriaSisbenId);

        if (reglaOpt.isEmpty()) {
            // Sin regla: registrar sin cobro pero con trazabilidad
            persistirLiquidacion(empresaId, pacienteId, admisionId, atencionId, facturaId,
                tipoCobro, servicioSaludId, null, valorServicio, null, BigDecimal.ZERO,
                false, null, "PENDIENTE", usuarioId);
            return BigDecimal.ZERO;
        }

        Map<String, Object> regla = reglaOpt.get();
        Long reglaId = regla.get("id") != null ? ((Number) regla.get("id")).longValue() : null;
        BigDecimal porcentaje = toBigDecimal(regla.get("porcentaje_cobro"));
        BigDecimal valorFijo = toBigDecimal(regla.get("valor_fijo"));
        BigDecimal topeEvento = toBigDecimal(regla.get("tope_evento"));
        BigDecimal topeAnual = toBigDecimal(regla.get("tope_anual"));

        // 3. Calcular valor bruto
        BigDecimal valorCalculado;
        if (porcentaje != null && porcentaje.compareTo(BigDecimal.ZERO) > 0) {
            valorCalculado = valorServicio.multiply(porcentaje)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if (valorFijo != null && valorFijo.compareTo(BigDecimal.ZERO) > 0) {
            valorCalculado = valorFijo;
        } else {
            valorCalculado = BigDecimal.ZERO;
        }

        // 4. Aplicar tope por evento
        if (topeEvento != null && topeEvento.compareTo(BigDecimal.ZERO) > 0) {
            if (valorCalculado.compareTo(topeEvento) > 0) {
                valorCalculado = topeEvento;
            }
        }

        // 5. Verificar tope anual
        Optional<Map<String, Object>> acumOpt = acumuladoQuery.findAcumulado(
            pacienteId, empresaId, vigencia, tipoCobro);

        BigDecimal acumuladoAnual = BigDecimal.ZERO;
        if (acumOpt.isPresent()) {
            acumuladoAnual = toBigDecimal(acumOpt.get().get("valor_acumulado_anual"));
        }

        if (topeAnual != null && topeAnual.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal disponible = topeAnual.subtract(acumuladoAnual);
            if (disponible.compareTo(BigDecimal.ZERO) <= 0) {
                persistirLiquidacion(empresaId, pacienteId, admisionId, atencionId, facturaId,
                    tipoCobro, servicioSaludId, reglaId, valorServicio, porcentaje,
                    BigDecimal.ZERO, false, "Tope anual alcanzado", "EXENTO", usuarioId);
                return BigDecimal.ZERO;
            }
            if (valorCalculado.compareTo(disponible) > 0) {
                valorCalculado = disponible;
            }
        }

        // 6. Persistir liquidación y actualizar acumulado
        persistirLiquidacion(empresaId, pacienteId, admisionId, atencionId, facturaId,
            tipoCobro, servicioSaludId, reglaId, valorServicio, porcentaje,
            valorCalculado, false, null, "PENDIENTE", usuarioId);

        actualizarAcumulado(empresaId, pacienteId, vigencia, tipoCobro,
            valorCalculado, topeEvento, topeAnual, acumOpt, usuarioId);

        return valorCalculado;
    }

    private void persistirLiquidacion(Long empresaId, Long pacienteId, Long admisionId,
            Long atencionId, Long facturaId, String tipoCobro, Long servicioSaludId,
            Long reglaId, BigDecimal base, BigDecimal porcentaje, BigDecimal valorCalculado,
            boolean esExento, String motivoExencion, String estado, Long usuarioId) {

        LiquidacionCobroPacienteEntity liq = new LiquidacionCobroPacienteEntity();
        liq.setEmpresa_id(empresaId);
        liq.setPaciente_id(pacienteId);
        liq.setAdmision_id(admisionId);
        liq.setAtencion_id(atencionId);
        liq.setFactura_id(facturaId);
        liq.setTipo_cobro(tipoCobro);
        liq.setServicio_salud_id(servicioSaludId);
        liq.setRegla_cobro_paciente_id(reglaId);
        liq.setBase_calculo(base);
        liq.setPorcentaje_aplicado(porcentaje);
        liq.setValor_calculado(valorCalculado != null ? valorCalculado : BigDecimal.ZERO);
        liq.setValor_cobrado(valorCalculado != null ? valorCalculado : BigDecimal.ZERO);
        liq.setAplica_exencion(esExento);
        liq.setMotivo_exencion(motivoExencion);
        liq.setEstado_recaudo(estado);
        liq.setUsuario_creacion(usuarioId);
        liquidacionJpa.save(liq);
    }

    private void actualizarAcumulado(Long empresaId, Long pacienteId, Integer vigencia,
            String tipoCobro, BigDecimal valor, BigDecimal topeEvento, BigDecimal topeAnual,
            Optional<Map<String, Object>> acumOpt, Long usuarioId) {

        AcumuladoCobroPacienteEntity acum;
        if (acumOpt.isPresent()) {
            Long acumId = ((Number) acumOpt.get().get("id")).longValue();
            acum = acumuladoJpa.findById(acumId).orElse(new AcumuladoCobroPacienteEntity());
        } else {
            acum = new AcumuladoCobroPacienteEntity();
            acum.setEmpresa_id(empresaId);
            acum.setPaciente_id(pacienteId);
            acum.setVigencia(vigencia);
            acum.setTipo_cobro(tipoCobro);
        }

        BigDecimal nuevoEvento = (acum.getValor_acumulado_evento() != null
            ? acum.getValor_acumulado_evento() : BigDecimal.ZERO).add(valor);
        BigDecimal nuevoAnual = (acum.getValor_acumulado_anual() != null
            ? acum.getValor_acumulado_anual() : BigDecimal.ZERO).add(valor);

        acum.setValor_acumulado_evento(nuevoEvento);
        acum.setValor_acumulado_anual(nuevoAnual);
        if (topeEvento != null) acum.setTope_evento_aplicado(topeEvento);
        if (topeAnual != null) acum.setTope_anual_aplicado(topeAnual);
        if (acum.getUsuario_creacion() == null) acum.setUsuario_creacion(usuarioId);
        acum.setUsuario_modificacion(usuarioId);
        acumuladoJpa.save(acum);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        return BigDecimal.valueOf(((Number) value).doubleValue());
    }
}
