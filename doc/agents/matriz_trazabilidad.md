# Matriz de Trazabilidad v2 — Historias de Usuario Fase 1 (Multi-Tenant)
## Sistema de Gestión Hospitalaria (SGH)

**Versión**: 2.0 (alineada con `backlog_fase1_v2.md` y BD v2 de 110 tablas)

---

## 1. Convenciones de la matriz

Columnas:
- **Código**: identificador de la HU.
- **Tablas principales**: tablas donde la HU ejecuta escrituras o validaciones clave.
- **Catálogos y lecturas**: tablas que solo se leen para validar o desplegar.
- **Filtro multi-tenant**: `E` si filtra por `empresa_id`, `E+S` si filtra por `empresa_id` y `sede_id`, `G` si opera sobre recursos globales (solo super-admin).

---

## 2. Matriz general: Historia ↔ Módulo ↔ Tablas ↔ Multi-tenant

### Bloque 0 — Multi-tenant y autenticación

| Código | Tablas principales | Catálogos y lecturas | Multi-tenant |
|--------|--------------------|-----------------------|--------------|
| HU-FASE1-000 | `empresa`, `sede`, `usuario`, `rol`, `usuario_rol`, `auditoria` | `pais`, `departamento`, `municipio` | G (solo super-admin) |
| HU-FASE1-001A | `sesion_usuario`, `intento_autenticacion` | `empresa` | E (filtra por código→empresa) |
| HU-FASE1-001B | `usuario`, `sesion_usuario`, `intento_autenticacion`, `historial_password` | `empresa`, `usuario_rol`, `sede` | E |
| HU-FASE1-001C | `sesion_usuario`, `auditoria` | `usuario`, `usuario_rol`, `rol`, `rol_permiso`, `permiso`, `sede` | E+S |
| HU-FASE1-001D | `sesion_usuario` | `usuario`, `usuario_rol`, `rol_permiso` | E+S |
| HU-FASE1-001E | `sesion_usuario`, `auditoria` | — | E+S |
| HU-FASE1-001F | `usuario`, `historial_password`, `sesion_usuario`, `auditoria` | — | E |

### Bloque 1 — Seguridad y estructura base

| Código | Tablas principales | Catálogos y lecturas | Multi-tenant |
|--------|--------------------|-----------------------|--------------|
| HU-FASE1-002 | `sede`, `auditoria` | `pais`, `departamento`, `municipio` | E |
| HU-FASE1-003 | `servicio_habilitado`, `auditoria` | `sede` | E+S |
| HU-FASE1-004 | `permiso`, `auditoria` | — | G (solo super-admin) |
| HU-FASE1-005 | `rol`, `rol_permiso`, `auditoria` | `permiso` | E |
| HU-FASE1-006 | `usuario`, `historial_password`, `auditoria` | `tercero` | E |
| HU-FASE1-007 | `usuario_rol`, `auditoria` | `usuario`, `rol`, `sede` | E |
| HU-FASE1-008 | `profesional_salud`, `auditoria` | `tercero`, `especialidad`, `usuario` | E |
| HU-FASE1-009 | — (solo lectura) | `auditoria` | E |

### Bloque 2 — Terceros y pacientes

| Código | Tablas principales | Catálogos y lecturas | Multi-tenant |
|--------|--------------------|-----------------------|--------------|
| HU-FASE1-010 | `tercero`, `auditoria` | Catálogos de personas | E |
| HU-FASE1-011 | — | `tercero`, `paciente`, `contacto_tercero`, `direccion_tercero` | E |
| HU-FASE1-012 | `tercero`, `auditoria` | Catálogos | E |
| HU-FASE1-013 | `paciente`, `auditoria` | `tercero`, catálogos clínicos | E |
| HU-FASE1-014 | `contacto_tercero`, `auditoria` | `tipo_contacto`, `tercero` | E |
| HU-FASE1-015 | `direccion_tercero`, `auditoria` | `zona_residencia`, geográficos, `tercero` | E |
| HU-FASE1-016 | `relacion_tercero`, `auditoria` | `tipo_relacion`, `tercero` | E |
| HU-FASE1-017 | `sisben_paciente`, `auditoria` | `paciente`, `grupo_sisben` | E |
| HU-FASE1-018 | `seguridad_social_paciente`, `auditoria` | `paciente`, `pagador`, `regimen`, `categoria_afiliacion`, `tipo_afiliacion`, `tercero` | E |
| HU-FASE1-019 | `contrato_paciente`, `auditoria` | `paciente`, `contrato` | E |

