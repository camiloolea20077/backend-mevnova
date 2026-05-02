# Trazabilidad — Glosas y conciliación

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

### Bloque 9 — Glosas y conciliación

| Código | Tablas principales | Catálogos y lecturas | Multi-tenant |
|--------|--------------------|-----------------------|--------------|
| HU-FASE2-061 | `motivo_glosa`, `auditoria` | — | G (solo super-admin) |
| HU-FASE2-062 | `glosa`, `auditoria` | `factura`, `radicacion`, `pagador`, `estado_factura` | E+S |
| HU-FASE2-063 | `detalle_glosa`, `auditoria` | `glosa`, `detalle_factura`, `motivo_glosa` | E |
| HU-FASE2-064 | `respuesta_glosa`, `glosa`, `auditoria` | `detalle_glosa`, `estado_glosa` | E |
| HU-FASE2-065 | `concertacion_glosa`, `glosa`, `auditoria` | `respuesta_glosa`, `factura` | E |
| HU-FASE2-066 | `cuenta_por_cobrar`, `movimiento_cuenta_por_cobrar`, `factura` | `concertacion_glosa`, `estado_cartera`, `estado_factura` | E |


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

### Glosas
- **motivo_glosa**: HU-FASE2-061, 063
- **glosa**: HU-FASE2-062, 063, 064, 065
- **detalle_glosa**: HU-FASE2-063, 064
- **respuesta_glosa**: HU-FASE2-064
- **concertacion_glosa**: HU-FASE2-065, 066
- **cuenta_por_cobrar**: HU-FASE2-066


---

## Dependencias del módulo

### Glosas
| HU | Depende de |
|----|------------|
| HU-FASE2-061 | — |
| HU-FASE2-062 | HU-FASE1-058 (radicación) |
| HU-FASE2-063 | HU-FASE2-061, 062 |
| HU-FASE2-064 | HU-FASE2-063 |
| HU-FASE2-065 | HU-FASE2-064 |
| HU-FASE2-066 | HU-FASE2-065, HU-FASE1-059 (CxC) |


---

## Permisos del módulo

### Módulo GLOSAS
- `gestionar_motivos_glosa` (solo super-admin)
- `recibir_glosa`
- `responder_glosa`
- `conciliar_glosa`
- `consultar_glosas`

