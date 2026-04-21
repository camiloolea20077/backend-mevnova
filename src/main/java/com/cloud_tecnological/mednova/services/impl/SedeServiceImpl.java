package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.sede.CreateSedeRequestDto;
import com.cloud_tecnological.mednova.dto.sede.SedeResponseDto;
import com.cloud_tecnological.mednova.dto.sede.SedeTableDto;
import com.cloud_tecnological.mednova.dto.sede.UpdateSedeRequestDto;
import com.cloud_tecnological.mednova.entity.SedeEntity;
import com.cloud_tecnological.mednova.repositories.sede.SedeJpaRepository;
import com.cloud_tecnological.mednova.repositories.sede.SedeQueryRepository;
import com.cloud_tecnological.mednova.services.SedeService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SedeServiceImpl implements SedeService {

    private final SedeJpaRepository   sedeJpaRepository;
    private final SedeQueryRepository sedeQueryRepository;

    public SedeServiceImpl(SedeJpaRepository sedeJpaRepository,
                           SedeQueryRepository sedeQueryRepository) {
        this.sedeJpaRepository   = sedeJpaRepository;
        this.sedeQueryRepository = sedeQueryRepository;
    }

    @Override
    @Transactional
    public SedeResponseDto create(CreateSedeRequestDto request) {
        Long empresa_id  = TenantContext.getEmpresaId();
        Long usuario_id  = TenantContext.getUsuarioId();

        if (sedeQueryRepository.existsByCodigoAndEmpresa(request.getCode().toUpperCase(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "Ya existe una sede con ese código en la empresa");
        }

        boolean esPrincipal = Boolean.TRUE.equals(request.getIsPrincipal());
        if (esPrincipal) {
            sedeQueryRepository.unmarkPrincipalByEmpresa(empresa_id);
        }

        SedeEntity sede = new SedeEntity();
        sede.setEmpresa_id(empresa_id);
        sede.setCodigo(request.getCode().toUpperCase());
        sede.setCodigo_habilitacion_reps(request.getRepsCode());
        sede.setNombre(request.getName());
        sede.setPais_id(request.getCountryId());
        sede.setDepartamento_id(request.getDepartmentId());
        sede.setMunicipio_id(request.getMunicipalityId());
        sede.setDireccion(request.getAddress());
        sede.setTelefono(request.getPhone());
        sede.setCorreo(request.getEmail());
        sede.setEs_principal(esPrincipal);
        sede.setUsuario_creacion(usuario_id);

        SedeEntity saved = sedeJpaRepository.save(sede);

        return sedeQueryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la sede creada"));
    }

    @Override
    public SedeResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return sedeQueryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Sede no encontrada"));
    }

    @Override
    public PageImpl<SedeTableDto> listSedes(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return sedeQueryRepository.listSedes(pageable, empresa_id);
    }

    @Override
    @Transactional
    public SedeResponseDto update(Long id, UpdateSedeRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        SedeEntity sede = sedeJpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Sede no encontrada"));

        validateTenantAndNotDeleted(sede, empresa_id);

        boolean esPrincipal = Boolean.TRUE.equals(request.getIsPrincipal());
        if (esPrincipal && !Boolean.TRUE.equals(sede.getEs_principal())) {
            sedeQueryRepository.unmarkPrincipalByEmpresa(empresa_id);
        }

        sede.setNombre(request.getName());
        sede.setCodigo_habilitacion_reps(request.getRepsCode());
        sede.setPais_id(request.getCountryId());
        sede.setDepartamento_id(request.getDepartmentId());
        sede.setMunicipio_id(request.getMunicipalityId());
        sede.setDireccion(request.getAddress());
        sede.setTelefono(request.getPhone());
        sede.setCorreo(request.getEmail());
        sede.setEs_principal(esPrincipal);
        sede.setUsuario_modificacion(usuario_id);
        sedeJpaRepository.save(sede);

        return sedeQueryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la sede actualizada"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        SedeEntity sede = sedeJpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Sede no encontrada"));

        validateTenantAndNotDeleted(sede, empresa_id);

        sede.setActivo(!Boolean.TRUE.equals(sede.getActivo()));
        sede.setUsuario_modificacion(usuario_id);
        sedeJpaRepository.save(sede);
        return sede.getActivo();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void validateTenantAndNotDeleted(SedeEntity sede, Long empresa_id) {
        if (sede.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Sede no encontrada");
        }
        if (!sede.getEmpresa_id().equals(empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Sede no encontrada");
        }
    }
}