### Bloque 3 — Admisiones

| Código | Tablas principales | Catálogos y lecturas | Multi-tenant |
|--------|--------------------|-----------------------|--------------|
| HU-FASE1-020 | `admision`, `auditoria` | `paciente`, `tipo_admision`, `estado_admision`, `origen_atencion`, `sede`, `pagador`, `contrato`, `tercero` | E+S |
| HU-FASE1-021 | `atencion` | `admision`, `estado_atencion`, `finalidad_atencion` | E+S (heredado) |
| HU-FASE1-022 | — | `admision`, `paciente`, `tercero`, `pagador`, `sede` | E+S |
| HU-FASE1-023 | `admision`, `auditoria` | `estado_admision` | E+S |
| HU-FASE1-024 | `admision`, `auditoria` | `atencion` | E+S |

### Bloque 4 — Triage y urgencias

| Código | Tablas principales | Catálogos y lecturas | Multi-tenant |
|--------|--------------------|-----------------------|--------------|
| HU-FASE1-025 | `atencion`, `auditoria` | `admision`, `paciente` | E+S |
| HU-FASE1-026 | `atencion`, `auditoria` | `admision` | E+S |
| HU-FASE1-027 | — | `admision`, `atencion`, `paciente`, `tercero` | E+S |
| HU-FASE1-028 | `atencion`, `auditoria` | `admision`, `paciente`, `tercero`, `diagnostico_atencion`, `orden_clinica`, `prescripcion` | E+S |
| HU-FASE1-029 | `diagnostico_atencion`, `auditoria` | `atencion`, `catalogo_diagnostico` (global) | E |
| HU-FASE1-030 | `orden_clinica`, `detalle_orden_clinica`, `auditoria` | `atencion`, `paciente`, `servicio_salud`, `catalogo_diagnostico`, `tipo_orden_clinica`, `estado_orden` | E+S |
| HU-FASE1-031 | `prescripcion`, `detalle_prescripcion`, `auditoria` | `atencion`, `paciente`, `servicio_salud`, `via_administracion`, `frecuencia_dosis`, `estado_prescripcion` | E+S |
| HU-FASE1-032 | `atencion`, `auditoria` | `diagnostico_atencion` | E+S |

### Bloque 5 — Hospitalización

| Código | Tablas principales | Catálogos y lecturas | Multi-tenant |
|--------|--------------------|-----------------------|--------------|
| HU-FASE1-033 | `admision`, `atencion`, `auditoria` | `estado_admision`, `diagnostico_atencion` | E+S |
| HU-FASE1-034 | `atencion`, `admision`, `auditoria` | `recurso_fisico`, `profesional_salud` | E+S |
| HU-FASE1-035 | `atencion`, `auditoria` | — | E+S |
| HU-FASE1-036 | `atencion`, `auditoria` | `admision` | E+S |
| HU-FASE1-037 | `orden_clinica`, `detalle_orden_clinica`, `auditoria` | `atencion`, `admision` | E+S |
| HU-FASE1-038 | `atencion`, `admision`, `diagnostico_atencion`, `auditoria` | `recurso_fisico` | E+S |

### Bloque 6 — Citas

