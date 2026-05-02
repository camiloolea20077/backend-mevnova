# Matriz de Trazabilidad Fase 2 — Historias de Usuario
## Sistema de Gestión Hospitalaria (SGH)

**Versión**: 1.0
**Continuación de**: `matriz_trazabilidad_v2.md` (fase 1)
**Tablas nuevas**: 32 (ver `agente_modelo_datos_fase2.md`)

---

## 1. Convenciones

| Columna | Significado |
|---------|------------|
| **Tablas principales** | Tablas donde la HU ejecuta escrituras o validaciones críticas |
| **Catálogos y lecturas** | Tablas que solo se leen para validar o desplegar |
| **Multi-tenant** | `E` (empresa), `E+S` (empresa + sede), `G` (global solo super-admin) |

---

## 2. Matriz: HU ↔ Tablas ↔ Multi-tenant

### Bloque 9 — Glosas y conciliación

| Código | Tablas principales | Catálogos y lecturas | Multi-tenant |
|--------|--------------------|-----------------------|--------------|
| HU-FASE2-061 | `motivo_glosa`, `auditoria` | — | G (solo super-admin) |
| HU-FASE2-062 | `glosa`, `auditoria` | `factura`, `radicacion`, `pagador`, `estado_factura` | E+S |
| HU-FASE2-063 | `detalle_glosa`, `auditoria` | `glosa`, `detalle_factura`, `motivo_glosa` | E |
| HU-FASE2-064 | `respuesta_glosa`, `glosa`, `auditoria` | `detalle_glosa`, `estado_glosa` | E |
| HU-FASE2-065 | `concertacion_glosa`, `glosa`, `auditoria` | `respuesta_glosa`, `factura` | E |
| HU-FASE2-066 | `cuenta_por_cobrar`, `movimiento_cuenta_por_cobrar`, `factura` | `concertacion_glosa`, `estado_cartera`, `estado_factura` | E |

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

## 4. Matriz inversa: Tabla → HUs que la impactan

### Glosas
- **motivo_glosa**: HU-FASE2-061, 063
- **glosa**: HU-FASE2-062, 063, 064, 065
- **detalle_glosa**: HU-FASE2-063, 064
- **respuesta_glosa**: HU-FASE2-064
- **concertacion_glosa**: HU-FASE2-065, 066
- **cuenta_por_cobrar**: HU-FASE2-066

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

## 5. Matriz de dependencias entre HUs

### Glosas
| HU | Depende de |
|----|------------|
| HU-FASE2-061 | — |
| HU-FASE2-062 | HU-FASE1-058 (radicación) |
| HU-FASE2-063 | HU-FASE2-061, 062 |
| HU-FASE2-064 | HU-FASE2-063 |
| HU-FASE2-065 | HU-FASE2-064 |
| HU-FASE2-066 | HU-FASE2-065, HU-FASE1-059 (CxC) |

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

## 6. Matriz de cobertura por rol

| Rol | HUs nuevas donde es actor principal |
|-----|-------------------------------------|
| Super-administrador | HU-FASE2-061 |
| Auditor de cuentas | HU-FASE2-062, 063, 064 |
| Auditor médico | HU-FASE2-064 |
| Coordinador de cuentas | HU-FASE2-065 |
| Administrador / Jefe de farmacia | HU-FASE2-067, 076, 077, 078 |
| Compras | HU-FASE2-068 |
| Auxiliar de farmacia | HU-FASE2-069, 070, 071, 073, 074, 075 |
| Profesional clínico (médico/enfermería) | HU-FASE2-072, 074, 075 |
| Médico | HU-FASE2-079, 080, 082, 084, 090, 091 |
| Médico tratante | HU-FASE2-090, 091 |
| Médico interconsultado | HU-FASE2-090 |
| Profesional de enfermería | HU-FASE2-085, 086, 087, 088 |
| Enfermería / Médico (compartido) | HU-FASE2-081, 083, 089, 092 |
| Profesional autorizado (consulta HC) | HU-FASE2-093 |
| Sistema (automático) | HU-FASE2-066 (impacto en cartera), HU-FASE2-087 (programación de dosis) |

---

## 7. Permisos nuevos requeridos en fase 2

Agregar al catálogo `permiso` (módulo correspondiente):

### Módulo GLOSAS
- `gestionar_motivos_glosa` (solo super-admin)
- `recibir_glosa`
- `responder_glosa`
- `conciliar_glosa`
- `consultar_glosas`

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

**Total nuevos permisos**: ~33 permisos para fase 2.

---

## 8. Consideraciones finales

### Datos semilla obligatorios fase 2
- **Catálogo `motivo_glosa`**: cargar todos los códigos de la Resolución 3047 de 2008 (~80 códigos).
- **Catálogo `tipo_antecedente`**: PATOLOGICO, QUIRURGICO, TRAUMATICO, ALERGICO, TOXICO, FARMACOLOGICO, GINECO_OBSTETRICO, PSIQUIATRICO, HOSPITALARIO, TRANSFUSIONAL.
- **Permisos nuevos**: ~33 permisos en `permiso` global.

### Cobertura de objetivos fase 2
- ✅ Glosas: ciclo completo desde recepción hasta impacto en cartera.
- ✅ Farmacia: trazabilidad por lote, FEFO, dispensación con auditoría completa.
- ✅ Historia clínica avanzada: anamnesis estructurada, MAR, balances, escalas, epicrisis y vista consolidada.

### Lo que sigue para fase 3
- Facturación electrónica DIAN
- MIPRES completo
- Interoperabilidad HL7/FHIR
- Portal del paciente
- Programación quirúrgica
- Laboratorio con integración de resultados
- Imágenes diagnósticas con PACS
- Telemedicina con video
