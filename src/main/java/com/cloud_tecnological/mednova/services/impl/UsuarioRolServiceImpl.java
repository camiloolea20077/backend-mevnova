package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.usuariorol.AsignarRolRequestDto;
import com.cloud_tecnological.mednova.dto.usuariorol.UsuarioRolResponseDto;
import com.cloud_tecnological.mednova.dto.usuariorol.UsuarioRolTableDto;
import com.cloud_tecnological.mednova.entity.UsuarioRolEntity;
import com.cloud_tecnological.mednova.repositories.rol.RolJpaRepository;
import com.cloud_tecnological.mednova.repositories.sede.SedeQueryRepository;
import com.cloud_tecnological.mednova.repositories.usuario.UsuarioJpaRepository;
import com.cloud_tecnological.mednova.repositories.usuariorol.UsuarioRolJpaRepository;
import com.cloud_tecnological.mednova.repositories.usuariorol.UsuarioRolQueryRepository;
import com.cloud_tecnological.mednova.services.UsuarioRolService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioRolServiceImpl implements UsuarioRolService {

    private final UsuarioRolJpaRepository   usuarioRolJpaRepository;
    private final UsuarioRolQueryRepository usuarioRolQueryRepository;
    private final UsuarioJpaRepository      usuarioJpaRepository;
    private final RolJpaRepository          rolJpaRepository;
    private final SedeQueryRepository       sedeQueryRepository;

    public UsuarioRolServiceImpl(
            UsuarioRolJpaRepository usuarioRolJpaRepository,
            UsuarioRolQueryRepository usuarioRolQueryRepository,
            UsuarioJpaRepository usuarioJpaRepository,
            RolJpaRepository rolJpaRepository,
            SedeQueryRepository sedeQueryRepository) {
        this.usuarioRolJpaRepository   = usuarioRolJpaRepository;
        this.usuarioRolQueryRepository = usuarioRolQueryRepository;
        this.usuarioJpaRepository      = usuarioJpaRepository;
        this.rolJpaRepository          = rolJpaRepository;
        this.sedeQueryRepository       = sedeQueryRepository;
    }

    @Override
    @Transactional
    public UsuarioRolResponseDto assign(AsignarRolRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        // Validar usuario pertenece a la empresa
        usuarioJpaRepository.findById(request.getUserId())
                .filter(u -> empresa_id.equals(u.getEmpresa_id()))
                .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST, "El usuario no pertenece a la empresa"));

        // Validar rol pertenece a la empresa
        rolJpaRepository.findById(request.getRoleId())
                .filter(r -> empresa_id.equals(r.getEmpresa_id()) && r.getDeleted_at() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST, "El rol no pertenece a la empresa"));

        // Validar sede si viene informada
        if (request.getSedeId() != null
                && !sedeQueryRepository.existsActiveByIdAndEmpresa(request.getSedeId(), empresa_id)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "La sede no existe o no pertenece a la empresa");
        }

        // Validar duplicado
        if (usuarioRolQueryRepository.existsAssignment(
                request.getUserId(), request.getRoleId(), request.getSedeId(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "El usuario ya tiene ese rol asignado en esa sede");
        }

        UsuarioRolEntity entity = new UsuarioRolEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setUsuario_id(request.getUserId());
        entity.setRol_id(request.getRoleId());
        entity.setSede_id(request.getSedeId());
        entity.setFecha_vigencia_desde(request.getValidFrom());
        entity.setFecha_vigencia_hasta(request.getValidUntil());
        entity.setUsuario_creacion(usuario_id);

        UsuarioRolEntity saved = usuarioRolJpaRepository.save(entity);

        return usuarioRolQueryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la asignación creada"));
    }

    @Override
    public UsuarioRolResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return usuarioRolQueryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Asignación no encontrada"));
    }

    @Override
    public PageImpl<UsuarioRolTableDto> listAssignments(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return usuarioRolQueryRepository.listAssignments(pageable, empresa_id);
    }

    @Override
    public List<UsuarioRolResponseDto> listByUser(Long userId) {
        Long empresa_id = TenantContext.getEmpresaId();
        return usuarioRolQueryRepository.listByUser(userId, empresa_id);
    }

    @Override
    @Transactional
    public Boolean revoke(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        UsuarioRolEntity entity = usuarioRolJpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Asignación no encontrada"));

        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Asignación no encontrada");
        }

        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuario_id);
        usuarioRolJpaRepository.save(entity);
        return true;
    }
}