| Código | Tablas principales | Catálogos y lecturas | Multi-tenant |
|--------|--------------------|-----------------------|--------------|
| HU-FASE1-039 | `calendario_cita`, `detalle_calendario_cita`, `auditoria` | — | E |
| HU-FASE1-040 | `recurso_fisico`, `auditoria` | `sede` | E+S |
| HU-FASE1-041 | `agenda_profesional`, `bloque_agenda`, `auditoria` | `profesional_salud`, `especialidad`, `sede`, `recurso_fisico`, `calendario_cita`, `estado_agenda` | E+S |
| HU-FASE1-042 | `disponibilidad_cita` | `agenda_profesional`, `bloque_agenda`, `calendario_cita`, `detalle_calendario_cita`, `estado_disponibilidad` | E+S (heredado) |
| HU-FASE1-043 | `cita`, `disponibilidad_cita`, `auditoria` | `paciente`, `agenda_profesional`, `servicio_salud`, `tipo_cita`, `estado_cita`, `especialidad` | E+S |
| HU-FASE1-044 | `cita`, `disponibilidad_cita`, `auditoria` | `motivo_cancelacion_cita`, `motivo_reprogramacion_cita` | E+S |
| HU-FASE1-045 | `lista_espera_cita`, `auditoria` | `paciente`, `especialidad`, `servicio_salud` | E+S |
| HU-FASE1-046 | `traslado_agenda`, `detalle_traslado_agenda`, `cita`, `disponibilidad_cita`, `auditoria` | `agenda_profesional` | E+S |

### Bloque 7 — Servicios, pagadores y contratos

| Código | Tablas principales | Catálogos y lecturas | Multi-tenant |
|--------|--------------------|-----------------------|--------------|
| HU-FASE1-047 | `servicio_salud`, `auditoria` | `categoria_servicio_salud`, `centro_costo` | E |
| HU-FASE1-048 | `centro_costo`, `auditoria` | — | E |
| HU-FASE1-049 | `pagador`, `auditoria` | `tercero`, `tipo_pagador`, `tipo_cliente` | E |
| HU-FASE1-050 | `contrato`, `auditoria` | `pagador`, `modalidad_pago`, `tarifario` | E |
| HU-FASE1-051 | `tarifario`, `detalle_tarifario`, `auditoria` | `servicio_salud` | E |
| HU-FASE1-052 | `tarifa_contrato`, `auditoria` | `contrato`, `servicio_salud` | E |
| HU-FASE1-053 | `servicio_contrato`, `auditoria` | `contrato`, `servicio_salud` | E |

### Bloque 8 — Facturación inicial

| Código | Tablas principales | Catálogos y lecturas | Multi-tenant |
|--------|--------------------|-----------------------|--------------|
| HU-FASE1-054 | `factura`, `detalle_factura`, `auditoria` | `admision`, `atencion`, `paciente`, `pagador`, `contrato`, `servicio_salud`, `tarifa_contrato`, `detalle_tarifario`, `estado_factura` | E+S |
| HU-FASE1-055 | `detalle_factura`, `factura`, `auditoria` | `servicio_salud` | E |
| HU-FASE1-056 | `detalle_factura`, `factura` | `paciente`, `seguridad_social_paciente`, `sisben_paciente`, `regimen`, `grupo_sisben` | E |
| HU-FASE1-057 | `rips_encabezado`, `rips_detalle`, `auditoria` | `factura`, `detalle_factura`, `atencion`, `diagnostico_atencion`, `paciente`, `pagador` | E |
| HU-FASE1-058 | `radicacion`, `factura`, `auditoria` | `pagador`, `estado_radicacion` | E+S |
| HU-FASE1-059 | `cuenta_por_cobrar`, `movimiento_cuenta_por_cobrar` | `factura`, `pagador`, `estado_cartera` | E |
| HU-FASE1-060 | — (solo lectura) | `cuenta_por_cobrar`, `movimiento_cuenta_por_cobrar`, `factura`, `pagador` | E |

---

## 3. Clasificación de tablas por aislamiento

### Tablas con `empresa_id` y `sede_id` (operativas, doble aislamiento)
- `admision`, `atencion`
- `cita`, `disponibilidad_cita`, `lista_espera_cita`, `traslado_agenda`
- `agenda_profesional`, `recurso_fisico`, `servicio_habilitado`
- `orden_clinica`, `prescripcion`
- `factura`, `radicacion`, `pago`

