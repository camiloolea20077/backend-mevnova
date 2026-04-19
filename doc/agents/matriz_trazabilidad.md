# Matriz de Trazabilidad — Historias de Usuario Fase 1
## Sistema de Gestión Hospitalaria (SGH)

Este documento correlaciona cada historia de usuario con su módulo funcional y las tablas del modelo de datos (esquema `sgh`) que impacta. Sirve como referencia cruzada para desarrollo, QA y evolución del sistema.

---

## 1. Matriz general: Historia ↔ Módulo ↔ Tablas principales

La columna **Tablas principales** lista las tablas donde la historia ejecuta escrituras o validaciones clave. La columna **Tablas consultadas/catálogos** lista tablas que solo se leen para validación, despliegue o lookup.

### Bloque 1 — Seguridad y estructura base

| Código | Historia | Módulo | Tablas principales | Tablas consultadas / catálogos |
|--------|----------|--------|---------------------|---------------------------------|
| HU-FASE1-001 | Gestión de sedes | Seguridad y estructura base | `sede`, `auditoria` | `pais`, `departamento`, `municipio` |
| HU-FASE1-002 | Servicios habilitados por sede | Seguridad y estructura base | `servicio_habilitado`, `auditoria` | `sede` |
| HU-FASE1-003 | Roles y permisos | Seguridad y estructura base | `rol`, `permiso`, `rol_permiso`, `auditoria` | — |
| HU-FASE1-004 | Usuarios del sistema | Seguridad y estructura base | `usuario`, `usuario_rol`, `auditoria` | `tercero`, `rol`, `sede` |
| HU-FASE1-005 | Autenticación e inicio de sesión | Seguridad y estructura base | `usuario`, `auditoria` | `usuario_rol`, `rol_permiso`, `sede` |
| HU-FASE1-006 | Registro de profesional de salud | Seguridad y estructura base | `profesional_salud`, `auditoria` | `tercero`, `especialidad`, `usuario` |
| HU-FASE1-007 | Auditoría de acciones | Seguridad y estructura base | `auditoria` (solo lectura) | Todas las tablas transaccionales |

### Bloque 2 — Terceros y pacientes

| Código | Historia | Módulo | Tablas principales | Tablas consultadas / catálogos |
|--------|----------|--------|---------------------|---------------------------------|
| HU-FASE1-008 | Creación de tercero | Terceros y pacientes | `tercero`, `auditoria` | `tipo_tercero`, `tipo_documento`, `sexo`, `genero`, `orientacion_sexual`, `identidad_genero`, `estado_civil`, `nivel_escolaridad`, `ocupacion`, `pertenencia_etnica`, `pais`, `municipio` |
| HU-FASE1-009 | Consulta y búsqueda de tercero | Terceros y pacientes | — (solo lectura) | `tercero`, `paciente`, `contacto_tercero`, `direccion_tercero` |
| HU-FASE1-010 | Actualización de tercero | Terceros y pacientes | `tercero`, `auditoria` | Mismos catálogos que HU-FASE1-008 |
| HU-FASE1-011 | Creación de paciente | Terceros y pacientes | `paciente`, `auditoria` | `tercero`, `grupo_sanguineo`, `factor_rh`, `discapacidad`, `grupo_atencion` |
| HU-FASE1-012 | Contactos del tercero | Terceros y pacientes | `contacto_tercero`, `auditoria` | `tercero`, `tipo_contacto` |
| HU-FASE1-013 | Direcciones del tercero | Terceros y pacientes | `direccion_tercero`, `auditoria` | `tercero`, `zona_residencia`, `pais`, `departamento`, `municipio` |
| HU-FASE1-014 | Relaciones entre terceros | Terceros y pacientes | `relacion_tercero`, `auditoria` | `tercero`, `tipo_relacion` |
| HU-FASE1-015 | SISBEN del paciente | Terceros y pacientes | `sisben_paciente`, `auditoria` | `paciente`, `grupo_sisben` |
| HU-FASE1-016 | Seguridad social del paciente | Terceros y pacientes | `seguridad_social_paciente`, `auditoria` | `paciente`, `pagador`, `regimen`, `categoria_afiliacion`, `tipo_afiliacion`, `tercero` |
| HU-FASE1-017 | Contratos del paciente | Terceros y pacientes | `contrato_paciente`, `auditoria` | `paciente`, `contrato` |

