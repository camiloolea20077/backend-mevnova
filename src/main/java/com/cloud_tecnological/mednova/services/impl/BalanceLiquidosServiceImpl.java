package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceItemRequestDto;
import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceItemResponseDto;
import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceLiquidosFilterParams;
import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceLiquidosResponseDto;
import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceLiquidosTableDto;
import com.cloud_tecnological.mednova.dto.balanceliquidos.CreateBalanceLiquidosRequestDto;
import com.cloud_tecnological.mednova.dto.balanceliquidos.UpdateBalanceLiquidosRequestDto;
import com.cloud_tecnological.mednova.entity.BalanceLiquidosEntity;
import com.cloud_tecnological.mednova.entity.DetalleBalanceLiquidosEntity;
import com.cloud_tecnological.mednova.repositories.balanceliquidos.BalanceLiquidosJpaRepository;
import com.cloud_tecnological.mednova.repositories.balanceliquidos.BalanceLiquidosQueryRepository;
import com.cloud_tecnological.mednova.repositories.balanceliquidos.DetalleBalanceLiquidosJpaRepository;
import com.cloud_tecnological.mednova.services.BalanceLiquidosService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BalanceLiquidosServiceImpl implements BalanceLiquidosService {

    private static final String TIPO_INGRESO = "INGRESO";
    private static final String TIPO_EGRESO  = "EGRESO";

    private final BalanceLiquidosJpaRepository jpa;
    private final DetalleBalanceLiquidosJpaRepository jpaDetail;
    private final BalanceLiquidosQueryRepository query;

    public BalanceLiquidosServiceImpl(BalanceLiquidosJpaRepository jpa,
                                      DetalleBalanceLiquidosJpaRepository jpaDetail,
                                      BalanceLiquidosQueryRepository query) {
        this.jpa       = jpa;
        this.jpaDetail = jpaDetail;
        this.query     = query;
    }

    @Override
    @Transactional
    public BalanceLiquidosResponseDto create(CreateBalanceLiquidosRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        validatePaciente(empresa_id, request.getPatientId());
        validateAtencion(empresa_id, sede_id, request.getEncounterId());
        validateAtencionMatchesPaciente(request.getEncounterId(), request.getPatientId());
        validateProfesional(empresa_id, request.getProfessionalId());

        LocalDate fecha = request.getBalanceDate() == null ? LocalDate.now() : request.getBalanceDate();
        String turno = (request.getShift() == null || request.getShift().isBlank()) ? null : request.getShift();

        // Regla de negocio: un balance por (atencion, fecha_balance, turno)
        if (query.existsBalanceForShift(request.getEncounterId(), fecha, turno, empresa_id, sede_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Ya existe un balance para esta atención, fecha y turno.");
        }

        BalanceLiquidosEntity entity = new BalanceLiquidosEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setSede_id(sede_id);
        entity.setAtencion_id(request.getEncounterId());
        entity.setPaciente_id(request.getPatientId());
        entity.setProfesional_id(request.getProfessionalId());
        entity.setFecha_balance(fecha);
        entity.setTurno(turno);
        entity.setTotal_ingresos(BigDecimal.ZERO);
        entity.setTotal_egresos(BigDecimal.ZERO);
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);
        BalanceLiquidosEntity saved = jpa.save(entity);

        // Persistir items iniciales si vienen
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (BalanceItemRequestDto item : request.getItems()) {
                persistItem(saved.getId(), empresa_id, item);
            }
            recalculateAndPersistTotals(saved, empresa_id, usuario_id);
        }

        return query.findActiveById(saved.getId(), empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el balance creado"));
    }

    @Override
    @Transactional
    public BalanceLiquidosResponseDto update(Long id, UpdateBalanceLiquidosRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        BalanceLiquidosEntity entity = getValidEntity(id, empresa_id, sede_id);

        validateProfesional(empresa_id, request.getProfessionalId());

        LocalDate newFecha = request.getBalanceDate() == null ? entity.getFecha_balance() : request.getBalanceDate();
        String newTurno = request.getShift() == null ? entity.getTurno() :
                (request.getShift().isBlank() ? null : request.getShift());

        // Si cambia fecha o turno, validar unicidad
        boolean shiftOrDateChanged =
                !newFecha.equals(entity.getFecha_balance())
                || !java.util.Objects.equals(newTurno, entity.getTurno());
        if (shiftOrDateChanged
                && query.existsBalanceForShift(entity.getAtencion_id(), newFecha, newTurno,
                        empresa_id, sede_id, id)) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Ya existe un balance para esta atención, fecha y turno.");
        }

        entity.setProfesional_id(request.getProfessionalId());
        entity.setFecha_balance(newFecha);
        entity.setTurno(newTurno);
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el balance actualizado"));
    }

    @Override
    public BalanceLiquidosResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Balance de líquidos no encontrado"));
    }

    @Override
    public PageImpl<BalanceLiquidosTableDto> list(PageableDto<BalanceLiquidosFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return query.listBalances(pageable, empresa_id, sede_id);
    }

    @Override
    @Transactional
    public BalanceItemResponseDto addItem(Long balanceId, BalanceItemRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        BalanceLiquidosEntity balance = getValidEntity(balanceId, empresa_id, sede_id);

        DetalleBalanceLiquidosEntity item = persistItem(balanceId, empresa_id, request);
        recalculateAndPersistTotals(balance, empresa_id, usuario_id);

        return query.findItemById(item.getId(), balanceId, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el detalle creado"));
    }

    @Override
    public List<BalanceItemResponseDto> listItems(Long balanceId) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        getValidEntity(balanceId, empresa_id, sede_id);
        return query.findItemsByBalanceId(balanceId, empresa_id);
    }

    @Override
    @Transactional
    public void deleteItem(Long balanceId, Long itemId) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        BalanceLiquidosEntity balance = getValidEntity(balanceId, empresa_id, sede_id);

        DetalleBalanceLiquidosEntity item = jpaDetail.findById(itemId)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Detalle no encontrado"));
        if (!empresa_id.equals(item.getEmpresa_id())
                || !balanceId.equals(item.getBalance_id())
                || item.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Detalle no encontrado");
        }

        item.setDeleted_at(LocalDateTime.now());
        item.setActivo(false);
        jpaDetail.save(item);

        recalculateAndPersistTotals(balance, empresa_id, usuario_id);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        BalanceLiquidosEntity entity = getValidEntity(id, empresa_id, sede_id);
        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private DetalleBalanceLiquidosEntity persistItem(Long balanceId, Long empresa_id, BalanceItemRequestDto req) {
        DetalleBalanceLiquidosEntity item = new DetalleBalanceLiquidosEntity();
        item.setEmpresa_id(empresa_id);
        item.setBalance_id(balanceId);
        item.setTipo(req.getType());
        item.setVia(req.getRoute());
        item.setDescripcion(req.getDescription());
        item.setCantidad_ml(req.getAmountMl());
        item.setHora_registro(req.getRecordedAt());
        return jpaDetail.save(item);
    }

    /** CA2: recalcula total_ingresos y total_egresos. La BD calcula `balance` con GENERATED. */
    private void recalculateAndPersistTotals(BalanceLiquidosEntity entity, Long empresa_id, Long usuario_id) {
        BigDecimal totalIn  = query.sumDetailsByType(entity.getId(), empresa_id, TIPO_INGRESO);
        BigDecimal totalOut = query.sumDetailsByType(entity.getId(), empresa_id, TIPO_EGRESO);
        entity.setTotal_ingresos(totalIn == null ? BigDecimal.ZERO : totalIn);
        entity.setTotal_egresos(totalOut == null ? BigDecimal.ZERO : totalOut);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);
    }

    private BalanceLiquidosEntity getValidEntity(Long id, Long empresa_id, Long sede_id) {
        BalanceLiquidosEntity entity = jpa.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Balance de líquidos no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id())
                || !sede_id.equals(entity.getSede_id())
                || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Balance de líquidos no encontrado");
        }
        return entity;
    }

    private void validatePaciente(Long empresa_id, Long paciente_id) {
        if (!query.pacienteExistsInEmpresa(paciente_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }
    }

    private void validateAtencion(Long empresa_id, Long sede_id, Long atencion_id) {
        if (!query.atencionExistsInTenant(atencion_id, empresa_id, sede_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada");
        }
    }

    private void validateAtencionMatchesPaciente(Long atencion_id, Long paciente_id) {
        if (!query.atencionMatchesPaciente(atencion_id, paciente_id)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "La atención no corresponde al paciente indicado");
        }
    }

    private void validateProfesional(Long empresa_id, Long profesional_id) {
        if (!query.profesionalActivoInEmpresa(profesional_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Profesional no encontrado");
        }
    }
}
