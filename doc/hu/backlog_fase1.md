# Backlog de Historias de Usuario — Fase 1 (v2 Multi-Tenant)
## Sistema de Gestión Hospitalaria (SGH)

**Versión**: 2.0 (reemplaza backlog_fase1.md y su complemento)
**Arquitectura**: Multi-tenant estricto (empresa + sede aislados)
**Modelo de datos**: 110 tablas en esquema `sgh` (v2)
**Autenticación**: JWT en 3 pasos (pre-auth → login → select-sede)

---

## Tabla de contenido

1. [Inventario de necesidades de fase 1](#1-inventario-de-necesidades-de-fase-1)
2. [Mapa de módulos de fase 1](#2-mapa-de-módulos-de-fase-1)
3. [Principios de arquitectura multi-tenant](#3-principios-de-arquitectura-multi-tenant)
4. [Lista priorizada de historias de usuario](#4-lista-priorizada-de-historias-de-usuario)
5. [Bloque 0. Multi-tenant y autenticación](#bloque-0-multi-tenant-y-autenticación)
6. [Bloque 1. Seguridad y estructura base](#bloque-1-seguridad-y-estructura-base)
7. [Bloque 2. Terceros y pacientes](#bloque-2-terceros-y-pacientes)
8. [Bloque 3. Admisiones](#bloque-3-admisiones)
9. [Bloque 4. Triage y urgencias](#bloque-4-triage-y-urgencias)
10. [Bloque 5. Hospitalización](#bloque-5-hospitalización)
11. [Bloque 6. Citas](#bloque-6-citas)
12. [Bloque 7. Servicios, pagadores y contratos](#bloque-7-servicios-pagadores-y-contratos)
13. [Bloque 8. Facturación inicial](#bloque-8-facturación-inicial)

---

## 1. Inventario de necesidades de fase 1

La fase 1 debe dejar operativo el núcleo mínimo para que múltiples instituciones prestadoras de servicios de salud puedan operar sobre una misma plataforma con aislamiento total de sus datos, cubriendo:

- **Administración de tenants** (empresas) y sus sedes.
- **Autenticación multi-tenant** (pre-auth de empresa, login, selección de sede).
- **Identificación y gestión** de pacientes y demás terceros.
- **Admisión** por urgencias, consulta externa u hospitalización.
- **Triage** y atención clínica en urgencias.
- **Hospitalización básica** con nota de ingreso, evolución y egreso.
- **Asignación de citas** de consulta externa.
- **Diagnósticos, órdenes y prescripciones** estructurados.
- **Catálogos** de servicios de salud, pagadores y contratos.
- **Facturación inicial** y preparación para RIPS.
- **Seguridad, permisos y auditoría** por empresa y sede.

**Lo que NO entra en fase 1**:

- Historia clínica avanzada (escalas, instrumentos).
- Quirófanos y programación quirúrgica.
- Enfermería avanzada, farmacia/dispensación, laboratorio con integración de resultados.
- Contabilidad completa (solo CxC básica).
- Interoperabilidad HL7/FHIR.
- App móvil del paciente.
- Biometría, foto y SSO.

---

## 2. Mapa de módulos de fase 1

| # | Módulo | Submódulos cubiertos |
|---|--------|----------------------|
| 0 | **Multi-tenant y autenticación** | empresas, pre-auth, login, selección de sede, refresh, logout, cambio de contraseña |
| 1 | Seguridad y estructura base | sedes, servicios habilitados, roles, permisos, usuarios, profesionales, auditoría |
| 2 | Terceros y pacientes | tercero, paciente, contactos, direcciones, relaciones, SISBEN, seguridad social, contratos del paciente |
| 3 | Admisiones | registro, apertura automática de atención, estado, egreso administrativo |
| 4 | Triage y urgencias | triage, cola de urgencias, consola de atención, diagnóstico, órdenes, prescripción, conducta |
| 5 | Hospitalización | solicitud, ingreso, nota de ingreso, evolución, órdenes activas, egreso básico |
| 6 | Citas | calendarios, recursos, agendas, disponibilidad, asignación, lista de espera, traslado |
| 7 | Servicios, pagadores y contratos | catálogo de servicios, centros de costo, pagadores, contratos, tarifas |
| 8 | Facturación inicial | factura, detalle, base RIPS, radicación, cuentas por cobrar |

---

## 3. Principios de arquitectura multi-tenant

Estos principios son **transversales** a TODAS las historias de usuario de este backlog. No se repiten en cada HU, pero se dan por aplicados.

### 3.1 Aislamiento total de datos
- Toda tabla transaccional contiene `empresa_id`. Las consultas siempre filtran por `empresa_id = TenantContext.getEmpresaId()`.
- Las tablas operativas (admisión, atención, cita, factura, orden, prescripción, recurso físico, servicio habilitado, radicación, pago) también filtran por `sede_id`.
- Ningún usuario de una empresa puede ver, listar, buscar ni modificar datos de otra empresa. Ninguna sede puede ver datos de otra sede en el ámbito operativo.
- Los catálogos globales (tipos de documento, sexo, CIE-10, países, municipios, etc.) son compartidos entre empresas.

### 3.2 Origen del contexto
- `empresa_id`, `sede_id` y `usuario_id` se extraen **exclusivamente** del JWT (`TenantContext`).
- Los DTOs de request **nunca** aceptan `companyId` ni `branchId`. Si los recibieran, se ignoran o se rechazan.
- Al crear un registro, `empresa_id`, `sede_id` (si aplica), `usuario_creacion` se setean desde `TenantContext`.

### 3.3 Validación cruzada
- Todo `findById`, `update`, `delete` aplica `WHERE id = :id AND empresa_id = :empresa_id`.
- Intentar operar sobre registros de otra empresa retorna `HTTP 404` (no 403, para no confirmar existencia).

### 3.4 Auditoría
- Cada inserción, actualización, eliminación y login queda en la tabla `auditoria` con `empresa_id`, `sede_id`, `usuario_id`, `ip_origen`, `datos_antes`, `datos_despues`.
- Cada intento de autenticación (exitoso o fallido) queda en `intento_autenticacion`.
- Cada token emitido queda en `sesion_usuario` con su JTI para permitir revocación.

### 3.5 Criterios de aceptación transversales
Todas las HUs incluyen implícitamente estos criterios de aceptación de aislamiento:

- **CA-T1**: La operación solo afecta registros de la empresa del usuario autenticado.
- **CA-T2**: El usuario no puede ver, listar ni buscar registros de otras empresas.
- **CA-T3**: En módulos operativos, el usuario solo ve registros de su sede activa (la seleccionada al login).
- **CA-T4**: Intentar acceder por ID a un registro de otra empresa retorna 404.
- **CA-T5**: Todo evento queda auditado con `empresa_id`, `sede_id`, `usuario_id`.

---

## 4. Lista priorizada de historias de usuario

Prioridad: A=Alta, M=Media, B=Baja.

### Bloque 0 — Multi-tenant y autenticación
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE1-000 | Gestión de empresas (tenants) | A |
| HU-FASE1-001A | Pre-autenticación de empresa | A |
| HU-FASE1-001B | Login con credenciales | A |
| HU-FASE1-001C | Selección de sede y emisión del JWT final | A |
| HU-FASE1-001D | Refresh del access token | A |
| HU-FASE1-001E | Logout y revocación de tokens | A |
| HU-FASE1-001F | Cambio de contraseña | A |

### Bloque 1 — Seguridad y estructura base
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE1-002 | Gestión de sedes de la empresa | A |
| HU-FASE1-003 | Gestión de servicios habilitados por sede | A |
| HU-FASE1-004 | Gestión de permisos del sistema (catálogo global) | A |
| HU-FASE1-005 | Gestión de roles por empresa | A |
| HU-FASE1-006 | Gestión de usuarios de la empresa | A |
| HU-FASE1-007 | Asignación de roles y sedes al usuario | A |
| HU-FASE1-008 | Registro de profesional de salud | A |
| HU-FASE1-009 | Consulta de auditoría del sistema | M |

### Bloque 2 — Terceros y pacientes
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE1-010 | Creación de tercero | A |
| HU-FASE1-011 | Consulta y búsqueda de tercero | A |
| HU-FASE1-012 | Actualización de tercero | A |
| HU-FASE1-013 | Creación de paciente a partir de tercero | A |
| HU-FASE1-014 | Gestión de contactos del tercero | A |
| HU-FASE1-015 | Gestión de direcciones del tercero | A |
| HU-FASE1-016 | Gestión de relaciones entre terceros | M |
| HU-FASE1-017 | Registro de SISBEN del paciente | M | ✅ |
| HU-FASE1-018 | Registro de seguridad social del paciente | A | ✅ |
| HU-FASE1-019 | Asociación de contratos al paciente | A | ✅ |
| HU-FASE1-019A | Verificación de derechos y cobertura del paciente | A | ✅ |

### Bloque 3 — Admisiones
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE1-020 | Registro de admisión | A |
| HU-FASE1-021 | Apertura automática de atención al admitir | A |
| HU-FASE1-022 | Consulta de admisiones activas de la sede | A |
| HU-FASE1-023 | Cambio de estado de admisión | A |
| HU-FASE1-024 | Registro de egreso administrativo de admisión | M |

### Bloque 4 — Triage y urgencias
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE1-025 | Registro de triage de urgencias | A |
| HU-FASE1-026 | Reclasificación de triage | M |
| HU-FASE1-027 | Visualización de cola de urgencias de la sede | A |
| HU-FASE1-028 | Consola de atención médica de urgencias | A |
| HU-FASE1-029 | Registro de diagnósticos en la atención | A |
| HU-FASE1-030 | Generación de órdenes clínicas desde la atención | A |
| HU-FASE1-031 | Generación de prescripción desde la atención | A |
| HU-FASE1-032 | Definición de conducta y cierre de atención de urgencias | A |

### Bloque 5 — Hospitalización
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE1-033 | Solicitud de hospitalización desde urgencias | A |
| HU-FASE1-034 | Ingreso hospitalario por médico tratante | A |
| HU-FASE1-035 | Nota de ingreso hospitalario | A |
| HU-FASE1-036 | Registro de evolución hospitalaria | A |
| HU-FASE1-037 | Visualización de órdenes activas del paciente hospitalizado | M |
| HU-FASE1-038 | Egreso hospitalario básico | A |

### Bloque 6 — Citas
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE1-039 | Gestión de calendarios de citas | A |
| HU-FASE1-040 | Gestión de recursos físicos | M |
| HU-FASE1-041 | Creación de agenda de profesional | A |
| HU-FASE1-042 | Generación de disponibilidad de cita | A |
| HU-FASE1-043 | Asignación de cita a paciente | A |
| HU-FASE1-044 | Cancelación y reprogramación de cita | A |
| HU-FASE1-045 | Gestión de lista de espera | M |
| HU-FASE1-046 | Traslado masivo de agenda | M |

### Bloque 7 — Servicios, pagadores y contratos
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE1-047 | Gestión del catálogo de servicios de salud | A | ✅ |
| HU-FASE1-048 | Gestión de centros de costo | M | ✅ |
| HU-FASE1-049 | Registro de pagador | A | ✅ |
| HU-FASE1-050 | Registro de contrato con pagador | A | ✅ |
| HU-FASE1-051 | Carga de tarifario base | A | ✅ |
| HU-FASE1-052 | Registro de tarifas específicas por contrato | A | ✅ |
| HU-FASE1-053 | Asignación de servicios al contrato | A | ✅ |

### Bloque 8 — Facturación inicial
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE1-054 | Generación de factura desde atención | A |
| HU-FASE1-055 | Gestión del detalle de factura | A |
| HU-FASE1-056 | Cálculo de copago y cuota moderadora | A |
| HU-FASE1-057 | Generación de estructura base RIPS | A |
| HU-FASE1-058 | Radicación de factura ante pagador | A |
| HU-FASE1-059 | Creación automática de cuenta por cobrar | A |
| HU-FASE1-060 | Consulta de cartera básica por pagador | M |

---

## Bloque 0. Multi-tenant y autenticación

---

### HU-FASE1-000 — Gestión de empresas (tenants)

**Módulo**: Multi-tenant y autenticación
**Actor principal**: Super-administrador del sistema

**Historia de usuario**
Como super-administrador, quiero crear y mantener las empresas (tenants) del sistema, para habilitar el uso de la plataforma a cada organización cliente con aislamiento total de sus datos.

**Objetivo funcional**
Administrar el ciclo de vida de los tenants del SGH, garantizando que cada empresa tenga un aislamiento total de información respecto a las demás.

**Descripción funcional detallada**
1. El super-administrador accede al módulo "Empresas" (solo visible para usuarios con `es_super_admin = true` y `empresa_id = NULL`).
2. Visualiza el listado de empresas con: código, NIT, razón social, estado, fecha de creación, cantidad de sedes, cantidad de usuarios.
3. Puede crear una nueva empresa capturando: código, NIT + dígito de verificación, razón social, nombre comercial, representante legal, teléfono, correo, país/departamento/municipio, dirección, logo URL.
4. Al crear una empresa, el sistema exige (como proceso guiado):
   - Crear al menos una sede inicial (se marca como `es_principal = true`).
   - Crear al menos un usuario administrador de la empresa (rol administrador completo).
5. Puede inactivar una empresa (bloquea el acceso a todos sus usuarios).
6. Puede reactivar una empresa previamente inactiva.
7. No puede eliminar una empresa con movimientos históricos (solo inactivación).

**Reglas de negocio**
- El `codigo` de empresa es único globalmente (usado en pre-autenticación).
- El `nit` es único globalmente.
- Una empresa inactiva bloquea login a todos sus usuarios.
- Solo super-administradores pueden crear, inactivar o reactivar empresas.
- La auditoría de creación/modificación de empresa queda en `auditoria` con `empresa_id = NULL` (acción global).
- El proceso de alta de empresa es transaccional: si falla la creación de la sede principal o del usuario admin, se revierte todo.

**Validaciones**
- Campos obligatorios: código, NIT, razón social, país, departamento, municipio.
- NIT válido con su dígito de verificación.
- Código alfanumérico (3–20 caracteres, sin espacios).
- Correo con formato válido.
- Unicidad de código y NIT.

**Datos involucrados**
- `empresa`
- `sede` (sede principal creada junto con la empresa)
- `usuario` (usuario administrador creado junto con la empresa)
- `rol`, `usuario_rol`
- `pais`, `departamento`, `municipio`
- `auditoria`

**Dependencias**
- Catálogos geográficos cargados.
- Existencia de al menos un super-administrador en la instalación inicial.

**Criterios de aceptación**
- CA1: Puedo crear una empresa y en el mismo flujo registrar su sede principal y usuario administrador.
- CA2: No puedo crear dos empresas con el mismo código ni con el mismo NIT.
- CA3: Una empresa inactiva no permite que sus usuarios hagan pre-auth.
- CA4: Solo veo y accedo a este módulo si soy super-administrador.
- CA5: No puedo eliminar una empresa con movimientos (solo inactivar).
- CA6: La creación es transaccional: un fallo en cualquier paso revierte todo.

**Prioridad**: Alta

**Observaciones**
Esta historia es prerrequisito absoluto para usar el sistema. Debe ejecutarse como primera HU técnica del Sprint 1. El seed inicial del sistema debe incluir al menos un usuario super-administrador con `empresa_id = NULL`.

---

### HU-FASE1-001A — Pre-autenticación de empresa

**Módulo**: Multi-tenant y autenticación
**Actor principal**: Cualquier usuario del sistema (visitante)

**Historia de usuario**
Como usuario del sistema, quiero identificar primero la empresa a la que pertenezco antes de iniciar sesión, para que el sistema me aísle correctamente dentro de mi organización.

**Objetivo funcional**
Implementar el primer paso del flujo de autenticación multi-tenant: resolver el tenant antes de validar credenciales.

**Descripción funcional detallada**
1. El usuario accede a la pantalla de inicio del sistema.
2. Ingresa el `codigo` de la empresa (o `nit`).
3. El sistema valida contra la tabla `empresa`:
   - La empresa existe.
   - Está activa (`activo = true`).
4. Si la validación es correcta, el sistema emite un `pre_auth_token` JWT firmado con:
   - Claim `empresa_id`.
   - Claim `tipo_token = "PRE_AUTH"`.
   - JTI único (uuid v4).
   - Expiración de 5 minutos.
5. El sistema guarda el JTI en `sesion_usuario` con `tipo_token = 'PRE_AUTH'` y `usado = false`.
6. El sistema registra el intento en `intento_autenticacion` con `paso = 'PRE_AUTH'`, IP y `user_agent`.
7. Responde con: `pre_auth_token`, nombre comercial de la empresa, logo URL (para marca blanca), segundos hasta expiración.
8. Si la validación falla, devuelve mensaje genérico `"Empresa no encontrada o inactiva"` sin revelar el detalle.

**Reglas de negocio**
- El `pre_auth_token` no otorga ningún permiso de negocio; solo identifica el tenant.
- El `pre_auth_token` es de un solo uso: al ser consumido en el login se marca `usado = true`.
- Expira automáticamente a los 5 minutos.
- Rate limiting por IP: máximo 10 intentos de pre-auth por IP cada 5 minutos.
- Nunca revelar si la falla fue por código inexistente o empresa inactiva.

**Validaciones**
- Campo obligatorio: `empresa_codigo` (o NIT, excluyentes).
- Longitud del código entre 3 y 20 caracteres.
- Formato válido de NIT si se usa ese identificador.

**Datos involucrados**
- `empresa` (lectura)
- `sesion_usuario` (escritura: token emitido)
- `intento_autenticacion` (escritura: registro del intento)

**Dependencias**
- HU-FASE1-000 (debe existir al menos una empresa activa).

**Criterios de aceptación**
- CA1: Envío el código de empresa válido y recibo un `pre_auth_token` de 5 minutos.
- CA2: Si la empresa no existe o está inactiva, recibo el mismo error genérico.
- CA3: El token contiene únicamente `empresa_id` y `tipo_token = 'PRE_AUTH'`.
- CA4: El intento queda registrado en `intento_autenticacion` con IP y user agent.
- CA5: Un `pre_auth_token` marcado como usado no puede reutilizarse.
- CA6: Al superar 10 intentos fallidos desde una IP en 5 minutos, el sistema bloquea temporalmente esa IP.

**Prioridad**: Alta

**Observaciones**
En fase 2 se puede considerar autocompletado de empresa por subdominio (ej: `hospitalx.sgh.com`).

---

### HU-FASE1-001B — Login con credenciales

**Módulo**: Multi-tenant y autenticación
**Actor principal**: Todos los usuarios del sistema

**Historia de usuario**
Como usuario del sistema, quiero iniciar sesión con mis credenciales dentro del ámbito de una empresa previamente identificada, para acceder a las funcionalidades autorizadas por mi rol.

**Objetivo funcional**
Validar credenciales del usuario en el contexto de la empresa ya identificada y decidir el siguiente paso (emisión directa de JWT final si hay una sola sede, o emisión de `session_token` si hay varias).

**Descripción funcional detallada**
1. El usuario envía el `pre_auth_token` (header `X-Pre-Auth-Token`) junto con `username` y `password`.
2. El sistema valida el `pre_auth_token`: firma, expiración, `tipo_token = 'PRE_AUTH'`, `usado = false`.
3. Extrae `empresa_id` del token.
4. Busca el usuario por `(nombre_usuario, empresa_id)` en `usuario`.
5. Valida estado del usuario:
   - `activo = true`.
   - `bloqueado = false`.
6. Valida la contraseña con BCrypt contra `hash_password`.
7. Si credenciales correctas:
   - Reinicia `intentos_fallidos = 0`, actualiza `fecha_ultimo_ingreso`, `ip_ultimo_ingreso`.
   - Marca el `pre_auth_token` como usado.
   - Si `requiere_cambio_password = true`: emite un `password_change_token` (15 min) y retorna `require_password_change = true`.
   - Si no: carga las sedes disponibles para el usuario en esa empresa desde `usuario_rol`.
   - Si tiene **una sola sede**: genera el JWT final directamente (salta HU-FASE1-001C).
   - Si tiene **varias sedes**: emite un `session_token` (10 min) con `empresa_id`, `usuario_id`, lista de `sedes_disponibles`, y responde la lista para que el usuario elija.
   - Si no tiene sedes asignadas: rechaza con `"Usuario sin sedes asignadas"`.
8. Si falla:
   - Incrementa `intentos_fallidos`.
   - Al alcanzar 5 fallos: marca `bloqueado = true`, `fecha_bloqueo = now()`, `motivo_bloqueo = 'intentos_excedidos'`.
9. Registra el intento en `intento_autenticacion` con el motivo de fallo.

**Reglas de negocio**
- Se requiere `pre_auth_token` válido; sin él rechaza con 401.
- Mensaje de error de credenciales siempre genérico: `"Credenciales inválidas"` (no revelar si el usuario existe).
- Tiempos de respuesta similares entre éxito y fallo (usar BCrypt incluso si el usuario no existe, para evitar timing attacks).
- Al alcanzar 5 intentos fallidos, el usuario queda bloqueado y requiere desbloqueo por administrador.
- Si el usuario debe cambiar contraseña, se emite un token especial limitado a ese propósito.
- Cualquier otro tipo de token (no PRE_AUTH) rechazado en este paso.

**Validaciones**
- Campos obligatorios: `username`, `password`.
- Header `X-Pre-Auth-Token` obligatorio.
- Token no expirado, no usado, tipo correcto.

**Datos involucrados**
- `empresa`, `usuario`, `usuario_rol`, `sede`, `rol`
- `sesion_usuario` (lectura del pre_auth, escritura de session_token)
- `intento_autenticacion`
- `historial_password` (lectura, si se activa políticas)

**Dependencias**
- HU-FASE1-001A, HU-FASE1-006, HU-FASE1-007.

**Criterios de aceptación**
- CA1: Con `pre_auth_token` válido y credenciales correctas recibo `session_token` + lista de sedes o JWT final directo.
- CA2: Sin `pre_auth_token` válido, el login rechaza con 401.
- CA3: Tras 5 intentos fallidos, el usuario queda bloqueado.
- CA4: Si tengo una sola sede, recibo JWT final directamente.
- CA5: Si tengo varias sedes, recibo `session_token` con ellas.
- CA6: Si mi contraseña requiere cambio, recibo `password_change_token` y debo cambiarla antes de seguir.
- CA7: Todo intento queda en `intento_autenticacion` con IP, user agent y motivo de fallo si aplica.
- CA8: El `pre_auth_token` se marca como usado y no puede reutilizarse.

**Prioridad**: Alta

---

### HU-FASE1-001C — Selección de sede y emisión del JWT final

**Módulo**: Multi-tenant y autenticación
**Actor principal**: Usuario con acceso a múltiples sedes

**Historia de usuario**
Como usuario con acceso a varias sedes, quiero elegir la sede en la que voy a trabajar al iniciar sesión, para que el sistema filtre los datos operativos de esa ubicación exclusivamente.

**Objetivo funcional**
Completar el flujo de autenticación emitiendo el JWT final (`access_token` + `refresh_token`) con el contexto completo: empresa + sede + usuario + roles + permisos.

**Descripción funcional detallada**
1. El usuario envía el `session_token` (header `X-Session-Token`) junto con el `sede_id` seleccionada.
2. El sistema valida el `session_token`: firma, expiración, `tipo_token = 'SESSION'`, `usado = false`.
3. Extrae `empresa_id`, `usuario_id`, `sedes_disponibles` del token.
4. Valida que `sede_id` esté en `sedes_disponibles`.
5. Valida que la sede esté activa y pertenezca a la empresa.
6. Carga los roles efectivos del usuario para esa combinación (empresa + sede):
   - Roles con `sede_id = null` aplican a todas las sedes.
   - Roles con `sede_id = X` solo aplican a esa sede.
7. Carga los permisos efectivos (unión de los permisos de todos los roles del usuario).
8. Genera el `access_token` (24h) y `refresh_token` (30 días) con los claims completos.
9. Guarda ambos JTI en `sesion_usuario` con `tipo_token = 'ACCESS'` y `'REFRESH'`.
10. Marca el `session_token` como usado.
11. Registra evento en `auditoria` con `accion = 'LOGIN'`, `empresa_id`, `sede_id`, `usuario_id`, IP, user agent.
12. Retorna los tokens y el objeto usuario con su información básica.

**Reglas de negocio**
- Solo se pueden seleccionar sedes activas de la empresa.
- La sede elegida debe estar en la lista `sedes_disponibles` del `session_token`.
- El `session_token` se quema al emitir el JWT final (uso único).
- El `access_token` contiene `empresa_id`, `sede_id`, `usuario_id`, `username`, `roles`, `permisos`, JTI.
- En fase 1, el cambio de sede requiere logout + nuevo login. En fase 2 se agrega endpoint `switch-sede`.
- Un usuario puede tener múltiples sesiones activas simultáneas en distintos dispositivos.

**Validaciones**
- Header `X-Session-Token` obligatorio.
- `sede_id` obligatorio.
- `sede_id` debe estar en `sedes_disponibles`.
- Sede activa.

**Datos involucrados**
- `sesion_usuario` (lectura session, escritura access + refresh)
- `usuario`, `usuario_rol`, `rol`, `rol_permiso`, `permiso`, `sede`
- `auditoria`

**Dependencias**
- HU-FASE1-001B.

**Criterios de aceptación**
- CA1: Con `session_token` válido y `sede_id` permitida, recibo `access_token` + `refresh_token`.
- CA2: Si `sede_id` no está en mis sedes permitidas, recibo error.
- CA3: El `access_token` contiene todos los claims de contexto.
- CA4: El `session_token` se invalida tras emitir el JWT final.
- CA5: Los JTI de los tokens quedan en `sesion_usuario` para control de revocación.
- CA6: El evento de login exitoso queda auditado con `empresa_id`, `sede_id`, `usuario_id`, IP.

**Prioridad**: Alta

---

### HU-FASE1-001D — Refresh del access token

**Módulo**: Multi-tenant y autenticación
**Actor principal**: Usuario autenticado

**Historia de usuario**
Como usuario autenticado, quiero renovar mi access token sin tener que volver a ingresar credenciales, para mantener una experiencia continua.

**Objetivo funcional**
Permitir la renovación del `access_token` usando el `refresh_token` válido, con rotación opcional del refresh.

**Descripción funcional detallada**
1. El usuario envía el `refresh_token` en el header `Authorization: Bearer <refresh_token>`.
2. El sistema valida firma, expiración, `tipo_token = 'REFRESH'`, no revocado en `sesion_usuario`.
3. Valida que el usuario siga activo y no bloqueado.
4. Genera un nuevo `access_token` con claims actualizados (roles y permisos se recalculan).
5. Opción A: mantiene el refresh existente.
6. Opción B (recomendada): rotación. Emite nuevo `refresh_token` y revoca el anterior.
7. Responde con los nuevos tokens.

**Reglas de negocio**
- Un `refresh_token` revocado no puede usarse.
- Si se detecta uso de un refresh revocado, se revocan todas las sesiones del usuario (posible compromiso).
- La rotación de refresh es la estrategia por defecto.
- El tiempo de vida del refresh no se extiende por el refresh (expira según su `exp` original).

**Validaciones**
- Header `Authorization: Bearer` obligatorio con tipo REFRESH.

**Datos involucrados**
- `sesion_usuario`
- `usuario`

**Dependencias**
- HU-FASE1-001C.

**Criterios de aceptación**
- CA1: Con refresh válido recibo nuevo access token (y nuevo refresh si se usa rotación).
- CA2: Con refresh expirado o revocado, recibo 401 y debo volver a hacer login.
- CA3: El uso de un refresh revocado fuerza revocación de todas mis sesiones.

**Prioridad**: Alta

---

### HU-FASE1-001E — Logout y revocación de tokens

**Módulo**: Multi-tenant y autenticación
**Actor principal**: Usuario autenticado

**Historia de usuario**
Como usuario autenticado, quiero cerrar sesión para revocar mis tokens y terminar mi acceso.

**Objetivo funcional**
Revocar de forma inmediata los tokens asociados a la sesión actual.

**Descripción funcional detallada**
1. El usuario envía el `access_token` en header.
2. El sistema extrae el JTI y marca `fecha_revocacion = now()` en `sesion_usuario` para ese access.
3. Revoca también el refresh token asociado a la misma sesión.
4. Registra evento `LOGOUT` en `auditoria`.
5. Responde con 200 OK.

**Reglas de negocio**
- Un token revocado no puede usarse aunque aún no haya expirado.
- El filtro de autenticación debe consultar `sesion_usuario` para saber si el JTI está revocado.
- Opcional: endpoint `logout-all` para revocar todas las sesiones del usuario (útil tras cambio de contraseña).

**Datos involucrados**
- `sesion_usuario`
- `auditoria`

**Dependencias**
- HU-FASE1-001C.

**Criterios de aceptación**
- CA1: Tras logout, mi access y refresh quedan revocados.
- CA2: El uso de un token revocado retorna 401.
- CA3: El logout queda auditado.

**Prioridad**: Alta

---

### HU-FASE1-001F — Cambio de contraseña

**Módulo**: Multi-tenant y autenticación
**Actor principal**: Usuario autenticado o usuario en primer login

**Historia de usuario**
Como usuario, quiero cambiar mi contraseña de forma segura, ya sea porque es la primera vez que ingreso o porque deseo actualizarla voluntariamente.

**Objetivo funcional**
Proveer un mecanismo seguro de cambio de contraseña respetando políticas y manteniendo historial.

**Descripción funcional detallada**
1. El usuario accede al endpoint `POST /api/auth/change-password`.
2. Casos:
   - **Primer login**: usa el `password_change_token` emitido en HU-FASE1-001B. No requiere contraseña actual.
   - **Voluntario**: usa el `access_token`. Requiere contraseña actual.
3. El sistema valida:
   - Token vigente y de tipo permitido.
   - Nueva contraseña cumple política: mínimo 8 caracteres, una mayúscula, una minúscula, un número, un carácter especial.
   - Nueva contraseña no es igual a ninguna de las últimas 3 (verifica `historial_password`).
   - En caso voluntario: contraseña actual válida.
4. Si es válido:
   - Guarda hash actual en `historial_password` antes de reemplazar.
   - Actualiza `hash_password` en `usuario`.
   - Marca `requiere_cambio_password = false`.
   - Revoca todas las sesiones activas del usuario (forzar re-login con la nueva).
   - Registra evento en `auditoria`.

**Reglas de negocio**
- No se aceptan contraseñas iguales a las últimas 3 del historial.
- El hash se calcula con BCrypt (strength 12).
- Al cambiar contraseña se revocan todas las sesiones activas.
- Un `password_change_token` se consume una sola vez.

**Validaciones**
- Política de contraseña.
- Token válido y no usado.
- Contraseña actual válida (en cambio voluntario).

**Datos involucrados**
- `usuario`
- `historial_password`
- `sesion_usuario` (revocación masiva)
- `auditoria`

**Dependencias**
- HU-FASE1-001B (para primer login) o HU-FASE1-001C (para cambio voluntario).

**Criterios de aceptación**
- CA1: Puedo cambiar mi contraseña cumpliendo la política.
- CA2: No puedo usar una contraseña igual a ninguna de las últimas 3.
- CA3: Al cambiar la contraseña, todas mis sesiones activas se revocan.
- CA4: El historial de contraseñas queda almacenado (hash).
- CA5: En primer login no se requiere contraseña actual, solo el `password_change_token`.

**Prioridad**: Alta

---

## Bloque 1. Seguridad y estructura base

---

### HU-FASE1-002 — Gestión de sedes de la empresa

**Módulo**: Seguridad y estructura base
**Actor principal**: Administrador de la empresa

**Historia de usuario**
Como administrador de la empresa, quiero registrar y mantener las sedes de mi institución, para organizar la operación asistencial, administrativa y financiera por ubicación física.

**Objetivo funcional**
Administrar las sedes físicas de la empresa, garantizando que cada sede quede aislada operativamente del resto.

**Descripción funcional detallada**
1. El administrador accede al módulo "Sedes" (solo ve sedes de su empresa).
2. Visualiza el listado con: código, nombre, municipio, es_principal, activo.
3. Puede crear una sede capturando: código (único dentro de la empresa), código de habilitación REPS, nombre, país/departamento/municipio, dirección, teléfono, correo, es_principal.
4. Puede editar o inactivar sedes.
5. Todos los cambios quedan en auditoría.

**Reglas de negocio**
- Toda sede pertenece a la empresa del usuario autenticado (se setea automáticamente desde `TenantContext`).
- El código de sede es único **dentro de la empresa** (no global).
- Solo una sede por empresa puede ser `es_principal = true` (al marcar otra, la anterior se desmarca).
- Una sede inactiva no puede asociarse a nuevas admisiones, agendas, servicios habilitados.
- No se puede eliminar una sede con movimientos históricos, solo inactivar.
- El administrador nunca puede ver ni editar sedes de otras empresas.

**Validaciones**
- Campos obligatorios: código, nombre, país, departamento, municipio.
- Unicidad `(empresa_id, codigo)`.
- Longitud máxima del nombre: 200.
- Correo con formato válido si se registra.

**Datos involucrados**
- `sede`
- `pais`, `departamento`, `municipio`
- `auditoria`

**Dependencias**
- HU-FASE1-000 (la empresa debe existir).
- Catálogos geográficos cargados.

**Criterios de aceptación**
- CA1: Puedo crear sedes de mi empresa con los campos obligatorios.
- CA2: No puedo crear dos sedes con el mismo código en mi empresa.
- CA3: No puedo ver ni editar sedes de otras empresas.
- CA4: Al marcar una sede como principal, la anterior se desmarca automáticamente.
- CA5: Los cambios quedan auditados con mi `empresa_id` y `usuario_id`.
- Aplican además los criterios transversales CA-T1 a CA-T5.

**Prioridad**: Alta

---

### HU-FASE1-003 — Gestión de servicios habilitados por sede

**Módulo**: Seguridad y estructura base
**Actor principal**: Administrador de la empresa

**Historia de usuario**
Como administrador, quiero registrar los servicios de salud habilitados en el REPS de cada sede, para que el sistema solo permita prestar servicios en sedes donde están autorizados.

**Objetivo funcional**
Garantizar cumplimiento normativo de habilitación y bloquear facturación en sedes no habilitadas.

**Descripción funcional detallada**
1. El administrador accede a "Servicios habilitados".
2. Selecciona una sede de su empresa.
3. Visualiza los servicios habilitados con modalidad, complejidad, resolución y vigencia.
4. Puede agregar, editar o inactivar habilitaciones.
5. El sistema valida vigencia antes de permitir operaciones asistenciales.

**Reglas de negocio**
- Todo servicio habilitado pertenece a la empresa del usuario y a una sede de esa empresa.
- Modalidad obligatoria: intramural, extramural, telemedicina, domiciliario.
- Complejidad obligatoria: baja, media, alta.
- Un servicio con habilitación vencida se considera no habilitado.
- Unicidad: no duplicar el mismo código de servicio activo en la misma sede.

**Validaciones**
- Campos obligatorios: sede, código, nombre, modalidad, complejidad, fecha de habilitación.
- Fecha de vencimiento posterior a fecha de habilitación.

**Datos involucrados**
- `servicio_habilitado`
- `sede`
- `auditoria`

**Dependencias**
- HU-FASE1-002.

**Criterios de aceptación**
- CA1: Puedo habilitar servicios en sedes de mi empresa.
- CA2: El sistema bloquea duplicar un servicio activo en la misma sede.
- CA3: Veo si una habilitación está vigente o vencida.
- CA4: No puedo habilitar servicios en sedes de otras empresas.
- CA-T1 a CA-T5 aplican.

**Prioridad**: Alta

---

### HU-FASE1-004 — Gestión de permisos del sistema (catálogo global)

**Módulo**: Seguridad y estructura base
**Actor principal**: Super-administrador

**Historia de usuario**
Como super-administrador, quiero mantener el catálogo global de permisos del sistema, para que todas las empresas los usen al construir sus roles.

**Objetivo funcional**
Mantener un catálogo maestro global de permisos técnicos del SGH que las empresas combinan en sus roles.

**Descripción funcional detallada**
1. El super-administrador accede a "Permisos del sistema".
2. Visualiza y mantiene permisos con: código técnico (snake_case), nombre, descripción, módulo.
3. Solo el equipo técnico/super-admin puede crear permisos (no los administradores de empresa).
4. El catálogo es global y compartido entre todas las empresas.

**Reglas de negocio**
- `permiso` es un catálogo global sin `empresa_id`.
- Códigos únicos globales.
- Los administradores de empresa solo leen este catálogo (no lo editan).
- Un permiso inactivo no puede asignarse a nuevos roles; los roles que ya lo tienen pierden ese permiso efectivamente.

**Validaciones**
- Campos obligatorios: código, nombre, módulo.
- Código único global, snake_case.

**Datos involucrados**
- `permiso`
- `auditoria`

**Dependencias**
- Ninguna previa.

**Criterios de aceptación**
- CA1: Solo super-admin puede crear, editar o inactivar permisos.
- CA2: Los administradores de empresa solo consultan el catálogo.
- CA3: Los permisos son compartidos entre todas las empresas.

**Prioridad**: Alta

**Observaciones**
Seed inicial mínimo de permisos del SGH: `gestionar_empresas`, `gestionar_sedes`, `gestionar_usuarios`, `gestionar_roles`, `gestionar_terceros`, `gestionar_pacientes`, `registrar_admision`, `atender_urgencias`, `atender_hospitalizacion`, `asignar_citas`, `gestionar_contratos`, `facturar`, `radicar`, `consultar_cartera`, `consultar_auditoria`.

---

### HU-FASE1-005 — Gestión de roles por empresa

**Módulo**: Seguridad y estructura base
**Actor principal**: Administrador de la empresa

**Historia de usuario**
Como administrador de mi empresa, quiero crear y mantener roles con combinaciones específicas de permisos, para controlar qué puede hacer cada usuario de mi organización.

**Objetivo funcional**
Establecer el modelo RBAC específico de cada empresa sobre el catálogo global de permisos.

**Descripción funcional detallada**
1. El administrador accede a "Roles".
2. Visualiza los roles de su empresa (no ve los de otras empresas ni los globales).
3. Crea un rol con código (único dentro de la empresa), nombre, descripción.
4. Selecciona los permisos que tendrá el rol.
5. Puede editar, inactivar o eliminar roles que aún no tengan usuarios asignados.

**Reglas de negocio**
- Un rol pertenece a una empresa (`empresa_id NOT NULL`, `es_global = false`).
- Los roles globales (`es_global = true`, `empresa_id = NULL`) solo los gestiona el super-admin.
- Código único dentro de la empresa.
- Un rol inactivo no se asigna a nuevos usuarios; los que lo tienen pierden los permisos efectivamente.
- Debe existir al menos un rol con permisos administrativos por empresa.

**Validaciones**
- Código, nombre, al menos un permiso obligatorio.
- Unicidad `(empresa_id, codigo)`.

**Datos involucrados**
- `rol`, `rol_permiso`, `permiso`
- `auditoria`

**Dependencias**
- HU-FASE1-004.

**Criterios de aceptación**
- CA1: Puedo crear roles en mi empresa con permisos del catálogo global.
- CA2: No veo ni edito roles de otras empresas.
- CA3: Un rol sin permisos no puede activarse.
- CA4: Los cambios quedan auditados con mi `empresa_id`.

**Prioridad**: Alta

---

### HU-FASE1-006 — Gestión de usuarios de la empresa

**Módulo**: Seguridad y estructura base
**Actor principal**: Administrador de la empresa

**Historia de usuario**
Como administrador, quiero crear y mantener los usuarios de mi empresa, para controlar quién puede acceder al sistema dentro de mi organización.

**Objetivo funcional**
Administrar el ciclo de vida de las credenciales de los usuarios de una empresa.

**Descripción funcional detallada**
1. El administrador accede a "Usuarios".
2. Visualiza solo los usuarios de su empresa.
3. Crea un usuario capturando: tercero asociado (opcional), nombre de usuario, correo, contraseña inicial.
4. El sistema guarda la contraseña como hash BCrypt y marca `requiere_cambio_password = true`.
5. Puede bloquear, desbloquear, inactivar o resetear la contraseña del usuario.
6. Nunca puede crear super-administradores (solo el seed inicial del sistema o el super-admin lo hace).

**Reglas de negocio**
- `empresa_id` se asigna automáticamente del `TenantContext`.
- `nombre_usuario` y `correo` únicos **dentro de la empresa** (pueden repetirse entre empresas).
- La contraseña inicial obliga al cambio en el primer ingreso.
- Tras 5 intentos fallidos de login, el usuario se bloquea.
- Un usuario inactivo no puede iniciar sesión.
- Un administrador no puede crear otro administrador con más permisos que los suyos.

**Validaciones**
- Campos obligatorios: nombre de usuario, correo, contraseña inicial.
- Correo con formato válido.
- Unicidad `(empresa_id, nombre_usuario)` y `(empresa_id, correo)`.
- Política de contraseña inicial.

**Datos involucrados**
- `usuario`, `tercero` (opcional)
- `historial_password`
- `auditoria`

**Dependencias**
- HU-FASE1-000.

**Criterios de aceptación**
- CA1: Puedo crear usuarios en mi empresa.
- CA2: No veo usuarios de otras empresas.
- CA3: No puedo crear super-administradores.
- CA4: Al crear el usuario, se le fuerza el cambio de contraseña en su primer ingreso.
- CA5: Las contraseñas están cifradas (BCrypt) en BD.

**Prioridad**: Alta

---

### HU-FASE1-007 — Asignación de roles y sedes al usuario

**Módulo**: Seguridad y estructura base
**Actor principal**: Administrador de la empresa

**Historia de usuario**
Como administrador, quiero asignar uno o varios roles al usuario, con opcional restricción a una sede específica, para controlar qué puede hacer y dónde.

**Objetivo funcional**
Modelar que un usuario puede tener distintos roles según la sede en que trabaje.

**Descripción funcional detallada**
1. Desde la ficha del usuario, el administrador accede a "Roles y sedes".
2. Agrega una asignación indicando: rol (de la empresa), sede (opcional), vigencia desde/hasta.
3. Si `sede_id = null`, el rol aplica a todas las sedes de la empresa.
4. Si `sede_id` tiene valor, el rol solo aplica en esa sede.
5. Las sedes a las que el usuario tiene acceso se calculan como la unión de todas sus asignaciones.
6. Al hacer login, solo ve/selecciona las sedes que tiene por asignación.

**Reglas de negocio**
- Un usuario puede tener múltiples asignaciones de rol.
- Una asignación expira cuando llega `fecha_vigencia_hasta`.
- Cambios en asignación de roles no afectan sesiones ya emitidas hasta que se haga nuevo login o refresh.
- Un usuario sin ninguna asignación activa no puede iniciar sesión.

**Validaciones**
- Campos obligatorios: usuario, rol.
- Rol y sede deben pertenecer a la empresa del administrador.
- Unicidad `(usuario_id, rol_id, sede_id)`.

**Datos involucrados**
- `usuario_rol`
- `usuario`, `rol`, `sede`
- `auditoria`

**Dependencias**
- HU-FASE1-005, HU-FASE1-006.

**Criterios de aceptación**
- CA1: Puedo asignar un rol a un usuario para todas las sedes o para una sede específica.
- CA2: Las sedes a las que el usuario tiene acceso se derivan de sus asignaciones.
- CA3: Un usuario sin asignaciones activas no puede iniciar sesión.

**Prioridad**: Alta

---

### HU-FASE1-008 — Registro de profesional de salud

**Módulo**: Seguridad y estructura base
**Actor principal**: Administrador de la empresa

**Historia de usuario**
Como administrador, quiero registrar a los profesionales de salud de mi empresa como especialización del tercero, para que puedan firmar atenciones, órdenes y prescripciones.

**Objetivo funcional**
Mantener el listado de profesionales habilitados para actuar clínicamente en la empresa.

**Descripción funcional detallada**
1. El administrador ubica o crea un tercero tipo "profesional" en su empresa.
2. Marca al tercero como profesional capturando: número de registro médico (ReTHUS), especialidad principal, fecha de ingreso.
3. Opcionalmente le asocia un usuario del sistema para que pueda iniciar sesión.
4. Puede inactivar a un profesional (conserva historial).

**Reglas de negocio**
- Todo profesional pertenece a una empresa y se basa en un tercero de la misma empresa.
- Relación 1 a 1 con tercero dentro de la empresa.
- Número de registro médico único dentro de la empresa.
- Un profesional inactivo conserva su historial pero no aparece como seleccionable en nuevas atenciones.

**Validaciones**
- Campos obligatorios: tercero, especialidad principal.
- Unicidad `(empresa_id, numero_registro_medico)`.
- Tercero debe ser de la misma empresa.

**Datos involucrados**
- `profesional_salud`
- `tercero`, `especialidad`, `usuario` (opcional)
- `auditoria`

**Dependencias**
- HU-FASE1-010.

**Criterios de aceptación**
- CA1: Puedo registrar profesionales de mi empresa.
- CA2: No puedo usar un tercero de otra empresa como profesional.
- CA3: No puedo duplicar registro médico dentro de mi empresa.

**Prioridad**: Alta

---

### HU-FASE1-009 — Consulta de auditoría del sistema

**Módulo**: Seguridad y estructura base
**Actor principal**: Auditor / Administrador

**Historia de usuario**
Como auditor, quiero consultar las acciones realizadas por los usuarios de mi empresa, para hacer seguimiento y control.

**Objetivo funcional**
Proveer trazabilidad de las acciones sensibles dentro del ámbito de mi empresa.

**Descripción funcional detallada**
1. El sistema registra automáticamente en `auditoria` toda acción sensible con `empresa_id`, `sede_id`, `usuario_id`, IP.
2. El auditor accede a la consola de auditoría.
3. Ve solo registros de su empresa.
4. Filtra por tabla, usuario, fecha, acción, IP.
5. Ve `datos_antes` y `datos_despues`.
6. Puede exportar (exportación también se audita).

**Reglas de negocio**
- La auditoría es solo lectura.
- El auditor de una empresa solo ve registros de su empresa.
- El super-admin puede ver auditoría global.
- No se elimina ni depura salvo por políticas de retención.

**Datos involucrados**
- `auditoria`

**Dependencias**
- HU-FASE1-006.

**Criterios de aceptación**
- CA1: Toda acción sensible queda registrada automáticamente.
- CA2: Como auditor de una empresa, solo veo registros de mi empresa.
- CA3: Puedo filtrar por tabla, usuario, fecha y acción.
- CA4: No existe opción de edición sobre `auditoria`.

**Prioridad**: Media

---

## Bloque 2. Terceros y pacientes

---

### HU-FASE1-010 — Creación de tercero

**Módulo**: Terceros y pacientes
**Actor principal**: Recepcionista / Auxiliar de admisiones

**Historia de usuario**
Como recepcionista, quiero crear un tercero en mi empresa, para tener una única fuente de verdad de identidad dentro de mi organización (pacientes, profesionales, proveedores, pagadores).

**Objetivo funcional**
Respetar la decisión arquitectónica "todo es tercero" manteniendo aislamiento por empresa.

**Descripción funcional detallada**
1. El usuario accede al módulo "Terceros" de su empresa.
2. Antes de crear, busca por tipo + número de documento en su empresa.
3. Si no existe, captura: tipo de tercero, tipo de documento, número de documento, nombres/apellidos o razón social, fecha de nacimiento, sexo, género, estado civil, nivel de escolaridad, ocupación, pertenencia étnica, país y municipio de nacimiento.
4. Guarda el tercero; `empresa_id` se asigna desde el contexto.
5. Queda auditado.

**Reglas de negocio**
- `empresa_id` se asigna automáticamente del `TenantContext`.
- La unicidad de documento es `(empresa_id, tipo_documento_id, numero_documento)`: dos empresas pueden tener registrado el mismo paciente independientemente.
- Persona natural requiere nombres y apellidos; persona jurídica requiere razón social.
- El tercero de una empresa nunca es visible desde otra empresa.

**Validaciones**
- Campos obligatorios: tipo de tercero, tipo de documento, número de documento.
- Para persona natural: primer nombre + primer apellido.
- Para persona jurídica: razón social.
- Fecha de nacimiento no futura.
- Unicidad `(empresa_id, tipo_documento_id, numero_documento)`.

**Datos involucrados**
- `tercero`
- Catálogos de personas
- `auditoria`

**Dependencias**
- HU-FASE1-000. Catálogos cargados.

**Criterios de aceptación**
- CA1: Puedo crear un tercero en mi empresa.
- CA2: No puedo crear un tercero con tipo+número duplicado en mi empresa.
- CA3: Puedo crear un tercero con mismo documento que existe en otra empresa (aislamiento garantizado).
- CA4: Los cambios quedan auditados con mi `empresa_id`.

**Prioridad**: Alta

---

### HU-FASE1-011 — Consulta y búsqueda de tercero

**Módulo**: Terceros y pacientes
**Actor principal**: Cualquier usuario autorizado

**Historia de usuario**
Como usuario del sistema, quiero buscar un tercero de forma rápida dentro de mi empresa, para identificar al paciente o entidad con la que voy a operar sin duplicar registros.

**Objetivo funcional**
Búsqueda flexible y eficiente dentro del ámbito de la empresa.

**Descripción funcional detallada**
1. Accedo al buscador de terceros.
2. Busco por número de documento (exacto) o por nombre/apellidos/razón social (parcial, sin importar tildes).
3. Veo resultados paginados con: tipo y número de documento, nombre completo, fecha de nacimiento, tipo de tercero, estado.
4. Selecciono para ver la ficha completa.
5. Acciones rápidas: ver paciente, contactos, direcciones, editar.

**Reglas de negocio**
- Búsqueda ignora tildes y es case-insensitive (usa `unaccent`).
- Solo veo terceros de mi empresa.
- Muestra visualmente si está inactivo.

**Validaciones**
- Criterio mínimo: número de documento o al menos 3 caracteres.

**Datos involucrados**
- `tercero`, `paciente`, `contacto_tercero`, `direccion_tercero`

**Dependencias**
- HU-FASE1-010.

**Criterios de aceptación**
- CA1: Encuentro un tercero por documento en <2 segundos.
- CA2: La búsqueda por nombre ignora tildes y mayúsculas.
- CA3: No veo terceros de otras empresas.
- CA4: Desde el resultado accedo a la ficha completa.

**Prioridad**: Alta

---

### HU-FASE1-012 — Actualización de tercero

**Módulo**: Terceros y pacientes
**Actor principal**: Recepcionista / Administrador

**Historia de usuario**
Como recepcionista, quiero actualizar los datos de un tercero de mi empresa, para mantener su información al día.

**Objetivo funcional**
Edición controlada de datos del tercero con trazabilidad.

**Descripción funcional detallada**
1. Abro la ficha del tercero (de mi empresa).
2. Edito los campos permitidos; `tipo_documento` y `numero_documento` requieren flujo especial de corrección.
3. Guardo y el sistema registra `usuario_modificacion`, `fecha_modificacion`.
4. La auditoría guarda antes/después.

**Reglas de negocio**
- Solo puedo editar terceros de mi empresa.
- No puedo editar terceros inactivos salvo para reactivarlos.

**Datos involucrados**
- `tercero`, `auditoria`

**Dependencias**
- HU-FASE1-010, HU-FASE1-011.

**Criterios de aceptación**
- CA1: Puedo editar los datos demográficos del tercero de mi empresa.
- CA2: No puedo editar tipo ni número de documento.
- CA3: Un intento de editar un tercero de otra empresa retorna 404.

**Prioridad**: Alta

---

### HU-FASE1-013 — Creación de paciente a partir de tercero

**Módulo**: Terceros y pacientes
**Actor principal**: Recepcionista

**Historia de usuario**
Como recepcionista, quiero convertir un tercero de mi empresa en paciente, para registrar datos clínico-sociales.

**Descripción funcional detallada**
1. Ubico un tercero de mi empresa.
2. Si aún no es paciente, selecciono "Crear paciente".
3. Captura: grupo sanguíneo, RH, discapacidad, grupo de atención, alergias conocidas, observaciones clínicas.
4. El `empresa_id` del paciente se toma del contexto y debe coincidir con el del tercero.

**Reglas de negocio**
- Relación 1 a 1 con tercero.
- El tercero debe ser de la misma empresa (invariante).
- Los datos demográficos no se duplican.

**Validaciones**
- No duplicar paciente sobre el mismo tercero.

**Datos involucrados**
- `paciente`
- `tercero`, catálogos clínicos
- `auditoria`

**Dependencias**
- HU-FASE1-010.

**Criterios de aceptación**
- CA1: Creo paciente desde un tercero de mi empresa.
- CA2: No puedo crear dos pacientes sobre el mismo tercero.
- CA3: Intentar usar un tercero de otra empresa retorna 404.

**Prioridad**: Alta

---

### HU-FASE1-014 — Gestión de contactos del tercero

**Módulo**: Terceros y pacientes
**Actor principal**: Recepcionista

**Historia de usuario**
Como recepcionista, quiero registrar contactos (teléfono, celular, correo) del tercero de mi empresa.

**Descripción funcional detallada**
1. Desde la ficha del tercero de mi empresa, accedo a "Contactos".
2. Agrego contacto: tipo, valor, es_principal, acepta notificaciones.
3. Edito/inactivo contactos.

**Reglas de negocio**
- Solo un contacto principal por tipo.
- Los contactos heredan la empresa del tercero.

**Validaciones**
- Tipo y valor obligatorios. Formato válido según tipo.

**Datos involucrados**
- `contacto_tercero`, `tipo_contacto`

**Dependencias**
- HU-FASE1-010.

**Criterios de aceptación**
- CA1: Registro múltiples contactos en terceros de mi empresa.
- CA2: Solo un principal por tipo.

**Prioridad**: Alta

---

### HU-FASE1-015 — Gestión de direcciones del tercero

**Módulo**: Terceros y pacientes
**Actor principal**: Recepcionista

**Historia de usuario**
Como recepcionista, quiero registrar direcciones del tercero por tipo (residencia, correspondencia, facturación).

**Descripción funcional detallada**
1. Desde la ficha del tercero de mi empresa, accedo a "Direcciones".
2. Agrego dirección con tipo, zona, país/departamento/municipio, dirección, barrio, código postal, referencia, coordenadas opcionales.
3. Marco principal por tipo.

**Reglas de negocio**
- Al menos una dirección de residencia principal para pacientes.
- Solo una principal por tipo.

**Validaciones**
- Campos geográficos obligatorios. Coherencia departamento/municipio.

**Datos involucrados**
- `direccion_tercero`

**Dependencias**
- HU-FASE1-010.

**Criterios de aceptación**
- CA1: Registro múltiples direcciones para terceros de mi empresa.
- CA2: La dirección hereda la empresa del tercero.

**Prioridad**: Alta

---

### HU-FASE1-016 — Gestión de relaciones entre terceros

**Módulo**: Terceros y pacientes
**Actor principal**: Recepcionista

**Historia de usuario**
Como recepcionista, quiero registrar relaciones entre terceros de mi empresa (familiar, acompañante, responsable).

**Descripción funcional detallada**
1. Desde ficha del tercero, accedo a "Relaciones".
2. Agrego relación: tercero destino, tipo de relación, es responsable, es contacto de emergencia.

**Reglas de negocio**
- Los dos terceros deben pertenecer a la misma empresa (mi empresa).
- No se puede relacionar a un tercero consigo mismo.

**Validaciones**
- Tercero destino y tipo de relación obligatorios.
- `tercero_origen_id != tercero_destino_id`.

**Datos involucrados**
- `relacion_tercero`, `tipo_relacion`

**Dependencias**
- HU-FASE1-010.

**Criterios de aceptación**
- CA1: Puedo vincular dos terceros de mi empresa.
- CA2: No puedo vincular un tercero de mi empresa con uno de otra.

**Prioridad**: Media

---

### HU-FASE1-017 — Registro de SISBEN del paciente

**Módulo**: Terceros y pacientes
**Actor principal**: Auxiliar de admisiones

**Historia de usuario**
Como auxiliar, quiero registrar clasificación SISBEN del paciente de mi empresa.

**Descripción funcional detallada**
1. Desde ficha del paciente de mi empresa accedo a "SISBEN".
2. Registro: grupo SISBEN, puntaje, ficha, fechas.
3. Solo uno vigente a la vez.

**Reglas de negocio**
- Un nuevo SISBEN vigente desmarca el anterior.
- Afecta reglas de copago/cuota moderadora.

**Datos involucrados**
- `sisben_paciente`, `grupo_sisben`

**Dependencias**
- HU-FASE1-013.

**Criterios de aceptación**
- CA1: Registro SISBEN a pacientes de mi empresa.
- CA2: Al registrar uno nuevo vigente, el anterior queda no vigente.

**Prioridad**: Media

---

### HU-FASE1-018 — Registro de seguridad social del paciente

**Módulo**: Terceros y pacientes
**Actor principal**: Auxiliar de admisiones

**Historia de usuario**
Como auxiliar, quiero registrar la afiliación a seguridad social del paciente de mi empresa.

**Descripción funcional detallada**
1. Desde ficha del paciente accedo a "Seguridad social".
2. Registro pagador (de mi empresa), régimen, categoría, tipo de afiliación, número, cotizante, vigencias.

**Reglas de negocio**
- Solo uno vigente a la vez.
- Si beneficiario, requiere cotizante (tercero de mi empresa).
- Pagador debe ser de mi empresa.

**Datos involucrados**
- `seguridad_social_paciente`, `pagador`

**Dependencias**
- HU-FASE1-013, HU-FASE1-049.

**Criterios de aceptación**
- CA1: Registro afiliación vigente del paciente.
- CA2: El pagador debe existir en mi empresa.

**Prioridad**: Alta

---

### HU-FASE1-019 — Asociación de contratos al paciente

**Módulo**: Terceros y pacientes
**Actor principal**: Auxiliar de admisiones

**Historia de usuario**
Como auxiliar, quiero asociar contratos específicos al paciente de mi empresa, para facturar correctamente servicios cubiertos por pólizas o convenios especiales.

**Descripción funcional detallada**
1. Desde ficha del paciente accedo a "Contratos".
2. Selecciono contrato (de mi empresa) e ingreso número de póliza y vigencia.

**Reglas de negocio**
- Contrato debe ser de mi empresa y estar activo/vigente.
- Pueden haber varios contratos vigentes simultáneamente.

**Datos involucrados**
- `contrato_paciente`

**Dependencias**
- HU-FASE1-013, HU-FASE1-050.

**Criterios de aceptación**
- CA1: Asocio contratos de mi empresa al paciente.
- CA2: No puedo asociar contratos de otras empresas.

**Prioridad**: Alta

---
### HU-FASE1-019A — Verificación de derechos y cobertura del paciente

**Módulo**: Servicios, pagadores y contratos  
**Actor principal**: Auxiliar de admisiones

**Historia de usuario**  
Como auxiliar de admisiones, quiero verificar los derechos y la cobertura del paciente, para confirmar si puede ser atendido con un pagador y contrato vigente.

**Descripción funcional detallada**  
1. Selecciono el paciente de mi empresa.  
2. El sistema consulta su seguridad social activa.  
3. El sistema identifica pagador, régimen, tipo de afiliación y contrato asociado.  
4. El sistema valida si el contrato está vigente.  
5. El sistema valida si el servicio requerido está cubierto.  
6. Si aplica, el sistema indica si requiere autorización.  
7. El sistema muestra el resultado de cobertura para continuar la admisión.

**Reglas de negocio**  
- El paciente debe pertenecer a mi empresa.  
- La verificación debe hacerse solo con afiliaciones activas.  
- Solo pueden usarse contratos vigentes del pagador.  
- Si el servicio no está cubierto, el sistema debe informarlo.  
- Si el servicio requiere autorización, debe indicarlo antes de continuar.

**Datos involucrados**  
- `paciente`
- `seguridad_social_paciente`
- `contrato_paciente`
- `pagador`
- `contrato`
- `servicio_contrato`

**Dependencias**  
- HU-FASE1-017  
- HU-FASE1-018  
- HU-FASE1-019  
- HU-FASE1-050  
- HU-FASE1-053

**Criterios de aceptación**  
- CA1: El sistema solo valida afiliaciones y contratos de mi empresa.  
- CA2: El sistema informa si el servicio está cubierto o no.  
- CA3: El sistema indica si requiere autorización.  
- CA4: No se permite continuar con cobertura inválida sin dejar trazabilidad.

**Prioridad**: Alta

---
## Bloque 3. Admisiones

---

### HU-FASE1-020 — Registro de admisión

**Módulo**: Admisiones
**Actor principal**: Auxiliar de admisiones

**Historia de usuario**
Como auxiliar de admisiones, quiero registrar formalmente la admisión de un paciente en mi sede activa, para iniciar el episodio de atención.

**Objetivo funcional**
Crear el registro formal del ingreso del paciente vinculando empresa + sede + paciente + pagador + contrato.

**Descripción funcional detallada**
1. Selecciono o creo al paciente (debe ser de mi empresa).
2. Valido afiliación vigente.
3. Capturo: tipo de admisión, origen de atención, pagador (de mi empresa), contrato (si aplica, de mi empresa), motivo, acompañante (tercero de mi empresa).
4. El sistema asigna `empresa_id` y `sede_id` desde el contexto y genera consecutivo único `(empresa_id, sede_id, numero_admision)`.
5. Al guardar, queda en estado "admitido".

**Reglas de negocio**
- `empresa_id` y `sede_id` se toman del contexto.
- `numero_admision` consecutivo único dentro de `(empresa_id, sede_id)`.
- El paciente, pagador, contrato, acompañante deben pertenecer a mi empresa.
- Una admisión abierta bloquea nueva admisión del mismo tipo para el mismo paciente en la misma sede.

**Validaciones**
- Campos obligatorios: paciente, tipo de admisión, origen, pagador.
- Coherencia empresa de todas las referencias.

**Datos involucrados**
- `admision`, catálogos de admisión
- `paciente`, `pagador`, `contrato`, `sede`

**Dependencias**
- HU-FASE1-002, HU-FASE1-013, HU-FASE1-018, HU-FASE1-049, HU-FASE1-050.

**Criterios de aceptación**
- CA1: Admito un paciente en mi sede activa.
- CA2: No puedo usar paciente/pagador/contrato de otra empresa.
- CA3: El número de admisión es consecutivo por `(empresa_id, sede_id)`.
- CA4: No puedo duplicar admisión abierta del mismo tipo para el mismo paciente en la misma sede.

**Prioridad**: Alta

---

### HU-FASE1-021 — Apertura automática de atención al admitir

**Módulo**: Admisiones
**Actor principal**: Sistema (automático)

**Historia de usuario**
Como sistema, al registrar una admisión quiero abrir automáticamente la atención asociada.

**Descripción funcional detallada**
1. Al guardar la admisión, el sistema crea la primera atención dentro de la misma transacción.
2. `empresa_id` y `sede_id` de la atención coinciden con la admisión.
3. Estado inicial según tipo de admisión (pendiente / en triage).

**Reglas de negocio**
- Creación transaccional con la admisión.
- Hereda `empresa_id`, `sede_id` de la admisión.

**Datos involucrados**
- `atencion`, `admision`

**Dependencias**
- HU-FASE1-020.

**Criterios de aceptación**
- CA1: La atención se crea automáticamente con `empresa_id`/`sede_id` heredados.
- CA2: Si falla la creación, falla la admisión (atomicidad).

**Prioridad**: Alta

---

### HU-FASE1-022 — Consulta de admisiones activas de la sede

**Módulo**: Admisiones
**Actor principal**: Auxiliar / Coordinador asistencial

**Historia de usuario**
Como coordinador, quiero ver el listado de admisiones activas de mi sede.

**Descripción funcional detallada**
1. Accedo a "Admisiones activas".
2. Solo veo admisiones de mi empresa Y de mi sede activa.
3. Filtro por tipo, estado, fecha, pagador.
4. Abro una admisión para ver su detalle y atenciones.

**Reglas de negocio**
- Filtros implícitos: `empresa_id = TenantContext.getEmpresaId()` y `sede_id = TenantContext.getSedeId()`.
- No veo admisiones de otras sedes ni empresas.

**Datos involucrados**
- `admision`, `paciente`, `tercero`, `pagador`, `sede`

**Dependencias**
- HU-FASE1-020.

**Criterios de aceptación**
- CA1: Veo solo admisiones de mi sede activa.
- CA2: No puedo ver admisiones de otras sedes ni empresas.

**Prioridad**: Alta

---

### HU-FASE1-023 — Cambio de estado de admisión

**Módulo**: Admisiones
**Actor principal**: Auxiliar / Profesional

**Historia de usuario**
Como auxiliar, quiero cambiar el estado de una admisión de mi sede para reflejar el avance del paciente.

**Descripción funcional detallada**
1. Abro una admisión de mi sede.
2. Cambio estado con observación.
3. El sistema valida máquina de estados.

**Reglas de negocio**
- Solo admisiones de mi sede (y empresa).
- Transiciones válidas según máquina de estados.
- Cambios auditados.

**Datos involucrados**
- `admision`, `auditoria`

**Dependencias**
- HU-FASE1-020.

**Criterios de aceptación**
- CA1: Cambio estado de admisiones de mi sede.
- CA2: No puedo cambiar estado de admisiones de otras sedes.

**Prioridad**: Alta

---

### HU-FASE1-024 — Registro de egreso administrativo de admisión

**Módulo**: Admisiones
**Actor principal**: Auxiliar de admisiones

**Historia de usuario**
Como auxiliar, quiero cerrar administrativamente una admisión de mi sede al egresar al paciente.

**Descripción funcional detallada**
1. Cuando las atenciones están cerradas, registro el egreso administrativo con fecha/hora.
2. La admisión queda lista para facturar.

**Reglas de negocio**
- No permitir cerrar con atenciones abiertas.
- Solo admisiones de mi sede.

**Datos involucrados**
- `admision`, `atencion`

**Dependencias**
- HU-FASE1-020, HU-FASE1-021, HU-FASE1-032, HU-FASE1-038.

**Criterios de aceptación**
- CA1: Cierro la admisión cuando todas sus atenciones están cerradas.
- CA2: Aplica solo a admisiones de mi sede.

**Prioridad**: Media

---

## Bloque 4. Triage y urgencias

---

### HU-FASE1-025 — Registro de triage de urgencias

**Módulo**: Triage y urgencias
**Actor principal**: Profesional de triage

**Historia de usuario**
Como profesional de triage, quiero clasificar al paciente que llega a urgencias en mi sede según prioridad clínica.

**Descripción funcional detallada**
1. Veo pacientes admitidos en urgencias de mi sede pendientes de triage.
2. Selecciono un paciente y registro motivo de consulta, signos vitales, nivel de triage.

**Reglas de negocio**
- Solo pacientes admitidos en mi sede.
- Nivel de triage I–V.

**Datos involucrados**
- `atencion`, `admision`

**Dependencias**
- HU-FASE1-020, HU-FASE1-021.

**Criterios de aceptación**
- CA1: Solo trabajo sobre pacientes de mi sede.
- CA2: Debo registrar motivo y nivel.
- CA3: El sistema respeta rangos de signos vitales.

**Prioridad**: Alta

---

### HU-FASE1-026 — Reclasificación de triage

**Módulo**: Triage y urgencias
**Actor principal**: Profesional de triage

**Historia de usuario**
Como profesional de triage, quiero reclasificar pacientes de mi sede cuando su condición cambia.

**Descripción funcional detallada**
1. Selecciono paciente en sala de espera de mi sede.
2. Registro nuevo triage con justificación.

**Reglas de negocio**
- Solo aplica a pacientes no atendidos aún.
- Solo pacientes de mi sede.

**Datos involucrados**
- `atencion`

**Dependencias**
- HU-FASE1-025.

**Criterios de aceptación**
- CA1: Reclasifico mientras el paciente no haya sido atendido.
- CA2: Justificación obligatoria.

**Prioridad**: Media

---

### HU-FASE1-027 — Visualización de cola de urgencias de la sede

**Módulo**: Triage y urgencias
**Actor principal**: Médico general de urgencias / Coordinador

**Historia de usuario**
Como médico de urgencias, quiero ver la cola de pacientes de mi sede ordenada por prioridad.

**Descripción funcional detallada**
1. Accedo al tablero "Urgencias" de mi sede.
2. Veo pacientes ordenados por nivel de triage y tiempo de espera.
3. Los pacientes fuera de SLA se resaltan.

**Reglas de negocio**
- Solo pacientes de mi sede.
- Orden por nivel ASC, luego tiempo de espera DESC.

**Datos involucrados**
- `admision`, `atencion`, `paciente`

**Dependencias**
- HU-FASE1-025.

**Criterios de aceptación**
- CA1: Solo veo pacientes de mi sede.
- CA2: Los fuera de SLA se resaltan.

**Prioridad**: Alta

---

### HU-FASE1-028 — Consola de atención médica de urgencias

**Módulo**: Triage y urgencias
**Actor principal**: Médico general de urgencias

**Historia de usuario**
Como médico, quiero una consola única para atender pacientes de urgencias de mi sede, sin saltar entre pantallas.

**Descripción funcional detallada**
1. Abro la consola para un paciente de la cola de mi sede.
2. Encabezado fijo: nombre, edad, sexo, documento, pagador, contrato, alergias, triage, signos vitales.
3. Secciones: motivo, enfermedad actual, antecedentes, examen físico, diagnósticos, plan, conducta.
4. Acciones rápidas: solicitar laboratorio, imágenes, formular, hospitalizar, alta, remitir.
5. Línea de tiempo clínica.
6. Cierre: requiere diagnóstico principal y conducta.

**Reglas de negocio**
- Solo pacientes de mi sede activa.
- Todo cambio auditado.

**Datos involucrados**
- `atencion`, `admision`, `paciente`, diagnósticos, órdenes, prescripciones

**Dependencias**
- HU-FASE1-025, HU-FASE1-027, HU-FASE1-029, HU-FASE1-030, HU-FASE1-031.

**Criterios de aceptación**
- CA1: Consola usable para pacientes de mi sede.
- CA2: Cierre solo con diagnóstico y conducta.
- CA3: Todo queda auditado con mi `empresa_id`, `sede_id`, `usuario_id`.

**Prioridad**: Alta

---

### HU-FASE1-029 — Registro de diagnósticos en la atención

**Módulo**: Triage y urgencias
**Actor principal**: Médico

**Historia de usuario**
Como médico, quiero codificar diagnósticos CIE-10 para la atención del paciente de mi sede.

**Descripción funcional detallada**
1. Desde la consola, busco diagnósticos por código o nombre (catálogo CIE-10 global).
2. Los marco como principal, relacionado, complicación o comorbilidad.
3. Confirmado/presuntivo, recurrente, observaciones.

**Reglas de negocio**
- Uno solo principal por atención.
- Validar coherencia sexo/edad.
- Catálogo CIE-10 es global, el diagnostico_atencion es por empresa.

**Datos involucrados**
- `diagnostico_atencion`, `catalogo_diagnostico`

**Dependencias**
- HU-FASE1-028. Catálogo CIE-10 cargado.

**Criterios de aceptación**
- CA1: Agrego múltiples diagnósticos con uno principal.
- CA2: El sistema valida coherencia sexo/edad.
- CA3: Los registros quedan ligados a mi `empresa_id`.

**Prioridad**: Alta

---

### HU-FASE1-030 — Generación de órdenes clínicas desde la atención

**Módulo**: Triage y urgencias
**Actor principal**: Médico

**Historia de usuario**
Como médico, quiero generar órdenes desde la consola de atención.

**Descripción funcional detallada**
1. Selecciono "Nueva orden" con tipo (laboratorio, imágenes, procedimiento, interconsulta).
2. Agrego detalles con servicios del catálogo de mi empresa.
3. El sistema genera número de orden único por `(empresa_id, numero_orden)`.

**Reglas de negocio**
- Servicios deben ser del catálogo de mi empresa.
- Número de orden consecutivo por empresa.
- `empresa_id`, `sede_id` se heredan de la atención.

**Datos involucrados**
- `orden_clinica`, `detalle_orden_clinica`, `servicio_salud`

**Dependencias**
- HU-FASE1-028, HU-FASE1-029, HU-FASE1-047.

**Criterios de aceptación**
- CA1: Creo órdenes con servicios de mi empresa.
- CA2: El número es consecutivo dentro de mi empresa.
- CA3: No puedo usar servicios de otra empresa.

**Prioridad**: Alta

---

### HU-FASE1-031 — Generación de prescripción desde la atención

**Módulo**: Triage y urgencias
**Actor principal**: Médico

**Historia de usuario**
Como médico, quiero formular medicamentos desde la consola de atención de mi sede.

**Descripción funcional detallada**
1. Selecciono "Nueva prescripción".
2. Agrego medicamentos del catálogo de mi empresa con dosis, vía, frecuencia, duración.
3. Marco no PBS si aplica (MIPRES).

**Reglas de negocio**
- Catálogo de medicamentos de mi empresa.
- Número de prescripción consecutivo por empresa.
- `empresa_id`, `sede_id` heredados.

**Datos involucrados**
- `prescripcion`, `detalle_prescripcion`, `servicio_salud`

**Dependencias**
- HU-FASE1-028, HU-FASE1-047.

**Criterios de aceptación**
- CA1: Prescribo con medicamentos de mi empresa.
- CA2: El sistema genera el número automáticamente.

**Prioridad**: Alta

---

### HU-FASE1-032 — Definición de conducta y cierre de atención de urgencias

**Módulo**: Triage y urgencias
**Actor principal**: Médico

**Historia de usuario**
Como médico, quiero definir conducta y cerrar la atención de urgencias de mi paciente.

**Descripción funcional detallada**
1. Completo diagnóstico, órdenes, prescripción.
2. Selecciono conducta: alta, observación, remisión, hospitalización.
3. Según conducta, el sistema actúa (cierra, mantiene abierta, dispara hospitalización, etc.).

**Reglas de negocio**
- Cierre requiere diagnóstico principal.
- Solo atenciones de mi sede.
- Hospitalización dispara HU-FASE1-033.

**Datos involucrados**
- `atencion`, `diagnostico_atencion`

**Dependencias**
- HU-FASE1-028, HU-FASE1-029.

**Criterios de aceptación**
- CA1: Cierro con conducta.
- CA2: No cierro sin diagnóstico principal.
- CA3: Solo atenciones de mi sede.

**Prioridad**: Alta

---

## Bloque 5. Hospitalización

---

### HU-FASE1-033 — Solicitud de hospitalización desde urgencias

**Módulo**: Hospitalización
**Actor principal**: Médico general de urgencias

**Historia de usuario**
Como médico de urgencias, quiero solicitar hospitalización de pacientes de mi sede.

**Descripción funcional detallada**
1. En conducta selecciono "Hospitalización".
2. Capturo servicio destino, justificación, diagnóstico de ingreso.
3. La admisión cambia a "pendiente de hospitalización".

**Reglas de negocio**
- Diagnóstico principal confirmado obligatorio.
- Solo pacientes de mi sede.
- El servicio destino es una sección clínica de mi sede.

**Datos involucrados**
- `admision`, `atencion`

**Dependencias**
- HU-FASE1-028, HU-FASE1-032.

**Criterios de aceptación**
- CA1: Solicito hospitalización de pacientes de mi sede.
- CA2: El paciente entra a la cola de hospitalización de la sede destino.

**Prioridad**: Alta

---

### HU-FASE1-034 — Ingreso hospitalario por médico tratante

**Módulo**: Hospitalización
**Actor principal**: Médico hospitalario

**Historia de usuario**
Como médico hospitalario, quiero recibir pacientes solicitados para hospitalización en mi servicio y sede.

**Descripción funcional detallada**
1. Accedo a "Pendientes de recibir" en mi servicio/sede.
2. Selecciono paciente y confirmo recepción.
3. Le asocio cama/recurso físico de mi sede.

**Reglas de negocio**
- Solo recibo pacientes pendientes en mi sede.
- La cama debe estar disponible en mi sede.
- Se crea atención hospitalaria con `empresa_id`, `sede_id`.

**Datos involucrados**
- `atencion`, `admision`, `recurso_fisico`

**Dependencias**
- HU-FASE1-033, HU-FASE1-040.

**Criterios de aceptación**
- CA1: Recibo pacientes solo en mi sede.
- CA2: Uso camas de mi sede.

**Prioridad**: Alta

---

### HU-FASE1-035 — Nota de ingreso hospitalario

**Módulo**: Hospitalización
**Actor principal**: Médico hospitalario

**Historia de usuario**
Como médico hospitalario, quiero registrar la nota de ingreso del paciente que recibí.

**Descripción funcional detallada**
1. Desde consola de hospitalización selecciono "Nota de ingreso".
2. Se precarga información de urgencias (motivo, enfermedad actual, signos, diagnósticos).
3. Completo antecedentes, examen físico, análisis, plan.

**Reglas de negocio**
- Precarga no sobreescribe datos originales.
- Firma obligatoria; una vez firmada no admite edición.
- Solo pacientes de mi sede.

**Datos involucrados**
- `atencion`

**Dependencias**
- HU-FASE1-034.

**Criterios de aceptación**
- CA1: Registro nota de ingreso para pacientes de mi sede.
- CA2: No puedo crear evoluciones sin nota de ingreso.

**Prioridad**: Alta

---

### HU-FASE1-036 — Registro de evolución hospitalaria

**Módulo**: Hospitalización
**Actor principal**: Médico hospitalario

**Historia de usuario**
Como médico hospitalario, quiero registrar evoluciones diarias de mis pacientes hospitalizados en mi sede.

**Descripción funcional detallada**
1. Selecciono paciente y registro evolución SOAP.
2. Desde allí puedo generar órdenes y prescripciones adicionales.

**Reglas de negocio**
- Evolución firmada no se edita.
- Solo pacientes activos en mi sede.

**Datos involucrados**
- `atencion`

**Dependencias**
- HU-FASE1-035.

**Criterios de aceptación**
- CA1: Registro evoluciones SOAP en mis pacientes.
- CA2: Una evolución firmada no se edita.

**Prioridad**: Alta

---

### HU-FASE1-037 — Visualización de órdenes activas del paciente hospitalizado

**Módulo**: Hospitalización
**Actor principal**: Médico / Auxiliar de enfermería

**Historia de usuario**
Como médico hospitalario, quiero ver órdenes activas de mis pacientes en mi sede.

**Descripción funcional detallada**
1. Accedo a "Órdenes activas" del paciente.
2. Filtro por estado.
3. Puedo anular con justificación.

**Reglas de negocio**
- Solo órdenes de pacientes de mi sede.
- Anular requiere justificación.

**Datos involucrados**
- `orden_clinica`, `detalle_orden_clinica`

**Dependencias**
- HU-FASE1-030, HU-FASE1-034.

**Criterios de aceptación**
- CA1: Veo órdenes agrupadas por tipo.
- CA2: Anular queda auditado.

**Prioridad**: Media

---

### HU-FASE1-038 — Egreso hospitalario básico

**Módulo**: Hospitalización
**Actor principal**: Médico hospitalario

**Historia de usuario**
Como médico, quiero dar de alta a mi paciente hospitalizado.

**Descripción funcional detallada**
1. Selecciono "Egreso" con tipo, diagnósticos de egreso, epicrisis básica, indicaciones, plan.
2. Libero la cama.
3. La admisión pasa a "pendiente de egreso administrativo".

**Reglas de negocio**
- Solo pacientes de mi sede.
- Al menos un diagnóstico de egreso.
- La cama queda disponible en mi sede.

**Datos involucrados**
- `atencion`, `admision`, `diagnostico_atencion`, `recurso_fisico`

**Dependencias**
- HU-FASE1-034, HU-FASE1-035, HU-FASE1-036.

**Criterios de aceptación**
- CA1: Egreso con tipo, diagnóstico y epicrisis.
- CA2: La cama queda liberada en mi sede.

**Prioridad**: Alta

---

## Bloque 6. Citas

---

### HU-FASE1-039 — Gestión de calendarios de citas

**Módulo**: Citas
**Actor principal**: Administrador / Coordinador de agendas

**Historia de usuario**
Como coordinador, quiero definir calendarios de mi empresa con días hábiles y festivos.

**Descripción funcional detallada**
1. Creo calendario con código (único en mi empresa), nombre, descripción.
2. Agrego detalles por fecha (hábil, festivo, observaciones).
3. Puedo cargar masivamente un año.

**Reglas de negocio**
- Calendario por empresa (no por sede, compartible entre sedes de la misma empresa).
- Código único `(empresa_id, codigo)`.

**Datos involucrados**
- `calendario_cita`, `detalle_calendario_cita`

**Dependencias**
- Ninguna previa.

**Criterios de aceptación**
- CA1: Gestiono calendarios de mi empresa.
- CA2: No veo calendarios de otras empresas.

**Prioridad**: Alta

---
### HU-FASE1-039A — Parametrización de tipos de cita, estados y motivos

**Módulo**: Citas  
**Actor principal**: Administrador de la empresa

**Historia de usuario**  
Como administrador, quiero parametrizar tipos de cita, estados y motivos, para que el módulo de citas funcione con reglas configurables de mi empresa.

**Descripción funcional detallada**  
1. Ingreso al módulo de parametrización de citas de mi empresa.  
2. Registro tipos de cita.  
3. Registro estados de cita.  
4. Registro motivos de cancelación.  
5. Registro motivos de reprogramación.  
6. Activo o inactivo los parámetros según necesidad.  
7. El sistema deja disponibles esos parámetros para agendas, asignación, cancelación y reprogramación.

**Reglas de negocio**  
- Los parámetros deben crearse solo para mi empresa.  
- No se puede eliminar un parámetro si ya fue usado en citas.  
- Los parámetros inactivos no deben aparecer en nuevas transacciones.  
- Debe mantenerse trazabilidad de creación y modificación.

**Datos involucrados**  
- `tipo_cita`
- `estado_cita`
- `motivo_cancelacion_cita`
- `motivo_reprogramacion_cita`

**Dependencias**  
- HU-FASE1-002  
- HU-FASE1-006

**Criterios de aceptación**  
- CA1: Solo se visualizan y administran parámetros de mi empresa.  
- CA2: Los parámetros creados quedan disponibles en la asignación y gestión de citas.  
- CA3: No se permite borrar parámetros ya usados.  
- CA4: El sistema registra usuario y fecha de los cambios.

**Prioridad**: Alta

---

### HU-FASE1-040 — Gestión de recursos físicos

**Módulo**: Citas
**Actor principal**: Administrador

**Historia de usuario**
Como administrador, quiero registrar recursos físicos agendables (consultorios, salas, camas) en sedes de mi empresa.

**Descripción funcional detallada**
1. Creo recurso con código, nombre, tipo, sede (de mi empresa).

**Reglas de negocio**
- Recurso pertenece a una sede de mi empresa.
- Código único `(sede_id, codigo)`.

**Datos involucrados**
- `recurso_fisico`, `sede`

**Dependencias**
- HU-FASE1-002.

**Criterios de aceptación**
- CA1: Gestiono recursos de las sedes de mi empresa.
- CA2: No veo recursos de otras empresas.

**Prioridad**: Media

---
### HU-FASE1-040A — Asignación de cama y traslado a hospitalización

**Módulo**: Hospitalización  
**Actor principal**: Coordinador asistencial

**Historia de usuario**  
Como coordinador asistencial, quiero asignar cama y trasladar a hospitalización a un paciente proveniente de urgencias, para dejarlo ubicado y disponible para el médico hospitalario.

**Descripción funcional detallada**  
1. Consulto las solicitudes de hospitalización generadas desde urgencias en mi empresa.  
2. Selecciono el paciente.  
3. El sistema muestra camas o recursos físicos disponibles.  
4. Selecciono cama, sala o ubicación de destino.  
5. Confirmo el traslado.  
6. El sistema cambia el estado del paciente a hospitalizado.  
7. El sistema deja disponible la información para el médico tratante de hospitalización.

**Reglas de negocio**  
- Solo pueden asignarse recursos físicos de mi empresa.  
- Solo pueden asignarse camas disponibles.  
- El paciente debe tener solicitud de hospitalización previa.  
- La asignación debe quedar asociada a fecha, hora y usuario.  
- No puede asignarse más de una cama activa al mismo paciente.

**Datos involucrados**  
- `recurso_fisico`
- `admision`
- `atencion`
- `traslado_paciente`

**Dependencias**  
- HU-FASE1-033  
- HU-FASE1-040

**Criterios de aceptación**  
- CA1: Solo se asignan camas disponibles de mi empresa.  
- CA2: El paciente debe venir con solicitud de hospitalización activa.  
- CA3: La asignación cambia el estado del flujo asistencial.  
- CA4: Queda trazabilidad de usuario, fecha, hora y recurso asignado.

**Prioridad**: Alta

---

### HU-FASE1-041 — Creación de agenda de profesional

**Módulo**: Citas
**Actor principal**: Coordinador de agendas

**Historia de usuario**
Como coordinador, quiero crear agendas para los profesionales de mi empresa en sus sedes.

**Descripción funcional detallada**
1. Creo agenda: profesional (de mi empresa), especialidad, sede (de mi empresa), recurso, calendario, duración, vigencia.
2. Agrego bloques horarios por día de la semana.

**Reglas de negocio**
- Profesional, sede, recurso, calendario deben ser de mi empresa.
- Bloques no superpuestos.
- `empresa_id`, `sede_id` heredados del contexto.

**Datos involucrados**
- `agenda_profesional`, `bloque_agenda`

**Dependencias**
- HU-FASE1-008, HU-FASE1-039, HU-FASE1-040.

**Criterios de aceptación**
- CA1: Creo agendas de mi empresa.
- CA2: No puedo usar profesionales/sedes de otras empresas.

**Prioridad**: Alta

---

### HU-FASE1-042 — Generación de disponibilidad de cita

**Módulo**: Citas
**Actor principal**: Coordinador / Sistema

**Historia de usuario**
Como coordinador, quiero generar disponibilidad para un rango de fechas de las agendas de mi empresa.

**Descripción funcional detallada**
1. Selecciono agenda y rango de fechas.
2. Sistema genera slots respetando el calendario y bloques.

**Reglas de negocio**
- Solo agendas de mi empresa.
- `empresa_id`, `sede_id` heredados de la agenda.
- No duplicar slots existentes.
- No generar en días no hábiles.

**Datos involucrados**
- `disponibilidad_cita`

**Dependencias**
- HU-FASE1-039, HU-FASE1-041.

**Criterios de aceptación**
- CA1: Genero disponibilidad para agendas de mi empresa.
- CA2: Los slots heredan `empresa_id`, `sede_id`.

**Prioridad**: Alta

---

### HU-FASE1-043 — Asignación de cita a paciente

**Módulo**: Citas
**Actor principal**: Recepcionista / Agente call center

**Historia de usuario**
Como recepcionista, quiero asignar citas a pacientes de mi empresa en sedes de mi empresa.

**Descripción funcional detallada**
1. Busco paciente de mi empresa.
2. Filtro disponibilidad por sede, especialidad, profesional, fecha.
3. Selecciono slot y confirmo.
4. Sistema genera número de cita `(empresa_id, numero_cita)` y marca el slot.

**Reglas de negocio**
- Paciente, agenda, servicio, todos de mi empresa.
- Un paciente no puede tener dos citas activas mismo día/hora/profesional.
- `sede_id` del slot debe ser sede de mi empresa.

**Datos involucrados**
- `cita`, `disponibilidad_cita`, `agenda_profesional`, `paciente`, `servicio_salud`

**Dependencias**
- HU-FASE1-013, HU-FASE1-042, HU-FASE1-047.

**Criterios de aceptación**
- CA1: Asigno citas usando recursos de mi empresa.
- CA2: El slot queda marcado como ocupado.
- CA3: No puedo asignar a pacientes ni usar slots de otras empresas.

**Prioridad**: Alta

---

### HU-FASE1-044 — Cancelación y reprogramación de cita

**Módulo**: Citas
**Actor principal**: Recepcionista

**Historia de usuario**
Como recepcionista, quiero cancelar o reprogramar citas de mi empresa.

**Descripción funcional detallada**
1. Ubico cita de mi empresa.
2. Selecciono Cancelar o Reprogramar con motivo del catálogo.
3. Si reprogramo, elijo nuevo slot disponible de mi empresa.

**Reglas de negocio**
- Solo citas de mi empresa.
- Motivo obligatorio.
- Reprogramación conserva número de cita.

**Datos involucrados**
- `cita`, `disponibilidad_cita`

**Dependencias**
- HU-FASE1-043.

**Criterios de aceptación**
- CA1: Cancelo/reprogramo citas de mi empresa.
- CA2: No puedo modificar citas de otras empresas.

**Prioridad**: Alta

---

### HU-FASE1-045 — Gestión de lista de espera

**Módulo**: Citas
**Actor principal**: Recepcionista / Coordinador

**Historia de usuario**
Como recepcionista, quiero inscribir pacientes de mi empresa en lista de espera cuando no hay disponibilidad.

**Descripción funcional detallada**
1. Inscribo paciente con especialidad, servicio, prioridad, fechas preferidas.
2. Al liberarse cupos, el sistema sugiere candidatos.

**Reglas de negocio**
- Pacientes de mi empresa.
- Un paciente no puede repetir entrada por misma especialidad/servicio.

**Datos involucrados**
- `lista_espera_cita`

**Dependencias**
- HU-FASE1-013.

**Criterios de aceptación**
- CA1: Solo veo lista de espera de mi empresa.
- CA2: Inscribo solo pacientes de mi empresa.

**Prioridad**: Media

---

### HU-FASE1-046 — Traslado masivo de agenda

**Módulo**: Citas
**Actor principal**: Coordinador de agendas

**Historia de usuario**
Como coordinador, quiero trasladar citas de una agenda/fecha a otra de mi empresa.

**Descripción funcional detallada**
1. Selecciono agenda origen, fecha origen, agenda destino, fecha destino (todas de mi empresa).
2. Capturo motivo.
3. Sistema intenta trasladar y reporta trasladadas/fallidas.

**Reglas de negocio**
- Todas las agendas deben ser de mi empresa.
- Fallidas quedan en lista de espera.

**Datos involucrados**
- `traslado_agenda`, `detalle_traslado_agenda`, `cita`, `disponibilidad_cita`

**Dependencias**
- HU-FASE1-043.

**Criterios de aceptación**
- CA1: Traslados solo entre agendas de mi empresa.
- CA2: Queda documentado con motivo y usuario.

**Prioridad**: Media

---

## Bloque 7. Servicios, pagadores y contratos

---

### HU-FASE1-047 — Gestión del catálogo de servicios de salud

**Módulo**: Servicios, pagadores y contratos
**Actor principal**: Administrador / Facturación

**Historia de usuario**
Como administrador, quiero mantener el catálogo de servicios de mi empresa (consultas, procedimientos, medicamentos, insumos, estancias).

**Descripción funcional detallada**
1. Creo servicio con código interno (único en mi empresa), CUPS, nombre, categoría, centro de costo (de mi empresa), unidad de medida, si requiere autorización/diagnóstico.

**Reglas de negocio**
- Catálogo por empresa.
- Código interno único `(empresa_id, codigo_interno)`.
- Inactivo no se puede ordenar/prescribir/facturar.

**Datos involucrados**
- `servicio_salud`, `centro_costo`

**Dependencias**
- HU-FASE1-048.

**Criterios de aceptación**
- CA1: Gestiono servicios de mi empresa.
- CA2: No veo servicios de otras empresas.

**Prioridad**: Alta

---

### HU-FASE1-048 — Gestión de centros de costo

**Módulo**: Servicios, pagadores y contratos
**Actor principal**: Administrador / Contabilidad

**Historia de usuario**
Como contador, quiero mantener los centros de costo de mi empresa con jerarquía.

**Descripción funcional detallada**
1. Creo centro de costo con código único en mi empresa, nombre, padre (jerarquía).

**Reglas de negocio**
- Jerarquía solo dentro de mi empresa.
- Un centro con hijos activos no se inactiva.

**Datos involucrados**
- `centro_costo`

**Dependencias**
- Ninguna previa.

**Criterios de aceptación**
- CA1: Gestiono centros de costo de mi empresa.

**Prioridad**: Media

---

### HU-FASE1-049 — Registro de pagador

**Módulo**: Servicios, pagadores y contratos
**Actor principal**: Administrador / Facturación

**Historia de usuario**
Como administrador, quiero registrar los pagadores con los que mi empresa trabaja.

**Descripción funcional detallada**
1. Ubico o creo un tercero tipo "pagador" en mi empresa.
2. Lo marco como pagador con código, tipo, códigos EPS/administradora, días de radicación y respuesta.

**Reglas de negocio**
- Todo pagador es un tercero de mi empresa.
- Código único en mi empresa.
- Tercero 1 a 1 con pagador dentro de la empresa.

**Datos involucrados**
- `pagador`, `tercero`

**Dependencias**
- HU-FASE1-010.

**Criterios de aceptación**
- CA1: Gestiono pagadores de mi empresa.
- CA2: No veo pagadores de otras empresas.

**Prioridad**: Alta

---

### HU-FASE1-050 — Registro de contrato con pagador

**Módulo**: Servicios, pagadores y contratos
**Actor principal**: Administrador / Facturación

**Historia de usuario**
Como administrador, quiero registrar contratos con pagadores de mi empresa.

**Descripción funcional detallada**
1. Creo contrato: número (único en mi empresa), pagador (de mi empresa), modalidad, tarifario (de mi empresa), vigencia, valores.

**Reglas de negocio**
- Todas las referencias deben ser de mi empresa.
- Un contrato vencido no se usa en facturación.

**Datos involucrados**
- `contrato`, `pagador`, `tarifario`

**Dependencias**
- HU-FASE1-049, HU-FASE1-051.

**Criterios de aceptación**
- CA1: Gestiono contratos de mi empresa.
- CA2: No puedo usar pagadores/tarifarios de otras empresas.

**Prioridad**: Alta

---

### HU-FASE1-051 — Carga de tarifario base

**Módulo**: Servicios, pagadores y contratos
**Actor principal**: Administrador / Facturación

**Historia de usuario**
Como administrador, quiero cargar tarifarios base para mi empresa (SOAT, ISS, propio).

**Descripción funcional detallada**
1. Creo tarifario con código, nombre, vigencia.
2. Agrego detalles con servicio (de mi empresa) y valor.
3. Carga masiva desde archivo.

**Reglas de negocio**
- Tarifario y servicios de mi empresa.
- Unicidad `(tarifario_id, servicio_salud_id)`.

**Datos involucrados**
- `tarifario`, `detalle_tarifario`, `servicio_salud`

**Dependencias**
- HU-FASE1-047.

**Criterios de aceptación**
- CA1: Gestiono tarifarios de mi empresa.

**Prioridad**: Alta

---

### HU-FASE1-052 — Registro de tarifas específicas por contrato

**Módulo**: Servicios, pagadores y contratos
**Actor principal**: Administrador / Facturación

**Historia de usuario**
Como administrador, quiero registrar tarifas específicas negociadas en contratos de mi empresa.

**Descripción funcional detallada**
1. Desde un contrato de mi empresa agrego tarifas por servicio, valor, descuento, vigencia.

**Reglas de negocio**
- Contrato y servicio de mi empresa.
- Tarifa de contrato prevalece sobre tarifario base.

**Datos involucrados**
- `tarifa_contrato`, `contrato`, `servicio_salud`

**Dependencias**
- HU-FASE1-050, HU-FASE1-051.

**Criterios de aceptación**
- CA1: Gestiono tarifas por contrato de mi empresa.

**Prioridad**: Alta

---

### HU-FASE1-053 — Asignación de servicios al contrato

**Módulo**: Servicios, pagadores y contratos
**Actor principal**: Administrador / Facturación

**Historia de usuario**
Como administrador, quiero definir qué servicios están cubiertos en cada contrato de mi empresa.

**Descripción funcional detallada**
1. Agrego servicios (de mi empresa) al contrato, con si requieren autorización y cantidad máxima.

**Reglas de negocio**
- Contrato y servicios de mi empresa.
- Unicidad `(contrato_id, servicio_salud_id)`.

**Datos involucrados**
- `servicio_contrato`

**Dependencias**
- HU-FASE1-050.

**Criterios de aceptación**
- CA1: Gestiono cobertura de contratos de mi empresa.

**Prioridad**: Alta

---

## Bloque 8. Facturación inicial

---

### HU-FASE1-054 — Generación de factura desde atención

**Módulo**: Facturación inicial
**Actor principal**: Facturador

**Historia de usuario**
Como facturador, quiero generar la factura de una admisión de mi empresa (y mi sede).

**Descripción funcional detallada**
1. Selecciono admisión cerrada de mi sede.
2. El sistema precarga servicios con tarifas del contrato o tarifario base de mi empresa.
3. Ajusto detalles, sistema calcula totales.
4. Genera número de factura único `(empresa_id, prefijo, numero)`.

**Reglas de negocio**
- Factura hereda `empresa_id`, `sede_id` de la admisión.
- Servicios, contrato, pagador, tarifas deben ser de mi empresa.
- Factura aprobada no se edita.
- Numeración legal autorizada por la DIAN (consecutivo por empresa + prefijo).

**Datos involucrados**
- `factura`, `detalle_factura`, `admision`, `atencion`, `paciente`, `pagador`, `contrato`

**Dependencias**
- HU-FASE1-024, HU-FASE1-050, HU-FASE1-051, HU-FASE1-052, HU-FASE1-053.

**Criterios de aceptación**
- CA1: Facturo admisiones de mi sede.
- CA2: Uso tarifas/contratos/servicios de mi empresa.
- CA3: Numeración consecutiva en mi empresa.

**Prioridad**: Alta

---

### HU-FASE1-055 — Gestión del detalle de factura

**Módulo**: Facturación inicial
**Actor principal**: Facturador

**Historia de usuario**
Como facturador, quiero editar detalles de una factura en borrador de mi empresa.

**Descripción funcional detallada**
1. En facturas en borrador de mi empresa agrego/edito/elimino ítems.
2. Cada cambio recalcula totales.

**Reglas de negocio**
- Solo facturas de mi empresa en borrador.
- Cambios auditados.

**Datos involucrados**
- `detalle_factura`, `factura`

**Dependencias**
- HU-FASE1-054.

**Criterios de aceptación**
- CA1: Edito solo facturas en borrador de mi empresa.

**Prioridad**: Alta

---

### HU-FASE1-055A — Parametrización de copago y cuota moderadora

**Módulo**: Facturación y cartera  
**Actor principal**: Administrador financiero

**Historia de usuario**  
Como administrador financiero, quiero parametrizar reglas de copago y cuota moderadora, para que el sistema liquide correctamente los cobros al paciente según vigencia, régimen, categoría o condición aplicable.

**Descripción funcional detallada**  
1. Ingreso a la parametrización de cobros al paciente de mi empresa.  
2. Registro la vigencia de la regla.  
3. Selecciono el régimen al que aplica.  
4. Indico el tipo de cobro: copago o cuota moderadora.  
5. Defino el criterio de aplicación por rango de ingreso, categoría SISBEN o ambos, según corresponda.  
6. Registro porcentaje de cobro o valor fijo.  
7. Registro tope por evento y tope anual, si aplica.  
8. Defino la unidad de valor usada por la regla.  
9. Configuro observaciones y activo o inactivo la regla.  
10. El sistema permite asociar exenciones por servicio mediante la configuración de servicios exentos de cobro.  
11. El sistema deja disponible la regla para procesos de admisión, liquidación y facturación.

**Reglas de negocio**  
- Las reglas deben configurarse solo para mi empresa.  
- No puede existir más de una regla activa incompatible para la misma vigencia, régimen, tipo de cobro y criterio de aplicación.  
- Las reglas inactivas no deben participar en nuevos cálculos.  
- Los servicios exentos deben manejarse de forma separada mediante su propia parametrización.  
- Debe mantenerse trazabilidad de creación y modificación.

**Datos involucrados**  
- `regla_cobro_paciente`  
- `servicio_exento_cobro`  
- `regimen`  
- `grupo_sisben`  
- `servicio_salud`

**Dependencias**  
- HU-FASE1-047  
- HU-FASE1-050  
- HU-FASE1-052

**Criterios de aceptación**  
- CA1: El sistema permite crear reglas de cobro solo para mi empresa.  
- CA2: El sistema valida que no existan reglas activas duplicadas o solapadas para el mismo criterio.  
- CA3: El sistema permite registrar porcentaje, valor fijo y topes.  
- CA4: El sistema permite definir servicios exentos de cobro.  
- CA5: El sistema registra usuario y fecha de creación o modificación.

**Prioridad**: Alta

---


### HU-FASE1-056 — Cálculo de copago y cuota moderadora

**Módulo**: Facturación y cartera  
**Actor principal**: Sistema

**Historia de usuario**  
Como sistema, quiero calcular automáticamente el copago o la cuota moderadora aplicable al paciente, para determinar el valor correcto a cobrar según la regla vigente, el servicio prestado, las exenciones y los topes definidos.

**Descripción funcional detallada**  
1. El sistema recibe el contexto de liquidación desde la admisión, atención o facturación de mi empresa.  
2. El sistema identifica el paciente, servicio, régimen, contrato y demás datos necesarios para el cálculo.  
3. El sistema consulta las reglas activas de cobro al paciente para la vigencia correspondiente.  
4. El sistema determina si aplica copago o cuota moderadora.  
5. El sistema valida si el servicio está marcado como exento de cobro.  
6. Si el servicio no está exento, el sistema identifica el porcentaje o valor fijo aplicable.  
7. El sistema valida topes por evento y topes anuales según los acumulados del paciente.  
8. El sistema calcula el valor del cobro.  
9. El sistema genera la liquidación del cobro del paciente, asociándola a la admisión, atención o factura cuando corresponda.  
10. El sistema deja disponible la liquidación para recaudo, consulta y auditoría.

**Reglas de negocio**  
- El cálculo solo debe ejecutarse con reglas activas de mi empresa.  
- Para una misma liquidación no debe aplicarse simultáneamente copago y cuota moderadora.  
- Si el servicio está exento, el sistema debe registrar la exención y no generar cobro.  
- Si existen topes por evento o por vigencia, el sistema debe respetarlos.  
- Si no existe una regla válida, el sistema no debe calcular y debe dejar trazabilidad del evento.  
- Toda liquidación debe quedar asociada a la regla utilizada cuando aplique.

**Datos involucrados**  
- `regla_cobro_paciente`  
- `servicio_exento_cobro`  
- `liquidacion_cobro_paciente`  
- `acumulado_cobro_paciente`  
- `paciente`  
- `admision`  
- `atencion`  
- `factura`  
- `servicio_salud`

**Dependencias**  
- HU-FASE1-055A  
- HU-FASE1-017  
- HU-FASE1-018  
- HU-FASE1-019  
- HU-FASE1-047  
- HU-FASE1-050  
- HU-FASE1-052

**Criterios de aceptación**  
- CA1: El sistema calcula el cobro usando únicamente reglas activas de mi empresa.  
- CA2: El sistema identifica si aplica copago o cuota moderadora, pero no ambos al mismo tiempo.  
- CA3: El sistema respeta servicios exentos y registra el motivo de exención.  
- CA4: El sistema aplica topes por evento y topes acumulados cuando corresponda.  
- CA5: El sistema genera una liquidación con valor calculado, regla aplicada y estado inicial de recaudo.  
- CA6: Si no existe una regla válida, el sistema no liquida y deja trazabilidad.

**Prioridad**: Alta

---

### HU-FASE1-056A — Recaudo de copago y cuota moderadora

**Módulo**: Facturación y cartera  
**Actor principal**: Cajero

**Historia de usuario**  
Como cajero, quiero registrar el recaudo de copago o cuota moderadora, para dejar constancia del pago o del estado pendiente del cobro al paciente.

**Descripción funcional detallada**  
1. Consulto la liquidación de cobro generada para el paciente de mi empresa.  
2. El sistema muestra tipo de cobro, servicio, valor calculado, valor cobrado acumulado y estado actual.  
3. Registro el valor pagado por el paciente.  
4. Selecciono el medio de pago.  
5. Registro número de recibo y observaciones si aplica.  
6. Confirmo el recaudo.  
7. El sistema guarda el recaudo asociado a la liquidación.  
8. El sistema actualiza el estado de la liquidación como pagado, parcial, exento o anulado según corresponda.  
9. El sistema actualiza los acumulados del paciente cuando aplique.  
10. El sistema deja disponible el recaudo para consulta y auditoría.

**Reglas de negocio**  
- Solo pueden recaudarse liquidaciones activas de mi empresa.  
- No puede registrarse recaudo sobre liquidaciones anuladas.  
- El sistema debe permitir pago total, parcial o exención.  
- Si la liquidación está exenta, no debe exigirse valor pagado.  
- Todo recaudo debe quedar asociado a medio de pago, usuario y fecha.  
- Los recaudos deben impactar el acumulado del paciente cuando aplique control de topes.

**Datos involucrados**  
- `liquidacion_cobro_paciente`  
- `recaudo_cobro_paciente`  
- `acumulado_cobro_paciente`  
- `medio_pago`

**Dependencias**  
- HU-FASE1-056  
- HU-FASE1-055A

**Criterios de aceptación**  
- CA1: El sistema registra recaudos solo sobre liquidaciones de mi empresa.  
- CA2: El sistema permite registrar pago total, parcial o exención.  
- CA3: El sistema actualiza el estado de la liquidación después del recaudo.  
- CA4: El sistema actualiza acumulados del paciente cuando corresponda.  
- CA5: El sistema conserva trazabilidad del recaudo con usuario, fecha, medio de pago y número de recibo.

**Prioridad**: Media

---

### HU-FASE1-056B — Consulta de liquidación de cobros al paciente

**Módulo**: Facturación y cartera  
**Actor principal**: Facturación

**Historia de usuario**  
Como usuario de facturación, quiero consultar la liquidación de copago y cuota moderadora del paciente, para validar el valor aplicado, su regla de origen y su estado antes de facturar o auditar.

**Descripción funcional detallada**  
1. Ingreso a la consulta de liquidaciones de cobro de mi empresa.  
2. Busco por paciente, admisión, atención, factura, servicio o fecha.  
3. El sistema muestra la regla aplicada al cobro.  
4. El sistema muestra tipo de cobro, base de cálculo, porcentaje aplicado, valor calculado y valor cobrado.  
5. El sistema muestra si hubo exención, motivo de exención y estado del recaudo.  
6. El sistema permite consultar recaudos realizados sobre la liquidación.  
7. El sistema permite consultar acumulados del paciente por vigencia cuando apliquen topes.  
8. El sistema deja disponible esta información como soporte para auditoría y facturación.

**Reglas de negocio**  
- Solo se consultan liquidaciones de mi empresa.  
- La consulta debe mostrar trazabilidad del cálculo, la regla aplicada y el recaudo.  
- Si hubo exención, debe visualizarse su motivo.  
- Si hubo recaudo parcial, debe visualizarse saldo pendiente.  
- Si existen topes aplicados, el sistema debe mostrarlos.

**Datos involucrados**  
- `liquidacion_cobro_paciente`  
- `regla_cobro_paciente`  
- `recaudo_cobro_paciente`  
- `acumulado_cobro_paciente`  
- `servicio_exento_cobro`

**Dependencias**  
- HU-FASE1-056  
- HU-FASE1-056A  
- HU-FASE1-055A

**Criterios de aceptación**  
- CA1: El usuario consulta solo liquidaciones de su empresa.  
- CA2: El sistema muestra regla aplicada, base de cálculo, valor calculado y estado.  
- CA3: El sistema muestra exenciones, recaudos y saldos pendientes.  
- CA4: El sistema muestra acumulados y topes cuando apliquen.  
- CA5: La consulta sirve como soporte para facturación, caja y auditoría.

**Prioridad**: Media

---

### HU-FASE1-057 — Generación de estructura base RIPS

**Módulo**: Facturación inicial
**Actor principal**: Facturador / Auditor

**Historia de usuario**
Como facturador, quiero generar la estructura RIPS de una factura de mi empresa.

**Descripción funcional detallada**
1. Desde factura generada selecciono "Generar RIPS".
2. Se crean encabezado y detalles por tipo (AC/AP/AM/AH/AU/AN/AT/CT/US) en `rips_detalle.linea_datos` JSON.

**Reglas de negocio**
- Una factura → un conjunto RIPS.
- Datos conforme a norma vigente (Res 3374 / 2275).

**Datos involucrados**
- `rips_encabezado`, `rips_detalle`

**Dependencias**
- HU-FASE1-054.

**Criterios de aceptación**
- CA1: Genero RIPS de facturas de mi empresa.

**Prioridad**: Alta

---

### HU-FASE1-058 — Radicación de factura ante pagador

**Módulo**: Facturación inicial
**Actor principal**: Radicador / Facturador

**Historia de usuario**
Como radicador, quiero radicar facturas de mi empresa ante pagadores de mi empresa.

**Descripción funcional detallada**
1. Selecciono factura generada de mi empresa.
2. Capturo fecha, número de radicado (único por pagador), soporte.
3. Sistema calcula fecha límite de respuesta.

**Reglas de negocio**
- Factura y pagador de mi empresa.
- Número de radicado único por pagador.

**Datos involucrados**
- `radicacion`, `factura`, `pagador`

**Dependencias**
- HU-FASE1-054.

**Criterios de aceptación**
- CA1: Radico solo facturas de mi empresa.

**Prioridad**: Alta

---

### HU-FASE1-059 — Creación automática de cuenta por cobrar

**Módulo**: Facturación inicial
**Actor principal**: Sistema (automático)

**Historia de usuario**
Como sistema, al aprobar/radicar una factura quiero crear automáticamente la cuenta por cobrar de la empresa correspondiente.

**Descripción funcional detallada**
1. Al aprobar factura (según política), el sistema crea CxC con empresa, factura, pagador, estado, vigencias, saldo inicial.

**Reglas de negocio**
- `empresa_id` heredado de la factura.
- Un CxC por factura.
- Saldo actualizable por movimientos.

**Datos involucrados**
- `cuenta_por_cobrar`, `movimiento_cuenta_por_cobrar`

**Dependencias**
- HU-FASE1-054.

**Criterios de aceptación**
- CA1: Creación automática con `empresa_id` heredado.

**Prioridad**: Alta

---

### HU-FASE1-060 — Consulta de cartera básica por pagador

**Módulo**: Facturación inicial
**Actor principal**: Cartera / Coordinador financiero

**Historia de usuario**
Como coordinador de cartera, quiero consultar la cartera de mi empresa por pagador.

**Descripción funcional detallada**
1. Accedo a "Cartera por pagador" de mi empresa.
2. Veo totales y edades (0-30, 31-60, 61-90, 91-180, +180).
3. Filtro por pagador, fecha, estado.
4. Exporto a Excel.

**Reglas de negocio**
- Solo pagadores y CxC de mi empresa.

**Datos involucrados**
- `cuenta_por_cobrar`, `movimiento_cuenta_por_cobrar`, `factura`, `pagador`

**Dependencias**
- HU-FASE1-059.

**Criterios de aceptación**
- CA1: Veo cartera exclusivamente de mi empresa.
- CA2: Export también filtrado a mi empresa.

**Prioridad**: Media

---

## Cierre

Este backlog v2 contiene **65 historias de usuario** distribuidas en 9 bloques (incluyendo el nuevo Bloque 0 de multi-tenant y autenticación). Todas respetan los principios transversales de aislamiento (CA-T1 a CA-T5) definidos en la sección 3.

**Cambios vs backlog v1**:
- Nuevo Bloque 0 con 7 HUs de multi-tenant y autenticación (HU-FASE1-000 a HU-FASE1-001F).
- Todas las HUs operativas (admisión, atención, cita, factura, orden, prescripción) especifican filtros por `sede_id`.
- Todas las HUs transaccionales especifican filtros por `empresa_id`.
- Los catálogos globales (CIE-10, tipos de documento, sexo, etc.) se aclaran como compartidos.
- Los catálogos por empresa (servicios de salud, tarifarios, pagadores, contratos, centros de costo, calendarios) quedan aislados por `empresa_id`.

**Próximos pasos sugeridos**:
1. Actualizar `matriz_trazabilidad.md` a v2 con las nuevas HUs y columnas `empresa_id`, `sede_id`.
2. Priorización definitiva con el equipo de negocio.
3. Descomposición técnica de las HUs del Bloque 0 usando el agente JWT multi-tenant.
4. Implementar orden sugerido: Bloque 0 completo → resto de Sprint 1 → Sprint 2 en adelante.