### Bloque 3 — Admisiones

| Código | Historia | Módulo | Tablas principales | Tablas consultadas / catálogos |
|--------|----------|--------|---------------------|---------------------------------|
| HU-FASE1-018 | Registro de admisión | Admisiones | `admision`, `auditoria` | `paciente`, `tipo_admision`, `estado_admision`, `origen_atencion`, `sede`, `pagador`, `contrato`, `tercero` |
| HU-FASE1-019 | Apertura automática de atención | Admisiones | `atencion` | `admision`, `estado_atencion`, `finalidad_atencion` |
| HU-FASE1-020 | Consulta de admisiones activas | Admisiones | — (solo lectura) | `admision`, `paciente`, `tercero`, `pagador`, `sede` |
| HU-FASE1-021 | Cambio de estado de admisión | Admisiones | `admision`, `auditoria` | `estado_admision` |
| HU-FASE1-022 | Egreso administrativo | Admisiones | `admision`, `auditoria` | `atencion` |

### Bloque 4 — Triage y urgencias

| Código | Historia | Módulo | Tablas principales | Tablas consultadas / catálogos |
|--------|----------|--------|---------------------|---------------------------------|
| HU-FASE1-023 | Registro de triage | Triage y urgencias | `atencion`, `auditoria` | `admision`, `paciente` |
| HU-FASE1-024 | Reclasificación de triage | Triage y urgencias | `atencion`, `auditoria` | `admision` |
| HU-FASE1-025 | Cola de urgencias | Triage y urgencias | — (solo lectura) | `admision`, `atencion`, `paciente`, `tercero` |
| HU-FASE1-026 | Consola de atención de urgencias | Triage y urgencias | `atencion`, `auditoria` | `admision`, `paciente`, `tercero`, `diagnostico_atencion`, `orden_clinica`, `prescripcion` |
| HU-FASE1-027 | Diagnósticos en atención | Triage y urgencias | `diagnostico_atencion`, `auditoria` | `atencion`, `catalogo_diagnostico` |
| HU-FASE1-028 | Órdenes clínicas | Triage y urgencias | `orden_clinica`, `detalle_orden_clinica`, `auditoria` | `atencion`, `paciente`, `servicio_salud`, `catalogo_diagnostico`, `tipo_orden_clinica`, `estado_orden` |
| HU-FASE1-029 | Prescripción | Triage y urgencias | `prescripcion`, `detalle_prescripcion`, `auditoria` | `atencion`, `paciente`, `servicio_salud`, `via_administracion`, `frecuencia_dosis`, `estado_prescripcion` |
| HU-FASE1-030 | Conducta y cierre | Triage y urgencias | `atencion`, `auditoria` | `diagnostico_atencion` |

### Bloque 5 — Hospitalización

| Código | Historia | Módulo | Tablas principales | Tablas consultadas / catálogos |
|--------|----------|--------|---------------------|---------------------------------|
| HU-FASE1-031 | Solicitud de hospitalización | Hospitalización | `admision`, `atencion`, `auditoria` | `estado_admision`, `diagnostico_atencion` |
| HU-FASE1-032 | Ingreso hospitalario | Hospitalización | `atencion`, `admision`, `auditoria` | `recurso_fisico`, `profesional_salud` |
| HU-FASE1-033 | Nota de ingreso | Hospitalización | `atencion`, `auditoria` | — |
| HU-FASE1-034 | Evolución hospitalaria | Hospitalización | `atencion`, `auditoria` | `admision` |
| HU-FASE1-035 | Órdenes activas | Hospitalización | `orden_clinica`, `detalle_orden_clinica`, `auditoria` | `atencion`, `admision` |
| HU-FASE1-036 | Egreso hospitalario | Hospitalización | `atencion`, `admision`, `diagnostico_atencion`, `auditoria` | `recurso_fisico` |

