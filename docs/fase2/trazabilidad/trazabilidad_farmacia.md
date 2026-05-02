# Trazabilidad — Farmacia e inventario

> Archivo modular extraído de `matriz_trazabilidad_fase2.md`.

## 1. Convenciones

| Columna | Significado |
|---------|------------|
| **Tablas principales** | Tablas donde la HU ejecuta escrituras o validaciones críticas |
| **Catálogos y lecturas** | Tablas que solo se leen para validar o desplegar |
| **Multi-tenant** | `E` (empresa), `E+S` (empresa + sede), `G` (global solo super-admin) |

---


---

## HU ↔ Tablas ↔ Multi-tenant

### Bloque 10 — Farmacia e inventario

| Código | Tablas principales | Catálogos y lecturas | Multi-tenant |
|--------|--------------------|-----------------------|--------------|
| HU-FASE2-067 | `bodega`, `auditoria` | `sede`, `profesional_salud` | E+S |
| HU-FASE2-068 | `proveedor`, `auditoria` | `tercero` | E |
| HU-FASE2-069 | `compra`, `detalle_compra`, `lote`, `stock_lote`, `movimiento_inventario`, `auditoria` | `proveedor`, `bodega`, `servicio_salud` | E+S |
| HU-FASE2-070 | `lote`, `auditoria` | `servicio_salud`, `proveedor`, `stock_lote` (totales) | E |
| HU-FASE2-071 | — (solo lectura) | `stock_lote`, `lote`, `bodega`, `servicio_salud` | E+S |
| HU-FASE2-072 | `solicitud_medicamento`, `detalle_solicitud_medicamento`, `auditoria` | `bodega`, `servicio_salud`, `profesional_salud` | E+S |
| HU-FASE2-073 | `solicitud_medicamento`, `detalle_solicitud_medicamento`, `stock_lote`, `movimiento_inventario`, `auditoria` | `lote`, `bodega` | E+S |
| HU-FASE2-074 | `dispensacion`, `detalle_dispensacion`, `stock_lote`, `movimiento_inventario`, `auditoria` | `prescripcion`, `detalle_prescripcion`, `paciente`, `lote`, `bodega` | E+S |
| HU-FASE2-075 | `movimiento_inventario`, `stock_lote`, `auditoria` | `dispensacion`, `detalle_dispensacion`, `lote`, `bodega` | E+S |
| HU-FASE2-076 | `movimiento_inventario`, `stock_lote`, `auditoria` | `bodega`, `lote` | E+S |
| HU-FASE2-077 | `ajuste_inventario`, `detalle_ajuste_inventario`, `stock_lote`, `movimiento_inventario`, `auditoria` | `bodega`, `lote`, `servicio_salud`, `usuario` | E+S |
| HU-FASE2-078 | — (solo lectura) | `movimiento_inventario`, `lote`, `stock_lote`, `servicio_salud`, `bodega` | E |


---

## 3. Clasificación de tablas nuevas por aislamiento

### Tablas con `empresa_id` y `sede_id` (operativas, doble aislamiento)
- `bodega`, `compra`, `solicitud_medicamento`, `dispensacion`, `ajuste_inventario`
- `plan_cuidados_enfermeria`, `nota_enfermeria`, `administracion_medicamento`
- `balance_liquidos`, `escala_clinica`, `interconsulta`, `epicrisis`, `adjunto_clinico`, `consentimiento_informado`
- `stock_lote`, `movimiento_inventario`

### Tablas con `empresa_id` únicamente
- `proveedor`, `lote`, `detalle_compra`, `detalle_solicitud_medicamento`, `detalle_dispensacion`, `detalle_ajuste_inventario`
- `concertacion_glosa`
- `antecedente_personal`, `antecedente_familiar`, `habito_paciente`, `revision_sistemas`, `vacuna_paciente`, `medicacion_habitual`, `detalle_balance_liquidos`

### Tablas globales (sin `empresa_id`)
- `motivo_glosa` (catálogo Resolución 3047)
- `tipo_antecedente`

---


---

## Matriz inversa del módulo

### Farmacia e inventario
- **bodega**: HU-FASE2-067, 069, 071, 072, 073, 074, 075, 076, 077, 078
- **proveedor**: HU-FASE2-068, 069, 070
- **compra / detalle_compra**: HU-FASE2-069
- **lote**: HU-FASE2-069, 070, 071, 073, 074, 075, 076, 077, 078, 087
- **stock_lote**: HU-FASE2-069, 071, 073, 074, 075, 076, 077, 078
- **movimiento_inventario**: HU-FASE2-069, 073, 074, 075, 076, 077, 078
- **solicitud_medicamento / detalle**: HU-FASE2-072, 073
- **dispensacion / detalle**: HU-FASE2-074, 075, 087, 093
- **ajuste_inventario / detalle**: HU-FASE2-077


---

## Dependencias del módulo

### Farmacia e inventario
| HU | Depende de |
|----|------------|
| HU-FASE2-067 | HU-FASE1-002 (sedes), HU-FASE1-008 (profesionales) |
| HU-FASE2-068 | HU-FASE1-010 (terceros) |
| HU-FASE2-069 | HU-FASE2-067, 068, HU-FASE1-047 (servicios) |
| HU-FASE2-070 | HU-FASE2-069 |
| HU-FASE2-071 | HU-FASE2-067, 070 |
| HU-FASE2-072 | HU-FASE2-067 |
| HU-FASE2-073 | HU-FASE2-072 |
| HU-FASE2-074 | HU-FASE1-031 (prescripción), HU-FASE2-070 |
| HU-FASE2-075 | HU-FASE2-074 |
| HU-FASE2-076 | HU-FASE2-067, 070 |
| HU-FASE2-077 | HU-FASE2-067, 070 |
| HU-FASE2-078 | HU-FASE2-069 a 077 |


---

## Permisos del módulo

### Módulo FARMACIA
- `gestionar_bodegas`
- `gestionar_proveedores`
- `recibir_compra`
- `gestionar_lotes`
- `consultar_stock`
- `solicitar_medicamento`
- `despachar_solicitud`
- `dispensar_medicamento`
- `registrar_devolucion`
- `trasladar_inventario`
- `ajustar_inventario`
- `aprobar_ajuste_inventario`
- `consultar_kardex`

