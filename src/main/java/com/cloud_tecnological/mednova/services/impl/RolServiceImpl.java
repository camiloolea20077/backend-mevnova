package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.permiso.PermisoDto;
import com.cloud_tecnological.mednova.dto.rol.CreateRolRequestDto;
import com.cloud_tecnological.mednova.dto.rol.RolResponseDto;
import com.cloud_tecnological.mednova.dto.rol.RolTableDto;
import com.cloud_tecnological.mednova.dto.rol.UpdateRolRequestDto;
import com.cloud_tecnological.mednova.entity.RolEntity;
import com.cloud_tecnological.mednova.entity.RolPermisoEntity;
import com.cloud_tecnological.mednova.repositories.rol.PermisoQueryRepository;
import com.cloud_tecnological.mednova.repositories.rol.RolJpaRepository;
import com.cloud_tecnological.mednova.repositories.rol.RolPermisoJpaRepository;
import com.cloud_tecnological.mednova.repositories.rol.RolQueryRepository;
import com.cloud_tecnological.mednova.services.RolService;
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
public class RolServiceImpl implements RolService {

    private final RolJpaRepository       rolJpaRepository;
    private final RolPermisoJpaRepository rolPermisoJpaRepository;
    private final RolQueryRepository     rolQueryRepository;
    private final PermisoQueryRepository permisoQueryRepository;

    public RolServiceImpl(
            RolJpaRepository rolJpaRepository,
            RolPermisoJpaRepository rolPermisoJpaRepository,
            RolQueryRepository rolQueryRepository,
            PermisoQueryRepository permisoQueryRepository) {
        this.rolJpaRepository        = rolJpaRepository;
        this.rolPermisoJpaRepository = rolPermisoJpaRepository;
        this.rolQueryRepository      = rolQueryRepository;
        this.permisoQueryRepository  = permisoQueryRepository;
    }

    @Override
    @Transactional
    public RolResponseDto create(CreateRolRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        if (rolQueryRepository.existsByCodigoAndEmpresa(request.getCode().toUpperCase(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "Ya existe un rol con ese código en la empresa");
        }

        RolEntity rol = new RolEntity();
        rol.setEmpresa_id(empresa_id);
        rol.setCodigo(request.getCode().toUpperCase());
        rol.setNombre(request.getName());
        rol.setDescripcion(request.getDescription());
        rol.setEs_global(false);
        rol.setUsuario_creacion(usuario_id);
        RolEntity saved = rolJpaRepository.save(rol);

        asignarPermisos(saved.getId(), request.getPermissionIds());

        return rolQueryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el rol creado"));
    }

    @Override
    public RolResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return rolQueryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Rol no encontrado"));
    }

    @Override
    public PageImpl<RolTableDto> listRoles(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return rolQueryRepository.listRoles(pageable, empresa_id);
    }

    @Override
    @Transactional
    public RolResponseDto update(Long id, UpdateRolRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        RolEntity rol = rolJpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Rol no encontrado"));

        validateTenantAndNotDeleted(rol, empresa_id);

        // Desactivar permisos existentes
        List<RolPermisoEntity> existing = rolPermisoJpaRepository.findAll()
                .stream().filter(rp -> rp.getRol_id().equals(id) && Boolean.TRUE.equals(rp.getActivo())).toList();
        existing.forEach(rp -> {
            rp.setActivo(false);
            rolPermisoJpaRepository.save(rp);
        });

        // Insertar nuevos permisos
        asignarPermisos(id, request.getPermissionIds());

        rol.setNombre(request.getName());
        rol.setDescripcion(request.getDescription());
        rol.setUsuario_modificacion(usuario_id);
        rolJpaRepository.save(rol);

        return rolQueryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el rol actualizado"));
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        RolEntity rol = rolJpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Rol no encontrado"));

        validateTenantAndNotDeleted(rol, empresa_id);

        if (rolQueryRepository.hasActiveUsers(id, empresa_id)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "No se puede eliminar un rol con usuarios asignados activos");
        }

        rol.setDeleted_at(LocalDateTime.now());
        rol.setUsuario_modificacion(usuario_id);
        rolJpaRepository.save(rol);
        return true;
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        RolEntity rol = rolJpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Rol no encontrado"));

        validateTenantAndNotDeleted(rol, empresa_id);

        boolean activando = !Boolean.TRUE.equals(rol.getActivo());
        if (activando && !rolQueryRepository.hasActivePermissions(id)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "El rol no tiene permisos activos y no puede activarse");
        }

        rol.setActivo(activando);
        rol.setUsuario_modificacion(usuario_id);
        rolJpaRepository.save(rol);
        return rol.getActivo();
    }

    @Override
    public List<PermisoDto> listPermissions() {
        return permisoQueryRepository.listAll();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void asignarPermisos(Long rolId, List<Long> permissionIds) {
        permissionIds.forEach(permisoId -> {
            RolPermisoEntity rp = new RolPermisoEntity();
            rp.setRol_id(rolId);
            rp.setPermiso_id(permisoId);
            rp.setActivo(true);
            rolPermisoJpaRepository.save(rp);
        });
    }

    private void validateTenantAndNotDeleted(RolEntity rol, Long empresa_id) {
        if (rol.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Rol no encontrado");
        }
        if (!empresa_id.equals(rol.getEmpresa_id())) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Rol no encontrado");
        }
    }
}
