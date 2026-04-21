package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.permiso.PermisoDto;
import com.cloud_tecnological.mednova.dto.rol.CreateRolRequestDto;
import com.cloud_tecnological.mednova.dto.rol.RolResponseDto;
import com.cloud_tecnological.mednova.dto.rol.RolTableDto;
import com.cloud_tecnological.mednova.dto.rol.UpdateRolRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public interface RolService {

    RolResponseDto create(CreateRolRequestDto request);

    RolResponseDto findById(Long id);

    PageImpl<RolTableDto> listRoles(PageableDto<?> pageable);

    RolResponseDto update(Long id, UpdateRolRequestDto request);

    Boolean delete(Long id);

    Boolean toggleActive(Long id);

    List<PermisoDto> listPermissions();
}
