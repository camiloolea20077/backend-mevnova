# Backlog de Historias de Usuario — Fase 2
## Sistema de Gestión Hospitalaria (SGH)

**Versión**: 1.0
**Continuación de**: `backlog_fase1_v2.md` (65 HUs ya completadas)
**Multi-tenant**: aplica completamente (heredado de fase 1)
**Modelo de datos**: 142 tablas tras fase 2 (110 fase 1 + 32 nuevas)

---

## Tabla de contenido

1. [Inventario de necesidades de fase 2](#1-inventario-de-necesidades-de-fase-2)
2. [Mapa de módulos de fase 2](#2-mapa-de-módulos-de-fase-2)
3. [Principios heredados de fase 1](#3-principios-heredados-de-fase-1)
4. [Lista priorizada de historias de usuario](#4-lista-priorizada-de-historias-de-usuario)
5. [Bloque 9 — Glosas y conciliación](#bloque-9--glosas-y-conciliación)
6. [Bloque 10 — Farmacia e inventario](#bloque-10--farmacia-e-inventario)
7. [Bloque 11 — Historia clínica avanzada](#bloque-11--historia-clínica-avanzada)

---

## 1. Inventario de necesidades de fase 2

La fase 2 cubre los tres vacíos críticos detectados al cerrar fase 1:

1. **Glosas y conciliación**: cerrar el ciclo financiero con respuesta y conciliación de glosas, impactando la cartera correctamente.
2. **Farmacia e inventario**: trazabilidad completa de medicamentos e insumos por lote, vencimiento, bodega; dispensación con FEFO; alertas de stock y vencimientos.
3. **Historia clínica avanzada**: anamnesis estructurada, notas de enfermería, MAR, balances, escalas clínicas, interconsulta, epicrisis y vista consolidada de HC.

**Lo que NO entra en fase 2** (queda para fase 3):
- Facturación electrónica DIAN
- MIPRES completo (medicamentos no PBS)
- Interoperabilidad HL7/FHIR
- Portal del paciente
- Programación quirúrgica
- Laboratorio con integración de resultados (mensajería HL7)
- Imágenes diagnósticas con PACS
- Telemedicina con video

---

## 2. Mapa de módulos de fase 2

| # | Módulo | Submódulos cubiertos |
|---|--------|----------------------|
| 9 | **Glosas y conciliación** | catálogo de motivos, recepción, detalle por ítem, respuesta, conciliación, impacto en cartera |
| 10 | **Farmacia e inventario** | bodegas, proveedores, compras, lotes, stock, solicitudes, despacho FEFO, dispensación, devoluciones, traslados, ajustes, kardex, alertas |
| 11 | **Historia clínica avanzada** | antecedentes, hábitos, revisión por sistemas, vacunas, medicación habitual, plan de cuidados, notas de enfermería, MAR, balance de líquidos, escalas, interconsulta, epicrisis, adjuntos, consentimientos, vista consolidada |

---

## 3. Principios heredados de fase 1

Fase 2 hereda **íntegramente** los principios de fase 1:

### 3.1 Aislamiento total de datos
- Toda tabla nueva con `empresa_id` filtra siempre por `TenantContext.getEmpresaId()`.
- Las tablas operativas (dispensación, solicitud, ajuste, balance, nota de enfermería) también filtran por `sede_id`.
- Catálogos globales (motivos de glosa, tipos de antecedente) compartidos entre empresas.

### 3.2 Auditoría obligatoria
- Cada inserción, actualización y eliminación queda en `auditoria` con `empresa_id`, `sede_id`, `usuario_id`, `ip_origen`, `datos_antes`, `datos_despues`.
- Las acciones de farmacia (dispensar, ajustar, dar de baja) son especialmente sensibles y se auditan con detalle.

### 3.3 Soft delete universal
- Toda tabla nueva con `deleted_at` (timestamp). Nunca borrado físico.

### 3.4 Criterios de aceptación transversales
Todas las HUs de fase 2 incluyen implícitamente:

- **CA-T1**: La operación solo afecta registros de la empresa del usuario autenticado.
- **CA-T2**: El usuario no puede ver, listar ni buscar registros de otras empresas.
- **CA-T3**: En módulos operativos, el usuario solo ve registros de su sede activa.
- **CA-T4**: Intentar acceder por ID a un registro de otra empresa retorna 404.
- **CA-T5**: Todo evento queda auditado con `empresa_id`, `sede_id`, `usuario_id`.

### 3.5 Convenciones técnicas
- Backend: Spring Boot 3.x + Java 17 + JPA mínimo + QueryRepository SQL nativo.
- BD: nombres en español, snake_case, singular; auditoría en inglés (`created_at`, `updated_at`, `deleted_at`).
- DTOs: inglés camelCase. Mappers traducen EN↔ES.

---

## 4. Lista priorizada de historias de usuario

Prioridad: A=Alta, M=Media, B=Baja.

### Bloque 9 — Glosas y conciliación
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE2-061 | Catálogo de motivos de glosa | A |
| HU-FASE2-062 | Recepción de glosa por radicación | A |
| HU-FASE2-063 | Detalle de glosa por ítem de factura | A |
| HU-FASE2-064 | Respuesta a glosa por ítem | A |
| HU-FASE2-065 | Conciliación y cierre de glosa | A |
| HU-FASE2-066 | Impacto de glosa en cuenta por cobrar | A |

### Bloque 10 — Farmacia e inventario
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE2-067 | Gestión de bodegas | A |
| HU-FASE2-068 | Gestión de proveedores | A |
| HU-FASE2-069 | Recepción de compras | A |
| HU-FASE2-070 | Gestión de lotes y vencimientos | A |
| HU-FASE2-071 | Consulta de stock por lote y bodega | A |
| HU-FASE2-072 | Solicitud de medicamento desde servicio | A |
| HU-FASE2-073 | Despacho de solicitud con FEFO | A |
| HU-FASE2-074 | Dispensación con trazabilidad de lote | A |
| HU-FASE2-075 | Devolución a farmacia | M |
| HU-FASE2-076 | Traslado entre bodegas | M |
| HU-FASE2-077 | Ajuste de inventario | M |
| HU-FASE2-078 | Kardex y alertas | A |

### Bloque 11 — Historia clínica avanzada
| Código | Título | Prioridad |
|--------|--------|-----------|
| HU-FASE2-079 | Antecedentes personales clasificados | A |
| HU-FASE2-080 | Antecedentes familiares | A |
| HU-FASE2-081 | Hábitos y estilo de vida | M |
| HU-FASE2-082 | Revisión por sistemas | M |
| HU-FASE2-083 | Esquema de vacunación | M |
| HU-FASE2-084 | Medicación habitual del paciente | A |
| HU-FASE2-085 | Plan de cuidados de enfermería | A |
| HU-FASE2-086 | Notas de enfermería | A |
| HU-FASE2-087 | Administración de medicamentos (MAR) | A |
| HU-FASE2-088 | Balance de líquidos | A |
| HU-FASE2-089 | Escalas clínicas | A |
| HU-FASE2-090 | Interconsulta y respuesta | A |
| HU-FASE2-091 | Epicrisis estructurada al egreso | A |
| HU-FASE2-092 | Adjuntos clínicos del paciente | M |
| HU-FASE2-093 | Vista consolidada de historia clínica | A |

**Total fase 2**: 33 HUs.

---

## Bloque 9. Glosas y conciliación

---

### HU-FASE2-061 — Catálogo de motivos de glosa

**Módulo**: Glosas y conciliación
**Actor principal**: Super-administrador

**Historia de usuario**
Como super-administrador, quiero mantener el catálogo global de motivos de glosa según la Resolución 3047 de 2008, para que todas las empresas usen los códigos oficiales al gestionar glosas.

**Objetivo funcional**
Proveer el catálogo maestro de códigos oficiales de glosa que la institución usa al recibir glosas y responderlas.

**Descripción funcional detallada**
1. El super-administrador accede a "Motivos de glosa".
2. Visualiza los códigos cargados con: código (ej: FA0101), nombre, grupo (FACTURACION, TARIFAS, COBERTURA, AUTORIZACIONES, PERTINENCIA, DEVOLUCION), descripción, indicador `aplica_devolucion`.
3. Puede activar/inactivar códigos (no eliminar).
4. La carga inicial se hace por seed con todos los códigos de la Resolución 3047.

**Reglas de negocio**
- Catálogo global sin `empresa_id`.
- Código único globalmente.
- Solo super-administrador puede crear/editar.
- Las empresas solo consultan.
- Códigos con `aplica_devolucion = true` son causales de devolución total de la factura.

**Validaciones**
- Código, nombre y grupo obligatorios.
- Código único.

**Datos involucrados**
- `motivo_glosa` (escritura)

**Dependencias**
- Ninguna previa.

**Criterios de aceptación**
- CA1: Al iniciar el sistema están cargados todos los motivos de la Resolución 3047.
- CA2: Como super-admin puedo activar/desactivar motivos.
- CA3: Como administrador de empresa solo consulto el catálogo.

**Prioridad**: Alta

---

### HU-FASE2-062 — Recepción de glosa por radicación

**Módulo**: Glosas y conciliación
**Actor principal**: Auditor de cuentas / Facturador

**Historia de usuario**
Como auditor de cuentas, quiero registrar las glosas que el pagador notifica sobre una factura ya radicada de mi empresa, para iniciar el proceso de respuesta y conciliación.

**Objetivo funcional**
Crear el encabezado de la glosa asociado a una factura radicada y empezar el flujo de respuesta.

**Descripción funcional detallada**
1. El auditor selecciona una factura de su empresa con estado `RADICADA`.
2. Captura: número de oficio del pagador, fecha de oficio, fecha de notificación, valor total glosado.
3. Adjunta el oficio (URL al archivo).
4. El sistema crea el registro `glosa` con estado `ABIERTA` y calcula `fecha_limite_respuesta` (15 días hábiles según norma).
5. La factura cambia su estado a `GLOSADA`.

**Reglas de negocio**
- Solo facturas `RADICADA` o `EN_AUDITORIA` aceptan glosa.
- `empresa_id` y `sede_id` se heredan de la factura.
- Una factura puede tener varias glosas (parciales).
- `fecha_limite_respuesta` se calcula automáticamente: 15 días hábiles desde la notificación.
- Cambiar estado de factura a `GLOSADA` actualiza el saldo en cartera (queda pendiente conciliación).

**Validaciones**
- Campos obligatorios: factura, número de oficio, fecha notificación, valor glosado.
- Valor glosado mayor que cero y no mayor que valor total de la factura.

**Datos involucrados**
- `glosa` (escritura)
- `factura` (lectura, actualización de estado)
- `radicacion` (lectura)
- `auditoria`

**Dependencias**
- HU-FASE1-058 (radicación).

**Criterios de aceptación**
- CA1: Solo registro glosas de facturas radicadas de mi empresa.
- CA2: El sistema calcula la fecha límite automáticamente.
- CA3: La factura cambia a estado glosada.
- CA4: No puedo glosar más del valor total de la factura.
- Aplican CA-T1 a CA-T5.

**Prioridad**: Alta

---

### HU-FASE2-063 — Detalle de glosa por ítem de factura

**Módulo**: Glosas y conciliación
**Actor principal**: Auditor de cuentas

**Historia de usuario**
Como auditor de cuentas, quiero registrar qué ítems específicos de la factura están glosados con su motivo oficial, para tener trazabilidad detallada y poder responder técnicamente.

**Descripción funcional detallada**
1. Desde la glosa abierta, el auditor selecciona "Agregar ítems glosados".
2. Por cada ítem objetado registra: detalle de factura, motivo de glosa (catálogo), valor glosado, observación del pagador.
3. La suma de los detalles debe coincidir con el valor total de la glosa.

**Reglas de negocio**
- Cada `detalle_glosa` referencia un motivo del catálogo.
- Un mismo `detalle_factura` puede tener múltiples motivos de glosa.
- Suma de `valor_glosado` de detalles = `valor_glosado` de la glosa (validación al cerrar captura).
- Solo se permite editar mientras la glosa esté `ABIERTA`.

**Validaciones**
- Detalle de factura, motivo y valor obligatorios.
- Valor glosado del detalle no puede superar el valor del ítem en la factura.
- Suma cuadrada antes de cerrar captura.

**Datos involucrados**
- `detalle_glosa` (escritura, FK a `motivo_glosa`)
- `detalle_factura` (lectura)
- `motivo_glosa` (lectura)

**Dependencias**
- HU-FASE2-061, HU-FASE2-062.

**Criterios de aceptación**
- CA1: Agrego múltiples ítems con motivo oficial.
- CA2: La suma de detalles cuadra con el total de la glosa.
- CA3: No puedo glosar más del valor del ítem original.

**Prioridad**: Alta

---

### HU-FASE2-064 — Respuesta a glosa por ítem

**Módulo**: Glosas y conciliación
**Actor principal**: Auditor médico / Facturador

**Historia de usuario**
Como auditor médico, quiero responder a cada ítem glosado con argumentación clínica/técnica y soporte documental, para sustentar la posición de la institución ante el pagador.

**Descripción funcional detallada**
1. Desde la glosa, accedo a la lista de ítems glosados.
2. Por cada ítem registro mi respuesta: tipo (acepta total, acepta parcial, no acepta), valor que la institución acepta, argumentación, soporte adjunto.
3. La glosa cambia a estado `EN_RESPUESTA`.
4. Al completar la respuesta de todos los ítems, paso al estado `RESPONDIDA`.

**Reglas de negocio**
- Una respuesta por ítem (no múltiples).
- Si "acepta total", el valor aceptado = valor glosado del ítem.
- Si "acepta parcial", se requiere el valor exacto y argumentación.
- Si "no acepta", el valor aceptado = 0.
- Estado de la glosa: ABIERTA → EN_RESPUESTA al iniciar la primera respuesta → RESPONDIDA al cerrar todas.
- Solo se responde dentro del plazo (alerta visual si está vencido).

**Validaciones**
- Tipo de respuesta y argumentación obligatorios.
- Si parcial, valor aceptado mayor que cero y menor que el glosado.

**Datos involucrados**
- `respuesta_glosa` (escritura)
- `detalle_glosa` (lectura)
- `glosa` (actualización de estado)

**Dependencias**
- HU-FASE2-063.

**Criterios de aceptación**
- CA1: Respondo cada ítem con uno de los 3 tipos.
- CA2: La glosa avanza de estado conforme respondo.
- CA3: El sistema me alerta si voy a responder fuera de plazo.

**Prioridad**: Alta

---

### HU-FASE2-065 — Conciliación y cierre de glosa

**Módulo**: Glosas y conciliación
**Actor principal**: Coordinador de cuentas

**Historia de usuario**
Como coordinador de cuentas, quiero registrar el resultado final de la conciliación con el pagador, para cerrar la glosa con valores definitivos.

**Descripción funcional detallada**
1. Tras reunión de conciliación con el pagador, accedo a la glosa.
2. Registro: fecha de concertación, valor aceptado por la institución, valor aceptado por el pagador (lo que recupera la institución), acta firmada (URL).
3. El sistema calcula automáticamente `valor_recuperado = valor_glosa_inicial - valor_aceptado_institucion`.
4. La glosa pasa a estado `CERRADA`.
5. Se dispara el impacto financiero (HU-FASE2-066).

**Reglas de negocio**
- Solo se concilia si la glosa está `RESPONDIDA` o `RATIFICADA`.
- `valor_aceptado_institucion + valor_recuperado = valor_glosa_inicial`.
- Acta de conciliación obligatoria.
- Glosa cerrada no admite cambios.

**Validaciones**
- Fecha, valores y acta obligatorios.
- Suma cuadrada.

**Datos involucrados**
- `concertacion_glosa` (escritura)
- `glosa` (actualización de estado)
- `auditoria`

**Dependencias**
- HU-FASE2-064.

**Criterios de aceptación**
- CA1: Cierro la glosa con valores conciliados y acta.
- CA2: El sistema calcula el valor recuperado.
- CA3: Una glosa cerrada no se edita.

**Prioridad**: Alta

---

### HU-FASE2-066 — Impacto de glosa en cuenta por cobrar

**Módulo**: Glosas y conciliación
**Actor principal**: Sistema (automático)

**Historia de usuario**
Como sistema, al cerrarse la conciliación de una glosa quiero registrar automáticamente el movimiento contra la cuenta por cobrar, para que la cartera refleje el saldo real.

**Descripción funcional detallada**
1. Al pasar la glosa a `CERRADA` (HU-FASE2-065), el sistema:
   - Localiza la `cuenta_por_cobrar` de la factura.
   - Crea un `movimiento_cuenta_por_cobrar` tipo `GLOSA_ACEPTADA` con valor = `valor_aceptado_institucion`.
   - Actualiza el saldo de la CxC restando ese valor.
   - Si la factura tenía múltiples glosas y todas están conciliadas, la factura puede pasar a estado `APROBADA` o `PAGADA` según el caso.

**Reglas de negocio**
- Se ejecuta automáticamente, sin intervención del usuario.
- Trazabilidad: el movimiento referencia la concertación de glosa.
- Se ejecuta en la misma transacción que el cierre de glosa.

**Datos involucrados**
- `cuenta_por_cobrar` (actualización)
- `movimiento_cuenta_por_cobrar` (escritura)
- `factura` (posible actualización de estado)
- `concertacion_glosa` (lectura)

**Dependencias**
- HU-FASE2-065, HU-FASE1-059 (CxC).

**Criterios de aceptación**
- CA1: El cierre de glosa genera automáticamente el movimiento en cartera.
- CA2: El saldo de la CxC refleja el valor aceptado por la institución.
- CA3: El movimiento es trazable a la concertación.

**Prioridad**: Alta

---

## Bloque 10. Farmacia e inventario

---

### HU-FASE2-067 — Gestión de bodegas

**Módulo**: Farmacia e inventario
**Actor principal**: Administrador / Jefe de farmacia

**Historia de usuario**
Como administrador, quiero registrar las bodegas físicas de almacenamiento de mi empresa por sede, para organizar el inventario por ubicación (farmacia central, botiquín de piso, urgencias, quirófano).

**Descripción funcional detallada**
1. El administrador accede a "Bodegas" en el módulo de farmacia.
2. Ve solo bodegas de su empresa.
3. Crea una bodega: sede (de su empresa), código, nombre, tipo (FARMACIA_CENTRAL, BOTIQUIN_PISO, QUIROFANO, URGENCIAS, CONSULTORIO, CARRO_PARO, OTRA), responsable (profesional de la empresa), ubicación física, indicadores `permite_dispensar` y `permite_recibir`.
4. Marca una bodega por sede como `es_principal` (típicamente la farmacia central).
5. Inactiva bodegas que no se usan más (no elimina si tienen movimientos).

**Reglas de negocio**
- Sede y responsable deben ser de la empresa del usuario.
- Código único `(sede_id, codigo)`.
- Solo una bodega `es_principal` por sede.
- Bodega inactiva no recibe nuevos movimientos pero conserva histórico.
- Una bodega con stock no puede inactivarse.

**Validaciones**
- Sede, código, nombre, tipo obligatorios.
- Unicidad por sede.

**Datos involucrados**
- `bodega` (escritura)
- `sede`, `profesional_salud` (lectura)

**Dependencias**
- HU-FASE1-002 (sedes), HU-FASE1-008 (profesionales).

**Criterios de aceptación**
- CA1: Gestiono bodegas de mi empresa.
- CA2: No puedo crear dos bodegas con el mismo código en la misma sede.
- CA3: Solo una principal por sede.
- CA4: No puedo inactivar bodega con stock.

**Prioridad**: Alta

---

### HU-FASE2-068 — Gestión de proveedores

**Módulo**: Farmacia e inventario
**Actor principal**: Administrador de farmacia / Compras

**Historia de usuario**
Como responsable de compras, quiero registrar los proveedores de medicamentos e insumos de mi empresa, para asociarlos a las recepciones de mercancía.

**Descripción funcional detallada**
1. Localizo o creo un tercero tipo `PROVEEDOR` en mi empresa.
2. Lo marco como proveedor capturando: código, cuenta contable, plazo de pago en días, descuento por pronto pago, indicador `requiere_orden_compra`, observaciones.

**Reglas de negocio**
- Todo proveedor es un tercero de la empresa.
- Relación 1 a 1 con tercero por empresa.
- Código único en la empresa.

**Datos involucrados**
- `proveedor` (escritura)
- `tercero` (lectura)

**Dependencias**
- HU-FASE1-010 (terceros).

**Criterios de aceptación**
- CA1: Gestiono proveedores de mi empresa.
- CA2: Un tercero solo puede ser proveedor una vez.
- CA3: No veo proveedores de otras empresas.

**Prioridad**: Alta

---

### HU-FASE2-069 — Recepción de compras

**Módulo**: Farmacia e inventario
**Actor principal**: Auxiliar de farmacia / Almacenista

**Historia de usuario**
Como auxiliar de farmacia, quiero registrar la recepción de mercancía de un proveedor con su detalle por lote, para incrementar el stock y mantener trazabilidad.

**Descripción funcional detallada**
1. Accedo al módulo de compras de mi empresa.
2. Creo una compra capturando: bodega de destino (de mi empresa), proveedor, número de factura del proveedor, fecha de compra, fecha de recepción, soporte (URL).
3. Por cada ítem agrego: servicio de salud (medicamento/insumo), número de lote (crea lote nuevo o referencia existente), fecha de vencimiento, registro INVIMA, cantidad, valor unitario, IVA, descuento.
4. El sistema calcula subtotal, IVA, descuentos, total.
5. Al confirmar la recepción (estado `RECIBIDA`):
   - Crea/actualiza el `lote` correspondiente.
   - Genera `stock_lote` o suma a la cantidad existente.
   - Genera `movimiento_inventario` tipo `ENTRADA_COMPRA`.
6. La compra ya recibida no admite ediciones (solo anulación con justificación).

**Reglas de negocio**
- Bodega y proveedor deben ser de la empresa.
- El número de compra es consecutivo por empresa.
- Servicios deben ser categoría `MEDICAMENTO` o `INSUMO`.
- Si el lote ya existe (mismo número, mismo servicio), se suma al stock.
- Si es lote nuevo, se crea el registro en `lote`.
- Fecha de vencimiento obligatoria y futura.
- Toda la operación es transaccional.

**Validaciones**
- Campos obligatorios completos.
- Valores positivos.
- Fechas coherentes.
- Cantidades > 0.

**Datos involucrados**
- `compra` (escritura)
- `detalle_compra` (escritura)
- `lote` (escritura/lectura)
- `stock_lote` (escritura/actualización)
- `movimiento_inventario` (escritura)

**Dependencias**
- HU-FASE2-067, HU-FASE2-068, HU-FASE2-070, HU-FASE1-047 (servicios).

**Criterios de aceptación**
- CA1: Registro recepción con detalle por lote.
- CA2: Al confirmar la recepción, el stock se incrementa.
- CA3: Se genera el movimiento de inventario con trazabilidad a la compra.
- CA4: Lotes nuevos se crean automáticamente.
- CA5: No puedo recibir en bodegas de otras empresas.

**Prioridad**: Alta

---

### HU-FASE2-070 — Gestión de lotes y vencimientos

**Módulo**: Farmacia e inventario
**Actor principal**: Auxiliar de farmacia

**Historia de usuario**
Como auxiliar, quiero consultar y mantener los lotes de los medicamentos e insumos de mi empresa, para controlar fechas de vencimiento y trazabilidad.

**Descripción funcional detallada**
1. Accedo a "Lotes".
2. Veo todos los lotes activos de la empresa con: medicamento, número de lote, fecha de vencimiento, registro INVIMA, proveedor de origen, stock total.
3. Filtro por: vencimiento próximo (30/60/90 días), vencidos, por servicio.
4. Edito datos no críticos del lote (registro INVIMA, observaciones).
5. Marco un lote como `INACTIVO` si está agotado o se da de baja.

**Reglas de negocio**
- Un lote pertenece a un solo servicio (medicamento/insumo).
- Unicidad `(empresa_id, servicio_salud_id, numero_lote)`.
- Fecha de vencimiento siempre obligatoria.
- Lotes con stock no se eliminan, solo se inactivan.
- No se modifican `numero_lote`, `fecha_vencimiento` después de creado (ajuste solo por ajuste de inventario).

**Datos involucrados**
- `lote` (escritura/lectura)
- `stock_lote` (lectura para totales)

**Dependencias**
- HU-FASE2-069.

**Criterios de aceptación**
- CA1: Veo todos los lotes de mi empresa.
- CA2: Filtro por vencimiento próximo.
- CA3: No puedo modificar número de lote ni fecha de vencimiento.

**Prioridad**: Alta

---

### HU-FASE2-071 — Consulta de stock por lote y bodega

**Módulo**: Farmacia e inventario
**Actor principal**: Auxiliar / Profesional clínico

**Historia de usuario**
Como auxiliar de farmacia, quiero consultar el stock disponible de un medicamento por bodega y lote en mi empresa, para saber qué hay disponible y dónde.

**Descripción funcional detallada**
1. Accedo al módulo "Stock".
2. Busco por medicamento o por bodega.
3. Veo: medicamento, bodega, lote, fecha de vencimiento, cantidad disponible, cantidad reservada, total.
4. El listado se ordena por fecha de vencimiento ascendente (FEFO visual).
5. Filtro por sede, bodega, medicamento, vencimiento próximo.

**Reglas de negocio**
- Solo veo stock de mi empresa.
- Si tengo `sede_id` activa, veo solo stock de mi sede operativa.
- Stock reservado = lo apartado para una solicitud pendiente.
- Stock disponible = lo que se puede dispensar realmente.

**Datos involucrados**
- `stock_lote` (lectura)
- `lote`, `bodega`, `servicio_salud` (lectura para joins)

**Dependencias**
- HU-FASE2-067, HU-FASE2-070.

**Criterios de aceptación**
- CA1: Consulto stock por bodega y lote de mi empresa.
- CA2: Orden por vencimiento (FEFO).
- CA3: Filtros por sede, bodega y vencimiento próximo.

**Prioridad**: Alta

---

### HU-FASE2-072 — Solicitud de medicamento desde servicio

**Módulo**: Farmacia e inventario
**Actor principal**: Profesional de enfermería / Médico

**Historia de usuario**
Como profesional de un servicio (piso, urgencias, quirófano), quiero solicitar medicamentos a la farmacia central con prioridad, para reabastecer mi botiquín o despachar a un paciente.

**Descripción funcional detallada**
1. El profesional crea una solicitud capturando: bodega de origen (farmacia), bodega de destino (su botiquín), prioridad (NORMAL, URGENTE, VITAL), motivo.
2. Agrega los ítems solicitados con cantidad.
3. La solicitud queda en estado `PENDIENTE`.
4. La farmacia ve las solicitudes pendientes y procede a despachar (HU-FASE2-073).

**Reglas de negocio**
- Bodegas origen y destino deben ser de la misma empresa.
- Origen debe permitir despacho (`permite_dispensar = true`).
- Destino debe permitir recibir (`permite_recibir = true`).
- Número de solicitud consecutivo por empresa.
- Prioridad VITAL ordena al inicio del listado de farmacia.

**Datos involucrados**
- `solicitud_medicamento` (escritura)
- `detalle_solicitud_medicamento` (escritura)
- `bodega` (lectura)
- `servicio_salud` (lectura)

**Dependencias**
- HU-FASE2-067.

**Criterios de aceptación**
- CA1: Creo solicitudes a bodegas de mi empresa.
- CA2: Indico prioridad y se respeta el orden.
- CA3: La solicitud queda pendiente para farmacia.

**Prioridad**: Alta

---

### HU-FASE2-073 — Despacho de solicitud con FEFO

**Módulo**: Farmacia e inventario
**Actor principal**: Auxiliar de farmacia

**Historia de usuario**
Como auxiliar de farmacia, quiero despachar las solicitudes pendientes seleccionando lotes según FEFO, para garantizar que se use primero lo más cercano a vencer.

**Descripción funcional detallada**
1. Accedo a "Solicitudes pendientes" de mi empresa.
2. Veo el listado ordenado por prioridad (VITAL → URGENTE → NORMAL) y luego por fecha.
3. Selecciono una solicitud y veo sus ítems.
4. Por cada ítem, el sistema sugiere el lote más cercano a vencer con stock suficiente (FEFO).
5. Puedo aceptar la sugerencia o cambiar lote manualmente con justificación.
6. Al confirmar:
   - Se descuenta del `stock_lote` origen.
   - Se incrementa `stock_lote` en bodega destino.
   - Se crean dos `movimiento_inventario` (TRASLADO_SALIDA y TRASLADO_ENTRADA).
   - La solicitud pasa a `DESPACHADA` o `PARCIAL`.

**Reglas de negocio**
- FEFO obligatorio por defecto, override con justificación.
- No se permite despacho con lotes vencidos.
- Si la cantidad solicitada supera el stock, se hace despacho parcial.
- Si el sistema no puede sugerir (sin stock), se rechaza el ítem.
- Toda la operación es transaccional.

**Datos involucrados**
- `solicitud_medicamento`, `detalle_solicitud_medicamento` (actualización)
- `stock_lote` (descuento + incremento)
- `movimiento_inventario` (escritura, dos registros)

**Dependencias**
- HU-FASE2-072.

**Criterios de aceptación**
- CA1: El sistema sugiere lote por FEFO.
- CA2: No se puede usar lote vencido.
- CA3: Despacho parcial si no hay stock suficiente.
- CA4: Stock origen y destino se actualizan correctamente.
- CA5: Se generan los movimientos de traslado.

**Prioridad**: Alta

---

### HU-FASE2-074 — Dispensación con trazabilidad de lote

**Módulo**: Farmacia e inventario
**Actor principal**: Auxiliar de farmacia / Profesional de enfermería

**Historia de usuario**
Como dispensador, quiero registrar la entrega de medicamentos al paciente o al servicio responsable con trazabilidad del lote, para cumplir norma y poder rastrear posibles efectos adversos.

**Descripción funcional detallada**
1. Selecciono una `prescripcion` activa de mi sede.
2. Por cada ítem prescrito, registro: lote dispensado (sugerido por FEFO), cantidad, observaciones.
3. Capturo el profesional dispensador y opcionalmente el receptor (enfermera, paciente).
4. Al confirmar:
   - Se descuenta `stock_lote`.
   - Se crea `movimiento_inventario` tipo `SALIDA_DISPENSACION`.
   - Se crea `dispensacion` y `detalle_dispensacion`.
   - El `detalle_prescripcion` se marca como dispensado total/parcial.
5. La dispensación queda asociada al paciente y a la prescripción.

**Reglas de negocio**
- Solo se dispensa contra prescripciones activas de la sede.
- Lote no vencido, FEFO obligatorio (override con justificación).
- Cantidad dispensada ≤ cantidad prescrita.
- Número de dispensación consecutivo por empresa.
- Auditoría detallada (quién dispensó, cuándo, qué lote, a qué paciente).

**Datos involucrados**
- `dispensacion` (escritura)
- `detalle_dispensacion` (escritura)
- `stock_lote` (descuento)
- `movimiento_inventario` (escritura)
- `prescripcion`, `detalle_prescripcion` (lectura/actualización)

**Dependencias**
- HU-FASE1-031 (prescripción), HU-FASE2-067, HU-FASE2-070.

**Criterios de aceptación**
- CA1: Dispenso contra prescripciones de mi sede.
- CA2: Trazabilidad completa: lote, cantidad, dispensador, paciente.
- CA3: Stock se descuenta correctamente.
- CA4: No se permite usar lote vencido.

**Prioridad**: Alta

---

### HU-FASE2-075 — Devolución a farmacia

**Módulo**: Farmacia e inventario
**Actor principal**: Auxiliar de enfermería / Auxiliar de farmacia

**Historia de usuario**
Como auxiliar, quiero registrar devoluciones de medicamentos no usados (por suspensión, paciente egresado, sobrante de turno) para que el stock vuelva a la farmacia.

**Descripción funcional detallada**
1. Identifico una dispensación previa cuyo medicamento se devuelve.
2. Capturo: lote, cantidad devuelta, motivo, bodega destino.
3. El sistema:
   - Crea `movimiento_inventario` tipo `DEVOLUCION_PACIENTE`.
   - Incrementa `stock_lote` en la bodega destino.

**Reglas de negocio**
- Solo se devuelven medicamentos no aplicados (no es rescate de medicamento ya administrado).
- Cantidad devuelta ≤ cantidad dispensada.
- Lote vencido se devuelve a baja (no a stock).

**Datos involucrados**
- `movimiento_inventario` (escritura)
- `stock_lote` (incremento)
- `dispensacion`, `detalle_dispensacion` (lectura)

**Dependencias**
- HU-FASE2-074.

**Criterios de aceptación**
- CA1: Registro devoluciones contra dispensaciones de mi sede.
- CA2: El stock se restituye correctamente.

**Prioridad**: Media

---

### HU-FASE2-076 — Traslado entre bodegas

**Módulo**: Farmacia e inventario
**Actor principal**: Jefe de farmacia / Auxiliar

**Historia de usuario**
Como jefe de farmacia, quiero trasladar stock entre bodegas de mi empresa (entre sedes o dentro de la misma sede), para optimizar disponibilidad.

**Descripción funcional detallada**
1. Selecciono bodega origen y bodega destino (ambas de mi empresa).
2. Por cada ítem agrego: lote, cantidad.
3. Al confirmar, el sistema:
   - Descuenta `stock_lote` en origen.
   - Incrementa `stock_lote` en destino.
   - Crea dos `movimiento_inventario` (TRASLADO_SALIDA y TRASLADO_ENTRADA).

**Reglas de negocio**
- Bodegas deben ser de la misma empresa.
- Lote no vencido.
- Cantidad disponible suficiente en origen.

**Datos involucrados**
- `movimiento_inventario` (dos registros)
- `stock_lote` (descuento + incremento)

**Dependencias**
- HU-FASE2-067, HU-FASE2-070.

**Criterios de aceptación**
- CA1: Traslado entre bodegas de mi empresa.
- CA2: Stock se actualiza correctamente en ambas.

**Prioridad**: Media

---

### HU-FASE2-077 — Ajuste de inventario

**Módulo**: Farmacia e inventario
**Actor principal**: Jefe de farmacia / Auditor

**Historia de usuario**
Como jefe de farmacia, quiero registrar ajustes de inventario cuando el conteo físico no coincide con el sistema, para mantener la información correcta.

**Descripción funcional detallada**
1. Creo un ajuste capturando: bodega, tipo (FISICO, DETERIORO, VENCIMIENTO, ERROR_CAPTURA, PERDIDA, OTRO), motivo detallado.
2. Por cada ítem registro: lote, cantidad sistema, cantidad real, valor unitario.
3. El sistema calcula la diferencia y el valor del ajuste.
4. El ajuste queda en `BORRADOR` y requiere aprobación (estado `APROBADO`).
5. Al aplicar (estado `APLICADO`):
   - Se crea `movimiento_inventario` tipo `AJUSTE_POSITIVO` o `AJUSTE_NEGATIVO`.
   - Se actualiza `stock_lote`.

**Reglas de negocio**
- Aprobación por usuario distinto al creador (segregación de funciones).
- Una vez aplicado, el ajuste no se modifica (solo anular con justificación).
- Toda la operación auditada con detalle.

**Datos involucrados**
- `ajuste_inventario` (escritura)
- `detalle_ajuste_inventario` (escritura)
- `stock_lote` (actualización)
- `movimiento_inventario` (escritura)

**Dependencias**
- HU-FASE2-067, HU-FASE2-070.

**Criterios de aceptación**
- CA1: Ajusto inventario solo en bodegas de mi empresa.
- CA2: Aprobación por otro usuario.
- CA3: Stock se actualiza al aplicar.
- CA4: Trazabilidad completa.

**Prioridad**: Media

---

### HU-FASE2-078 — Kardex y alertas

**Módulo**: Farmacia e inventario
**Actor principal**: Jefe de farmacia / Auditor

**Historia de usuario**
Como jefe de farmacia, quiero ver el kardex (movimientos cronológicos) de un medicamento por lote y recibir alertas de vencimiento próximo y stock mínimo, para tomar decisiones a tiempo.

**Descripción funcional detallada**
1. **Kardex**: selecciono un medicamento o un lote y veo todos los movimientos (entradas, salidas, traslados, ajustes, dispensaciones) ordenados cronológicamente con cantidad inicial, movimiento, saldo después.
2. **Alertas de vencimiento**: panel que muestra lotes con vencimiento en 30, 60, 90 días.
3. **Alertas de stock**: panel que muestra medicamentos por debajo del stock mínimo (parámetro por servicio o bodega).
4. **Alertas de vencidos**: lotes ya vencidos con stock disponible (deben darse de baja).

**Reglas de negocio**
- Solo veo datos de mi empresa.
- Stock mínimo se parametriza en `servicio_salud` o por bodega (extensión menor).
- Las alertas se calculan en tiempo real con SQL nativo.

**Datos involucrados**
- `movimiento_inventario` (lectura)
- `lote`, `stock_lote` (lectura)
- `servicio_salud` (lectura)

**Dependencias**
- HU-FASE2-069 a HU-FASE2-077.

**Criterios de aceptación**
- CA1: Veo kardex completo de un lote o medicamento.
- CA2: Veo alertas de vencimiento próximo.
- CA3: Veo alertas de stock mínimo.
- CA4: Solo datos de mi empresa.

**Prioridad**: Alta

---

## Bloque 11. Historia clínica avanzada

---

### HU-FASE2-079 — Antecedentes personales clasificados

**Módulo**: Historia clínica avanzada
**Actor principal**: Médico / Profesional de salud

**Historia de usuario**
Como médico, quiero registrar los antecedentes personales del paciente clasificados por tipo (patológico, quirúrgico, traumático, alérgico, tóxico, gineco-obstétrico, etc.), para tener una visión estructurada de su historia.

**Descripción funcional detallada**
1. Desde la ficha del paciente o desde la consola de atención, accedo a "Antecedentes personales".
2. Veo los antecedentes existentes agrupados por tipo.
3. Agrego nuevo: tipo (catálogo), CIE-10 opcional, descripción, fecha inicio, fecha fin (si aplica), severidad (LEVE/MODERADA/GRAVE/CRITICA), observaciones.
4. Edito o inactivo antecedentes registrados.

**Reglas de negocio**
- Antecedentes pertenecen al paciente y a la empresa.
- Solo profesionales con permiso `registrar_antecedentes` los crean.
- Al inactivar un antecedente, no se elimina (queda histórico).
- Antecedentes alérgicos se destacan visualmente en toda la HC.

**Datos involucrados**
- `antecedente_personal` (escritura)
- `tipo_antecedente` (lectura)
- `catalogo_diagnostico` (lectura)

**Dependencias**
- HU-FASE1-013 (paciente).

**Criterios de aceptación**
- CA1: Registro antecedentes con tipo y severidad.
- CA2: Los alérgicos se destacan en la HC del paciente.
- CA3: Solo veo y edito antecedentes de pacientes de mi empresa.

**Prioridad**: Alta

---

### HU-FASE2-080 — Antecedentes familiares

**Módulo**: Historia clínica avanzada
**Actor principal**: Médico

**Historia de usuario**
Como médico, quiero registrar antecedentes familiares relevantes del paciente, para identificar factores de riesgo hereditarios.

**Descripción funcional detallada**
1. Desde la ficha del paciente accedo a "Antecedentes familiares".
2. Agrego: parentesco (madre, padre, hermano, abuelo materno/paterno, etc.), CIE-10 opcional, descripción, edad de aparición, indicador `es_fallecido` y causa, observaciones.

**Reglas de negocio**
- Pertenece al paciente y empresa.
- Solo profesionales con permiso registran/editan.

**Datos involucrados**
- `antecedente_familiar` (escritura)
- `catalogo_diagnostico` (lectura)

**Dependencias**
- HU-FASE1-013.

**Criterios de aceptación**
- CA1: Registro antecedentes familiares con parentesco y causa.
- CA2: Trazabilidad por empresa.

**Prioridad**: Alta

---

### HU-FASE2-081 — Hábitos y estilo de vida

**Módulo**: Historia clínica avanzada
**Actor principal**: Médico / Enfermería

**Historia de usuario**
Como profesional, quiero registrar los hábitos del paciente (alcohol, tabaco, sustancias, ejercicio, alimentación, sueño, sexual) para evaluar factores de riesgo.

**Descripción funcional detallada**
1. Accedo a "Hábitos" del paciente.
2. Por cada tipo registro: descripción, frecuencia, cantidad, tiempo de consumo, fecha inicio/fin, estado (ACTIVO, EX_CONSUMIDOR, NUNCA, OCASIONAL).

**Reglas de negocio**
- Un solo registro `ACTIVO` por tipo de hábito.
- Al cambiar a estado `EX_CONSUMIDOR`, se registra `fecha_fin`.

**Datos involucrados**
- `habito_paciente` (escritura)

**Dependencias**
- HU-FASE1-013.

**Criterios de aceptación**
- CA1: Registro hábitos del paciente.
- CA2: Solo un hábito activo por tipo.

**Prioridad**: Media

---

### HU-FASE2-082 — Revisión por sistemas

**Módulo**: Historia clínica avanzada
**Actor principal**: Médico

**Historia de usuario**
Como médico, durante una atención quiero registrar la revisión por sistemas del paciente, marcando hallazgos por sistema corporal.

**Descripción funcional detallada**
1. En la consola de atención accedo a "Revisión por sistemas".
2. Por cada sistema (cardiovascular, respiratorio, gastrointestinal, etc.) marco `sin_alteracion` o describo hallazgos.

**Reglas de negocio**
- La revisión está atada a una `atencion`.
- Una sola revisión por sistema por atención.

**Datos involucrados**
- `revision_sistemas` (escritura)
- `atencion` (lectura)

**Dependencias**
- HU-FASE1-028 (consola atención urgencias) o consola hospitalización.

**Criterios de aceptación**
- CA1: Registro revisión por sistemas dentro de una atención de mi sede.
- CA2: La revisión queda firmada cuando se firma la atención.

**Prioridad**: Media

---

### HU-FASE2-083 — Esquema de vacunación

**Módulo**: Historia clínica avanzada
**Actor principal**: Enfermería / Médico

**Historia de usuario**
Como profesional, quiero registrar el esquema de vacunación del paciente, para hacer seguimiento de dosis aplicadas y próximas.

**Descripción funcional detallada**
1. Desde la ficha del paciente accedo a "Vacunas".
2. Veo el listado cronológico de vacunas aplicadas.
3. Registro nueva: nombre vacuna, código (PAI), dosis, total dosis del esquema, fecha aplicación, fecha próxima dosis, laboratorio, lote, vía, profesional que aplicó, institución (si fue externa).

**Reglas de negocio**
- Vacuna pertenece a paciente y empresa.
- Si se aplicó en otra institución, registrar institución externa.
- Sistema sugiere `fecha_proxima_dosis` según esquema PAI.

**Datos involucrados**
- `vacuna_paciente` (escritura)

**Dependencias**
- HU-FASE1-013.

**Criterios de aceptación**
- CA1: Registro vacunas con su esquema completo.
- CA2: Veo cronología de vacunas del paciente.

**Prioridad**: Media

---

### HU-FASE2-084 — Medicación habitual del paciente

**Módulo**: Historia clínica avanzada
**Actor principal**: Médico / Enfermería

**Historia de usuario**
Como profesional, quiero registrar los medicamentos que el paciente toma de forma crónica (no relacionados con la atención actual), para tener claridad sobre su medicación de fondo.

**Descripción funcional detallada**
1. Accedo a "Medicación habitual" del paciente.
2. Registro: medicamento (servicio_salud o nombre libre), dosis, vía, frecuencia, fecha inicio/fin, indicación, profesional prescriptor (texto libre si fue externo).

**Reglas de negocio**
- Diferente de las prescripciones de la atención actual (HU-FASE1-031).
- Estado activo = medicamento que aún toma.
- Al egresar al paciente puede importarse a su medicación habitual.

**Datos involucrados**
- `medicacion_habitual` (escritura)
- `servicio_salud`, `via_administracion`, `frecuencia_dosis` (lectura)

**Dependencias**
- HU-FASE1-013.

**Criterios de aceptación**
- CA1: Registro medicación habitual del paciente.
- CA2: Indico activo/inactivo por medicamento.

**Prioridad**: Alta

---

### HU-FASE2-085 — Plan de cuidados de enfermería

**Módulo**: Historia clínica avanzada
**Actor principal**: Profesional de enfermería

**Historia de usuario**
Como enfermero/a, quiero registrar el plan de cuidados del paciente hospitalizado con diagnóstico de enfermería, objetivos, intervenciones y evaluación, para sistematizar la atención.

**Descripción funcional detallada**
1. Desde la consola del paciente hospitalizado accedo a "Plan de cuidados".
2. Registro: diagnóstico de enfermería (NANDA), objetivos (NOC), intervenciones (NIC), fecha inicio.
3. Actualizo evaluación periódicamente.
4. Cierro el plan al cumplir o al egresar al paciente.

**Reglas de negocio**
- Plan asociado a una atención de mi sede.
- Estados: ACTIVO, CUMPLIDO, MODIFICADO, SUSPENDIDO.
- Solo enfermería crea/edita planes.

**Datos involucrados**
- `plan_cuidados_enfermeria` (escritura)
- `atencion`, `paciente` (lectura)

**Dependencias**
- HU-FASE1-034 (ingreso hospitalario).

**Criterios de aceptación**
- CA1: Creo planes de cuidados de pacientes en mi sede.
- CA2: El plan queda asociado a la atención hospitalaria.
- CA3: Lo actualizo periódicamente y lo cierro al egreso.

**Prioridad**: Alta

---

### HU-FASE2-086 — Notas de enfermería

**Módulo**: Historia clínica avanzada
**Actor principal**: Profesional de enfermería

**Historia de usuario**
Como enfermero/a, quiero registrar notas de enfermería de distintos tipos (ingreso, evolución, novedad, entrega de turno, post-procedimiento, educación) con signos vitales asociados, para documentar cuidados e información clínica del turno.

**Descripción funcional detallada**
1. En la consola del paciente accedo a "Nota de enfermería".
2. Selecciono tipo y turno.
3. Registro contenido y signos vitales tomados (TA sistólica/diastólica, FC, FR, Tº, SatO2, glucometría, dolor EVA).
4. Firmo la nota (no se edita después de firmada).

**Reglas de negocio**
- Solo notas en pacientes activos en mi sede.
- Una nota firmada no se edita (modificaciones se hacen mediante nueva nota).
- La firma graba `fecha_firma` y bloquea edición.

**Datos involucrados**
- `nota_enfermeria` (escritura)
- `atencion`, `paciente` (lectura)

**Dependencias**
- HU-FASE1-034 a HU-FASE1-036.

**Criterios de aceptación**
- CA1: Registro notas de pacientes de mi sede.
- CA2: Una nota firmada no se edita.
- CA3: Veo cronología completa de notas del paciente.
- CA4: Capturo signos vitales junto a la nota.

**Prioridad**: Alta

---

### HU-FASE2-087 — Administración de medicamentos (MAR)

**Módulo**: Historia clínica avanzada
**Actor principal**: Profesional de enfermería

**Historia de usuario**
Como enfermero/a, quiero registrar la administración real de cada dosis de medicamento prescrito al paciente, para tener un registro MAR completo y trazable.

**Descripción funcional detallada**
1. El sistema genera automáticamente las dosis programadas a partir de cada `detalle_prescripcion` (según frecuencia y duración).
2. La enfermera ve el panel MAR del paciente con dosis pendientes y administradas en orden cronológico.
3. Al administrar registra: hora real, dosis administrada, vía, lote (referenciado desde dispensación), reacción adversa si la hubo.
4. Marca como administrada, omitida (con motivo) o suspendida.

**Reglas de negocio**
- Las dosis programadas se generan automáticamente desde la prescripción.
- Una dosis administrada es trazable al lote dispensado.
- Reacción adversa se documenta (puede generar alerta para el médico tratante).
- Solo enfermería de la sede registra MAR.

**Datos involucrados**
- `administracion_medicamento` (escritura)
- `detalle_prescripcion` (lectura)
- `dispensacion`, `lote` (lectura)

**Dependencias**
- HU-FASE1-031, HU-FASE2-074.

**Criterios de aceptación**
- CA1: El sistema programa las dosis según prescripción.
- CA2: Registro administración con trazabilidad de lote.
- CA3: Documento omisiones con motivo.
- CA4: Reacciones adversas quedan registradas.

**Prioridad**: Alta

---

### HU-FASE2-088 — Balance de líquidos

**Módulo**: Historia clínica avanzada
**Actor principal**: Profesional de enfermería

**Historia de usuario**
Como enfermero/a, quiero registrar ingresos y egresos de líquidos del paciente por turno o de 24 horas, para calcular balance hídrico y monitorear su estado.

**Descripción funcional detallada**
1. En la consola del paciente abro "Balance de líquidos".
2. Selecciono turno (mañana, tarde, noche, día completo).
3. Registro ingresos (oral, IV, SNG) y egresos (orina SVD, drenaje, vómito, deposición, sudoración, insensibles) con cantidad en mL y hora.
4. El sistema calcula `total_ingresos`, `total_egresos`, `balance`.

**Reglas de negocio**
- Un balance por turno y paciente.
- Balance positivo (ingresos > egresos) o negativo (egresos > ingresos).
- Balance del día = suma de los 3 turnos o un registro de 24h.

**Datos involucrados**
- `balance_liquidos` (escritura)
- `detalle_balance_liquidos` (escritura)

**Dependencias**
- HU-FASE1-034.

**Criterios de aceptación**
- CA1: Registro balance por turno con detalle.
- CA2: El sistema calcula totales y balance neto.

**Prioridad**: Alta

---

### HU-FASE2-089 — Escalas clínicas

**Módulo**: Historia clínica avanzada
**Actor principal**: Médico / Enfermería / Terapista

**Historia de usuario**
Como profesional, quiero aplicar escalas clínicas estandarizadas (Glasgow, EVA, Norton, Braden, Morse, Downton, Barthel, Lawton, Katz, Mini-Mental, APGAR, Silverman) al paciente, para evaluar su estado de forma objetiva y reproducible.

**Descripción funcional detallada**
1. En la consola del paciente accedo a "Escalas".
2. Selecciono el tipo de escala.
3. El sistema muestra los ítems de la escala con sus puntajes posibles.
4. Marco las opciones, el sistema calcula el puntaje total y la interpretación (riesgo BAJO/MEDIO/ALTO/MUY_ALTO).
5. Registro observaciones.
6. La escala queda asociada a la atención y al paciente.

**Reglas de negocio**
- Cada escala tiene su lógica de puntaje y umbrales (encapsulada en backend o frontend).
- El detalle de los ítems se guarda en JSON (`detalle_escala`).
- Aplicaciones repetidas crean nuevos registros (no se sobrescriben).

**Datos involucrados**
- `escala_clinica` (escritura)
- `atencion`, `paciente` (lectura)

**Dependencias**
- HU-FASE1-028 o HU-FASE1-034.

**Criterios de aceptación**
- CA1: Aplico escalas estandarizadas con cálculo automático.
- CA2: Veo la evolución de una escala en el tiempo (Glasgow seriado, EVA seriado).
- CA3: La escala se asocia a la atención.

**Prioridad**: Alta

---

### HU-FASE2-090 — Interconsulta y respuesta

**Módulo**: Historia clínica avanzada
**Actor principal**: Médico tratante / Médico interconsultado

**Historia de usuario**
Como médico tratante, quiero solicitar interconsulta a otra especialidad y recibir respuesta documentada, para coordinar atención multidisciplinaria.

**Descripción funcional detallada**
1. **Solicitud**: el médico tratante crea una interconsulta con: especialidad destino, motivo, impresión diagnóstica, pregunta clínica, prioridad (NORMAL, URGENTE, VITAL).
2. La interconsulta queda en `PENDIENTE`.
3. El especialista interconsultado ve sus interconsultas pendientes en su tablero.
4. **Respuesta**: el especialista accede, atiende al paciente (crea nueva atención si requiere), registra respuesta y recomendaciones, marca como `RESPONDIDA`.
5. La atención de respuesta queda enlazada a la interconsulta.

**Reglas de negocio**
- Interconsulta dentro de la misma sede o entre sedes de la misma empresa.
- Número de interconsulta consecutivo por empresa.
- VITAL aparece al inicio del listado del especialista.

**Datos involucrados**
- `interconsulta` (escritura)
- `atencion`, `especialidad`, `profesional_salud` (lectura)

**Dependencias**
- HU-FASE1-028, HU-FASE1-034.

**Criterios de aceptación**
- CA1: Solicito interconsulta a especialidades de mi empresa.
- CA2: El especialista ve sus pendientes ordenadas por prioridad.
- CA3: La respuesta queda enlazada a la solicitud.

**Prioridad**: Alta

---

### HU-FASE2-091 — Epicrisis estructurada al egreso

**Módulo**: Historia clínica avanzada
**Actor principal**: Médico tratante

**Historia de usuario**
Como médico tratante, quiero generar la epicrisis estructurada al egreso del paciente hospitalizado, con motivo de ingreso, diagnósticos, evolución, complicaciones, plan de seguimiento y recomendaciones.

**Descripción funcional detallada**
1. Al dar egreso hospitalario (HU-FASE1-038), accedo a "Epicrisis".
2. El sistema precarga datos de la admisión: motivo de ingreso, diagnósticos, procedimientos.
3. Completo: evolución resumida, complicaciones (si las hubo), plan de seguimiento, medicamentos al egreso, recomendaciones, indicaciones de dieta y actividad, fecha de próximo control.
4. Firmo la epicrisis (no se edita después).
5. El sistema genera PDF de la epicrisis para entregar al paciente.

**Reglas de negocio**
- Una sola epicrisis por admisión.
- Solo se crea al dar egreso hospitalario.
- Firmada bloquea edición.

**Datos involucrados**
- `epicrisis` (escritura)
- `admision`, `diagnostico_atencion`, `atencion`, `prescripcion` (lectura)

**Dependencias**
- HU-FASE1-038.

**Criterios de aceptación**
- CA1: Genero epicrisis al egreso de mi paciente hospitalizado.
- CA2: Datos clínicos precargados desde la admisión.
- CA3: Una vez firmada no se edita.
- CA4: Se genera PDF para el paciente.

**Prioridad**: Alta

---

### HU-FASE2-092 — Adjuntos clínicos del paciente

**Módulo**: Historia clínica avanzada
**Actor principal**: Médico / Enfermería / Auxiliar

**Historia de usuario**
Como profesional, quiero adjuntar documentos a la HC del paciente (resultados externos, imágenes, fotos clínicas, ECGs, reportes de patología), para tener información complementaria centralizada.

**Descripción funcional detallada**
1. Desde la HC del paciente accedo a "Adjuntos".
2. Subo un archivo (PDF, imagen): tipo de documento, descripción, fecha del documento, indicador `es_confidencial`.
3. El sistema guarda la URL del archivo (S3, MinIO, sistema de archivos).
4. Veo todos los adjuntos del paciente con filtros por tipo y fecha.

**Reglas de negocio**
- Adjuntos son por paciente y empresa.
- Adjuntos `es_confidencial = true` solo los ven profesionales con permiso especial.
- Tipos: RESULTADO_LABORATORIO, IMAGEN_DIAGNOSTICA, REPORTE_PATOLOGIA, CONSENTIMIENTO, EXAMEN_EXTERNO, FOTO_CLINICA, ECG, OTRO.

**Datos involucrados**
- `adjunto_clinico` (escritura)
- Almacenamiento de archivos (S3/MinIO/FS, no en BD).

**Dependencias**
- HU-FASE1-013.

**Criterios de aceptación**
- CA1: Subo adjuntos a pacientes de mi empresa.
- CA2: Filtro y consulto adjuntos.
- CA3: Confidenciales solo visibles con permiso.

**Prioridad**: Media

---

### HU-FASE2-093 — Vista consolidada de historia clínica

**Módulo**: Historia clínica avanzada
**Actor principal**: Médico / Enfermería autorizada

**Historia de usuario**
Como profesional autorizado, quiero ver una vista consolidada de la historia clínica del paciente con todos sus episodios, antecedentes, notas, órdenes, prescripciones, vacunas y adjuntos, para tener visión integral.

**Descripción funcional detallada**
1. Accedo a la HC del paciente.
2. Encabezado fijo: datos demográficos, alergias destacadas, grupo sanguíneo, grupo de atención.
3. Pestañas:
   - **Resumen**: diagnósticos activos, medicación habitual, última atención, próxima cita.
   - **Episodios**: lista de admisiones con sus atenciones, diagnósticos, conducta.
   - **Anamnesis**: antecedentes personales, familiares, hábitos, vacunas.
   - **Notas**: cronología de notas médicas y de enfermería.
   - **Órdenes**: histórico de órdenes clínicas con su estado.
   - **Medicamentos**: prescripciones, dispensaciones, MAR.
   - **Escalas**: aplicaciones de escalas con evolución.
   - **Adjuntos**: documentos asociados.
   - **Línea de tiempo**: vista cronológica visual de todos los eventos.
4. Toda lectura queda auditada.

**Reglas de negocio**
- Solo profesionales con permiso `consultar_historia_clinica` acceden.
- Toda lectura genera registro en `auditoria` con `accion = 'VIEW'`.
- HC aislada por empresa: si el paciente fue atendido en dos empresas, son dos HC separadas.
- Pacientes con alergias se destacan con indicador rojo permanente.

**Datos involucrados**
- Lectura agregada de: `paciente`, `tercero`, `admision`, `atencion`, `diagnostico_atencion`, `orden_clinica`, `prescripcion`, `dispensacion`, `administracion_medicamento`, `nota_enfermeria`, `antecedente_personal`, `antecedente_familiar`, `habito_paciente`, `vacuna_paciente`, `medicacion_habitual`, `escala_clinica`, `interconsulta`, `epicrisis`, `adjunto_clinico`.
- `auditoria` (escritura por cada lectura).

**Dependencias**
- Todas las HUs anteriores del bloque 11 + del flujo asistencial fase 1.

**Criterios de aceptación**
- CA1: Veo HC consolidada de pacientes de mi empresa.
- CA2: Solo accedo si tengo permiso.
- CA3: Cada lectura queda auditada.
- CA4: Las alergias se destacan visualmente.
- CA5: La línea de tiempo muestra todos los eventos en orden.

**Prioridad**: Alta

---

## Cierre

Este backlog contiene **33 historias de usuario** distribuidas en 3 bloques nuevos (9, 10, 11), todas alineadas con los principios multi-tenant y de auditoría heredados de fase 1.

**Total acumulado** (fase 1 + fase 2): 98 HUs.

**Próximos pasos**:
1. Validar el backlog con el equipo de negocio.
2. Aplicar los DDL del agente de modelo de datos al esquema `sgh`.
3. Cargar el seed del catálogo de motivos de glosa (Resolución 3047).
4. Comenzar Sprint 8 — Glosas (HU-FASE2-061 a 066).
