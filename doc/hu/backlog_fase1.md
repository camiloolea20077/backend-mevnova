# Backlog de Historias de Usuario — Fase 1
## Sistema de Gestión Hospitalaria (SGH)

**Documento**: Backlog funcional de fase 1
**Rol emisor**: Analista Funcional Senior
**Base**: Agente base SGH + Agente Analista Funcional Fase 1
**Modelo de datos de referencia**: 106 tablas ya creadas en PostgreSQL (esquema `sgh`)

---

## Tabla de contenido

1. [Inventario de necesidades de fase 1](#1-inventario-de-necesidades-de-fase-1)
2. [Mapa de módulos de fase 1](#2-mapa-de-módulos-de-fase-1)
3. [Lista priorizada de historias de usuario](#3-lista-priorizada-de-historias-de-usuario)
4. [Bloque 1. Seguridad y estructura base](#bloque-1-seguridad-y-estructura-base)
5. [Bloque 2. Terceros y pacientes](#bloque-2-terceros-y-pacientes)
6. [Bloque 3. Admisiones](#bloque-3-admisiones)
7. [Bloque 4. Triage y urgencias](#bloque-4-triage-y-urgencias)
8. [Bloque 5. Hospitalización](#bloque-5-hospitalización)
9. [Bloque 6. Citas](#bloque-6-citas)
10. [Bloque 7. Servicios, pagadores y contratos](#bloque-7-servicios-pagadores-y-contratos)
11. [Bloque 8. Facturación inicial](#bloque-8-facturación-inicial)

---

## 1. Inventario de necesidades de fase 1

La fase 1 debe dejar operativo el núcleo mínimo para que una institución prestadora de servicios de salud pueda:

- Identificar y gestionar a sus pacientes y demás terceros desde una sola fuente de verdad.
- Admitir pacientes por urgencias, consulta externa u hospitalización.
- Clasificar pacientes en urgencias (triage) y atenderlos con registro clínico estructurado.
- Recibir pacientes en hospitalización con nota de ingreso, evolución y egreso básico.
- Asignar citas de consulta externa con agenda real.
- Registrar diagnósticos, órdenes clínicas y prescripciones.
- Mantener catálogos maestros de servicios de salud, pagadores y contratos.
- Generar facturas y preparar la información para RIPS y radicación.
- Administrar usuarios, roles, permisos, sedes y profesionales con trazabilidad.

**Lo que NO entra en fase 1** (explícitamente):

- Historia clínica avanzada (antecedentes complejos, escalas, instrumentos)
- Quirófanos y programación quirúrgica
- Enfermería avanzada (planes de cuidado, medicación por dosis unitaria)
- Farmacia y dispensación
- Laboratorio e imágenes con integración a resultados
- Contabilidad completa (solo se deja preparada la estructura de cuentas por cobrar)
- Interoperabilidad HL7/FHIR
- App móvil de pacientes
- Biometría y foto de pacientes

---

## 2. Mapa de módulos de fase 1

| # | Módulo | Submódulos cubiertos en fase 1 |
|---|--------|--------------------------------|
| 1 | Seguridad y estructura base | usuarios, roles, permisos, sedes, profesionales, servicios habilitados, auditoría |
| 2 | Terceros y pacientes | tercero, paciente, contactos, direcciones, relaciones, SISBEN, seguridad social, contratos del paciente |
| 3 | Admisiones | registro, apertura de atención, cambio de estado, consulta |
| 4 | Triage y urgencias | triage, cola de urgencias, consola de atención, diagnóstico, órdenes, prescripción, conducta |
| 5 | Hospitalización | solicitud, ingreso, nota de ingreso, evolución, órdenes activas, egreso básico |
| 6 | Citas | calendarios, recursos, agendas, disponibilidad, asignación, lista de espera, traslado de agenda |
| 7 | Servicios, pagadores y contratos | catálogo de servicios, centros de costo, pagadores, contratos, tarifas |
| 8 | Facturación inicial | factura, detalle, base RIPS, radicación, cuentas por cobrar básicas |

---

## 3. Lista priorizada de historias de usuario

Priorización sugerida (A=Alta, M=Media, B=Baja). Se ordena por dependencia técnica y valor de negocio.

### Bloque 1 — Seguridad y estructura base
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE1-001 | Gestión de sedes | A |
| HU-FASE1-002 | Gestión de servicios habilitados por sede | A |
| HU-FASE1-003 | Gestión de roles y permisos | A |
| HU-FASE1-004 | Gestión de usuarios del sistema | A |
| HU-FASE1-005 | Autenticación e inicio de sesión | A |
| HU-FASE1-006 | Registro de profesional de salud | A |
| HU-FASE1-007 | Auditoría de acciones del sistema | M |

### Bloque 2 — Terceros y pacientes
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE1-008 | Creación de tercero | A |
| HU-FASE1-009 | Consulta y búsqueda de tercero | A |
| HU-FASE1-010 | Actualización de tercero | A |
| HU-FASE1-011 | Creación de paciente a partir de tercero | A |
| HU-FASE1-012 | Gestión de contactos del tercero | A |
| HU-FASE1-013 | Gestión de direcciones del tercero | A |
| HU-FASE1-014 | Gestión de relaciones entre terceros | M |
| HU-FASE1-015 | Registro de SISBEN del paciente | M |
| HU-FASE1-016 | Registro de seguridad social del paciente | A |
| HU-FASE1-017 | Asociación de contratos al paciente | A |

### Bloque 3 — Admisiones
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE1-018 | Registro de admisión | A |
| HU-FASE1-019 | Apertura automática de atención al admitir | A |
| HU-FASE1-020 | Consulta de admisiones activas | A |
| HU-FASE1-021 | Cambio de estado de admisión | A |
| HU-FASE1-022 | Registro de egreso administrativo de admisión | M |

### Bloque 4 — Triage y urgencias
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE1-023 | Registro de triage de urgencias | A |
| HU-FASE1-024 | Reclasificación de triage | M |
| HU-FASE1-025 | Visualización de cola de urgencias | A |
| HU-FASE1-026 | Consola de atención médica de urgencias | A |
| HU-FASE1-027 | Registro de diagnósticos en la atención | A |
| HU-FASE1-028 | Generación de órdenes clínicas desde la atención | A |
| HU-FASE1-029 | Generación de prescripción desde la atención | A |
| HU-FASE1-030 | Definición de conducta y cierre de atención de urgencias | A |

### Bloque 5 — Hospitalización
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE1-031 | Solicitud de hospitalización desde urgencias | A |
| HU-FASE1-032 | Ingreso hospitalario por médico tratante | A |
| HU-FASE1-033 | Nota de ingreso hospitalario | A |
| HU-FASE1-034 | Registro de evolución hospitalaria | A |
| HU-FASE1-035 | Visualización de órdenes activas del paciente hospitalizado | M |
| HU-FASE1-036 | Egreso hospitalario básico | A |

### Bloque 6 — Citas
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE1-037 | Gestión de calendarios de citas | A |
| HU-FASE1-038 | Gestión de recursos físicos | M |
| HU-FASE1-039 | Creación de agenda de profesional | A |
| HU-FASE1-040 | Generación de disponibilidad de cita | A |
| HU-FASE1-041 | Asignación de cita a paciente | A |
| HU-FASE1-042 | Cancelación y reprogramación de cita | A |
| HU-FASE1-043 | Gestión de lista de espera | M |
| HU-FASE1-044 | Traslado masivo de agenda | M |

### Bloque 7 — Servicios, pagadores y contratos
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE1-045 | Gestión del catálogo de servicios de salud | A |
| HU-FASE1-046 | Gestión de centros de costo | M |
| HU-FASE1-047 | Registro de pagador | A |
| HU-FASE1-048 | Registro de contrato con pagador | A |
| HU-FASE1-049 | Carga de tarifario base | A |
| HU-FASE1-050 | Registro de tarifas específicas por contrato | A |
| HU-FASE1-051 | Asignación de servicios al contrato | A |

### Bloque 8 — Facturación inicial
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE1-052 | Generación de factura desde atención | A |
| HU-FASE1-053 | Gestión del detalle de factura | A |
| HU-FASE1-054 | Cálculo de copago y cuota moderadora | A |
| HU-FASE1-055 | Generación de estructura base RIPS | A |
| HU-FASE1-056 | Radicación de factura ante pagador | A |
| HU-FASE1-057 | Creación automática de cuenta por cobrar | A |
| HU-FASE1-058 | Consulta de cartera básica por pagador | M |

---

## Bloque 1. Seguridad y estructura base

---

### HU-FASE1-001 — Gestión de sedes

**Módulo**: Seguridad y estructura base
**Actor principal**: Administrador del sistema

**Historia de usuario**
Como administrador del sistema, quiero registrar y mantener las sedes de la institución, para que toda la operación asistencial, administrativa y financiera pueda asociarse a una sede específica.

**Objetivo funcional**
Establecer la estructura física y organizativa sobre la cual se atienden pacientes, se habilitan servicios y se asignan usuarios y profesionales.

**Descripción funcional detallada**
1. El administrador accede al módulo de seguridad y estructura base.
2. Ingresa a la opción "Sedes".
3. Visualiza el listado de sedes existentes, con búsqueda por nombre o código.
4. Puede crear una nueva sede capturando: código, código de habilitación REPS, nombre, país, departamento, municipio, dirección, teléfono, correo.
5. Puede editar los datos de una sede existente.
6. Puede inactivar una sede (no se eliminan físicamente).
7. Todos los cambios se registran en la auditoría.

**Reglas de negocio**
- Toda sede debe tener un código único institucional.
- Toda sede debe estar asociada a un municipio (país-departamento-municipio).
- El código de habilitación REPS es opcional pero recomendado para sedes asistenciales.
- Una sede inactiva no puede asociarse a nuevas admisiones, agendas, ni servicios habilitados.
- No se permite eliminar una sede si tiene movimientos históricos.

**Validaciones**
- Campos obligatorios: código, nombre, país, departamento, municipio.
- Código único: no puede repetirse.
- Longitud máxima de nombre: 200 caracteres.
- Correo con formato válido si se registra.

**Datos involucrados**
- `sede`
- `pais`, `departamento`, `municipio`
- `auditoria`

**Dependencias**
- Catálogos geográficos cargados (`pais`, `departamento`, `municipio`).

**Criterios de aceptación**
- CA1: Puedo crear una sede con todos los campos obligatorios y verla en el listado.
- CA2: No puedo crear dos sedes con el mismo código.
- CA3: Puedo inactivar una sede y ya no aparece como disponible en selectores operativos.
- CA4: Los cambios en sedes quedan registrados en la tabla de auditoría con usuario y fecha.
- CA5: Puedo buscar una sede por nombre parcial o código exacto.

**Prioridad**: Alta

**Observaciones**
Es requisito previo para habilitar servicios, crear agendas y registrar admisiones. Debe ser la primera historia ejecutada en el sprint inicial.

---

### HU-FASE1-002 — Gestión de servicios habilitados por sede

**Módulo**: Seguridad y estructura base
**Actor principal**: Administrador del sistema

**Historia de usuario**
Como administrador del sistema, quiero registrar los servicios de salud que cada sede tiene habilitados en el REPS, para que el sistema solo permita prestar servicios donde están habilitados.

**Objetivo funcional**
Garantizar cumplimiento normativo de habilitación y evitar que se facturen servicios en sedes donde no están autorizados.

**Descripción funcional detallada**
1. El administrador accede a la opción "Servicios habilitados".
2. Selecciona la sede.
3. Visualiza los servicios habilitados actuales con su modalidad, complejidad y fechas de vigencia.
4. Puede agregar, editar o inactivar servicios habilitados.
5. El sistema valida vigencia antes de permitir operaciones asistenciales.

**Reglas de negocio**
- Todo servicio habilitado debe asociarse a una sede activa.
- La fecha de vencimiento controla la vigencia de la habilitación.
- Un servicio con habilitación vencida se considera no habilitado para nuevas atenciones.
- Debe indicarse modalidad (intramural, extramural, telemedicina) y complejidad (baja, media, alta).

**Validaciones**
- Campos obligatorios: sede, código de servicio, nombre, modalidad, complejidad.
- Fecha de vencimiento posterior a fecha de habilitación.
- No permitir duplicar el mismo código de servicio activo en la misma sede.

**Datos involucrados**
- `servicio_habilitado`
- `sede`
- `auditoria`

**Dependencias**
- HU-FASE1-001 (Sedes creadas).

**Criterios de aceptación**
- CA1: Puedo registrar un servicio habilitado con todos sus campos obligatorios.
- CA2: El sistema bloquea registrar el mismo servicio activo dos veces en la misma sede.
- CA3: Al consultar, veo claramente cuáles servicios están vigentes y cuáles vencidos.
- CA4: Puedo filtrar por sede, complejidad y estado.

**Prioridad**: Alta

**Observaciones**
Esta historia habilita reglas de validación posteriores en admisiones y citas.

---

### HU-FASE1-003 — Gestión de roles y permisos

**Módulo**: Seguridad y estructura base
**Actor principal**: Administrador del sistema

**Historia de usuario**
Como administrador del sistema, quiero crear roles y asignarles permisos, para controlar qué acciones puede ejecutar cada tipo de usuario en el sistema.

**Objetivo funcional**
Establecer el modelo de autorización basado en roles (RBAC) que controle el acceso a los módulos funcionales del SGH.

**Descripción funcional detallada**
1. El administrador accede a la opción "Roles y permisos".
2. Visualiza los roles existentes (ej: recepcionista, médico de urgencias, facturador, etc.).
3. Crea un rol con código, nombre y descripción.
4. Selecciona los permisos que el rol tendrá, organizados por módulo.
5. Puede inactivar un rol o un permiso.
6. Los cambios quedan auditados.

**Reglas de negocio**
- El código del rol es único.
- Los permisos son un catálogo maestro gestionado por el equipo técnico (no los crea el usuario final).
- Un rol inactivo no puede asignarse a nuevos usuarios; los usuarios que ya lo tienen pierden los permisos asociados.
- Debe existir al menos un rol con permisos administrativos plenos (superadministrador).

**Validaciones**
- Campos obligatorios del rol: código, nombre.
- Al menos un permiso debe estar asignado al rol para activarse.
- Código único de rol.

**Datos involucrados**
- `rol`, `permiso`, `rol_permiso`
- `auditoria`

**Dependencias**
- Catálogo de permisos previamente cargado (semilla técnica).

**Criterios de aceptación**
- CA1: Puedo crear un rol y asignarle múltiples permisos agrupados por módulo.
- CA2: Puedo quitar permisos de un rol existente.
- CA3: Un rol sin permisos no puede activarse.
- CA4: Los cambios de roles y permisos quedan auditados.

**Prioridad**: Alta

**Observaciones**
El catálogo inicial de permisos debe cubrir: gestionar_terceros, gestionar_pacientes, registrar_admision, atender_urgencias, atender_hospitalizacion, asignar_citas, gestionar_contratos, facturar, radicar, administrar_sistema.

---

### HU-FASE1-004 — Gestión de usuarios del sistema

**Módulo**: Seguridad y estructura base
**Actor principal**: Administrador del sistema

**Historia de usuario**
Como administrador del sistema, quiero crear y mantener los usuarios que pueden ingresar al sistema y asignarles roles, para controlar el acceso a las funcionalidades del SGH.

**Objetivo funcional**
Permitir la administración del ciclo de vida de las credenciales de los usuarios y su vinculación a roles y sedes.

**Descripción funcional detallada**
1. El administrador accede a la opción "Usuarios".
2. Busca o crea un usuario capturando: tercero asociado (opcional), nombre de usuario, correo, contraseña inicial.
3. Asigna uno o varios roles al usuario, con sede opcional y vigencia.
4. Puede bloquear, desbloquear, inactivar o forzar cambio de contraseña.
5. El sistema guarda la contraseña como hash y nunca en texto plano.

**Reglas de negocio**
- Nombre de usuario y correo son únicos.
- La contraseña inicial obliga al cambio en el primer ingreso.
- Tras N intentos fallidos (configurable, por defecto 5), el usuario queda bloqueado.
- Un usuario inactivo no puede iniciar sesión.
- Un usuario puede tener distintos roles según la sede.

**Validaciones**
- Campos obligatorios: nombre de usuario, correo, al menos un rol.
- Correo con formato válido.
- Contraseña con política mínima (longitud, mayúscula, número, carácter especial).
- Unicidad de nombre de usuario y correo.

**Datos involucrados**
- `usuario`, `usuario_rol`
- `tercero` (opcional)
- `rol`, `sede`
- `auditoria`

**Dependencias**
- HU-FASE1-001, HU-FASE1-003.

**Criterios de aceptación**
- CA1: Puedo crear un usuario, asignarle roles y ver que puede ingresar al sistema.
- CA2: Tras crear un usuario, el primer ingreso obliga a cambio de contraseña.
- CA3: Un usuario bloqueado no puede iniciar sesión hasta ser desbloqueado.
- CA4: Puedo asignar roles distintos por sede al mismo usuario.
- CA5: Las contraseñas están cifradas (hash) en la base de datos.

**Prioridad**: Alta

**Observaciones**
Integración con HU-FASE1-005 para el flujo real de login.

---

### HU-FASE1-005 — Autenticación e inicio de sesión

**Módulo**: Seguridad y estructura base
**Actor principal**: Todos los usuarios del sistema

**Historia de usuario**
Como usuario del sistema, quiero iniciar sesión con mis credenciales, para acceder a las funcionalidades autorizadas por mi rol.

**Objetivo funcional**
Proveer el mecanismo de autenticación y establecimiento de sesión para todos los usuarios del SGH.

**Descripción funcional detallada**
1. El usuario ingresa nombre de usuario y contraseña.
2. El sistema valida credenciales contra `usuario.hash_password`.
3. Si son correctas: actualiza `fecha_ultimo_ingreso`, reinicia `intentos_fallidos`, abre sesión, registra auditoría de login.
4. Si son incorrectas: incrementa `intentos_fallidos`; al alcanzar el umbral, bloquea al usuario.
5. Si `requiere_cambio_password` está activo, el sistema obliga a cambiar la contraseña antes de continuar.
6. Si el usuario tiene más de una sede, debe seleccionar sede de trabajo.
7. El usuario puede cerrar sesión manualmente; se registra la auditoría.

**Reglas de negocio**
- Un usuario inactivo o bloqueado no puede iniciar sesión.
- La sesión expira por inactividad (parametrizable, por defecto 30 minutos).
- No se muestra nunca si el error fue por usuario o por contraseña (solo "credenciales inválidas").
- Al cambiar contraseña, la nueva no puede ser igual a las últimas 3.

**Validaciones**
- Campos obligatorios: usuario, contraseña.
- Política de contraseña al cambiarla.

**Datos involucrados**
- `usuario`
- `usuario_rol`, `rol_permiso`
- `auditoria`

**Dependencias**
- HU-FASE1-004.

**Criterios de aceptación**
- CA1: Un usuario activo con credenciales correctas accede al sistema.
- CA2: Un usuario bloqueado no puede ingresar y recibe mensaje adecuado.
- CA3: Tras 5 intentos fallidos, el usuario queda bloqueado.
- CA4: El login, logout y cambios de contraseña quedan registrados en auditoría.
- CA5: Un usuario con varias sedes puede seleccionar la sede de trabajo al ingresar.

**Prioridad**: Alta

**Observaciones**
Esta historia es prerrequisito transversal. En fase 2 se puede considerar SSO o 2FA.

---

### HU-FASE1-006 — Registro de profesional de salud

**Módulo**: Seguridad y estructura base
**Actor principal**: Administrador del sistema

**Historia de usuario**
Como administrador del sistema, quiero registrar a los profesionales de salud como una especialización del tercero, para que puedan firmar atenciones, órdenes y prescripciones en el sistema.

**Objetivo funcional**
Mantener el listado oficial de profesionales asistenciales habilitados para actuar en el sistema, respetando la decisión arquitectónica de que todo profesional es primero un tercero.

**Descripción funcional detallada**
1. El administrador busca o crea un tercero del tipo "profesional".
2. Marca al tercero como profesional de salud completando: número de registro médico (ReTHUS), especialidad principal, fecha de ingreso.
3. Puede asociarle un usuario del sistema (HU-FASE1-004) para que pueda iniciar sesión.
4. Puede inactivar a un profesional cuando deja la institución.
5. Los profesionales inactivos no aparecen en selectores de atención ni agendas nuevas.

**Reglas de negocio**
- Todo profesional de salud debe estar asociado a un tercero.
- Un tercero solo puede tener un registro como profesional de salud (relación 1 a 1).
- El número de registro médico es único cuando está presente.
- Un profesional inactivo conserva su historial de atenciones pasadas.

**Validaciones**
- Campos obligatorios: tercero, especialidad principal.
- Número de registro médico único si se captura.

**Datos involucrados**
- `profesional_salud`
- `tercero`, `especialidad`
- `usuario` (opcional)
- `auditoria`

**Dependencias**
- HU-FASE1-008 (tercero debe existir).

**Criterios de aceptación**
- CA1: Puedo registrar un profesional a partir de un tercero existente.
- CA2: No puedo registrar dos profesionales para el mismo tercero.
- CA3: Al inactivar un profesional, no aparece como seleccionable para nuevas atenciones.
- CA4: Puedo asociar un usuario del sistema al profesional.

**Prioridad**: Alta

---

### HU-FASE1-007 — Auditoría de acciones del sistema

**Módulo**: Seguridad y estructura base
**Actor principal**: Administrador del sistema / Auditor

**Historia de usuario**
Como auditor, quiero consultar las acciones realizadas por los usuarios en el sistema, para poder hacer seguimiento, investigación y control.

**Objetivo funcional**
Proveer trazabilidad completa de las acciones sensibles del sistema según los estándares de auditoría en salud.

**Descripción funcional detallada**
1. El sistema registra automáticamente en la tabla `auditoria` toda acción sensible: inserción, actualización, eliminación lógica, login, logout y exportaciones.
2. El auditor accede a la consola de auditoría.
3. Puede filtrar por: tabla, usuario, fecha, acción, IP origen.
4. Visualiza los datos antes y después (campos `datos_antes` y `datos_despues`).
5. Puede exportar el resultado (la exportación también queda auditada).

**Reglas de negocio**
- La auditoría es de solo lectura; nunca se modifica.
- No se elimina ni se depura salvo por políticas de retención acordadas.
- Las acciones sensibles deben registrarse independientemente de si la transacción final fue exitosa.
- El cambio de datos clínicos y financieros debe capturarse obligatoriamente.

**Validaciones**
- Ningún usuario puede editar la tabla `auditoria`.
- La consulta debe ser rápida aun con millones de registros (uso de índices).

**Datos involucrados**
- `auditoria`

**Dependencias**
- HU-FASE1-004, HU-FASE1-005.

**Criterios de aceptación**
- CA1: Toda acción sensible queda registrada automáticamente.
- CA2: Puedo filtrar auditoría por usuario, tabla, fecha y acción.
- CA3: Veo el antes y el después de cada cambio en formato legible.
- CA4: No existe opción para editar registros de auditoría.

**Prioridad**: Media

**Observaciones**
Se recomienda implementar con triggers en base de datos o interceptores en capa de aplicación. La decisión final es técnica.

---

## Bloque 2. Terceros y pacientes

---

### HU-FASE1-008 — Creación de tercero

**Módulo**: Terceros y pacientes
**Actor principal**: Recepcionista / Auxiliar de admisiones / Administrador

**Historia de usuario**
Como recepcionista, quiero crear un tercero en el sistema, para tener una única fuente de verdad de identidad para pacientes, profesionales, proveedores y pagadores.

**Objetivo funcional**
Respetar la decisión arquitectónica de que todo en el sistema (paciente, profesional, pagador, acompañante) parte de la entidad `tercero`.

**Descripción funcional detallada**
1. El usuario accede al módulo "Terceros".
2. Primero ejecuta una búsqueda por tipo y número de documento para evitar duplicados.
3. Si no existe, crea el tercero capturando: tipo de tercero, tipo de documento, número de documento, nombres, apellidos, fecha de nacimiento, sexo, género, identidad de género, orientación sexual, estado civil, nivel de escolaridad, ocupación, pertenencia étnica, país y municipio de nacimiento.
4. Para personas jurídicas se captura razón social en lugar de nombres/apellidos.
5. Guarda el tercero y registra la auditoría.

**Reglas de negocio**
- Un tercero se identifica de forma única por la combinación `tipo_documento` + `numero_documento`.
- Antes de crear un tercero, es obligatorio buscar si ya existe.
- Una persona natural requiere nombres y apellidos; una persona jurídica requiere razón social.
- Un tercero puede pertenecer a varios tipos con el tiempo (paciente hoy, proveedor mañana); el campo `tipo_tercero` es el principal.

**Validaciones**
- Campos obligatorios: tipo de tercero, tipo de documento, número de documento, al menos nombre+apellido o razón social.
- Unicidad: no se permite tipo+número de documento repetido.
- Fecha de nacimiento no puede ser futura.
- Los valores de sexo, género, etc., deben venir de los catálogos vigentes.

**Datos involucrados**
- `tercero`, `tipo_tercero`, `tipo_documento`, `sexo`, `genero`, `estado_civil`, `nivel_escolaridad`, `ocupacion`, `pertenencia_etnica`
- `auditoria`

**Dependencias**
- Catálogos de personas cargados.

**Criterios de aceptación**
- CA1: Puedo crear un tercero persona natural con todos los datos obligatorios.
- CA2: Puedo crear un tercero persona jurídica con razón social.
- CA3: El sistema me impide crear un tercero con tipo+número de documento ya existentes.
- CA4: Antes de permitir crear, el sistema me obliga a buscar por documento.
- CA5: La creación queda registrada en auditoría.

**Prioridad**: Alta

**Observaciones**
Esta historia es base para todas las demás del bloque. Foto y biometría quedan fuera de fase 1.

---

### HU-FASE1-009 — Consulta y búsqueda de tercero

**Módulo**: Terceros y pacientes
**Actor principal**: Cualquier usuario autorizado

**Historia de usuario**
Como usuario del sistema, quiero buscar un tercero de forma rápida, para poder identificar al paciente o entidad con la que voy a operar sin duplicar registros.

**Objetivo funcional**
Proveer una búsqueda flexible y rápida que evite duplicados y acelere los flujos operativos (admisión, citas, facturación).

**Descripción funcional detallada**
1. El usuario accede al buscador de terceros.
2. Puede buscar por: número de documento (búsqueda exacta) o por nombre/apellidos/razón social (búsqueda parcial sin importar tildes).
3. Visualiza resultados paginados con: tipo y número de documento, nombre completo, fecha de nacimiento, tipo de tercero.
4. Selecciona un tercero para ver su ficha completa.
5. Desde la ficha puede acceder a acciones rápidas: ver paciente, ver contactos, ver direcciones, editar.

**Reglas de negocio**
- La búsqueda debe ignorar tildes y ser case-insensitive (usar `unaccent`).
- Se debe mostrar visualmente si un tercero está inactivo.
- La ficha resumen incluye: datos básicos, sexo, edad calculada, si es paciente, pagadores asociados.

**Validaciones**
- Criterio mínimo: número de documento o al menos 3 caracteres en nombres/apellidos.

**Datos involucrados**
- `tercero`, `paciente`, `contacto_tercero`, `direccion_tercero`
- Catálogos relacionados.

**Dependencias**
- HU-FASE1-008.

**Criterios de aceptación**
- CA1: Puedo encontrar un tercero por número de documento en menos de 2 segundos.
- CA2: La búsqueda por nombre ignora tildes y mayúsculas.
- CA3: Veo claramente si un tercero está activo o inactivo.
- CA4: Desde el resultado puedo acceder a la ficha completa.

**Prioridad**: Alta

---

### HU-FASE1-010 — Actualización de tercero

**Módulo**: Terceros y pacientes
**Actor principal**: Recepcionista / Auxiliar de admisiones / Administrador

**Historia de usuario**
Como recepcionista, quiero actualizar los datos de un tercero, para mantener su información al día.

**Objetivo funcional**
Permitir la edición controlada de los datos demográficos de un tercero con trazabilidad.

**Descripción funcional detallada**
1. El usuario abre la ficha del tercero.
2. Selecciona "Editar".
3. Puede modificar todos los campos excepto `tipo_documento` y `numero_documento` (cambio regulado).
4. Al guardar, el sistema registra usuario y fecha de modificación.
5. La auditoría guarda los datos antes y después.

**Reglas de negocio**
- El cambio de tipo o número de documento requiere una acción especial de corrección (fuera de fase 1, requiere justificación).
- No se pueden editar terceros inactivos salvo para reactivarlos.
- La edición de datos clínicos del paciente se hace en la ficha de paciente, no en la ficha del tercero.

**Validaciones**
- Las mismas del alta aplican a la edición.

**Datos involucrados**
- `tercero`
- `auditoria`

**Dependencias**
- HU-FASE1-008, HU-FASE1-009.

**Criterios de aceptación**
- CA1: Puedo editar los datos demográficos de un tercero.
- CA2: No puedo editar tipo ni número de documento.
- CA3: Los cambios quedan en auditoría con antes y después.

**Prioridad**: Alta

---

### HU-FASE1-011 — Creación de paciente a partir de tercero

**Módulo**: Terceros y pacientes
**Actor principal**: Recepcionista / Auxiliar de admisiones

**Historia de usuario**
Como recepcionista, quiero convertir un tercero en paciente, para registrar los datos clínico-sociales necesarios para su atención.

**Objetivo funcional**
Concretar la relación "todo paciente es un tercero" creando la especialización clínica del tercero sin duplicar datos demográficos.

**Descripción funcional detallada**
1. El usuario ubica un tercero existente.
2. Si aún no tiene registro de paciente, selecciona "Crear paciente".
3. Captura: grupo sanguíneo, factor RH, discapacidad, grupo de atención (gestante, menor, víctima, etc.), alergias conocidas, observaciones clínicas.
4. Guarda y el tercero queda habilitado como paciente.

**Reglas de negocio**
- Un tercero puede tener un único registro de paciente (relación 1 a 1).
- No se permite crear paciente si el tercero está inactivo.
- Los datos demográficos no se duplican: se heredan de `tercero`.
- El campo `alergias_conocidas` es un resumen rápido para alertas; no reemplaza los antecedentes clínicos detallados (fase posterior).

**Validaciones**
- No permitir crear un paciente duplicado sobre el mismo tercero.
- Campos opcionales pero altamente recomendados: grupo sanguíneo, RH.

**Datos involucrados**
- `paciente`
- `tercero`, `grupo_sanguineo`, `factor_rh`, `discapacidad`, `grupo_atencion`
- `auditoria`

**Dependencias**
- HU-FASE1-008.

**Criterios de aceptación**
- CA1: Desde la ficha de tercero puedo crear su registro de paciente.
- CA2: No puedo crear un segundo paciente sobre el mismo tercero.
- CA3: Veo en la ficha del paciente los datos heredados del tercero.
- CA4: La creación queda auditada.

**Prioridad**: Alta

---

### HU-FASE1-012 — Gestión de contactos del tercero

**Módulo**: Terceros y pacientes
**Actor principal**: Recepcionista / Auxiliar de admisiones

**Historia de usuario**
Como recepcionista, quiero registrar uno o varios contactos del tercero (teléfono, celular, correo), para poder comunicarme con él cuando sea necesario.

**Objetivo funcional**
Permitir múltiples canales de contacto por tercero con identificación del principal.

**Descripción funcional detallada**
1. Desde la ficha del tercero, el usuario accede a "Contactos".
2. Agrega un contacto indicando: tipo (celular, teléfono fijo, correo), valor, si es principal, si acepta notificaciones.
3. Puede editar o inactivar contactos.
4. Solo un contacto por tipo puede ser marcado como principal.

**Reglas de negocio**
- Cada tercero puede tener múltiples contactos.
- Solo puede haber un contacto principal por tipo.
- Los contactos inactivos no se usan para notificaciones automáticas.

**Validaciones**
- Campos obligatorios: tipo de contacto, valor.
- Formato válido según tipo (correo, número de teléfono).
- Si se marca otro contacto del mismo tipo como principal, el anterior se desmarca automáticamente.

**Datos involucrados**
- `contacto_tercero`, `tipo_contacto`
- `tercero`

**Dependencias**
- HU-FASE1-008.

**Criterios de aceptación**
- CA1: Puedo registrar múltiples contactos para un tercero.
- CA2: Solo un contacto por tipo queda marcado como principal.
- CA3: Un correo inválido no se guarda.
- CA4: Puedo inactivar un contacto sin eliminarlo.

**Prioridad**: Alta

---

### HU-FASE1-013 — Gestión de direcciones del tercero

**Módulo**: Terceros y pacientes
**Actor principal**: Recepcionista / Auxiliar de admisiones

**Historia de usuario**
Como recepcionista, quiero registrar una o varias direcciones del tercero (residencia, correspondencia, facturación), para facilitar los procesos de atención, notificación y facturación electrónica.

**Objetivo funcional**
Permitir múltiples direcciones por tercero clasificadas por tipo.

**Descripción funcional detallada**
1. Desde la ficha del tercero, el usuario accede a "Direcciones".
2. Agrega una dirección indicando: tipo (residencia, correspondencia, facturación, trabajo), zona (urbana/rural), país, departamento, municipio, dirección, barrio, código postal, referencia.
3. Puede registrar coordenadas geográficas (opcional).
4. Marca una dirección como principal.

**Reglas de negocio**
- Debe haber al menos una dirección de residencia principal para pacientes.
- Solo una dirección puede ser principal por tipo.
- La dirección de facturación es la que usa el módulo de facturación electrónica.

**Validaciones**
- Campos obligatorios: tipo, país, departamento, municipio, dirección.
- Municipio debe pertenecer al departamento seleccionado.

**Datos involucrados**
- `direccion_tercero`, `zona_residencia`, `pais`, `departamento`, `municipio`
- `tercero`

**Dependencias**
- HU-FASE1-008.

**Criterios de aceptación**
- CA1: Puedo registrar múltiples direcciones para un tercero.
- CA2: La dirección debe tener país, departamento y municipio coherentes.
- CA3: Si se marca una nueva principal del mismo tipo, la anterior se desmarca.
- CA4: Puedo ver una dirección de residencia vs facturación.

**Prioridad**: Alta

---

### HU-FASE1-014 — Gestión de relaciones entre terceros

**Módulo**: Terceros y pacientes
**Actor principal**: Recepcionista / Auxiliar de admisiones

**Historia de usuario**
Como recepcionista, quiero registrar las relaciones entre terceros (acompañante, responsable, familiar), para identificar contactos de emergencia y responsables del paciente.

**Objetivo funcional**
Soportar el grafo de relaciones interpersonales para procesos asistenciales y administrativos.

**Descripción funcional detallada**
1. Desde la ficha del tercero, el usuario accede a "Relaciones".
2. Agrega una relación indicando: tercero destino, tipo de relación (madre, padre, hijo/a, cónyuge, acompañante, empleador, etc.), si es responsable, si es contacto de emergencia.
3. Puede editar o inactivar relaciones.

**Reglas de negocio**
- Un tercero no puede tener una relación consigo mismo.
- Debe haber al menos un contacto de emergencia recomendado para pacientes menores o vulnerables.
- Una relación se crea en dirección origen → destino. Bidireccionalidad se maneja con un segundo registro si aplica.

**Validaciones**
- Campos obligatorios: tercero destino, tipo de relación.
- `tercero_origen_id <> tercero_destino_id`.

**Datos involucrados**
- `relacion_tercero`, `tipo_relacion`
- `tercero`

**Dependencias**
- HU-FASE1-008.

**Criterios de aceptación**
- CA1: Puedo vincular a dos terceros con un tipo de relación.
- CA2: No puedo vincular un tercero consigo mismo.
- CA3: Puedo marcar una relación como contacto de emergencia.
- CA4: Puedo inactivar una relación sin eliminarla.

**Prioridad**: Media

---

### HU-FASE1-015 — Registro de SISBEN del paciente

**Módulo**: Terceros y pacientes
**Actor principal**: Auxiliar de admisiones

**Historia de usuario**
Como auxiliar de admisiones, quiero registrar la clasificación SISBEN del paciente, para soportar reglas de cobro y acceso al régimen subsidiado.

**Objetivo funcional**
Mantener historial de clasificaciones SISBEN del paciente para efectos asistenciales y financieros.

**Descripción funcional detallada**
1. Desde la ficha del paciente, el usuario accede a "SISBEN".
2. Registra: grupo SISBEN (A1, A2, B1...), puntaje, ficha SISBEN, fecha de encuesta, fecha de vigencia desde/hasta.
3. Solo un SISBEN puede estar vigente a la vez.
4. Al registrar un nuevo SISBEN vigente, el anterior queda automáticamente como no vigente.

**Reglas de negocio**
- Un paciente puede tener múltiples registros SISBEN en el tiempo, pero solo uno vigente.
- El grupo SISBEN afecta reglas de copago/cuota moderadora (ver HU-FASE1-054).
- El sistema debe alertar si la vigencia está por vencer en menos de 30 días.

**Validaciones**
- Campos obligatorios: grupo SISBEN, fecha vigencia desde.
- Fecha vigencia hasta posterior a vigencia desde.
- Puntaje numérico entre 0 y 100.

**Datos involucrados**
- `sisben_paciente`, `grupo_sisben`
- `paciente`

**Dependencias**
- HU-FASE1-011.

**Criterios de aceptación**
- CA1: Puedo registrar un SISBEN al paciente.
- CA2: Al registrar uno nuevo vigente, el anterior queda marcado no vigente.
- CA3: Veo el historial SISBEN del paciente.

**Prioridad**: Media

---

### HU-FASE1-016 — Registro de seguridad social del paciente

**Módulo**: Terceros y pacientes
**Actor principal**: Auxiliar de admisiones

**Historia de usuario**
Como auxiliar de admisiones, quiero registrar la afiliación a seguridad social del paciente (EPS, régimen, tipo de afiliación), para saber quién cubre su atención y aplicar reglas de facturación.

**Objetivo funcional**
Mantener la historia de afiliaciones del paciente y soportar la selección del pagador correcto al admitir.

**Descripción funcional detallada**
1. Desde la ficha del paciente, el usuario accede a "Seguridad social".
2. Registra: pagador (EPS), régimen (contributivo/subsidiado/especial/excepción/particular), categoría de afiliación, tipo de afiliación (cotizante/beneficiario), número de afiliación, cotizante si es beneficiario, fecha de afiliación, fecha de vigencia desde/hasta.
3. Solo una afiliación puede estar vigente a la vez.

**Reglas de negocio**
- Un paciente puede tener historial de afiliaciones; solo una vigente.
- Si es beneficiario, debe asociar el tercero cotizante.
- Si es particular, no se requiere EPS, pero sí se registra un pagador de tipo "Particular".
- Al admitir, el sistema sugiere la afiliación vigente como pagador.

**Validaciones**
- Campos obligatorios: pagador, régimen, fecha de afiliación, fecha vigencia desde.
- Si tipo de afiliación = beneficiario, obligatorio cotizante.

**Datos involucrados**
- `seguridad_social_paciente`, `regimen`, `categoria_afiliacion`, `tipo_afiliacion`
- `paciente`, `pagador`, `tercero`

**Dependencias**
- HU-FASE1-011, HU-FASE1-047.

**Criterios de aceptación**
- CA1: Puedo registrar la afiliación vigente del paciente.
- CA2: Si es beneficiario, debo informar al cotizante (tercero).
- CA3: Al registrar una nueva vigente, la anterior deja de estarlo.
- CA4: La afiliación vigente aparece preseleccionada al admitir al paciente.

**Prioridad**: Alta

---

### HU-FASE1-017 — Asociación de contratos al paciente

**Módulo**: Terceros y pacientes
**Actor principal**: Auxiliar de admisiones / Facturación

**Historia de usuario**
Como auxiliar de admisiones, quiero asociar contratos específicos (pólizas, convenios) al paciente, para facturar correctamente los servicios que estén cubiertos por contratos especiales.

**Objetivo funcional**
Permitir que un paciente tenga contratos comerciales adicionales al de su EPS (pólizas, medicina prepagada, convenios especiales).

**Descripción funcional detallada**
1. Desde la ficha del paciente, el usuario accede a "Contratos".
2. Selecciona un contrato existente (del catálogo institucional) y registra: número de póliza, fecha vigencia desde/hasta.
3. Puede haber varios contratos vigentes simultáneamente (ej: EPS + póliza complementaria).
4. Al admitir al paciente, el sistema muestra los contratos vigentes para elegir cuál aplica.

**Reglas de negocio**
- Solo se pueden asociar contratos que estén activos y vigentes.
- La vigencia por paciente puede ser igual o menor a la del contrato institucional.
- El número de póliza es específico del paciente dentro del contrato.

**Validaciones**
- Campos obligatorios: contrato, fecha vigencia desde.
- Fecha vigencia hasta no posterior a la del contrato institucional.

**Datos involucrados**
- `contrato_paciente`
- `paciente`, `contrato`

**Dependencias**
- HU-FASE1-011, HU-FASE1-048.

**Criterios de aceptación**
- CA1: Puedo asociar uno o varios contratos vigentes al paciente.
- CA2: Al admitir, veo los contratos disponibles para aplicar a la atención.
- CA3: Un contrato vencido ya no aparece como seleccionable.

**Prioridad**: Alta

---

## Bloque 3. Admisiones

---

### HU-FASE1-018 — Registro de admisión

**Módulo**: Admisiones
**Actor principal**: Auxiliar de admisiones / Recepcionista

**Historia de usuario**
Como auxiliar de admisiones, quiero registrar formalmente la admisión de un paciente, para iniciar su episodio de atención con todos los datos administrativos y financieros necesarios.

**Objetivo funcional**
Crear el registro formal del ingreso del paciente a la institución, que agrupa posteriormente una o varias atenciones.

**Descripción funcional detallada**
1. El usuario selecciona o crea al paciente (HU-FASE1-008 a 017).
2. Valida que la afiliación del paciente esté vigente.
3. Captura: tipo de admisión (urgencias, consulta externa, hospitalización, cirugía), origen de atención (enfermedad general, accidente de trabajo, accidente de tránsito, etc.), sede, pagador (sugerido según afiliación vigente), contrato (si aplica), motivo de ingreso, acompañante (tercero).
4. El sistema asigna un número de admisión consecutivo.
5. Al guardar, queda en estado "admitido" y el paciente queda en la cola del módulo correspondiente (urgencias, hospitalización, etc.).

**Reglas de negocio**
- No se admite un paciente sin identificar (debe existir como tercero y paciente).
- La afiliación debe estar vigente o el sistema debe exigir autorización manual y justificación.
- El número de admisión es consecutivo por sede y prefijo.
- Una admisión abierta (estado distinto a "egreso") bloquea nuevas admisiones del mismo tipo para el mismo paciente en la misma sede.

**Validaciones**
- Campos obligatorios: paciente, tipo de admisión, origen de atención, sede, pagador.
- No duplicidad de admisión abierta del mismo tipo.
- Acompañante, si se registra, debe ser un tercero existente.

**Datos involucrados**
- `admision`, `tipo_admision`, `estado_admision`, `origen_atencion`
- `paciente`, `pagador`, `contrato`, `sede`, `tercero`
- `auditoria`

**Dependencias**
- HU-FASE1-001, HU-FASE1-011, HU-FASE1-016, HU-FASE1-047, HU-FASE1-048.

**Criterios de aceptación**
- CA1: Puedo admitir un paciente seleccionando tipo, origen, sede, pagador y contrato.
- CA2: El sistema genera automáticamente un número de admisión.
- CA3: No puedo admitir dos veces al mismo paciente para el mismo tipo en la misma sede al tiempo.
- CA4: La admisión queda en estado "admitido" y visible en la cola correspondiente.
- CA5: El registro queda auditado.

**Prioridad**: Alta

---

### HU-FASE1-019 — Apertura automática de atención al admitir

**Módulo**: Admisiones
**Actor principal**: Sistema (automático)

**Historia de usuario**
Como sistema, al registrar una admisión quiero abrir automáticamente la atención asociada, para que el proceso clínico esté inmediatamente disponible para el profesional.

**Objetivo funcional**
Evitar pasos manuales innecesarios y garantizar que toda admisión tenga al menos una atención asociada.

**Descripción funcional detallada**
1. Al guardar la admisión, el sistema crea automáticamente el primer registro en `atencion` asociado a esa admisión.
2. El estado inicial de la atención es "pendiente" (para consulta externa/cirugía) o "en triage" (para urgencias).
3. La atención queda visible para el profesional o para triage, según el flujo.
4. En hospitalización, la atención inicial la toma el médico tratante al aceptar al paciente.

**Reglas de negocio**
- Una admisión siempre debe tener al menos una atención.
- Pueden agregarse nuevas atenciones durante el episodio (ej: interconsultas).
- El profesional asignado se define al momento de la valoración, no al admitir.

**Validaciones**
- La creación automática nunca falla por falta de datos; si faltan, se crea la atención con los mínimos y el resto se completa en la valoración.

**Datos involucrados**
- `atencion`, `admision`
- `estado_atencion`, `finalidad_atencion`

**Dependencias**
- HU-FASE1-018.

**Criterios de aceptación**
- CA1: Al admitir, automáticamente se crea la atención asociada.
- CA2: La atención queda en estado inicial según el tipo de admisión.
- CA3: Si la creación de atención falla, la admisión también falla (transacción atómica).

**Prioridad**: Alta

---

### HU-FASE1-020 — Consulta de admisiones activas

**Módulo**: Admisiones
**Actor principal**: Auxiliar de admisiones / Coordinador asistencial

**Historia de usuario**
Como coordinador asistencial, quiero ver en un listado las admisiones activas por sede, para tener visibilidad global de pacientes en proceso de atención.

**Objetivo funcional**
Proveer un tablero operativo de admisiones con filtros rápidos.

**Descripción funcional detallada**
1. El usuario accede a "Admisiones activas".
2. Visualiza un listado con: número de admisión, paciente, tipo de admisión, estado, fecha/hora de admisión, sede, pagador.
3. Puede filtrar por: sede, tipo de admisión, estado, fecha, pagador.
4. Puede abrir una admisión para ver su detalle y sus atenciones.

**Reglas de negocio**
- Una admisión se considera activa mientras no esté en estado "egreso".
- El listado por defecto muestra admisiones de la sede del usuario.

**Validaciones**
- El usuario solo ve admisiones de sedes a las que tiene acceso (según rol).

**Datos involucrados**
- `admision`, `paciente`, `tercero`, `pagador`, `sede`

**Dependencias**
- HU-FASE1-018.

**Criterios de aceptación**
- CA1: Veo las admisiones activas de mi sede con los datos principales.
- CA2: Puedo filtrar por tipo de admisión y estado.
- CA3: Puedo abrir una admisión y ver sus atenciones.

**Prioridad**: Alta

---

### HU-FASE1-021 — Cambio de estado de admisión

**Módulo**: Admisiones
**Actor principal**: Auxiliar de admisiones / Profesional asistencial

**Historia de usuario**
Como auxiliar de admisiones, quiero cambiar el estado de una admisión (ej: de admitido a en observación, a hospitalizado, a egreso), para reflejar el avance del paciente en el proceso.

**Objetivo funcional**
Garantizar trazabilidad del estado de la admisión a lo largo del episodio.

**Descripción funcional detallada**
1. El usuario abre la admisión.
2. Selecciona "Cambiar estado" y elige el nuevo estado del catálogo.
3. Captura observaciones del cambio.
4. El cambio queda registrado con usuario, fecha y observación.
5. Ciertos cambios de estado disparan eventos en otros módulos (ej: "hospitalizado" abre proceso de asignación de cama).

**Reglas de negocio**
- Los cambios de estado siguen una máquina de estados definida (no todo estado transita a todo estado).
- El cambio a "egreso" requiere confirmación y es irreversible.
- Estados base: admitido, en triage, en atención, en observación, hospitalizado, egreso.

**Validaciones**
- La transición debe ser válida según la máquina de estados.
- Observación obligatoria para estados finales (egreso, fallecimiento).

**Datos involucrados**
- `admision`, `estado_admision`
- `auditoria`

**Dependencias**
- HU-FASE1-018.

**Criterios de aceptación**
- CA1: Puedo cambiar el estado siguiendo la máquina de estados definida.
- CA2: No puedo saltar estados no válidos.
- CA3: Los cambios quedan en auditoría.

**Prioridad**: Alta

---

### HU-FASE1-022 — Registro de egreso administrativo de admisión

**Módulo**: Admisiones
**Actor principal**: Auxiliar de admisiones

**Historia de usuario**
Como auxiliar de admisiones, quiero cerrar administrativamente una admisión al egresar el paciente, para dejar el episodio completo y habilitar facturación.

**Objetivo funcional**
Cerrar formalmente el episodio de atención y dar paso al proceso de facturación.

**Descripción funcional detallada**
1. Cuando la atención final está cerrada (alta, remisión, egreso hospitalario), el usuario registra el egreso administrativo.
2. Captura fecha y hora de egreso, tipo de egreso, observaciones.
3. El sistema valida que todas las atenciones estén cerradas.
4. Al cerrar, la admisión queda lista para facturar.

**Reglas de negocio**
- No se puede cerrar admisión con atenciones abiertas.
- Una admisión cerrada ya no permite modificar atenciones clínicas salvo por corrección formal.
- La fecha de egreso debe ser posterior a la fecha de admisión.

**Validaciones**
- Fecha y hora obligatorias.
- Todas las atenciones deben estar cerradas.

**Datos involucrados**
- `admision`, `atencion`
- `auditoria`

**Dependencias**
- HU-FASE1-018, HU-FASE1-019, HU-FASE1-030, HU-FASE1-036.

**Criterios de aceptación**
- CA1: Puedo cerrar la admisión cuando todas sus atenciones están cerradas.
- CA2: Si hay atención abierta, el sistema me lo advierte y no permite cerrar.
- CA3: La admisión cerrada queda disponible para facturación.

**Prioridad**: Media

---

## Bloque 4. Triage y urgencias

---

### HU-FASE1-023 — Registro de triage de urgencias

**Módulo**: Triage y urgencias
**Actor principal**: Profesional de triage (médico o enfermera entrenada)

**Historia de usuario**
Como profesional de triage, quiero clasificar al paciente que llega a urgencias según su prioridad clínica, para que sea atendido en el orden correcto.

**Objetivo funcional**
Implementar la clasificación de urgencias (Resolución 5596 de 2015) registrando signos vitales, motivo de consulta inicial y nivel de triage.

**Descripción funcional detallada**
1. El paciente ya está admitido en urgencias (HU-FASE1-018).
2. El profesional de triage selecciona al paciente en la cola de "pendientes de triage".
3. Captura: motivo de consulta inicial, signos vitales (TA, FC, FR, Tº, SatO2, peso, glucometría si aplica), nivel de triage (I a V), observaciones.
4. Guarda y el paciente pasa a la cola de atención médica con prioridad según el nivel.

**Reglas de negocio**
- El nivel de triage I (reanimación) tiene atención inmediata.
- El nivel II (emergencia) tiene atención en menos de 30 minutos.
- Todo triage debe registrar al menos motivo de consulta y nivel.
- Un paciente solo puede tener un triage activo por admisión, pero puede ser reclasificado.

**Validaciones**
- Campos obligatorios: motivo de consulta, nivel de triage.
- Rangos válidos para signos vitales (TA sistólica 40-300, FC 20-250, etc.).
- Nivel de triage entre I y V.

**Datos involucrados**
- `atencion` (creada en HU-FASE1-019)
- `admision`
- En fase 1 se usa la atención para registrar triage; en fase 2 puede crearse tabla `triage` dedicada.

**Dependencias**
- HU-FASE1-018, HU-FASE1-019.

**Criterios de aceptación**
- CA1: Puedo seleccionar un paciente admitido y registrar su triage.
- CA2: Debo registrar motivo y nivel para poder guardar.
- CA3: El paciente aparece en la cola de atención médica ordenado por nivel de triage.
- CA4: El registro queda auditado y con sello de tiempo exacto.

**Prioridad**: Alta

**Observaciones**
Evaluar en fase 2 crear la tabla dedicada `triage_urgencias` con escalas estandarizadas (ATS, Manchester).

---

### HU-FASE1-024 — Reclasificación de triage

**Módulo**: Triage y urgencias
**Actor principal**: Profesional de triage

**Historia de usuario**
Como profesional de triage, quiero reclasificar el triage de un paciente cuando su condición cambia en la sala de espera, para ajustar su prioridad.

**Objetivo funcional**
Permitir que el paciente que empeora o mejora sea reubicado en la cola según su nueva condición.

**Descripción funcional detallada**
1. El profesional selecciona al paciente desde la sala de espera.
2. Registra nuevo triage con nuevos signos vitales y justificación del cambio.
3. El sistema conserva el triage anterior y marca el nuevo como vigente.
4. La cola se reordena automáticamente.

**Reglas de negocio**
- La reclasificación no reemplaza al triage inicial; se registra como un nuevo triage con trazabilidad.
- Requiere justificación clara.
- Solo aplica si el paciente aún no ha sido atendido por médico.

**Validaciones**
- Justificación obligatoria.
- El nuevo nivel debe ser distinto al anterior.

**Datos involucrados**
- `atencion` (historial de triage)

**Dependencias**
- HU-FASE1-023.

**Criterios de aceptación**
- CA1: Puedo reclasificar mientras el paciente no haya sido atendido por médico.
- CA2: La justificación es obligatoria.
- CA3: Veo el historial completo de triages del paciente.

**Prioridad**: Media

---

### HU-FASE1-025 — Visualización de cola de urgencias

**Módulo**: Triage y urgencias
**Actor principal**: Médico general de urgencias / Coordinador asistencial

**Historia de usuario**
Como médico general de urgencias, quiero ver la cola de pacientes ordenada por prioridad clínica, para atender primero a los más críticos.

**Objetivo funcional**
Dar visibilidad en tiempo real a la cola de urgencias, con indicadores operativos clave.

**Descripción funcional detallada**
1. El médico accede al tablero "Urgencias".
2. Visualiza la lista de pacientes admitidos en urgencias ordenados por: nivel de triage, luego tiempo de espera.
3. Cada fila muestra: nombre, documento, edad, sexo, nivel de triage, signos vitales alertas, tiempo desde triage, estado de la atención.
4. Puede filtrar por nivel, tiempo de espera, estado.
5. Selecciona un paciente para abrir la consola de atención (HU-FASE1-026).

**Reglas de negocio**
- El orden por defecto es: nivel ascendente, luego tiempo de espera descendente.
- Los pacientes con tiempo de espera superior al SLA del nivel se resaltan en rojo.
- Los pacientes ya en atención se muestran en una sección aparte.

**Validaciones**
- El usuario solo ve pacientes de su sede y servicio.

**Datos involucrados**
- `admision`, `atencion`
- `paciente`, `tercero`

**Dependencias**
- HU-FASE1-023.

**Criterios de aceptación**
- CA1: Veo la cola ordenada correctamente.
- CA2: Los pacientes fuera de SLA se resaltan.
- CA3: Puedo abrir la atención desde la cola.

**Prioridad**: Alta

---

### HU-FASE1-026 — Consola de atención médica de urgencias

**Módulo**: Triage y urgencias
**Actor principal**: Médico general de urgencias

**Historia de usuario**
Como médico general de urgencias, quiero una única consola donde pueda ver la información completa del paciente y registrar toda la atención en un flujo limpio, para no perder tiempo saltando entre pantallas.

**Objetivo funcional**
Materializar la "Vista 1. Consola de atención de urgencias" definida en el agente base.

**Descripción funcional detallada**
1. El médico abre la consola para un paciente desde la cola.
2. La consola muestra un encabezado fijo con: foto (fase futura), nombre, edad, sexo, documento, pagador, contrato, alertas clínicas (alergias), nivel de triage, signos vitales.
3. En el cuerpo, presenta secciones navegables: motivo de consulta, enfermedad actual, antecedentes, examen físico, diagnósticos, plan y conducta.
4. Ofrece acciones rápidas en barra lateral: solicitar laboratorio, solicitar imágenes, formular medicamentos, hospitalizar, dar alta, remitir.
5. Muestra una línea de tiempo clínica con triage, signos vitales, órdenes y prescripciones.
6. El médico guarda conforme avanza; al finalizar, define conducta (HU-FASE1-030).

**Reglas de negocio**
- El médico puede ingresar en cualquier momento, pero solo puede cerrar cuando haya diagnóstico y conducta.
- Todo cambio queda en auditoría.
- Los campos clínicos son texto libre estructurado (con autocompletados sugeridos).

**Validaciones**
- Para cerrar la atención se requieren: al menos un diagnóstico principal y una conducta definida.

**Datos involucrados**
- `atencion`, `admision`, `paciente`, `tercero`
- `diagnostico_atencion`, `orden_clinica`, `prescripcion`
- `auditoria`

**Dependencias**
- HU-FASE1-023, HU-FASE1-025, HU-FASE1-027, HU-FASE1-028, HU-FASE1-029.

**Criterios de aceptación**
- CA1: Veo el encabezado fijo con datos clave del paciente en todo momento.
- CA2: Puedo navegar entre secciones sin perder información capturada.
- CA3: Puedo solicitar órdenes y formular medicamentos desde acciones rápidas.
- CA4: Veo la línea de tiempo actualizada del episodio.
- CA5: No puedo cerrar la atención sin diagnóstico y conducta.

**Prioridad**: Alta

**Observaciones**
La consola debe ser usable en tablet. Priorizar velocidad y mínima fricción.

---

### HU-FASE1-027 — Registro de diagnósticos en la atención

**Módulo**: Triage y urgencias
**Actor principal**: Médico general de urgencias / Médico hospitalario

**Historia de usuario**
Como médico, quiero registrar los diagnósticos del paciente codificados con CIE-10, para documentar el caso, soportar facturación y generar RIPS.

**Objetivo funcional**
Codificar correctamente los diagnósticos según CIE-10 con sus tipos (principal, relacionado, complicación, comorbilidad).

**Descripción funcional detallada**
1. Desde la consola de atención, el médico busca diagnósticos por código o nombre.
2. Selecciona uno o varios y los marca como: principal, relacionado, complicación o comorbilidad.
3. Indica si es confirmado o presuntivo, si es recurrente.
4. Registra observaciones opcionales.

**Reglas de negocio**
- Solo puede haber un diagnóstico principal por atención.
- Para cerrar una atención se requiere al menos el diagnóstico principal.
- La búsqueda debe ignorar tildes y buscar por prefijo/fragmento.
- El sistema debe validar coherencia con sexo y edad del paciente (ej: diagnósticos de parto solo en pacientes femeninas).

**Validaciones**
- Código CIE-10 válido (existente en catálogo).
- Obligatorio al menos un principal al cerrar.
- Coherencia sexo/edad.

**Datos involucrados**
- `diagnostico_atencion`, `catalogo_diagnostico`
- `atencion`

**Dependencias**
- HU-FASE1-026, catálogo CIE-10 cargado.

**Criterios de aceptación**
- CA1: Puedo agregar múltiples diagnósticos, uno marcado como principal.
- CA2: No puedo tener dos principales en la misma atención.
- CA3: La búsqueda por código o nombre funciona correctamente.
- CA4: El sistema me alerta si hay incoherencia con sexo o edad.

**Prioridad**: Alta

---

### HU-FASE1-028 — Generación de órdenes clínicas desde la atención

**Módulo**: Triage y urgencias
**Actor principal**: Médico general de urgencias / Médico hospitalario

**Historia de usuario**
Como médico, quiero generar órdenes clínicas (laboratorio, imágenes, procedimientos, interconsultas) desde la consola de atención, para solicitar los servicios requeridos por el paciente.

**Objetivo funcional**
Permitir la ordenación clínica estructurada con servicios del catálogo y diagnóstico asociado.

**Descripción funcional detallada**
1. Desde la consola de atención, el médico selecciona "Nueva orden".
2. Elige tipo de orden (laboratorio, imágenes, procedimiento, interconsulta).
3. Agrega uno o varios ítems del catálogo de servicios (`servicio_salud`) con diagnóstico asociado, cantidad, indicaciones y urgencia.
4. Guarda y el sistema genera número de orden y la envía al área correspondiente.

**Reglas de negocio**
- Toda orden debe asociarse a una atención abierta.
- Todo detalle de orden debe tener diagnóstico asociado.
- Cada detalle queda con estado "pendiente" hasta que sea ejecutado.
- El número de orden es consecutivo institucional.

**Validaciones**
- Campos obligatorios: tipo de orden, al menos un servicio.
- El servicio debe estar activo.
- El diagnóstico debe existir en la atención.

**Datos involucrados**
- `orden_clinica`, `detalle_orden_clinica`
- `servicio_salud`, `catalogo_diagnostico`, `atencion`

**Dependencias**
- HU-FASE1-026, HU-FASE1-027, HU-FASE1-045.

**Criterios de aceptación**
- CA1: Puedo crear una orden con varios servicios asociados a diagnósticos.
- CA2: El número de orden se genera automáticamente.
- CA3: La orden queda visible en la línea de tiempo del paciente.
- CA4: Los detalles quedan en estado "pendiente".

**Prioridad**: Alta

---

### HU-FASE1-029 — Generación de prescripción desde la atención

**Módulo**: Triage y urgencias
**Actor principal**: Médico general de urgencias / Médico hospitalario

**Historia de usuario**
Como médico, quiero formular medicamentos desde la consola de atención, para indicar al paciente el tratamiento farmacológico.

**Objetivo funcional**
Generar prescripciones estructuradas con dosis, vía, frecuencia y duración.

**Descripción funcional detallada**
1. Desde la consola, el médico selecciona "Nueva prescripción".
2. Agrega uno o varios medicamentos del catálogo de servicios (categoría medicamento).
3. Por cada medicamento captura: dosis, unidad, vía de administración, frecuencia, duración, cantidad a despachar, indicaciones.
4. Marca si es medicamento NO PBS (activa registro MIPRES).
5. Guarda y el sistema genera el número de prescripción.

**Reglas de negocio**
- Toda prescripción se asocia a la atención del paciente.
- La prescripción tiene vigencia configurable (por defecto 30 días).
- Para medicamentos NO PBS se debe generar código MIPRES.
- El sistema debe alertar interacciones conocidas en fase posterior.

**Validaciones**
- Campos obligatorios: medicamento, dosis, vía, frecuencia, duración.
- Cantidad coherente con dosis × frecuencia × duración.

**Datos involucrados**
- `prescripcion`, `detalle_prescripcion`
- `servicio_salud`, `via_administracion`, `frecuencia_dosis`
- `atencion`

**Dependencias**
- HU-FASE1-026, HU-FASE1-045.

**Criterios de aceptación**
- CA1: Puedo formular uno o varios medicamentos.
- CA2: Debo indicar dosis, vía, frecuencia y duración.
- CA3: El sistema genera número de prescripción automáticamente.
- CA4: Una prescripción NO PBS queda marcada para MIPRES.

**Prioridad**: Alta

---

### HU-FASE1-030 — Definición de conducta y cierre de atención de urgencias

**Módulo**: Triage y urgencias
**Actor principal**: Médico general de urgencias

**Historia de usuario**
Como médico general de urgencias, quiero definir la conducta al finalizar la atención (alta, observación, remisión u hospitalización) y cerrar la atención, para completar el proceso.

**Objetivo funcional**
Cerrar formalmente la atención de urgencias documentando el desenlace.

**Descripción funcional detallada**
1. El médico completa diagnóstico, órdenes y prescripción.
2. Selecciona la conducta: alta, observación, remisión, hospitalización.
3. Según la conducta, el sistema:
   - Alta: cierra atención y permite egreso administrativo.
   - Observación: mantiene atención abierta y paciente en sala de observación.
   - Remisión: genera documento de remisión (detalle en fase 2) y cierra atención.
   - Hospitalización: dispara HU-FASE1-031.
4. Registra plan y conducta final con firma del profesional.

**Reglas de negocio**
- No se puede cerrar sin diagnóstico principal.
- La hospitalización requiere justificación médica.
- La remisión requiere entidad destino y motivo.
- Una vez cerrada la atención no se pueden modificar datos clínicos salvo corrección formal.

**Validaciones**
- Conducta obligatoria.
- Diagnóstico principal obligatorio.
- Plan y conducta no vacíos.

**Datos involucrados**
- `atencion`
- `auditoria`

**Dependencias**
- HU-FASE1-026, HU-FASE1-027.

**Criterios de aceptación**
- CA1: Puedo cerrar la atención eligiendo la conducta.
- CA2: Si elijo hospitalización, se dispara el flujo correspondiente.
- CA3: El cierre queda registrado con sello de tiempo y usuario.

**Prioridad**: Alta

---

## Bloque 5. Hospitalización

---

### HU-FASE1-031 — Solicitud de hospitalización desde urgencias

**Módulo**: Hospitalización
**Actor principal**: Médico general de urgencias

**Historia de usuario**
Como médico de urgencias, quiero solicitar la hospitalización de un paciente cuando su condición lo requiere, para que pase al cuidado del servicio hospitalario.

**Objetivo funcional**
Formalizar la transición del paciente desde urgencias a hospitalización, dejando registro del servicio destino y la justificación.

**Descripción funcional detallada**
1. Desde la consola de urgencias, el médico selecciona conducta "Hospitalización".
2. Captura: servicio destino (pediatría, medicina interna, etc.), justificación, diagnóstico de ingreso, indicaciones iniciales.
3. El sistema cambia el estado de la admisión a "pendiente de hospitalización".
4. El paciente aparece en la cola de pendientes de recibir en el módulo de hospitalización.

**Reglas de negocio**
- Solo médicos con permiso pueden solicitar hospitalización.
- Debe existir al menos un diagnóstico principal confirmado.
- Mientras no sea recibido por el médico hospitalario, el paciente sigue bajo responsabilidad de urgencias.

**Validaciones**
- Campos obligatorios: servicio destino, justificación, diagnóstico de ingreso.

**Datos involucrados**
- `admision`, `atencion`, `estado_admision`
- `diagnostico_atencion`

**Dependencias**
- HU-FASE1-026, HU-FASE1-030.

**Criterios de aceptación**
- CA1: Puedo solicitar hospitalización indicando servicio destino y justificación.
- CA2: La admisión pasa al estado correcto.
- CA3: El paciente aparece en la cola del servicio hospitalario destino.

**Prioridad**: Alta

---

### HU-FASE1-032 — Ingreso hospitalario por médico tratante

**Módulo**: Hospitalización
**Actor principal**: Médico hospitalario

**Historia de usuario**
Como médico hospitalario, quiero recibir formalmente al paciente que ha sido solicitado para hospitalización, para iniciar su atención intrahospitalaria.

**Objetivo funcional**
Formalizar la aceptación del paciente por el médico tratante y abrir la atención hospitalaria.

**Descripción funcional detallada**
1. El médico accede a la cola de "pendientes de recibir" en su servicio.
2. Selecciona el paciente y confirma recepción.
3. El sistema crea una nueva atención asociada a la admisión, con tipo hospitalaria y médico tratante asignado.
4. El estado de la admisión cambia a "hospitalizado".
5. Se le asocia una cama/recurso físico (selección del catálogo de recursos).

**Reglas de negocio**
- Solo puede recibir un médico con rol de médico hospitalario del servicio correspondiente.
- La cama debe estar disponible (no ocupada por otro paciente).
- Al recibir, el médico puede proceder a la nota de ingreso.

**Validaciones**
- Recurso físico disponible.
- Profesional autorizado.

**Datos involucrados**
- `atencion`, `admision`
- `recurso_fisico`

**Dependencias**
- HU-FASE1-031.

**Criterios de aceptación**
- CA1: Puedo recibir al paciente y se crea la atención hospitalaria.
- CA2: Se le asocia una cama disponible.
- CA3: El paciente queda bajo mi responsabilidad en el sistema.

**Prioridad**: Alta

---

### HU-FASE1-033 — Nota de ingreso hospitalario

**Módulo**: Hospitalización
**Actor principal**: Médico hospitalario

**Historia de usuario**
Como médico hospitalario, quiero registrar la nota de ingreso, para documentar la valoración inicial del paciente al ser hospitalizado.

**Objetivo funcional**
Capturar de forma estructurada la valoración inicial del paciente al iniciar la hospitalización.

**Descripción funcional detallada**
1. Desde la consola de hospitalización, el médico selecciona "Nota de ingreso".
2. El sistema precarga información de la atención de urgencias (motivo, enfermedad actual, diagnósticos, signos vitales).
3. El médico completa o ajusta: antecedentes, examen físico, análisis, plan terapéutico, indicaciones.
4. Guarda la nota como parte de la atención hospitalaria.

**Reglas de negocio**
- La nota de ingreso debe existir antes de generar evoluciones.
- Los datos precargados de urgencias no sobreescriben registros originales.
- Una vez firmada, la nota solo admite correcciones formales (no edición directa).

**Validaciones**
- Campos mínimos: examen físico, análisis, plan.

**Datos involucrados**
- `atencion`

**Dependencias**
- HU-FASE1-032.

**Criterios de aceptación**
- CA1: Puedo registrar la nota de ingreso con datos precargados desde urgencias.
- CA2: No puedo crear evoluciones sin nota de ingreso.
- CA3: La nota queda firmada con mi usuario y sello de tiempo.

**Prioridad**: Alta

---

### HU-FASE1-034 — Registro de evolución hospitalaria

**Módulo**: Hospitalización
**Actor principal**: Médico hospitalario

**Historia de usuario**
Como médico hospitalario, quiero registrar evoluciones diarias del paciente, para documentar su estado clínico y las decisiones médicas a lo largo de la hospitalización.

**Objetivo funcional**
Proveer el registro estructurado y fechado de evoluciones en formato SOAP.

**Descripción funcional detallada**
1. Desde la consola de hospitalización, el médico selecciona "Nueva evolución".
2. Captura: subjetivo, objetivo (signos vitales), análisis, plan.
3. Puede agregar órdenes y prescripciones adicionales desde la evolución.
4. Firma y guarda.

**Reglas de negocio**
- Una evolución no puede modificarse una vez firmada; se hacen nuevas evoluciones o correcciones formales.
- Mínimo una evolución cada 24 horas para pacientes activos (se alerta si no la hay).
- El paciente egresado no admite nuevas evoluciones.

**Validaciones**
- Mínimo: análisis y plan.

**Datos involucrados**
- `atencion` (cada evolución puede ser una atención ligada a la admisión, o un registro estructurado dentro de la atención hospitalaria principal; decisión técnica)

**Dependencias**
- HU-FASE1-033.

**Criterios de aceptación**
- CA1: Puedo registrar evoluciones con estructura SOAP.
- CA2: Una evolución firmada no se puede editar.
- CA3: Veo el historial de evoluciones ordenado por fecha.

**Prioridad**: Alta

**Observaciones**
Decisión técnica pendiente: si cada evolución es una atención hija de la admisión, o un registro estructurado con tipo="evolución" dentro de la atención hospitalaria principal. Se recomienda crear tabla específica en fase 2.

---

### HU-FASE1-035 — Visualización de órdenes activas del paciente hospitalizado

**Módulo**: Hospitalización
**Actor principal**: Médico hospitalario / Auxiliar de enfermería

**Historia de usuario**
Como médico hospitalario, quiero ver las órdenes activas de mis pacientes hospitalizados, para monitorear qué se ha ordenado, qué está pendiente y qué ya se ha ejecutado.

**Objetivo funcional**
Proveer visibilidad en tiempo real del estado de las órdenes clínicas del paciente durante la hospitalización.

**Descripción funcional detallada**
1. Desde la consola de hospitalización, el usuario selecciona la pestaña "Órdenes activas".
2. Visualiza una lista agrupada por tipo (laboratorio, imágenes, procedimientos, interconsultas) con el estado de cada detalle.
3. Puede filtrar por estado (pendiente, ejecutada, reportada, anulada).
4. Desde ahí puede anular una orden con justificación (si tiene permiso).

**Reglas de negocio**
- Solo se pueden anular órdenes en estado "pendiente" o "ejecutada" (con justificación).
- El médico tratante o quien ordenó puede anular.

**Validaciones**
- Justificación obligatoria para anular.

**Datos involucrados**
- `orden_clinica`, `detalle_orden_clinica`
- `atencion`, `admision`

**Dependencias**
- HU-FASE1-028, HU-FASE1-032.

**Criterios de aceptación**
- CA1: Veo agrupadas las órdenes del paciente por tipo y estado.
- CA2: Puedo anular una orden con justificación.
- CA3: Los cambios quedan auditados.

**Prioridad**: Media

---

### HU-FASE1-036 — Egreso hospitalario básico

**Módulo**: Hospitalización
**Actor principal**: Médico hospitalario

**Historia de usuario**
Como médico hospitalario, quiero dar de alta al paciente registrando el egreso, para finalizar su hospitalización.

**Objetivo funcional**
Cerrar formalmente la hospitalización con epicrisis básica.

**Descripción funcional detallada**
1. Desde la consola, el médico selecciona "Egreso".
2. Captura: tipo de egreso (vivo, fallecido, traslado institucional, fuga), diagnósticos de egreso, resumen clínico (epicrisis básica), indicaciones de salida, medicamentos de salida, plan de seguimiento.
3. Libera la cama/recurso físico.
4. Cambia el estado de la admisión a "pendiente de egreso administrativo".

**Reglas de negocio**
- Todo egreso debe tener al menos un diagnóstico de egreso.
- Si el tipo es "fallecido", requiere registrar fecha y hora exactas del deceso.
- La cama asignada queda liberada para el siguiente paciente.
- La epicrisis debe estar completa (mínimo resumen clínico y plan).

**Validaciones**
- Campos obligatorios según tipo de egreso.

**Datos involucrados**
- `atencion`, `admision`, `diagnostico_atencion`
- `recurso_fisico`

**Dependencias**
- HU-FASE1-032, HU-FASE1-033, HU-FASE1-034.

**Criterios de aceptación**
- CA1: Puedo egresar al paciente registrando tipo, diagnósticos y epicrisis.
- CA2: La cama queda liberada automáticamente.
- CA3: La admisión pasa al siguiente estado para egreso administrativo.
- CA4: El egreso queda auditado.

**Prioridad**: Alta

---

## Bloque 6. Citas

---

### HU-FASE1-037 — Gestión de calendarios de citas

**Módulo**: Citas
**Actor principal**: Administrador / Coordinador de agendas

**Historia de usuario**
Como administrador de agendas, quiero definir calendarios con días hábiles, festivos y especiales, para que las agendas respeten el calendario institucional.

**Objetivo funcional**
Configurar el calendario que regirá la generación de disponibilidades y la validación de fechas de citas.

**Descripción funcional detallada**
1. El usuario crea un calendario con código, nombre y descripción.
2. Agrega detalles por fecha indicando si es hábil, si es festivo, observaciones.
3. Puede cargar masivamente un año de calendario (ej: cargar festivos oficiales de Colombia).
4. Un calendario puede aplicarse a una o varias agendas.

**Reglas de negocio**
- Una fecha en un calendario es única.
- Los días marcados no hábiles no generan disponibilidades.
- Los festivos pueden tener reglas especiales según la institución.

**Validaciones**
- Código único.
- Fecha única dentro del calendario.

**Datos involucrados**
- `calendario_cita`, `detalle_calendario_cita`

**Dependencias**
- Ninguna previa.

**Criterios de aceptación**
- CA1: Puedo crear calendarios y marcar fechas como festivo/no hábil.
- CA2: Puedo cargar masivamente un año.
- CA3: Las agendas usan el calendario para generar disponibilidades.

**Prioridad**: Alta

---

### HU-FASE1-038 — Gestión de recursos físicos

**Módulo**: Citas
**Actor principal**: Administrador

**Historia de usuario**
Como administrador, quiero registrar los recursos físicos agendables (consultorios, salas, equipos, camas), para que puedan asociarse a agendas.

**Objetivo funcional**
Mantener inventario de recursos físicos agendables por sede.

**Descripción funcional detallada**
1. El usuario crea un recurso físico con código, nombre, tipo (consultorio, sala, equipo, cama, box), sede.
2. Puede editar o inactivar recursos.
3. Los recursos inactivos no aparecen como asignables a nuevas agendas.

**Reglas de negocio**
- Código único por recurso.
- Un recurso pertenece a una sede.
- Un recurso inactivo no se puede asignar a nuevas agendas pero conserva su historial.

**Validaciones**
- Código único, nombre y sede obligatorios.

**Datos involucrados**
- `recurso_fisico`
- `sede`

**Dependencias**
- HU-FASE1-001.

**Criterios de aceptación**
- CA1: Puedo crear recursos físicos asociados a sedes.
- CA2: Puedo inactivar un recurso sin perder historial.

**Prioridad**: Media

---

### HU-FASE1-039 — Creación de agenda de profesional

**Módulo**: Citas
**Actor principal**: Coordinador de agendas

**Historia de usuario**
Como coordinador de agendas, quiero crear la agenda de un profesional con sus bloques horarios, para que el sistema pueda generar disponibilidad de citas.

**Objetivo funcional**
Parametrizar la agenda maestra de un profesional con sus horarios de atención por día de la semana.

**Descripción funcional detallada**
1. El usuario crea una agenda seleccionando: profesional, especialidad, sede, recurso físico, calendario, estado, vigencia, duración de cita (minutos).
2. Agrega bloques horarios por día de la semana con hora inicio/fin y cupos por bloque.
3. Guarda y la agenda queda lista para generar disponibilidad.

**Reglas de negocio**
- Un profesional puede tener varias agendas (ej: distintas sedes o especialidades).
- Los bloques no pueden superponerse dentro de la misma agenda/día.
- La hora de fin debe ser mayor que la de inicio.
- Cupos por bloque determinan cuántas citas concurrentes admite.

**Validaciones**
- Campos obligatorios: profesional, sede, vigencia desde.
- No superposición de bloques en el mismo día.
- Duración de cita > 0.

**Datos involucrados**
- `agenda_profesional`, `bloque_agenda`
- `profesional_salud`, `sede`, `recurso_fisico`, `calendario_cita`, `especialidad`

**Dependencias**
- HU-FASE1-006, HU-FASE1-037, HU-FASE1-038.

**Criterios de aceptación**
- CA1: Puedo crear una agenda con múltiples bloques por día.
- CA2: El sistema impide bloques superpuestos.
- CA3: La agenda queda lista para generar disponibilidades.

**Prioridad**: Alta

---

### HU-FASE1-040 — Generación de disponibilidad de cita

**Módulo**: Citas
**Actor principal**: Sistema (automático) / Coordinador de agendas

**Historia de usuario**
Como coordinador de agendas, quiero generar la disponibilidad de citas de un profesional para un período, para que los pacientes puedan agendar.

**Objetivo funcional**
Expandir agenda + bloques + calendario en slots concretos agendables.

**Descripción funcional detallada**
1. El usuario selecciona una agenda y un rango de fechas.
2. El sistema genera disponibilidades (slots) en base a los bloques horarios y la duración de cita, excluyendo días no hábiles.
3. Cada slot queda con cupos totales y cupos ocupados en cero.
4. El proceso puede ejecutarse periódicamente (ej: cada mes generar el siguiente mes).

**Reglas de negocio**
- No generar slots para fechas no hábiles del calendario.
- No duplicar slots ya generados.
- Respetar la duración de cita definida en la agenda.

**Validaciones**
- Rango de fechas dentro de la vigencia de la agenda.

**Datos involucrados**
- `disponibilidad_cita`
- `agenda_profesional`, `bloque_agenda`, `calendario_cita`, `detalle_calendario_cita`

**Dependencias**
- HU-FASE1-037, HU-FASE1-039.

**Criterios de aceptación**
- CA1: Puedo generar disponibilidad de un mes para una agenda.
- CA2: No se generan slots en festivos.
- CA3: No se duplican slots.
- CA4: Los slots respetan la duración y los cupos definidos.

**Prioridad**: Alta

---

### HU-FASE1-041 — Asignación de cita a paciente

**Módulo**: Citas
**Actor principal**: Recepcionista / Agente de call center

**Historia de usuario**
Como recepcionista, quiero asignar una cita a un paciente desde una vista limpia y rápida, para agendarlo en el profesional, sede y hora correctas.

**Objetivo funcional**
Materializar la "Vista 3. Asignación de citas" definida en el agente base.

**Descripción funcional detallada**
1. El usuario busca al paciente (por documento o nombre).
2. Visualiza datos clave: nombre, edad, sexo, afiliación vigente.
3. Filtra la disponibilidad por: sede, especialidad, profesional, servicio, fecha.
4. Visualiza los slots disponibles en formato de calendario o lista.
5. Selecciona un slot y confirma: tipo de cita, motivo, observaciones.
6. El sistema genera número de cita, marca el slot como ocupado, y envía notificación al paciente (si hay contacto).

**Reglas de negocio**
- Un paciente no puede tener dos citas activas al mismo día/hora/profesional.
- Un slot con `cupos_ocupados >= cupos_totales` no se puede asignar.
- La cita queda en estado "asignada" hasta que sea confirmada, atendida o cancelada.
- Si el paciente no tiene afiliación vigente, el sistema advierte pero permite si es particular.

**Validaciones**
- Slot disponible.
- Paciente existente y activo.
- Motivo no vacío.

**Datos involucrados**
- `cita`, `disponibilidad_cita`, `agenda_profesional`
- `paciente`, `servicio_salud`, `tipo_cita`, `estado_cita`, `especialidad`

**Dependencias**
- HU-FASE1-011, HU-FASE1-040, HU-FASE1-045.

**Criterios de aceptación**
- CA1: Puedo asignar una cita a un paciente en tres pasos (buscar paciente → filtrar disponibilidad → asignar).
- CA2: El slot queda marcado como ocupado.
- CA3: El sistema genera número de cita automáticamente.
- CA4: Si hay conflicto con otra cita del mismo paciente, el sistema lo advierte.

**Prioridad**: Alta

---

### HU-FASE1-042 — Cancelación y reprogramación de cita

**Módulo**: Citas
**Actor principal**: Recepcionista / Paciente (en futuro canal)

**Historia de usuario**
Como recepcionista, quiero cancelar o reprogramar una cita existente, para responder a imprevistos del paciente o del profesional.

**Objetivo funcional**
Gestionar el ciclo de vida de la cita con trazabilidad del motivo.

**Descripción funcional detallada**
1. El usuario ubica la cita y selecciona "Cancelar" o "Reprogramar".
2. Captura motivo de cancelación o reprogramación del catálogo.
3. Si es reprogramación, selecciona un nuevo slot disponible y confirma.
4. El sistema libera el slot anterior y ocupa el nuevo.
5. Se registra la trazabilidad con usuario, fecha y motivo.

**Reglas de negocio**
- Una cita atendida no puede cancelarse ni reprogramarse.
- La cancelación requiere motivo del catálogo.
- La reprogramación conserva el número de cita (no genera uno nuevo).

**Validaciones**
- Motivo obligatorio.
- Slot destino disponible.

**Datos involucrados**
- `cita`, `motivo_cancelacion_cita`, `motivo_reprogramacion_cita`
- `disponibilidad_cita`
- `auditoria`

**Dependencias**
- HU-FASE1-041.

**Criterios de aceptación**
- CA1: Puedo cancelar una cita con motivo.
- CA2: Puedo reprogramar una cita a un nuevo slot disponible.
- CA3: Los slots se actualizan correctamente.
- CA4: Todo queda auditado.

**Prioridad**: Alta

---

### HU-FASE1-043 — Gestión de lista de espera

**Módulo**: Citas
**Actor principal**: Recepcionista / Coordinador de agendas

**Historia de usuario**
Como recepcionista, quiero inscribir a un paciente en lista de espera cuando no hay disponibilidad, para llamarlo cuando se liberen cupos.

**Objetivo funcional**
Gestionar la demanda no cubierta y asignarla cuando aparezca disponibilidad.

**Descripción funcional detallada**
1. Si no hay disponibilidad, el usuario registra al paciente en lista de espera con: especialidad, servicio, prioridad, fechas preferidas, observaciones.
2. El sistema la muestra en el tablero de lista de espera ordenada por prioridad y fecha.
3. Cuando se libera disponibilidad o se genera nueva, el sistema sugiere candidatos de la lista.
4. Al asignar la cita, el registro de lista pasa a estado "asignada".

**Reglas de negocio**
- Un paciente no puede estar dos veces en la lista por la misma especialidad/servicio.
- La prioridad define el orden de asignación.
- Los registros vencidos pasan a estado "vencida".

**Validaciones**
- Especialidad o servicio obligatorio.
- Prioridad entre 1 y 4.

**Datos involucrados**
- `lista_espera_cita`
- `paciente`, `especialidad`, `servicio_salud`

**Dependencias**
- HU-FASE1-011.

**Criterios de aceptación**
- CA1: Puedo inscribir un paciente en lista de espera.
- CA2: Al liberar cupos, el sistema muestra candidatos sugeridos.
- CA3: Al asignar, el registro cambia a "asignada".

**Prioridad**: Media

---

### HU-FASE1-044 — Traslado masivo de agenda

**Módulo**: Citas
**Actor principal**: Coordinador de agendas

**Historia de usuario**
Como coordinador de agendas, quiero trasladar masivamente las citas de un profesional a otro o a otra fecha, para responder a ausencias, incapacidades o reorganizaciones.

**Objetivo funcional**
Resolver contingencias de agenda sin tener que cancelar y reasignar manualmente cada cita.

**Descripción funcional detallada**
1. El usuario selecciona agenda origen, fecha origen, agenda destino, fecha destino.
2. Captura motivo del traslado.
3. El sistema identifica las citas del día origen y las intenta mover al día destino respetando disponibilidad.
4. Para cada cita muestra: trasladada, fallida (sin slot disponible), omitida.
5. Al confirmar, actualiza las citas y registra el traslado.

**Reglas de negocio**
- Solo se trasladan citas activas (no atendidas ni canceladas).
- Si no hay slot en el destino, la cita queda en lista de espera automáticamente.
- El paciente debe ser notificado (fase futura integración SMS/correo).

**Validaciones**
- Motivo obligatorio.
- Agenda destino válida y vigente.

**Datos involucrados**
- `traslado_agenda`, `detalle_traslado_agenda`
- `cita`, `disponibilidad_cita`

**Dependencias**
- HU-FASE1-041.

**Criterios de aceptación**
- CA1: Puedo trasladar todas las citas de un día a otra agenda o fecha.
- CA2: El sistema me muestra cuántas se trasladaron y cuántas fallaron.
- CA3: Las citas fallidas quedan en lista de espera.
- CA4: El traslado queda documentado con motivo y usuario.

**Prioridad**: Media

---

## Bloque 7. Servicios, pagadores y contratos

---

### HU-FASE1-045 — Gestión del catálogo de servicios de salud

**Módulo**: Servicios, pagadores y contratos
**Actor principal**: Administrador / Facturación

**Historia de usuario**
Como administrador, quiero mantener el catálogo maestro de servicios de salud prestables (CUPS, medicamentos, insumos, estancias, paquetes), para que se puedan ordenar, prescribir y facturar desde un único catálogo institucional.

**Objetivo funcional**
Consolidar la fuente única de servicios prestables y facturables.

**Descripción funcional detallada**
1. El usuario accede al "Catálogo de servicios".
2. Puede buscar por código, CUPS o nombre.
3. Puede crear un servicio con: código interno, código CUPS, nombre, descripción, categoría (consulta, procedimiento, medicamento, insumo, ayuda diagnóstica, estancia), centro de costo, unidad de medida, si requiere autorización, si requiere diagnóstico.
4. Puede editar e inactivar.

**Reglas de negocio**
- El código interno es único.
- El código CUPS puede repetirse si es para variantes (pero se recomienda único).
- Un servicio inactivo no se puede ordenar, prescribir ni facturar.
- Los servicios de medicamentos deben tener unidad de medida clara.

**Validaciones**
- Código interno, nombre, categoría obligatorios.
- Código interno único.

**Datos involucrados**
- `servicio_salud`, `categoria_servicio_salud`, `centro_costo`

**Dependencias**
- HU-FASE1-046.

**Criterios de aceptación**
- CA1: Puedo crear servicios con sus datos obligatorios.
- CA2: Puedo buscar por código o nombre.
- CA3: Un servicio inactivo no aparece en módulos operativos.

**Prioridad**: Alta

---

### HU-FASE1-046 — Gestión de centros de costo

**Módulo**: Servicios, pagadores y contratos
**Actor principal**: Administrador / Contabilidad

**Historia de usuario**
Como contador, quiero mantener los centros de costo de la institución, para clasificar correctamente los servicios e ingresos.

**Objetivo funcional**
Soportar la contabilización y análisis por centro de costo.

**Descripción funcional detallada**
1. El usuario crea o edita centros de costo con código único, nombre, centro de costo padre (para jerarquía).
2. Los centros pueden inactivarse.

**Reglas de negocio**
- Jerarquía árbol con un padre opcional.
- Un centro de costo con hijos activos no puede inactivarse.

**Validaciones**
- Código único.

**Datos involucrados**
- `centro_costo`

**Dependencias**
- Ninguna previa.

**Criterios de aceptación**
- CA1: Puedo crear centros de costo en estructura jerárquica.
- CA2: No puedo inactivar un centro con hijos activos.

**Prioridad**: Media

---

### HU-FASE1-047 — Registro de pagador

**Módulo**: Servicios, pagadores y contratos
**Actor principal**: Administrador / Facturación

**Historia de usuario**
Como administrador, quiero registrar a los pagadores (EPS, ARL, SOAT, particulares, medicina prepagada), para poder asociarlos a afiliaciones, admisiones y facturas.

**Objetivo funcional**
Mantener el catálogo institucional de pagadores respetando la decisión de que todo pagador es un tercero.

**Descripción funcional detallada**
1. El usuario busca o crea un tercero tipo "pagador".
2. Lo marca como pagador completando: código, tipo de pagador (EPS/ARL/SOAT/etc.), tipo de cliente, código EPS, código administradora, días para radicación, días para respuesta de glosa.
3. Puede inactivar un pagador cuando termina el convenio.

**Reglas de negocio**
- Todo pagador es un tercero.
- Código único del pagador.
- Un pagador inactivo no se puede seleccionar para nuevas admisiones o contratos.

**Validaciones**
- Campos obligatorios: tercero, código, tipo de pagador.
- Código único.

**Datos involucrados**
- `pagador`
- `tercero`, `tipo_pagador`, `tipo_cliente`

**Dependencias**
- HU-FASE1-008.

**Criterios de aceptación**
- CA1: Puedo registrar un pagador desde un tercero.
- CA2: No puedo registrar dos pagadores para el mismo tercero.
- CA3: Los días de radicación y respuesta de glosa alimentan fechas límite.

**Prioridad**: Alta

---

### HU-FASE1-048 — Registro de contrato con pagador

**Módulo**: Servicios, pagadores y contratos
**Actor principal**: Administrador / Facturación

**Historia de usuario**
Como administrador, quiero registrar los contratos con cada pagador, para controlar modalidad de pago, vigencia, techo y tarifas aplicables.

**Objetivo funcional**
Establecer los acuerdos comerciales con pagadores, base para facturación y cartera.

**Descripción funcional detallada**
1. El usuario crea un contrato con: número, pagador, modalidad de pago (evento/cápita/PGP/paquete), tarifario base, objeto, vigencia desde/hasta, valor del contrato, techo mensual, observaciones.
2. Puede editar, prorrogar o dar por terminado el contrato.
3. Los contratos vencidos no se usan en nuevas admisiones ni facturas.

**Reglas de negocio**
- Número de contrato único.
- Vigencia desde obligatoria; vigencia hasta opcional (abierto).
- Un contrato vencido no permite facturación.
- Techo mensual permite alertar cuando se esté por superar el cupo.

**Validaciones**
- Campos obligatorios: número, pagador, modalidad, vigencia desde.
- Número único.
- Vigencia hasta posterior a vigencia desde (si aplica).

**Datos involucrados**
- `contrato`
- `pagador`, `modalidad_pago`, `tarifario`

**Dependencias**
- HU-FASE1-047, HU-FASE1-049.

**Criterios de aceptación**
- CA1: Puedo registrar un contrato con sus datos y vigencia.
- CA2: Un contrato vencido no se puede usar en nuevas facturas.
- CA3: Puedo consultar el listado de contratos vigentes por pagador.

**Prioridad**: Alta

---

### HU-FASE1-049 — Carga de tarifario base

**Módulo**: Servicios, pagadores y contratos
**Actor principal**: Administrador / Facturación

**Historia de usuario**
Como administrador, quiero cargar tarifarios base (SOAT, ISS 2001, ISS 2004, propio), para que sirvan como referencia en contratos.

**Objetivo funcional**
Mantener los manuales tarifarios oficiales y propios.

**Descripción funcional detallada**
1. El usuario crea un tarifario con código, nombre, vigencia.
2. Agrega detalles con servicio y valor.
3. Puede cargar masivamente desde archivo (CSV/Excel).

**Reglas de negocio**
- El valor debe ser positivo.
- El par (tarifario, servicio) es único.
- Un tarifario vencido puede seguir en uso para contratos históricos.

**Validaciones**
- Código único.
- Valor > 0.

**Datos involucrados**
- `tarifario`, `detalle_tarifario`
- `servicio_salud`

**Dependencias**
- HU-FASE1-045.

**Criterios de aceptación**
- CA1: Puedo crear un tarifario y cargar múltiples servicios con valor.
- CA2: Puedo cargar masivamente desde archivo.
- CA3: El sistema valida duplicados.

**Prioridad**: Alta

---

### HU-FASE1-050 — Registro de tarifas específicas por contrato

**Módulo**: Servicios, pagadores y contratos
**Actor principal**: Administrador / Facturación

**Historia de usuario**
Como administrador, quiero registrar tarifas específicas negociadas en cada contrato, para que sobrescriban las del tarifario base cuando aplique.

**Objetivo funcional**
Permitir precios negociados por contrato sin modificar el tarifario base.

**Descripción funcional detallada**
1. Desde un contrato, el usuario agrega tarifas específicas: servicio, valor, porcentaje de descuento, vigencia.
2. El sistema prioriza la tarifa del contrato sobre el tarifario base al facturar.

**Reglas de negocio**
- La tarifa de contrato prevalece sobre el tarifario base.
- Un servicio puede tener varias tarifas en el tiempo pero solo una vigente.

**Validaciones**
- Campos obligatorios: contrato, servicio, valor, vigencia desde.

**Datos involucrados**
- `tarifa_contrato`
- `contrato`, `servicio_salud`

**Dependencias**
- HU-FASE1-048, HU-FASE1-049.

**Criterios de aceptación**
- CA1: Puedo registrar tarifas específicas por contrato.
- CA2: Al facturar, se usa la tarifa del contrato si existe vigente.
- CA3: Puedo consultar las tarifas específicas de un contrato.

**Prioridad**: Alta

---

### HU-FASE1-051 — Asignación de servicios al contrato

**Módulo**: Servicios, pagadores y contratos
**Actor principal**: Administrador / Facturación

**Historia de usuario**
Como administrador, quiero definir qué servicios están cubiertos por cada contrato, para que el sistema bloquee la facturación de servicios no cubiertos.

**Objetivo funcional**
Permitir que los contratos tengan un subconjunto de servicios cubiertos.

**Descripción funcional detallada**
1. Desde un contrato, el usuario agrega los servicios cubiertos indicando: si requieren autorización, cantidad máxima.
2. Al facturar, el sistema valida que el servicio esté cubierto por el contrato.

**Reglas de negocio**
- Si un contrato no tiene servicios asignados, se considera que cubre todos los del catálogo (comportamiento configurable).
- Si tiene servicios asignados, solo esos están cubiertos.
- Los servicios que requieren autorización validan que exista la autorización antes de facturar (fase 2).

**Validaciones**
- No duplicar (contrato, servicio).

**Datos involucrados**
- `servicio_contrato`
- `contrato`, `servicio_salud`

**Dependencias**
- HU-FASE1-048.

**Criterios de aceptación**
- CA1: Puedo asignar servicios a un contrato.
- CA2: Puedo marcar un servicio como requerido de autorización.
- CA3: Al facturar, se valida la cobertura.

**Prioridad**: Alta

---

## Bloque 8. Facturación inicial

---

### HU-FASE1-052 — Generación de factura desde atención

**Módulo**: Facturación inicial
**Actor principal**: Facturador

**Historia de usuario**
Como facturador, quiero generar la factura de una admisión, para cobrar al pagador los servicios prestados al paciente.

**Objetivo funcional**
Transformar los servicios prestados en una atención en una factura con todos sus cálculos.

**Descripción funcional detallada**
1. El facturador selecciona una admisión cerrada.
2. El sistema precarga todos los servicios prestados (atenciones, órdenes ejecutadas, prescripciones), aplicando las tarifas del contrato o del tarifario base.
3. El facturador puede agregar o quitar ítems, corregir cantidades y valores.
4. El sistema calcula subtotal, IVA, descuentos, copago, cuota moderadora, total.
5. Al guardar, la factura queda en estado "borrador".
6. Al aprobar, pasa a estado "generada" y se asigna número de factura consecutivo.

**Reglas de negocio**
- Una admisión cerrada puede tener una o varias facturas (ej: particular + pagador).
- Los servicios no cubiertos por el contrato se marcan como no facturables al pagador.
- La factura aprobada no se puede modificar; se debe anular y generar una nueva.
- El número de factura sigue la numeración legal autorizada por la DIAN.

**Validaciones**
- Al menos un detalle.
- Totales coherentes con detalles.
- Paciente y pagador obligatorios.

**Datos involucrados**
- `factura`, `detalle_factura`
- `admision`, `atencion`, `paciente`, `pagador`, `contrato`
- `estado_factura`

**Dependencias**
- HU-FASE1-022, HU-FASE1-048, HU-FASE1-049, HU-FASE1-050, HU-FASE1-051.

**Criterios de aceptación**
- CA1: Puedo generar una factura desde una admisión cerrada.
- CA2: Los servicios se precargan con las tarifas correctas.
- CA3: Los totales se calculan automáticamente.
- CA4: Una factura aprobada no se puede editar.
- CA5: La numeración sigue el consecutivo legal.

**Prioridad**: Alta

---

### HU-FASE1-053 — Gestión del detalle de factura

**Módulo**: Facturación inicial
**Actor principal**: Facturador

**Historia de usuario**
Como facturador, quiero editar el detalle de una factura en borrador, para corregir ítems, cantidades o valores antes de aprobarla.

**Objetivo funcional**
Permitir ajustes controlados del detalle antes de aprobar la factura.

**Descripción funcional detallada**
1. Desde una factura en borrador, el facturador puede agregar, editar o eliminar detalles.
2. Por cada ítem puede ajustar cantidad, valor unitario, IVA, descuento, copago, cuota moderadora.
3. Cada cambio recalcula el total.
4. Los cambios quedan auditados.

**Reglas de negocio**
- Solo en estado "borrador" se pueden modificar detalles.
- El valor de copago y cuota moderadora debe ser coherente con el régimen y clasificación SISBEN del paciente.

**Validaciones**
- Cantidad > 0, valor unitario >= 0.

**Datos involucrados**
- `detalle_factura`
- `factura`, `servicio_salud`

**Dependencias**
- HU-FASE1-052.

**Criterios de aceptación**
- CA1: Puedo ajustar los detalles de una factura en borrador.
- CA2: Los totales se recalculan automáticamente.
- CA3: No puedo modificar detalles de una factura aprobada.

**Prioridad**: Alta

---

### HU-FASE1-054 — Cálculo de copago y cuota moderadora

**Módulo**: Facturación inicial
**Actor principal**: Sistema (automático)

**Historia de usuario**
Como sistema, al generar una factura quiero calcular automáticamente el copago y la cuota moderadora según régimen, clasificación SISBEN y tipo de servicio, para aplicar las reglas normativas.

**Objetivo funcional**
Aplicar correctamente las reglas normativas colombianas de copago y cuota moderadora.

**Descripción funcional detallada**
1. Al precargar detalles de factura, el sistema identifica:
   - Régimen del paciente (contributivo, subsidiado, especial, etc.).
   - Clasificación SISBEN vigente.
   - Tipo de servicio (consulta, procedimiento, medicamento, etc.).
2. Aplica las reglas paramétricas para calcular copago y cuota moderadora.
3. Presenta los valores calculados al facturador, quien puede ajustarlos con justificación.
4. Registra el cálculo en el detalle.

**Reglas de negocio**
- El régimen subsidiado aplica solo copago según Sisben.
- El régimen contributivo aplica cuota moderadora y/o copago según el servicio.
- Los niveles A del SISBEN están exentos de copago.
- Las reglas deben ser parametrizables, no hardcoded.

**Validaciones**
- La suma de copago + cuota moderadora no puede superar el valor del ítem.

**Datos involucrados**
- `factura`, `detalle_factura`
- `paciente`, `seguridad_social_paciente`, `sisben_paciente`, `regimen`, `grupo_sisben`

**Dependencias**
- HU-FASE1-015, HU-FASE1-016, HU-FASE1-052.

**Criterios de aceptación**
- CA1: El sistema calcula copago y cuota moderadora automáticamente al precargar detalles.
- CA2: El facturador puede ajustar los valores con justificación.
- CA3: Los valores se reflejan en los totales de la factura.

**Prioridad**: Alta

**Observaciones**
Las tablas de porcentajes deben ser parametrizables (nueva tabla `regla_copago` recomendada, propuesta para fase 2).

---

### HU-FASE1-055 — Generación de estructura base RIPS

**Módulo**: Facturación inicial
**Actor principal**: Facturador / Auditor

**Historia de usuario**
Como facturador, quiero generar la estructura de RIPS a partir de la factura, para poder reportarlos al pagador según la norma (Resolución 3374 y actualizaciones).

**Objetivo funcional**
Preparar los registros individuales de prestación de servicios alineados a la norma vigente.

**Descripción funcional detallada**
1. Desde una factura generada, el facturador selecciona "Generar RIPS".
2. El sistema crea el encabezado RIPS y los detalles por tipo de archivo (AC=consulta, AP=procedimiento, AM=medicamento, AH=hospitalización, AU=urgencia, AN=recién nacido, AT=otros, CT=control, US=usuarios).
3. Guarda la información estructurada en `rips_detalle.linea_datos` (JSON).
4. Marca el RIPS como "generado".

**Reglas de negocio**
- Cada factura genera un único conjunto RIPS.
- El RIPS debe actualizarse si se anula y regenera la factura.
- Los datos obligatorios se validan contra la norma vigente.

**Validaciones**
- Paciente con tipo y número de documento válidos.
- Diagnóstico principal obligatorio en la atención.
- Pagador y contrato consistentes.

**Datos involucrados**
- `rips_encabezado`, `rips_detalle`
- `factura`, `detalle_factura`, `atencion`, `diagnostico_atencion`

**Dependencias**
- HU-FASE1-052.

**Criterios de aceptación**
- CA1: Puedo generar RIPS desde una factura.
- CA2: El sistema crea los registros por tipo de archivo correspondiente.
- CA3: Veo las líneas RIPS generadas.
- CA4: El RIPS queda en estado "generado".

**Prioridad**: Alta

**Observaciones**
La norma RIPS está en transición a la resolución 2275/2023 (RIPS con factura electrónica). El modelo actual soporta ambos formatos vía `linea_datos` JSON.

---

### HU-FASE1-056 — Radicación de factura ante pagador

**Módulo**: Facturación inicial
**Actor principal**: Facturador / Radicador

**Historia de usuario**
Como radicador, quiero registrar la radicación de una factura ante el pagador, para iniciar el proceso de cobro formal.

**Objetivo funcional**
Formalizar la entrega de la factura al pagador y controlar el tiempo de respuesta.

**Descripción funcional detallada**
1. El usuario selecciona una o varias facturas listas para radicar.
2. Captura: fecha de radicación, número de radicado, soporte (archivo o URL), observaciones.
3. El sistema calcula la fecha límite de respuesta (según `dias_respuesta_glosa` del pagador).
4. La radicación queda en estado "radicada".
5. La factura cambia a estado "radicada".

**Reglas de negocio**
- Solo facturas generadas pueden radicarse.
- Una factura solo se radica una vez (la re-radicación es un caso especial).
- El número de radicado es único por pagador.

**Validaciones**
- Campos obligatorios: fecha, pagador, factura.
- Número de radicado único por pagador.

**Datos involucrados**
- `radicacion`
- `factura`, `pagador`, `estado_radicacion`

**Dependencias**
- HU-FASE1-052.

**Criterios de aceptación**
- CA1: Puedo radicar una factura indicando número y fecha.
- CA2: El sistema calcula la fecha límite de respuesta.
- CA3: La factura queda marcada como radicada.

**Prioridad**: Alta

---

### HU-FASE1-057 — Creación automática de cuenta por cobrar

**Módulo**: Facturación inicial
**Actor principal**: Sistema (automático)

**Historia de usuario**
Como sistema, al generar y/o radicar una factura quiero crear automáticamente la cuenta por cobrar correspondiente, para iniciar el control de cartera.

**Objetivo funcional**
Automatizar la gestión de cartera desde el proceso de facturación.

**Descripción funcional detallada**
1. Al aprobar la factura (o al radicar, según política), el sistema crea el registro en `cuenta_por_cobrar` con: factura, pagador, estado inicial, fecha de inicio, fecha de vencimiento (según políticas del pagador), valor inicial, saldo = valor inicial.
2. Los pagos y ajustes posteriores se registran en `movimiento_cuenta_por_cobrar`.
3. Los días de mora se calculan automáticamente.

**Reglas de negocio**
- Una factura tiene una única cuenta por cobrar.
- El saldo de la cuenta se actualiza con cada abono/ajuste.
- El estado de cartera refleja el ciclo: vigente → por vencer → vencida → en cobro → castigada.

**Validaciones**
- Valor inicial > 0.
- No duplicar cuenta por cobrar para la misma factura.

**Datos involucrados**
- `cuenta_por_cobrar`, `movimiento_cuenta_por_cobrar`
- `factura`, `pagador`, `estado_cartera`

**Dependencias**
- HU-FASE1-052.

**Criterios de aceptación**
- CA1: Al aprobar una factura se crea la cuenta por cobrar automáticamente.
- CA2: El saldo inicial coincide con el total de la factura.
- CA3: Los días de mora se calculan correctamente.

**Prioridad**: Alta

---

### HU-FASE1-058 — Consulta de cartera básica por pagador

**Módulo**: Facturación inicial
**Actor principal**: Cartera / Coordinador financiero

**Historia de usuario**
Como coordinador de cartera, quiero consultar la cartera vigente por pagador, para saber cuánto me deben y con qué antigüedad.

**Objetivo funcional**
Proveer visibilidad básica del estado de cartera de la institución.

**Descripción funcional detallada**
1. El usuario accede a "Cartera por pagador".
2. Visualiza un resumen: total cartera, cartera corriente, 0-30, 31-60, 61-90, 91-180, +180 días.
3. Puede entrar al detalle por pagador para ver facturas específicas.
4. Puede filtrar por pagador, rango de fechas, estado.
5. Puede exportar a Excel.

**Reglas de negocio**
- La edad de la cartera se calcula desde la fecha de radicación (o fecha de factura si no está radicada).
- Solo se muestran cuentas por cobrar activas (no canceladas ni castigadas).

**Validaciones**
- El usuario solo ve pagadores a los que tiene acceso.

**Datos involucrados**
- `cuenta_por_cobrar`, `movimiento_cuenta_por_cobrar`
- `factura`, `pagador`

**Dependencias**
- HU-FASE1-057.

**Criterios de aceptación**
- CA1: Veo el resumen de cartera por pagador y edad.
- CA2: Puedo ver el detalle por factura.
- CA3: Puedo exportar a Excel.

**Prioridad**: Media

---

## Cierre

Este backlog cubre los 8 bloques de la fase 1 del Sistema de Gestión Hospitalaria con 58 historias de usuario completas, siguiendo la estructura obligatoria del agente analista funcional.

**Próximos pasos sugeridos**:

1. Priorización definitiva con el equipo de negocio.
2. Agrupación en sprints (se recomienda 4–6 sprints de 2 semanas para fase 1).
3. Descomposición técnica en tareas de backend y frontend.
4. Definición de casos de prueba (QA) por historia.
5. Preparación de datos semilla para catálogos críticos (CIE-10, CUPS, geografía, etc.).
