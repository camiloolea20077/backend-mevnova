package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.empresa.*;
import com.cloud_tecnological.mednova.entity.EmpresaEntity;
import com.cloud_tecnological.mednova.entity.SedeEntity;
import com.cloud_tecnological.mednova.entity.UsuarioEntity;
import com.cloud_tecnological.mednova.repositories.empresa.EmpresaJpaRepository;
import com.cloud_tecnological.mednova.repositories.empresa.EmpresaQueryRepository;
import com.cloud_tecnological.mednova.repositories.sede.SedeJpaRepository;
import com.cloud_tecnological.mednova.repositories.usuario.UsuarioJpaRepository;
import com.cloud_tecnological.mednova.services.EmpresaService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaJpaRepository    empresaJpaRepository;
    private final EmpresaQueryRepository  empresaQueryRepository;
    private final SedeJpaRepository       sedeJpaRepository;
    private final UsuarioJpaRepository    usuarioJpaRepository;
    private final PasswordEncoder         passwordEncoder;

    public EmpresaServiceImpl(
            EmpresaJpaRepository empresaJpaRepository,
            EmpresaQueryRepository empresaQueryRepository,
            SedeJpaRepository sedeJpaRepository,
            UsuarioJpaRepository usuarioJpaRepository,
            PasswordEncoder passwordEncoder) {
        this.empresaJpaRepository   = empresaJpaRepository;
        this.empresaQueryRepository = empresaQueryRepository;
        this.sedeJpaRepository      = sedeJpaRepository;
        this.usuarioJpaRepository   = usuarioJpaRepository;
        this.passwordEncoder        = passwordEncoder;
    }

    @Override
    @Transactional
    public EmpresaResponseDto create(CreateEmpresaRequestDto request) {
        Long superAdminId = TenantContext.getUsuarioId();

        if (empresaQueryRepository.existsByCodigoExcludingId(request.getCode().toUpperCase(), null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "Ya existe una empresa con ese código");
        }
        if (empresaQueryRepository.existsByNitExcludingId(request.getNit(), null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "Ya existe una empresa con ese NIT");
        }

        // 1. Crear empresa
        EmpresaEntity empresa = new EmpresaEntity();
        empresa.setCodigo(request.getCode().toUpperCase());
        empresa.setNit(request.getNit());
        empresa.setDigito_verificacion(request.getVerificationDigit());
        empresa.setRazon_social(request.getBusinessName());
        empresa.setNombre_comercial(request.getTradeName());
        empresa.setRepresentante_legal(request.getLegalRepresentative());
        empresa.setTelefono(request.getPhone());
        empresa.setCorreo(request.getEmail());
        empresa.setPais_id(request.getCountryId());
        empresa.setDepartamento_id(request.getDepartmentId());
        empresa.setMunicipio_id(request.getMunicipalityId());
        empresa.setDireccion(request.getAddress());
        empresa.setObservaciones(request.getObservations());
        empresa.setUsuario_creacion(superAdminId);
        EmpresaEntity saved = empresaJpaRepository.save(empresa);

        // 2. Crear sede inicial (opcional)
        if (request.getInitialBranch() != null) {
            crearSede(request.getInitialBranch(), saved.getId(), superAdminId, true);
        }

        // 3. Crear administrador inicial (opcional)
        if (request.getInitialAdmin() != null) {
            crearAdminInicial(request.getInitialAdmin(), saved.getId(), superAdminId);
        }

        return empresaQueryRepository.findActiveById(saved.getId())
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar empresa creada"));
    }

    @Override
    public EmpresaResponseDto findById(Long id) {
        return empresaQueryRepository.findActiveById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Empresa no encontrada"));
    }

    @Override
    public PageImpl<EmpresaTableDto> findAll(PageableDto<?> pageable) {
        return empresaQueryRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public EmpresaResponseDto update(Long id, UpdateEmpresaRequestDto request) {
        Long superAdminId = TenantContext.getUsuarioId();

        EmpresaEntity empresa = empresaJpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Empresa no encontrada"));

        if (empresa.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Empresa no encontrada");
        }

        empresa.setRazon_social(request.getBusinessName());
        empresa.setNombre_comercial(request.getTradeName());
        empresa.setRepresentante_legal(request.getLegalRepresentative());
        empresa.setTelefono(request.getPhone());
        empresa.setCorreo(request.getEmail());
        empresa.setPais_id(request.getCountryId());
        empresa.setDepartamento_id(request.getDepartmentId());
        empresa.setMunicipio_id(request.getMunicipalityId());
        empresa.setDireccion(request.getAddress());
        empresa.setObservaciones(request.getObservations());
        if (request.getLogoUrl() != null) empresa.setLogo_url(request.getLogoUrl());
        empresa.setUsuario_modificacion(superAdminId);
        empresaJpaRepository.save(empresa);

        return empresaQueryRepository.findActiveById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar empresa"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long superAdminId = TenantContext.getUsuarioId();

        EmpresaEntity empresa = empresaJpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Empresa no encontrada"));

        if (empresa.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Empresa no encontrada");
        }

        empresa.setActivo(!Boolean.TRUE.equals(empresa.getActivo()));
        empresa.setUsuario_modificacion(superAdminId);
        empresaJpaRepository.save(empresa);
        return empresa.getActivo();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void crearSede(InitialBranchDto dto, Long empresaId, Long superAdminId, boolean esPrincipal) {
        SedeEntity sede = new SedeEntity();
        sede.setEmpresa_id(empresaId);
        sede.setCodigo(dto.getCode().toUpperCase());
        sede.setCodigo_habilitacion_reps(dto.getRepsCode());
        sede.setNombre(dto.getName());
        sede.setPais_id(dto.getCountryId());
        sede.setDepartamento_id(dto.getDepartmentId());
        sede.setMunicipio_id(dto.getMunicipalityId());
        sede.setDireccion(dto.getAddress());
        sede.setTelefono(dto.getPhone());
        sede.setCorreo(dto.getEmail());
        sede.setEs_principal(esPrincipal);
        sede.setUsuario_creacion(superAdminId);
        sedeJpaRepository.save(sede);
    }

    private void crearAdminInicial(InitialAdminDto dto, Long empresaId, Long superAdminId) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmpresa_id(empresaId);
        usuario.setNombre_usuario(dto.getUsername());
        usuario.setCorreo(dto.getEmail());
        usuario.setHash_password(passwordEncoder.encode(dto.getInitialPassword()));
        usuario.setEs_super_admin(false);
        usuario.setRequiere_cambio_password(true);
        usuario.setIntentos_fallidos(0);
        usuario.setBloqueado(false);
        usuario.setActivo(true);
        usuario.setUsuario_creacion(superAdminId);
        usuarioJpaRepository.save(usuario);
    }
}
