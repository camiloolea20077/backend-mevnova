package com.cloud_tecnological.mednova.dto.usuario;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UsuarioTableDto {

    private Long id;
    private String username;
    private String email;
    private Boolean active;
    private Boolean blocked;
}
