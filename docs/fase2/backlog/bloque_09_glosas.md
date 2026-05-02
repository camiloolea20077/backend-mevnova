# Bloque 09 Glosas

> Archivo modular extraído de `backlog_fase2.md`.

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