### Tablas con `empresa_id` únicamente (aislamiento por tenant)
- `sede`
- `tercero`, `paciente`, `contacto_tercero`, `direccion_tercero`, `relacion_tercero`, `sisben_paciente`, `seguridad_social_paciente`, `contrato_paciente`
- `usuario`, `rol`, `usuario_rol`, `historial_password`, `sesion_usuario`, `intento_autenticacion`
- `profesional_salud`
- `servicio_salud`, `centro_costo`, `pagador`, `contrato`, `tarifario`, `detalle_tarifario`, `tarifa_contrato`, `servicio_contrato`, `regla_contable_servicio`
- `calendario_cita`, `detalle_calendario_cita`, `bloque_agenda`
- `diagnostico_atencion`
- `detalle_factura`, `rips_encabezado`, `rips_detalle`
- `cuenta_por_cobrar`, `movimiento_cuenta_por_cobrar`
- `glosa`, `detalle_glosa`, `respuesta_glosa`
- `auditoria`

### Tablas globales (sin `empresa_id`, compartidas entre tenants)
- **Catálogos de personas**: `tipo_tercero`, `tipo_documento`, `sexo`, `genero`, `identidad_genero`, `orientacion_sexual`, `estado_civil`, `nivel_escolaridad`, `ocupacion`, `pertenencia_etnica`, `grupo_sanguineo`, `factor_rh`, `discapacidad`, `grupo_atencion`, `tipo_contacto`, `tipo_relacion`, `zona_residencia`
- **Catálogos geográficos**: `pais`, `departamento`, `municipio`
- **Catálogos clínicos**: `catalogo_diagnostico` (CIE-10), `equivalencia_diagnostico`, `especialidad`, `via_administracion`, `frecuencia_dosis`
- **Catálogos asistenciales**: `tipo_admision`, `estado_admision`, `estado_atencion`, `finalidad_atencion`, `origen_atencion`, `tipo_cita`, `estado_cita`, `estado_agenda`, `estado_disponibilidad`, `motivo_cancelacion_cita`, `motivo_reprogramacion_cita`, `tipo_orden_clinica`, `estado_orden`, `estado_prescripcion`
- **Catálogos financieros**: `regimen`, `categoria_afiliacion`, `tipo_afiliacion`, `grupo_sisben`, `tipo_pagador`, `tipo_cliente`, `modalidad_pago`, `categoria_servicio_salud`, `estado_factura`, `estado_cartera`, `estado_radicacion`, `estado_glosa`, `medio_pago`
- **Seguridad global**: `permiso`
- **Raíz**: `empresa`

---

## 4. Matriz inversa: Tabla → HUs que la impactan

### Núcleo multi-tenant
- **empresa**: HU-FASE1-000, 001A
- **sede**: HU-FASE1-000, 002, 020, 032–038, 040, 041
- **usuario**: HU-FASE1-006, 001B, 001F
- **sesion_usuario**: HU-FASE1-001A, 001B, 001C, 001D, 001E, 001F
- **rol / rol_permiso / permiso**: HU-FASE1-004, 005, 007
- **usuario_rol**: HU-FASE1-007, 001B, 001C

### Núcleo asistencial
- **tercero**: HU-FASE1-010, 011, 012, 013, 014, 015, 016, 018, 020, 008, 049
- **paciente**: HU-FASE1-013, 017, 018, 019, 020, 022, 025, 027–032, 034–038, 043, 045, 054, 056
- **admision**: HU-FASE1-020, 021, 022, 023, 024, 025, 027, 033, 034, 035, 036, 038, 054
- **atencion**: HU-FASE1-021, 024, 025, 026, 027, 028, 029, 030, 031, 032, 033, 034, 035, 036, 037, 038, 054
- **diagnostico_atencion**: HU-FASE1-029, 030, 033, 038, 057

