# Bloque 10 Farmacia

> Archivo modular extraído de `backlog_fase2.md`.

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
