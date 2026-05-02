# Regla Seguridad JWT SGH

Aplicar cuando se modifiquen archivos:
- src/main/java/**/security/**/*.java
- src/main/java/**/controller/**/Auth*.java
- src/main/java/**/service/**/Auth*.java

Reglas:
- empresa_id y sede_id salen del JWT mediante TenantContext.
- No aceptar empresa_id ni sede_id en requests funcionales.
- 401 para token faltante, inválido o vencido.
- 403 para permiso insuficiente.
- 404 para recurso inexistente o cross-tenant por ID.
- No revelar si usuario o empresa existen.
- BCrypt strength 12.
- No hardcodear secrets.
- Limpiar TenantContext al finalizar cada request.
- Validar JTI y revocación.
- Registrar intentos de autenticación.