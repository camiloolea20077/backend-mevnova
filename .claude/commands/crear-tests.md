# Crear tests SGH

Objetivo:
Crear pruebas para una HU específica.

Usar:
- `.claude/agents/qa-sgh-lite.md`
- Backlog del módulo.
- Trazabilidad de la HU.
- Modelo de tablas relacionadas.

Entrada:
Código de HU.

Proceso:
1. Leer la HU exacta.
2. Leer criterios de aceptación.
3. Leer trazabilidad.
4. Identificar tablas principales.
5. Crear pruebas mínimas.

Cobertura:
- Flujo exitoso.
- Campos obligatorios.
- Validaciones de negocio.
- Multi-tenant.
- Sede si aplica.
- Permisos.
- Soft delete.
- Auditoría.
- Error por recurso no encontrado.
- Error por cross-tenant.
- Rollback si hay transacción.

Entrega:
- Tests unitarios sugeridos.
- Tests de integración sugeridos.
- Casos funcionales manuales.
- Datos mínimos de prueba.