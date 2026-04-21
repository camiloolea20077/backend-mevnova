package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.usuariorol.AsignarRolRequestDto;
import com.cloud_tecnological.mednova.dto.usuariorol.UsuarioRolResponseDto;
import com.cloud_tecnological.mednova.dto.usuariorol.UsuarioRolTableDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public interface UsuarioRolService {

    UsuarioRolResponseDto assign(AsignarRolRequestDto request);

    UsuarioRolResponseDto findById(Long id);

    PageImpl<UsuarioRolTableDto> listAssignments(PageableDto<?> pageable);

    List<UsuarioRolResponseDto> listByUser(Long userId);

    Boolean revoke(Long id);
}