### Citas
- **agenda_profesional / bloque_agenda**: HU-FASE1-041, 042, 043, 046
- **disponibilidad_cita**: HU-FASE1-042, 043, 044, 046
- **cita**: HU-FASE1-043, 044, 046
- **recurso_fisico**: HU-FASE1-034, 038, 040, 041

### Servicios y contratos
- **servicio_salud**: HU-FASE1-030, 031, 043, 045, 047, 051, 052, 053, 054, 055
- **pagador**: HU-FASE1-018, 020, 049, 050, 054, 057, 058, 059, 060
- **contrato**: HU-FASE1-019, 020, 050, 052, 053, 054
- **tarifario / detalle_tarifario**: HU-FASE1-050, 051, 054
- **tarifa_contrato**: HU-FASE1-052, 054

### Facturación y cartera
- **factura / detalle_factura**: HU-FASE1-054, 055, 056, 057, 058, 059, 060
- **rips_encabezado / rips_detalle**: HU-FASE1-057
- **radicacion**: HU-FASE1-058
- **cuenta_por_cobrar / movimiento_cuenta_por_cobrar**: HU-FASE1-059, 060

### Transversales
- **auditoria**: impactada por todas las HUs que modifican datos
- **intento_autenticacion**: HU-FASE1-001A, 001B, 001C
- **historial_password**: HU-FASE1-006, 001F

---

## 5. Matriz de dependencias entre HUs

| HU | Depende de |
|----|------------|
| HU-FASE1-000 | — (primera HU del sistema) |
| HU-FASE1-001A | HU-FASE1-000 |
| HU-FASE1-001B | HU-FASE1-001A, HU-FASE1-006, HU-FASE1-007 |
| HU-FASE1-001C | HU-FASE1-001B |
| HU-FASE1-001D | HU-FASE1-001C |
| HU-FASE1-001E | HU-FASE1-001C |
| HU-FASE1-001F | HU-FASE1-001B o HU-FASE1-001C |
| HU-FASE1-002 | HU-FASE1-000 |
| HU-FASE1-003 | HU-FASE1-002 |
| HU-FASE1-004 | — |
| HU-FASE1-005 | HU-FASE1-004 |
| HU-FASE1-006 | HU-FASE1-000 |
| HU-FASE1-007 | HU-FASE1-005, HU-FASE1-006 |
| HU-FASE1-008 | HU-FASE1-010 |
| HU-FASE1-009 | HU-FASE1-006 |
| HU-FASE1-010 | Catálogos |
| HU-FASE1-011 | HU-FASE1-010 |
| HU-FASE1-012 | HU-FASE1-010 |
| HU-FASE1-013 | HU-FASE1-010 |
| HU-FASE1-014 | HU-FASE1-010 |
| HU-FASE1-015 | HU-FASE1-010 |
| HU-FASE1-016 | HU-FASE1-010 |
| HU-FASE1-017 | HU-FASE1-013 |
| HU-FASE1-018 | HU-FASE1-013, HU-FASE1-049 |
| HU-FASE1-019 | HU-FASE1-013, HU-FASE1-050 |
| HU-FASE1-020 | HU-FASE1-002, HU-FASE1-013, HU-FASE1-018, HU-FASE1-049, HU-FASE1-050 |
| HU-FASE1-021 | HU-FASE1-020 |
| HU-FASE1-022 | HU-FASE1-020 |
| HU-FASE1-023 | HU-FASE1-020 |
| HU-FASE1-024 | HU-FASE1-020, HU-FASE1-032, HU-FASE1-038 |
| HU-FASE1-025 | HU-FASE1-020, HU-FASE1-021 |
| HU-FASE1-026 | HU-FASE1-025 |
| HU-FASE1-027 | HU-FASE1-025 |
| HU-FASE1-028 | HU-FASE1-025, HU-FASE1-027, HU-FASE1-029, HU-FASE1-030, HU-FASE1-031 |
| HU-FASE1-029 | HU-FASE1-028 (CIE-10) |
| HU-FASE1-030 | HU-FASE1-028, HU-FASE1-029, HU-FASE1-047 |
| HU-FASE1-031 | HU-FASE1-028, HU-FASE1-047 |
| HU-FASE1-032 | HU-FASE1-028, HU-FASE1-029 |
| HU-FASE1-033 | HU-FASE1-028, HU-FASE1-032 |
| HU-FASE1-034 | HU-FASE1-033, HU-FASE1-040 |
| HU-FASE1-035 | HU-FASE1-034 |
| HU-FASE1-036 | HU-FASE1-035 |
| HU-FASE1-037 | HU-FASE1-030, HU-FASE1-034 |
| HU-FASE1-038 | HU-FASE1-034, HU-FASE1-035, HU-FASE1-036 |
| HU-FASE1-039 | — |
| HU-FASE1-040 | HU-FASE1-002 |
| HU-FASE1-041 | HU-FASE1-008, HU-FASE1-039, HU-FASE1-040 |
| HU-FASE1-042 | HU-FASE1-039, HU-FASE1-041 |
| HU-FASE1-043 | HU-FASE1-013, HU-FASE1-042, HU-FASE1-047 |
| HU-FASE1-044 | HU-FASE1-043 |
| HU-FASE1-045 | HU-FASE1-013 |
| HU-FASE1-046 | HU-FASE1-043 |
| HU-FASE1-047 | HU-FASE1-048 |
| HU-FASE1-048 | — |
| HU-FASE1-049 | HU-FASE1-010 |
| HU-FASE1-050 | HU-FASE1-049, HU-FASE1-051 |
| HU-FASE1-051 | HU-FASE1-047 |
| HU-FASE1-052 | HU-FASE1-050, HU-FASE1-051 |
| HU-FASE1-053 | HU-FASE1-050 |
| HU-FASE1-054 | HU-FASE1-024, HU-FASE1-050, HU-FASE1-051, HU-FASE1-052, HU-FASE1-053 |
| HU-FASE1-055 | HU-FASE1-054 |
| HU-FASE1-056 | HU-FASE1-017, HU-FASE1-018, HU-FASE1-054 |
| HU-FASE1-057 | HU-FASE1-054 |
| HU-FASE1-058 | HU-FASE1-054 |
| HU-FASE1-059 | HU-FASE1-054 |
| HU-FASE1-060 | HU-FASE1-059 |