### Bloque 6 — Citas

| Código | Historia | Módulo | Tablas principales | Tablas consultadas / catálogos |
|--------|----------|--------|---------------------|---------------------------------|
| HU-FASE1-037 | Calendarios | Citas | `calendario_cita`, `detalle_calendario_cita`, `auditoria` | — |
| HU-FASE1-038 | Recursos físicos | Citas | `recurso_fisico`, `auditoria` | `sede` |
| HU-FASE1-039 | Agenda del profesional | Citas | `agenda_profesional`, `bloque_agenda`, `auditoria` | `profesional_salud`, `especialidad`, `sede`, `recurso_fisico`, `calendario_cita`, `estado_agenda` |
| HU-FASE1-040 | Generación de disponibilidad | Citas | `disponibilidad_cita` | `agenda_profesional`, `bloque_agenda`, `calendario_cita`, `detalle_calendario_cita`, `estado_disponibilidad` |
| HU-FASE1-041 | Asignación de cita | Citas | `cita`, `disponibilidad_cita`, `auditoria` | `paciente`, `agenda_profesional`, `servicio_salud`, `tipo_cita`, `estado_cita`, `especialidad` |
| HU-FASE1-042 | Cancelación y reprogramación | Citas | `cita`, `disponibilidad_cita`, `auditoria` | `motivo_cancelacion_cita`, `motivo_reprogramacion_cita` |
| HU-FASE1-043 | Lista de espera | Citas | `lista_espera_cita`, `auditoria` | `paciente`, `especialidad`, `servicio_salud` |
| HU-FASE1-044 | Traslado masivo de agenda | Citas | `traslado_agenda`, `detalle_traslado_agenda`, `cita`, `disponibilidad_cita`, `auditoria` | `agenda_profesional` |

### Bloque 7 — Servicios, pagadores y contratos

| Código | Historia | Módulo | Tablas principales | Tablas consultadas / catálogos |
|--------|----------|--------|---------------------|---------------------------------|
| HU-FASE1-045 | Catálogo de servicios | Servicios, pagadores y contratos | `servicio_salud`, `auditoria` | `categoria_servicio_salud`, `centro_costo` |
| HU-FASE1-046 | Centros de costo | Servicios, pagadores y contratos | `centro_costo`, `auditoria` | — |
| HU-FASE1-047 | Registro de pagador | Servicios, pagadores y contratos | `pagador`, `auditoria` | `tercero`, `tipo_pagador`, `tipo_cliente` |
| HU-FASE1-048 | Registro de contrato | Servicios, pagadores y contratos | `contrato`, `auditoria` | `pagador`, `modalidad_pago`, `tarifario` |
| HU-FASE1-049 | Tarifario base | Servicios, pagadores y contratos | `tarifario`, `detalle_tarifario`, `auditoria` | `servicio_salud` |
| HU-FASE1-050 | Tarifas por contrato | Servicios, pagadores y contratos | `tarifa_contrato`, `auditoria` | `contrato`, `servicio_salud` |
| HU-FASE1-051 | Servicios del contrato | Servicios, pagadores y contratos | `servicio_contrato`, `auditoria` | `contrato`, `servicio_salud` |

### Bloque 8 — Facturación inicial

