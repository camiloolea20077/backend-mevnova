package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.usuario.CreateUsuarioRequestDto;
import com.cloud_tecnological.mednova.dto.usuario.ResetPasswordRequestDto;
import com.cloud_tecnological.mednova.dto.usuario.UpdateUsuarioRequestDto;
import com.cloud_tecnological.mednova.dto.usuario.UsuarioResponseDto;
import com.cloud_tecnological.mednova.dto.usuario.UsuarioTableDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface UsuarioService {

    UsuarioResponseDto create(CreateUsuarioRequestDto request);

    UsuarioResponseDto findById(Long id);

    PageImpl<UsuarioTableDto> listUsuarios(PageableDto<?> pageable);

    UsuarioResponseDto update(Long id, UpdateUsuarioRequestDto request);

    Boolean toggleActive(Long id);

    Boolean toggleBlocked(Long id);

    Boolean resetPassword(Long id, ResetPasswordRequestDto request);
}