---

## 6. Propuesta de agrupación en sprints (actualizada)

### Sprint 0 — Bootstrap del sistema (1 sprint de 2 semanas)
- HU-FASE1-000 Gestión de empresas (super-admin)
- HU-FASE1-004 Permisos del sistema (catálogo global)
- Carga inicial de catálogos globales (CIE-10, DANE, etc.)
- Seed de super-administrador

### Sprint 1 — Autenticación multi-tenant + seguridad empresa
- HU-FASE1-001A Pre-autenticación
- HU-FASE1-001B Login
- HU-FASE1-001C Selección de sede
- HU-FASE1-001D Refresh
- HU-FASE1-001E Logout
- HU-FASE1-001F Cambio de contraseña
- HU-FASE1-002 Sedes
- HU-FASE1-005 Roles
- HU-FASE1-006 Usuarios
- HU-FASE1-007 Asignación de roles
- HU-FASE1-009 Auditoría

### Sprint 2 — Personas y profesionales
- HU-FASE1-010, 011, 012 Tercero
- HU-FASE1-013, 014, 015, 016 Paciente, contactos, direcciones, relaciones
- HU-FASE1-008 Profesional de salud
- HU-FASE1-003 Servicios habilitados
- HU-FASE1-039 Calendarios
- HU-FASE1-040 Recursos físicos
- HU-FASE1-048 Centros de costo

### Sprint 3 — Servicios, pagadores, contratos
- HU-FASE1-017 SISBEN
- HU-FASE1-047 Catálogo de servicios
- HU-FASE1-049 Pagadores
- HU-FASE1-051 Tarifarios base
- HU-FASE1-050 Contratos
- HU-FASE1-052 Tarifas por contrato
- HU-FASE1-053 Servicios del contrato
- HU-FASE1-018 Seguridad social
- HU-FASE1-019 Contratos del paciente

