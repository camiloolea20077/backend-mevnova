package com.cloud_tecnological.mednova.dto.rol;

import com.cloud_tecnological.mednova.dto.permiso.PermisoDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class RolResponseDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean isGlobal;
    private Boolean active;
    private List<PermisoDto> permissions;
    private LocalDateTime createdAt;
}