| Código | Historia | Módulo | Tablas principales | Tablas consultadas / catálogos |
|--------|----------|--------|---------------------|---------------------------------|
| HU-FASE1-052 | Generación de factura | Facturación inicial | `factura`, `detalle_factura`, `auditoria` | `admision`, `atencion`, `paciente`, `pagador`, `contrato`, `servicio_salud`, `tarifa_contrato`, `detalle_tarifario`, `estado_factura` |
| HU-FASE1-053 | Detalle de factura | Facturación inicial | `detalle_factura`, `factura`, `auditoria` | `servicio_salud` |
| HU-FASE1-054 | Cálculo de copago y cuota moderadora | Facturación inicial | `detalle_factura`, `factura` | `paciente`, `seguridad_social_paciente`, `sisben_paciente`, `regimen`, `grupo_sisben` |
| HU-FASE1-055 | Generación RIPS | Facturación inicial | `rips_encabezado`, `rips_detalle`, `auditoria` | `factura`, `detalle_factura`, `atencion`, `diagnostico_atencion`, `paciente`, `pagador` |
| HU-FASE1-056 | Radicación | Facturación inicial | `radicacion`, `factura`, `auditoria` | `pagador`, `estado_radicacion` |
| HU-FASE1-057 | Cuenta por cobrar automática | Facturación inicial | `cuenta_por_cobrar`, `movimiento_cuenta_por_cobrar` | `factura`, `pagador`, `estado_cartera` |
| HU-FASE1-058 | Consulta de cartera | Facturación inicial | — (solo lectura) | `cuenta_por_cobrar`, `movimiento_cuenta_por_cobrar`, `factura`, `pagador` |

---

## 2. Matriz inversa: Tabla → Historias que la impactan

Esta vista es útil para estimación y análisis de impacto: al tocar una tabla, sabes qué historias pueden verse afectadas.

### Núcleo de personas
- **tercero**: HU-FASE1-008, 009, 010, 011, 012, 013, 014, 016, 018, 006, 047
- **paciente**: HU-FASE1-011, 015, 016, 017, 018, 020, 023, 025, 026, 028, 029, 032, 041, 043, 052
- **contacto_tercero**: HU-FASE1-012, 009
- **direccion_tercero**: HU-FASE1-013, 009
- **relacion_tercero**: HU-FASE1-014
- **sisben_paciente**: HU-FASE1-015, 054
- **seguridad_social_paciente**: HU-FASE1-016, 054
- **contrato_paciente**: HU-FASE1-017

### Flujo asistencial
- **admision**: HU-FASE1-018, 019, 020, 021, 022, 023, 025, 031, 032, 033, 034, 036, 052
- **atencion**: HU-FASE1-019, 022, 023, 024, 025, 026, 027, 028, 029, 030, 031, 032, 033, 034, 035, 036, 052
- **diagnostico_atencion**: HU-FASE1-027, 028, 031, 036, 055
- **orden_clinica** / **detalle_orden_clinica**: HU-FASE1-028, 035
- **prescripcion** / **detalle_prescripcion**: HU-FASE1-029
- **catalogo_diagnostico**: HU-FASE1-027, 028

### Citas
- **calendario_cita** / **detalle_calendario_cita**: HU-FASE1-037, 040
- **recurso_fisico**: HU-FASE1-032, 036, 038, 039
- **agenda_profesional** / **bloque_agenda**: HU-FASE1-039, 040, 041, 044
- **disponibilidad_cita**: HU-FASE1-040, 041, 042, 044
- **cita**: HU-FASE1-041, 042, 044
- **lista_espera_cita**: HU-FASE1-043
- **traslado_agenda** / **detalle_traslado_agenda**: HU-FASE1-044

### Servicios y contratos
- **servicio_salud**: HU-FASE1-028, 029, 041, 043, 045, 049, 050, 051, 052, 053
- **centro_costo**: HU-FASE1-045, 046
- **pagador**: HU-FASE1-016, 018, 047, 048, 052, 055, 056, 057, 058
- **contrato**: HU-FASE1-017, 018, 048, 050, 051, 052
- **tarifario** / **detalle_tarifario**: HU-FASE1-048, 049, 052
- **tarifa_contrato**: HU-FASE1-050, 052
- **servicio_contrato**: HU-FASE1-051

