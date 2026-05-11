package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.notaenfermeria.CreateNotaEnfermeriaRequestDto;
import com.cloud_tecnological.mednova.dto.notaenfermeria.NotaEnfermeriaFilterParams;
import com.cloud_tecnological.mednova.dto.notaenfermeria.NotaEnfermeriaResponseDto;
import com.cloud_tecnological.mednova.dto.notaenfermeria.NotaEnfermeriaTableDto;
import com.cloud_tecnological.mednova.dto.notaenfermeria.UpdateNotaEnfermeriaRequestDto;
import com.cloud_tecnological.mednova.entity.NotaEnfermeriaEntity;
import com.cloud_tecnological.mednova.repositories.notaenfermeria.NotaEnfermeriaJpaRepository;
import com.cloud_tecnological.mednova.repositories.notaenfermeria.NotaEnfermeriaQueryRepository;
import com.cloud_tecnological.mednova.services.NotaEnfermeriaService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotaEnfermeriaServiceImpl implements NotaEnfermeriaService {

    private final NotaEnfermeriaJpaRepository jpa;
    private final NotaEnfermeriaQueryRepository query;

    public NotaEnfermeriaServiceImpl(NotaEnfermeriaJpaRepository jpa,
                                     NotaEnfermeriaQueryRepository query) {
        this.jpa   = jpa;
        this.query = query;
    }

    @Override
    @Transactional
    public NotaEnfermeriaResponseDto create(CreateNotaEnfermeriaRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        validatePaciente(empresa_id, request.getPatientId());
        validateAtencion(empresa_id, sede_id, request.getEncounterId());
        validateAtencionMatchesPaciente(request.getEncounterId(), request.getPatientId());
        validateProfesional(empresa_id, request.getProfessionalId());

        NotaEnfermeriaEntity entity = new NotaEnfermeriaEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setSede_id(sede_id);
        entity.setAtencion_id(request.getEncounterId());
        entity.setPaciente_id(request.getPatientId());
        entity.setProfesional_id(request.getProfessionalId());
        entity.setTipo_nota(request.getNoteType());
        entity.setTurno(request.getShift());
        entity.setFecha_nota(request.getNoteDate() == null ? LocalDateTime.now() : request.getNoteDate());
        entity.setContenido(request.getContent());
        entity.setTension_sistolica(request.getSystolicBp());
        entity.setTension_diastolica(request.getDiastolicBp());
        entity.setFrecuencia_cardiaca(request.getHeartRate());
        entity.setFrecuencia_respiratoria(request.getRespiratoryRate());
        entity.setTemperatura(request.getTemperature());
        entity.setSaturacion_oxigeno(request.getOxygenSaturation());
        entity.setGlucometria(request.getGlucometry());
        entity.setDolor_eva(request.getPainEva());
        if (Boolean.TRUE.equals(request.getSignOnCreate())) {
            entity.setFirmada(true);
            entity.setFecha_firma(LocalDateTime.now());
        } else {
            entity.setFirmada(false);
        }
        entity.setUsuario_creacion(usuario_id);

        NotaEnfermeriaEntity saved = jpa.save(entity);

        return query.findActiveById(saved.getId(), empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la nota de enfermería creada"));
    }

    @Override
    @Transactional
    public NotaEnfermeriaResponseDto update(Long id, UpdateNotaEnfermeriaRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        NotaEnfermeriaEntity entity = getValidEntity(id, empresa_id, sede_id);
        if (Boolean.TRUE.equals(entity.getFirmada())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Una nota firmada no puede editarse. Cree una nueva nota.");
        }

        validateProfesional(empresa_id, request.getProfessionalId());

        entity.setProfesional_id(request.getProfessionalId());
        entity.setTipo_nota(request.getNoteType());
        entity.setTurno(request.getShift());
        if (request.getNoteDate() != null) entity.setFecha_nota(request.getNoteDate());
        entity.setContenido(request.getContent());
        entity.setTension_sistolica(request.getSystolicBp());
        entity.setTension_diastolica(request.getDiastolicBp());
        entity.setFrecuencia_cardiaca(request.getHeartRate());
        entity.setFrecuencia_respiratoria(request.getRespiratoryRate());
        entity.setTemperatura(request.getTemperature());
        entity.setSaturacion_oxigeno(request.getOxygenSaturation());
        entity.setGlucometria(request.getGlucometry());
        entity.setDolor_eva(request.getPainEva());
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la nota de enfermería actualizada"));
    }

    @Override
    @Transactional
    public NotaEnfermeriaResponseDto sign(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        NotaEnfermeriaEntity entity = getValidEntity(id, empresa_id, sede_id);
        if (Boolean.TRUE.equals(entity.getFirmada())) {
            throw new GlobalException(HttpStatus.CONFLICT, "La nota ya se encuentra firmada.");
        }

        entity.setFirmada(true);
        entity.setFecha_firma(LocalDateTime.now());
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la nota de enfermería"));
    }

    @Override
    public NotaEnfermeriaResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Nota de enfermería no encontrada"));
    }

    @Override
    public PageImpl<NotaEnfermeriaTableDto> list(PageableDto<NotaEnfermeriaFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return query.listNotas(pageable, empresa_id, sede_id);
    }

    @Override
    @Transactional
    public NotaEnfermeriaResponseDto setActive(Long id, boolean active) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        NotaEnfermeriaEntity entity = getValidEntity(id, empresa_id, sede_id);
        entity.setActivo(active);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la nota de enfermería"));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        NotaEnfermeriaEntity entity = getValidEntity(id, empresa_id, sede_id);
        if (Boolean.TRUE.equals(entity.getFirmada())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Una nota firmada no puede eliminarse.");
        }
        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private NotaEnfermeriaEntity getValidEntity(Long id, Long empresa_id, Long sede_id) {
        NotaEnfermeriaEntity entity = jpa.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Nota de enfermería no encontrada"));
        if (!empresa_id.equals(entity.getEmpresa_id())
                || !sede_id.equals(entity.getSede_id())
                || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Nota de enfermería no encontrada");
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
