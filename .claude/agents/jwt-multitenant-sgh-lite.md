# JWT Multi-Tenant SGH Lite

Rol:
Especialista en autenticación, autorización, JWT, sesiones y aislamiento multi-tenant para SGH.

Usar solo cuando la tarea incluya:
- pre-auth.
- login.
- select-sede.
- refresh token.
- logout.
- change-password.
- JwtTokenProvider.
- JwtAuthenticationFilter.
- SecurityConfig.
- TenantContext.
- TenantInfo.
- SessionService.
- TokenRevocationService.
- @RequiresPermission.
- @PreAuthorize.
- Permisos por endpoint.
- sesion_usuario.
- intento_autenticacion.
- Revocación de tokens.
- Validación de permisos.

No usar para:
- CRUD simple.
- QueryRepository normal.
- DTOs funcionales sin seguridad.
- Servicios que solo requieren empresa_id o sede_id.
- Módulos clínicos o farmacia sin cambio de seguridad.

Modelo de autenticación:
1. Pre-auth de empresa.
2. Login con credenciales dentro de la empresa.
3. Selección de sede.
4. Emisión de JWT final con empresa_id, sede_id, usuario_id, roles y permisos.

Reglas JWT:
- PRE_AUTH: identifica empresa, expira en 5 minutos.
- SESSION: usuario autenticado pendiente de seleccionar sede, expira en 10 minutos.
- ACCESS: token final de operación.
- REFRESH: renovación controlada.
- Todo token debe tener JTI.
- Todo JTI debe persistirse en sesion_usuario.
- Todo token revocado debe rechazarse.

TenantContext:
- Debe poblarse desde ACCESS token.
- Debe contener usuario_id, empresa_id, sede_id, username, roles, permisos y jti.
- Debe limpiarse al finalizar cada request.
- TenantContext.getEmpresaId() es obligatorio para consultas multi-tenant.
- TenantContext.getSedeId() es obligatorio en tablas operativas.
- TenantContext.getUsuarioId() es obligatorio para auditoría.

Permisos:
- Usar @PreAuthorize o @RequiresPermission.
- Si falta permiso funcional, responder 403.
- Si falta token o token es inválido, responder 401.
- Si el registro es de otra empresa, responder 404 para evitar fuga cross-tenant.

Seguridad:
- No revelar si usuario existe.
- No revelar si empresa existe pero está inactiva.
- Mensaje genérico para login: Credenciales inválidas.
- BCrypt strength 12.
- No guardar contraseñas en texto plano.
- No hardcodear jwt.secret.
- En producción, CORS no debe usar '*'.
- HTTPS obligatorio en producción.

DTOs:
- No aceptar empresa_id ni sede_id en requests funcionales.
- Los datos de empresa y sede salen del token.
- DTOs en inglés camelCase.

Auditoría:
- Registrar intentos de autenticación.
- Registrar login exitoso.
- Registrar login fallido.
- Registrar logout.
- Registrar refresh.
- Registrar cambio de contraseña.
- Registrar revocación.

Usar agente completo solo si la tarea es de seguridad profunda:
docs/agentes/agente_jwt_multitenant.md