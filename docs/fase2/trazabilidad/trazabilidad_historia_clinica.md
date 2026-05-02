# Trazabilidad — Historia clínica avanzada

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

### Bloque 11 — Historia clínica avanzada

| Código | Tablas principales | Catálogos y lecturas | Multi-tenant |
|--------|--------------------|-----------------------|--------------|
| HU-FASE2-079 | `antecedente_personal`, `auditoria` | `paciente`, `tipo_antecedente`, `catalogo_diagnostico` | E |
| HU-FASE2-080 | `antecedente_familiar`, `auditoria` | `paciente`, `catalogo_diagnostico` | E |
| HU-FASE2-081 | `habito_paciente`, `auditoria` | `paciente` | E |
| HU-FASE2-082 | `revision_sistemas`, `auditoria` | `atencion`, `paciente` | E |
| HU-FASE2-083 | `vacuna_paciente`, `auditoria` | `paciente`, `via_administracion`, `profesional_salud` | E |
| HU-FASE2-084 | `medicacion_habitual`, `auditoria` | `paciente`, `servicio_salud`, `via_administracion`, `frecuencia_dosis` | E |
| HU-FASE2-085 | `plan_cuidados_enfermeria`, `auditoria` | `atencion`, `paciente`, `profesional_salud` | E+S |
| HU-FASE2-086 | `nota_enfermeria`, `auditoria` | `atencion`, `paciente`, `profesional_salud` | E+S |
| HU-FASE2-087 | `administracion_medicamento`, `auditoria` | `detalle_prescripcion`, `dispensacion`, `lote`, `via_administracion` | E+S |
| HU-FASE2-088 | `balance_liquidos`, `detalle_balance_liquidos`, `auditoria` | `atencion`, `paciente` | E+S |
| HU-FASE2-089 | `escala_clinica`, `auditoria` | `atencion`, `paciente`, `profesional_salud` | E+S |
| HU-FASE2-090 | `interconsulta`, `auditoria` | `atencion`, `especialidad`, `profesional_salud` | E+S |
| HU-FASE2-091 | `epicrisis`, `auditoria` | `admision`, `diagnostico_atencion`, `prescripcion`, `paciente` | E+S |
| HU-FASE2-092 | `adjunto_clinico`, `auditoria` | `paciente`, `atencion` | E |
| HU-FASE2-093 | `auditoria` (escritura por cada VIEW) | Vista agregada de muchas tablas (ver detalle abajo) | E |

**HU-FASE2-093 — Tablas leídas para vista consolidada**:
`paciente`, `tercero`, `admision`, `atencion`, `diagnostico_atencion`, `orden_clinica`, `detalle_orden_clinica`, `prescripcion`, `detalle_prescripcion`, `dispensacion`, `detalle_dispensacion`, `administracion_medicamento`, `nota_enfermeria`, `antecedente_personal`, `antecedente_familiar`, `habito_paciente`, `vacuna_paciente`, `medicacion_habitual`, `escala_clinica`, `interconsulta`, `epicrisis`, `adjunto_clinico`, `revision_sistemas`, `plan_cuidados_enfermeria`, `balance_liquidos`.

---


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

### Historia clínica avanzada
- **tipo_antecedente**: HU-FASE2-079
- **antecedente_personal**: HU-FASE2-079, 093
- **antecedente_familiar**: HU-FASE2-080, 093
- **habito_paciente**: HU-FASE2-081, 093
- **revision_sistemas**: HU-FASE2-082, 093
- **vacuna_paciente**: HU-FASE2-083, 093
- **medicacion_habitual**: HU-FASE2-084, 093
- **plan_cuidados_enfermeria**: HU-FASE2-085, 093
- **nota_enfermeria**: HU-FASE2-086, 093
- **administracion_medicamento**: HU-FASE2-087, 093
- **balance_liquidos / detalle**: HU-FASE2-088, 093
- **escala_clinica**: HU-FASE2-089, 093
- **interconsulta**: HU-FASE2-090, 093
- **epicrisis**: HU-FASE2-091, 093
- **adjunto_clinico**: HU-FASE2-092, 093
- **consentimiento_informado**: usado en flujos de procedimientos (no tiene HU dedicada en este backlog)

---


---

## Dependencias del módulo

### Historia clínica avanzada
| HU | Depende de |
|----|------------|
| HU-FASE2-079 | HU-FASE1-013 (paciente) |
| HU-FASE2-080 | HU-FASE1-013 |
| HU-FASE2-081 | HU-FASE1-013 |
| HU-FASE2-082 | HU-FASE1-028 |
| HU-FASE2-083 | HU-FASE1-013 |
| HU-FASE2-084 | HU-FASE1-013 |
| HU-FASE2-085 | HU-FASE1-034 (ingreso hospitalario) |
| HU-FASE2-086 | HU-FASE1-034, 035, 036 |
| HU-FASE2-087 | HU-FASE1-031, HU-FASE2-074 |
| HU-FASE2-088 | HU-FASE1-034 |
| HU-FASE2-089 | HU-FASE1-028 o 034 |
| HU-FASE2-090 | HU-FASE1-028, 034 |
| HU-FASE2-091 | HU-FASE1-038 (egreso hospitalario) |
| HU-FASE2-092 | HU-FASE1-013 |
| HU-FASE2-093 | Todas las HUs anteriores del bloque 11 |

---


---

## Permisos del módulo

### Módulo HISTORIA_CLINICA
- `registrar_antecedentes`
- `registrar_habitos`
- `registrar_revision_sistemas`
- `registrar_vacunas`
- `registrar_medicacion_habitual`
- `gestionar_plan_cuidados`
- `registrar_nota_enfermeria`
- `registrar_administracion_medicamento`
- `registrar_balance_liquidos`
- `aplicar_escalas_clinicas`
- `solicitar_interconsulta`
- `responder_interconsulta`
- `generar_epicrisis`
- `cargar_adjuntos_clinicos`
- `consultar_historia_clinica`
- `consultar_adjuntos_confidenciales`