### Facturación y cartera
- **factura** / **detalle_factura**: HU-FASE1-052, 053, 054, 055, 056, 057, 058
- **rips_encabezado** / **rips_detalle**: HU-FASE1-055
- **radicacion**: HU-FASE1-056
- **cuenta_por_cobrar** / **movimiento_cuenta_por_cobrar**: HU-FASE1-057, 058

### Seguridad
- **sede**: HU-FASE1-001, 002, 004, 018, 020, 038, 039
- **servicio_habilitado**: HU-FASE1-002
- **profesional_salud**: HU-FASE1-006, 028, 029, 032, 039
- **usuario** / **usuario_rol**: HU-FASE1-004, 005, 006
- **rol** / **permiso** / **rol_permiso**: HU-FASE1-003, 004, 005
- **auditoria**: transversal a todas las historias

---

## 3. Matriz de dependencias entre historias

Grafo de dependencias críticas (para planificación de sprints).

| Historia | Depende de |
|----------|------------|
| HU-FASE1-001 | — |
| HU-FASE1-002 | HU-FASE1-001 |
| HU-FASE1-003 | — |
| HU-FASE1-004 | HU-FASE1-001, HU-FASE1-003 |
| HU-FASE1-005 | HU-FASE1-004 |
| HU-FASE1-006 | HU-FASE1-008 |
| HU-FASE1-007 | HU-FASE1-004, HU-FASE1-005 |
| HU-FASE1-008 | Catálogos cargados |
| HU-FASE1-009 | HU-FASE1-008 |
| HU-FASE1-010 | HU-FASE1-008, HU-FASE1-009 |
| HU-FASE1-011 | HU-FASE1-008 |
| HU-FASE1-012 | HU-FASE1-008 |
| HU-FASE1-013 | HU-FASE1-008 |
| HU-FASE1-014 | HU-FASE1-008 |
| HU-FASE1-015 | HU-FASE1-011 |
| HU-FASE1-016 | HU-FASE1-011, HU-FASE1-047 |
| HU-FASE1-017 | HU-FASE1-011, HU-FASE1-048 |
| HU-FASE1-018 | HU-FASE1-001, HU-FASE1-011, HU-FASE1-016, HU-FASE1-047, HU-FASE1-048 |
| HU-FASE1-019 | HU-FASE1-018 |
| HU-FASE1-020 | HU-FASE1-018 |
| HU-FASE1-021 | HU-FASE1-018 |
| HU-FASE1-022 | HU-FASE1-018, HU-FASE1-019, HU-FASE1-030, HU-FASE1-036 |
| HU-FASE1-023 | HU-FASE1-018, HU-FASE1-019 |
| HU-FASE1-024 | HU-FASE1-023 |
| HU-FASE1-025 | HU-FASE1-023 |
| HU-FASE1-026 | HU-FASE1-023, HU-FASE1-025, HU-FASE1-027, HU-FASE1-028, HU-FASE1-029 |
| HU-FASE1-027 | HU-FASE1-026 (catálogo CIE-10) |
| HU-FASE1-028 | HU-FASE1-026, HU-FASE1-027, HU-FASE1-045 |
| HU-FASE1-029 | HU-FASE1-026, HU-FASE1-045 |
| HU-FASE1-030 | HU-FASE1-026, HU-FASE1-027 |
| HU-FASE1-031 | HU-FASE1-026, HU-FASE1-030 |
| HU-FASE1-032 | HU-FASE1-031 |
| HU-FASE1-033 | HU-FASE1-032 |
| HU-FASE1-034 | HU-FASE1-033 |
| HU-FASE1-035 | HU-FASE1-028, HU-FASE1-032 |
| HU-FASE1-036 | HU-FASE1-032, HU-FASE1-033, HU-FASE1-034 |
| HU-FASE1-037 | — |
| HU-FASE1-038 | HU-FASE1-001 |
| HU-FASE1-039 | HU-FASE1-006, HU-FASE1-037, HU-FASE1-038 |
| HU-FASE1-040 | HU-FASE1-037, HU-FASE1-039 |
| HU-FASE1-041 | HU-FASE1-011, HU-FASE1-040, HU-FASE1-045 |
| HU-FASE1-042 | HU-FASE1-041 |
| HU-FASE1-043 | HU-FASE1-011 |
| HU-FASE1-044 | HU-FASE1-041 |
| HU-FASE1-045 | HU-FASE1-046 |
| HU-FASE1-046 | — |
| HU-FASE1-047 | HU-FASE1-008 |
| HU-FASE1-048 | HU-FASE1-047, HU-FASE1-049 |
| HU-FASE1-049 | HU-FASE1-045 |
| HU-FASE1-050 | HU-FASE1-048, HU-FASE1-049 |
| HU-FASE1-051 | HU-FASE1-048 |
| HU-FASE1-052 | HU-FASE1-022, HU-FASE1-048, HU-FASE1-049, HU-FASE1-050, HU-FASE1-051 |
| HU-FASE1-053 | HU-FASE1-052 |
| HU-FASE1-054 | HU-FASE1-015, HU-FASE1-016, HU-FASE1-052 |
| HU-FASE1-055 | HU-FASE1-052 |
| HU-FASE1-056 | HU-FASE1-052 |
| HU-FASE1-057 | HU-FASE1-052 |
| HU-FASE1-058 | HU-FASE1-057 |

