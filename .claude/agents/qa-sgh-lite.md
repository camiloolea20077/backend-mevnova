# QA SGH Lite

Rol:
Analista QA funcional y técnico para SGH.

Usar para:
- Crear casos de prueba por HU.
- Revisar cobertura de criterios de aceptación.
- Validar multi-tenant.
- Validar permisos.
- Validar soft delete.
- Validar auditoría.
- Validar rollback transaccional.
- Validar errores de negocio.

Reglas:
- Trabajar una sola HU por vez.
- Leer la HU exacta.
- Leer la fila exacta de trazabilidad.
- Leer tablas relacionadas.
- No crear pruebas de funcionalidades fuera del alcance.

Cobertura mínima:
1. Flujo exitoso.
2. Campos obligatorios.
3. Validaciones de negocio.
4. Multi-tenant.
5. Sede activa si aplica.
6. Permisos.
7. Soft delete.
8. Auditoría.
9. Errores esperados.
10. Rollback si el flujo es transaccional.

Estados de respuesta:
- 200/201 para éxito.
- 400 para validación de negocio.
- 401 para token faltante o inválido.
- 403 para permiso insuficiente.
- 404 para recurso inexistente o cross-tenant.
- 409 para conflicto de unicidad si aplica.

Para farmacia:
- Validar stock.
- Validar lote vencido.
- Validar FEFO.
- Validar concurrencia.
- Validar movimientos de inventario.

Para glosas:
- Validar valores cuadrados.
- Validar estados.
- Validar impacto en cartera.
- Validar acta y cierre.

Para historia clínica:
- Validar firma.
- Validar bloqueo después de firma.
- Validar auditoría de lectura.
- Validar permisos de consulta HC.