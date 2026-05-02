# Regla Tests SGH

Aplicar cuando se modifiquen archivos:
- src/test/**/*.java

Reglas:
- Probar flujo exitoso.
- Probar campos obligatorios.
- Probar validaciones de negocio.
- Probar multi-tenant.
- Probar sede_id si aplica.
- Probar permisos.
- Probar soft delete.
- Probar 404 en cross-tenant por ID.
- Probar rollback en flujos transaccionales.
- No depender de datos reales de producción.