---

## 4. Propuesta de agrupación en sprints

A partir de las dependencias, se propone la siguiente distribución (sprint de 2 semanas, capacidad promedio de ~10 historias completas).

### Sprint 1 — Fundamentos técnicos y seguridad
- HU-FASE1-001 Sedes
- HU-FASE1-003 Roles y permisos
- HU-FASE1-004 Usuarios
- HU-FASE1-005 Autenticación
- HU-FASE1-007 Auditoría
- HU-FASE1-046 Centros de costo
- HU-FASE1-037 Calendarios

### Sprint 2 — Personas y estructura maestra
- HU-FASE1-008 Creación de tercero
- HU-FASE1-009 Consulta de tercero
- HU-FASE1-010 Actualización de tercero
- HU-FASE1-011 Creación de paciente
- HU-FASE1-012 Contactos
- HU-FASE1-013 Direcciones
- HU-FASE1-006 Profesional de salud
- HU-FASE1-002 Servicios habilitados
- HU-FASE1-038 Recursos físicos
- HU-FASE1-045 Catálogo de servicios

### Sprint 3 — Pagadores, contratos y tarifas
- HU-FASE1-014 Relaciones entre terceros
- HU-FASE1-015 SISBEN
- HU-FASE1-047 Pagadores
- HU-FASE1-049 Tarifario base
- HU-FASE1-048 Contratos
- HU-FASE1-050 Tarifas por contrato
- HU-FASE1-051 Servicios del contrato
- HU-FASE1-016 Seguridad social
- HU-FASE1-017 Contratos del paciente

### Sprint 4 — Admisiones y triage
- HU-FASE1-018 Registro de admisión
- HU-FASE1-019 Apertura automática de atención
- HU-FASE1-020 Consulta de admisiones
- HU-FASE1-021 Cambio de estado
- HU-FASE1-022 Egreso administrativo
- HU-FASE1-023 Triage
- HU-FASE1-024 Reclasificación
- HU-FASE1-025 Cola de urgencias

### Sprint 5 — Atención clínica de urgencias
- HU-FASE1-026 Consola de urgencias
- HU-FASE1-027 Diagnósticos
- HU-FASE1-028 Órdenes clínicas
- HU-FASE1-029 Prescripción
- HU-FASE1-030 Conducta y cierre
- HU-FASE1-031 Solicitud de hospitalización

