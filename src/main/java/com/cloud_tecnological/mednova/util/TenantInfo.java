package com.cloud_tecnological.mednova.util;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class TenantInfo {
    private Long usuario_id;
    private Long empresa_id;
    private Long sede_id;
    private String username;
    private List<String> roles;
    private List<String> permisos;
    private String jti;
}