### Sprint 4 — Admisiones y triage
- HU-FASE1-020–024 Admisiones
- HU-FASE1-025–027 Triage y cola de urgencias

### Sprint 5 — Atención de urgencias
- HU-FASE1-028 Consola de urgencias
- HU-FASE1-029–032 Diagnósticos, órdenes, prescripción, cierre
- HU-FASE1-033 Solicitud de hospitalización

### Sprint 6 — Hospitalización y citas
- HU-FASE1-034–038 Hospitalización
- HU-FASE1-041 Agenda profesional
- HU-FASE1-042 Disponibilidad
- HU-FASE1-043 Asignación de cita
- HU-FASE1-044 Cancelación/reprogramación
- HU-FASE1-045 Lista de espera
- HU-FASE1-046 Traslado de agenda

### Sprint 7 — Facturación y cartera
- HU-FASE1-054–060 Facturación, RIPS, radicación, cartera

---

## 7. Matriz de cobertura por rol

| Rol | HUs donde es actor principal |
|-----|-------------------------------|
| Super-administrador | HU-FASE1-000, 004 |
| Administrador de empresa | HU-FASE1-002, 003, 005, 006, 007, 008, 040, 047, 048, 049, 050, 051, 052, 053 |
| Auditor | HU-FASE1-009, 057 |
| Recepcionista / Auxiliar de admisiones | HU-FASE1-010, 011, 012, 013, 014, 015, 016, 017, 018, 019, 020, 022, 023, 024, 043, 044, 045 |
| Profesional de triage | HU-FASE1-025, 026 |
| Médico general de urgencias | HU-FASE1-027, 028, 029, 030, 031, 032, 033 |
| Médico hospitalario | HU-FASE1-034, 035, 036, 037, 038, 029, 030, 031 |
| Coordinador asistencial | HU-FASE1-022, 027 |
| Coordinador de agendas | HU-FASE1-039, 041, 042, 046 |
| Facturador / Radicador | HU-FASE1-054, 055, 057, 058 |
| Cartera / Coordinador financiero | HU-FASE1-060 |
| Contador | HU-FASE1-048 |
| Todos los usuarios | HU-FASE1-001A, 001B, 001C, 001D, 001E, 001F, 011 |
| Sistema (automático) | HU-FASE1-021, 042, 056, 059 |

---

## 8. Consideraciones finales

### Datos semilla obligatorios antes del arranque
- Catálogos geográficos (DANE: 32 departamentos, ~1.100 municipios).
- Catálogo CIE-10 (~14.000 códigos, Ministerio de Salud).
- Catálogo CUPS (Clasificación Única de Procedimientos, Ministerio de Salud).
- Catálogos de personas, asistenciales, financieros (ver script `02_catalogos.sql`).
- **Permisos globales del sistema** (seed técnico).
- **Un super-administrador** con `empresa_id = NULL`.

### Cobertura de los objetivos del proyecto
- ✅ Multi-tenant con aislamiento total entre empresas y entre sedes.
- ✅ Autenticación JWT en 3 pasos con auditoría completa.
- ✅ Núcleo operativo (admisiones, urgencias, hospitalización, citas, facturación) por empresa y sede.
- ✅ Las 3 consolas prometidas: urgencias (HU-028), hospitalización (HU-034–038), asignación de citas (HU-043).
- ✅ Decisiones arquitectónicas preservadas: tercero maestro, paciente especialización, todo en español en BD, snake_case, modularidad.

### Fases futuras sugeridas
- **Fase 2**: historia clínica avanzada, escalas clínicas, farmacia/dispensación, laboratorio con resultados, imágenes, switch-sede sin re-login, 2FA, SSO.
- **Fase 3**: facturación electrónica DIAN, MIPRES completo, interoperabilidad HL7/FHIR, portal del paciente.
- **Fase 4**: indicadores y tableros gerenciales, IA clínica, análisis predictivo de cartera.