### Sprint 6 — Hospitalización y citas
- HU-FASE1-032 Ingreso hospitalario
- HU-FASE1-033 Nota de ingreso
- HU-FASE1-034 Evolución
- HU-FASE1-035 Órdenes activas
- HU-FASE1-036 Egreso hospitalario
- HU-FASE1-039 Agenda profesional
- HU-FASE1-040 Disponibilidad
- HU-FASE1-041 Asignación de cita
- HU-FASE1-042 Cancelación y reprogramación
- HU-FASE1-043 Lista de espera
- HU-FASE1-044 Traslado de agenda

### Sprint 7 — Facturación y cartera
- HU-FASE1-052 Generación de factura
- HU-FASE1-053 Detalle de factura
- HU-FASE1-054 Copago y cuota moderadora
- HU-FASE1-055 RIPS
- HU-FASE1-056 Radicación
- HU-FASE1-057 Cuenta por cobrar
- HU-FASE1-058 Consulta de cartera

---

## 5. Matriz de cobertura por rol

Verificación de que cada rol del sistema tenga al menos una historia que lo contemple como actor principal.

| Rol | Historias como actor principal |
|-----|--------------------------------|
| Administrador del sistema | HU-FASE1-001, 002, 003, 004, 006, 007, 037, 038, 045, 046, 047, 048, 049, 050, 051 |
| Recepcionista / Auxiliar de admisiones | HU-FASE1-008, 009, 010, 011, 012, 013, 014, 015, 016, 017, 018, 020, 021, 022, 041, 042, 043 |
| Profesional de triage | HU-FASE1-023, 024 |
| Médico general de urgencias | HU-FASE1-025, 026, 027, 028, 029, 030, 031 |
| Médico hospitalario | HU-FASE1-032, 033, 034, 035, 036, 027, 028, 029 |
| Coordinador asistencial | HU-FASE1-020, 025 |
| Coordinador de agendas | HU-FASE1-037, 039, 040, 043, 044 |
| Facturador / Radicador | HU-FASE1-045, 047, 048, 049, 050, 051, 052, 053, 055, 056 |
| Cartera / Coordinador financiero | HU-FASE1-058 |
| Auditor | HU-FASE1-007, 055 |
| Contador | HU-FASE1-046 |
| Todos los usuarios | HU-FASE1-005, 009 |
| Sistema (automático) | HU-FASE1-019, 040, 054, 057 |

---

## 6. Consideraciones finales

**Datos semilla requeridos antes del arranque**:
- Catálogos geográficos (Colombia: 32 departamentos, ~1.100 municipios) — fuente DANE.
- Catálogo CIE-10 (~14.000 códigos) — fuente Ministerio de Salud.
- Catálogo CUPS (Clasificación Única de Procedimientos en Salud) — fuente Ministerio de Salud.
- Catálogo de EPS, ARL y SOAT vigentes — fuente Supersalud.
- Tarifarios oficiales (SOAT, ISS 2001, ISS 2004) para referencia.
- Catálogos internos: tipos de documento, sexo, género, estado civil, regímenes, etc.

**Cobertura de los objetivos del agente**:
- ✓ Núcleo operativo cubierto.
- ✓ Consola de urgencias (HU-FASE1-026) cubierta.
- ✓ Consola de hospitalización (HU-FASE1-032 a 036) cubierta.
- ✓ Vista de asignación de citas (HU-FASE1-041) cubierta.
- ✓ Separación tercero ↔ paciente ↔ profesional ↔ pagador respetada.
- ✓ Trazabilidad (auditoría) presente desde el diseño.
- ✓ Facturación y RIPS cubiertos en modalidad base.

**Próximas fases sugeridas**:
- Fase 2: historia clínica estructurada avanzada, escalas clínicas, antecedentes detallados, notas de enfermería, interconsultas, farmacia/dispensación, laboratorio con resultados, imágenes con DICOM.
- Fase 3: facturación electrónica DIAN, autorizaciones y MIPRES, interoperabilidad HL7/FHIR, portal del paciente.
- Fase 4: indicadores y tableros gerenciales, IA para apoyo clínico, análisis de cartera predictivo.
