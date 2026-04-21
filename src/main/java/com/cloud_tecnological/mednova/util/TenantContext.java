package com.cloud_tecnological.mednova.util;

import org.springframework.http.HttpStatus;

public class TenantContext {

    private static final ThreadLocal<TenantInfo> CONTEXT = new ThreadLocal<>();

    public static void set(TenantInfo info) { CONTEXT.set(info); }

    public static TenantInfo get() { return CONTEXT.get(); }

    public static void clear() { CONTEXT.remove(); }

    public static Long getEmpresaId() {
        TenantInfo info = CONTEXT.get();
        if (info == null) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Contexto de tenant no disponible");
        }
        return info.getEmpresa_id();
    }

    public static Long getSedeId() {
        TenantInfo info = CONTEXT.get();
        if (info == null) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Contexto de tenant no disponible");
        }
        return info.getSede_id();
    }

    public static Long getUsuarioId() {
        TenantInfo info = CONTEXT.get();
        if (info == null) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Contexto de tenant no disponible");
        }
        return info.getUsuario_id();
    }

    public static boolean hasPermission(String permiso) {
        TenantInfo info = CONTEXT.get();
        return info != null && info.getPermisos() != null && info.getPermisos().contains(permiso);
    }

    public static boolean hasRole(String rol) {
        TenantInfo info = CONTEXT.get();
        return info != null && info.getRoles() != null && info.getRoles().contains(rol);
    }
}
