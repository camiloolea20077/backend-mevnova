package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.usuario.CreateUsuarioRequestDto;
import com.cloud_tecnological.mednova.dto.usuario.ResetPasswordRequestDto;
import com.cloud_tecnological.mednova.dto.usuario.UpdateUsuarioRequestDto;
import com.cloud_tecnological.mednova.dto.usuario.UsuarioResponseDto;
import com.cloud_tecnological.mednova.dto.usuario.UsuarioTableDto;
import com.cloud_tecnological.mednova.entity.UsuarioEntity;
import com.cloud_tecnological.mednova.repositories.usuario.UsuarioJpaRepository;
import com.cloud_tecnological.mednova.repositories.usuario.UsuarioQueryRepository;
import com.cloud_tecnological.mednova.services.UsuarioService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioJpaRepository   usuarioJpaRepository;
    private final UsuarioQueryRepository usuarioQueryRepository;
    private final PasswordEncoder        passwordEncoder;

    public UsuarioServiceImpl(
            UsuarioJpaRepository usuarioJpaRepository,
            UsuarioQueryRepository usuarioQueryRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioJpaRepository   = usuarioJpaRepository;
        this.usuarioQueryRepository = usuarioQueryRepository;
        this.passwordEncoder        = passwordEncoder;
    }

    @Override
    @Transactional
    public UsuarioResponseDto create(CreateUsuarioRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        if (usuarioQueryRepository.existsByUsernameAndEmpresa(request.getUsername(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "Ya existe un usuario con ese nombre de usuario en la empresa");
        }
        if (usuarioQueryRepository.existsByEmailAndEmpresa(request.getEmail(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo en la empresa");
        }

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmpresa_id(empresa_id);
        usuario.setNombre_usuario(request.getUsername());
        usuario.setCorreo(request.getEmail());
        usuario.setHash_password(passwordEncoder.encode(request.getInitialPassword()));
        usuario.setTercero_id(request.getThirdPartyId());
        usuario.setEs_super_admin(false);
        usuario.setRequiere_cambio_password(true);
        usuario.setUsuario_creacion(usuario_id);

        UsuarioEntity saved = usuarioJpaRepository.save(usuario);

        return usuarioQueryRepository.findActiveByIdAndEmpresa(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el usuario creado"));
    }

    @Override
    public UsuarioResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return usuarioQueryRepository.findActiveByIdAndEmpresa(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @Override
    public PageImpl<UsuarioTableDto> listUsuarios(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return usuarioQueryRepository.listUsuarios(pageable, empresa_id);
    }

    @Override
    @Transactional
    public UsuarioResponseDto update(Long id, UpdateUsuarioRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        UsuarioEntity usuario = getValidEntity(id, empresa_id);

        if (usuarioQueryRepository.existsByEmailAndEmpresa(request.getEmail(), empresa_id, id)) {
            throw new GlobalException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo en la empresa");
        }

        usuario.setCorreo(request.getEmail());
        usuario.setTercero_id(request.getThirdPartyId());
        usuario.setUsuario_modificacion(usuario_id);
        usuarioJpaRepository.save(usuario);

        return usuarioQueryRepository.findActiveByIdAndEmpresa(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el usuario actualizado"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        UsuarioEntity usuario = getValidEntity(id, empresa_id);
        usuario.setActivo(!Boolean.TRUE.equals(usuario.getActivo()));
        usuario.setUsuario_modificacion(usuario_id);
        usuarioJpaRepository.save(usuario);
        return usuario.getActivo();
    }

    @Override
    @Transactional
    public Boolean toggleBlocked(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        UsuarioEntity usuario = getValidEntity(id, empresa_id);
        boolean bloqueando = !Boolean.TRUE.equals(usuario.getBloqueado());
        usuario.setBloqueado(bloqueando);

        if (!bloqueando) {
            usuario.setIntentos_fallidos(0);
            usuario.setFecha_bloqueo(null);
            usuario.setMotivo_bloqueo(null);
        }

        usuario.setUsuario_modificacion(usuario_id);
        usuarioJpaRepository.save(usuario);
        return usuario.getBloqueado();
    }

    @Override
    @Transactional
    public Boolean resetPassword(Long id, ResetPasswordRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        UsuarioEntity usuario = getValidEntity(id, empresa_id);
        usuario.setHash_password(passwordEncoder.encode(request.getNewPassword()));
        usuario.setRequiere_cambio_password(true);
        usuario.setIntentos_fallidos(0);
        usuario.setUsuario_modificacion(usuario_id);
        usuarioJpaRepository.save(usuario);
        return true;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private UsuarioEntity getValidEntity(Long id, Long empresa_id) {
        UsuarioEntity usuario = usuarioJpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (!empresa_id.equals(usuario.getEmpresa_id())) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        if (Boolean.TRUE.equals(usuario.getEs_super_admin())) {
            throw new GlobalException(HttpStatus.FORBIDDEN, "No se puede gestionar un super-administrador");
        }
        return usuario;
    }
}
