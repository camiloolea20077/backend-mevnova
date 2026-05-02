# Propuesta de Sprints — Fase 2
## Sistema de Gestión Hospitalaria (SGH)

**Versión**: 1.0
**Cobertura**: 33 HUs nuevas (HU-FASE2-061 a HU-FASE2-093)
**Tablas nuevas**: 32 (ver `agente_modelo_datos_fase2.md`)
**Sprints planificados**: 6 sprints de 2 semanas (12 semanas totales)

---

## 1. Resumen ejecutivo

| # | Sprint | Tema | Duración | HUs |
|---|--------|------|----------|-----|
| 8 | Bootstrap fase 2 | Modelo de datos + permisos + seeds | 2 sem | 1 |
| 9 | Glosas | Recepción, respuesta, conciliación | 2 sem | 5 |
| 10 | Farmacia base | Bodegas, proveedores, compras, lotes, stock | 2 sem | 5 |
| 11 | Farmacia operativa | Solicitudes, despacho FEFO, dispensación, kardex | 2 sem | 7 |
| 12 | HC - Anamnesis y enfermería | Antecedentes, hábitos, vacunas, plan cuidados, notas | 2 sem | 8 |
| 13 | HC - MAR, escalas, epicrisis, vista | MAR, balance, escalas, interconsulta, epicrisis, vista consolidada | 2 sem | 7 |

**Total**: 6 sprints, ~12 semanas (3 meses), 33 HUs.

---

## 2. Detalle por sprint

### Sprint 8 — Bootstrap fase 2 (2 semanas)

**Objetivo**: dejar la BD lista con las 32 tablas nuevas, los permisos y los seeds, sin tocar funcionalidad de negocio aún.

**Trabajo técnico**:

Semana 1:
- Aplicar DDL del `agente_modelo_datos_fase2.md`:
  - 12 tablas del módulo glosas + farmacia (incluye `motivo_glosa`)
  - 13 tablas del módulo farmacia/inventario (`bodega`, `proveedor`, `compra`, `lote`, etc.)
- Validar FKs e índices contra la BD real (siguiendo el patrón usado en fase 1).
- Crear índices parciales `WHERE deleted_at IS NULL` en tablas de alto tráfico:
  - `stock_lote`, `movimiento_inventario`, `dispensacion`, `lote`
  - `nota_enfermeria`, `administracion_medicamento`, `escala_clinica`

Semana 2:
- Aplicar DDL de las 17 tablas del módulo HC avanzada.
- Cargar seeds:
  - `motivo_glosa` (Resolución 3047 - ~80 códigos)
  - `tipo_antecedente` (10 valores)
  - `permiso` global con los ~33 permisos nuevos
- Smoke test: ejecutar el master script de fase 1 + fase 2 contra una BD limpia, verificar que las 142 tablas existen.

**HU asociada**: HU-FASE2-061 (Catálogo de motivos de glosa).

**Entregable**:
- 3 scripts SQL nuevos: `12_fase2_glosas.sql`, `13_fase2_farmacia.sql`, `14_fase2_historia_clinica.sql`.
- 3 scripts seed: `08_motivos_glosa.sql`, `09_tipos_antecedente.sql`, `10_permisos_fase2.sql`.
- Verificación de las 32 tablas creadas y populadas.

**Riesgo**: bajo. Es trabajo técnico aislado.

---

### Sprint 9 — Glosas (2 semanas)

**Objetivo**: cerrar el ciclo de glosas y conciliación, con impacto automático en cartera.

**HUs**:
- HU-FASE2-062 Recepción de glosa por radicación
- HU-FASE2-063 Detalle de glosa por ítem
- HU-FASE2-064 Respuesta a glosa por ítem
- HU-FASE2-065 Conciliación y cierre de glosa
- HU-FASE2-066 Impacto en cuenta por cobrar (automático)

**Backend**:
- `GlosaController` + `GlosaService` + `GlosaQueryRepository`
- `MotivoGlosaController` (solo lectura para empresa, escritura para super-admin)
- `RespuestaGlosaService`
- `ConcertacionGlosaService` (transaccional con HU-066)
- Trigger de actualización de cartera al cerrar glosa

**Patrones a respetar**:
- Multi-tenant en todas las consultas (`empresa_id` + `sede_id` cuando aplique).
- DTOs en inglés camelCase, BD en español.
- Soft-delete en todas las operaciones.
- Una sola transacción para conciliación + impacto en cartera.

**Entregable**: ciclo completo de glosas operativo. Una factura radicada puede recibir glosa, ser respondida, conciliada y reflejarse en cartera.

**Riesgo**: medio. La lógica de impacto en cartera requiere mucho cuidado para no descuadrar saldos.

**Dependencias**: HU-FASE1-058 (radicación) y HU-FASE1-059 (CxC) cerradas.

---

### Sprint 10 — Farmacia base (2 semanas)

**Objetivo**: estructura básica de inventario para poder recibir mercancía y consultar stock.

**HUs**:
- HU-FASE2-067 Gestión de bodegas
- HU-FASE2-068 Gestión de proveedores
- HU-FASE2-069 Recepción de compras
- HU-FASE2-070 Gestión de lotes y vencimientos
- HU-FASE2-071 Consulta de stock por lote y bodega

**Backend**:
- `BodegaController` + service + queryRepository
- `ProveedorController` + service + queryRepository
- `CompraController` con flujo transaccional: compra → detalle → lote → stock_lote → movimiento_inventario
- `LoteController` (solo consulta y mantenimiento de datos no críticos)
- `StockQueryRepository` con SQL nativo optimizado para consultas grandes (joins de stock_lote + lote + bodega + servicio_salud)

**Punto crítico**: la transacción de recepción de compra debe ser atómica. Si falla cualquier paso, se revierte todo (no debe quedar lote sin stock o stock sin movimiento).

**Entregable**: la institución puede registrar bodegas, proveedores, recibir mercancía y ver stock. Aún no puede dispensar.

**Riesgo**: medio-alto. El manejo correcto de lotes es la base de todo el módulo.

---

### Sprint 11 — Farmacia operativa (2 semanas)

**Objetivo**: completar el ciclo operativo: solicitar, despachar, dispensar, devolver, trasladar, ajustar.

**HUs**:
- HU-FASE2-072 Solicitud de medicamento desde servicio
- HU-FASE2-073 Despacho con FEFO
- HU-FASE2-074 Dispensación con trazabilidad de lote
- HU-FASE2-075 Devolución a farmacia
- HU-FASE2-076 Traslado entre bodegas
- HU-FASE2-077 Ajuste de inventario
- HU-FASE2-078 Kardex y alertas

**Backend**:
- `SolicitudMedicamentoController` + service + queryRepository
- `DispensacionController` + service (transaccional con prescripción)
- `DespachoService` con lógica FEFO en SQL nativo:
  ```sql
  SELECT lote_id, cantidad_disponible
  FROM stock_lote sl
  JOIN lote l ON l.id = sl.lote_id
  WHERE sl.bodega_id = :bodega_id
    AND l.servicio_salud_id = :servicio_id
    AND l.fecha_vencimiento > current_date
    AND sl.cantidad_disponible > 0
    AND sl.deleted_at IS NULL
    AND l.deleted_at IS NULL
  ORDER BY l.fecha_vencimiento ASC
  ```
- `TrasladoService` (genera dos movimientos: salida + entrada)
- `AjusteInventarioService` con flujo aprobación
- `KardexQueryRepository` con consultas pesadas (movimientos cronológicos por lote)
- `AlertasFarmaciaQueryRepository` con consultas de:
  - Lotes próximos a vencer (30/60/90 días)
  - Lotes vencidos con stock
  - Productos bajo stock mínimo

**Patrones críticos**:
- Lock optimista o pesimista en `stock_lote` para evitar dispensaciones simultáneas.
- Aprobación de ajustes por usuario distinto al creador (segregación de funciones).

**Entregable**: módulo de farmacia operativo end-to-end. El médico ordena, la enfermería solicita, farmacia despacha por FEFO, se dispensa al paciente con lote trazable, se devuelve lo no usado.

**Riesgo**: alto. Es el sprint con más HUs y con la lógica más delicada (FEFO, locks de stock, aprobaciones).

**Dependencias**: HU-FASE1-031 (prescripción) cerrada.

---

### Sprint 12 — HC: anamnesis y enfermería (2 semanas)

**Objetivo**: estructurar la anamnesis del paciente y las notas de enfermería.

**HUs**:
- HU-FASE2-079 Antecedentes personales
- HU-FASE2-080 Antecedentes familiares
- HU-FASE2-081 Hábitos
- HU-FASE2-082 Revisión por sistemas
- HU-FASE2-083 Vacunas
- HU-FASE2-084 Medicación habitual
- HU-FASE2-085 Plan de cuidados
- HU-FASE2-086 Notas de enfermería

**Backend**:
- `AnamnesisService` con sub-controladores para cada tipo de antecedente
- `HabitoPacienteController`
- `RevisionSistemasController` (asociado a atención)
- `VacunaPacienteController`
- `MedicacionHabitualController`
- `PlanCuidadosEnfermeriaController`
- `NotaEnfermeriaController` con lógica de firma:
  - Una vez firmada, `firmada = true` y `fecha_firma`, no admite edición.
  - Modificaciones se hacen mediante nueva nota referenciando la anterior.

**Patrones a respetar**:
- Antecedentes alérgicos se destacan en consultas posteriores (flag visual).
- Solo profesionales con permiso `registrar_antecedentes` (o similar) pueden escribir.
- Lectura: cualquier profesional con permiso `consultar_historia_clinica`.

**Entregable**: la HC del paciente captura anamnesis estructurada y la enfermería puede registrar planes de cuidado y notas firmadas.

**Riesgo**: medio. Son muchas tablas pero el patrón es CRUD simple en cada una.

---

### Sprint 13 — HC: MAR, escalas, epicrisis, vista (2 semanas)

**Objetivo**: cerrar la HC avanzada con MAR, balances, escalas, interconsulta, epicrisis y la vista consolidada.

**HUs**:
- HU-FASE2-087 Administración de medicamentos (MAR)
- HU-FASE2-088 Balance de líquidos
- HU-FASE2-089 Escalas clínicas
- HU-FASE2-090 Interconsulta
- HU-FASE2-091 Epicrisis estructurada
- HU-FASE2-092 Adjuntos clínicos
- HU-FASE2-093 Vista consolidada de HC

**Backend**:
- `MARService`:
  - Genera dosis programadas automáticamente desde `detalle_prescripcion` según frecuencia y duración.
  - Endpoint para registrar administración real con trazabilidad de lote dispensado.
  - Manejo de omisión, suspensión, reacción adversa.
- `BalanceLiquidosController`
- `EscalaClinicaController` con lógica de cálculo de puntaje según tipo (Glasgow, EVA, Norton, Braden, Morse, etc.)
- `InterconsultaController` con flujo solicitud → respuesta y enlazamiento de atenciones
- `EpicrisisService`:
  - Precarga datos desde admisión, diagnósticos, prescripciones
  - Generación de PDF (PDFBox o similar)
  - Una sola epicrisis por admisión
- `AdjuntoClinicoController` con manejo de URLs (S3, MinIO, FS local)
- `HistoriaClinicaQueryRepository`:
  - Vista consolidada con SQL nativo agregando datos de ~25 tablas
  - Audita cada lectura en `auditoria` con `accion = 'VIEW'`
  - Diseño de respuesta paginada por sección (no cargar todo de golpe)

**Punto crítico — vista consolidada**:
- No hacer una sola consulta gigante. Mejor varios endpoints por sección:
  - `/hc/{paciente_id}/resumen`
  - `/hc/{paciente_id}/episodios`
  - `/hc/{paciente_id}/anamnesis`
  - `/hc/{paciente_id}/notas?tipo=...`
  - `/hc/{paciente_id}/medicamentos`
  - `/hc/{paciente_id}/escalas`
  - `/hc/{paciente_id}/adjuntos`
  - `/hc/{paciente_id}/linea-tiempo`

**Entregable**: HC del paciente completamente operativa con vista consolidada. El médico tiene visión integral del paciente, la enfermería tiene MAR/balance/escalas, el sistema genera epicrisis al egreso.

**Riesgo**: alto. Es el sprint más denso clínicamente, y la vista consolidada requiere cuidado de performance.

**Dependencias**: HU-FASE1-031 (prescripción), HU-FASE2-074 (dispensación), HU-FASE1-038 (egreso).

---

## 3. Plan de calidad

### Pruebas por sprint
Cada sprint debe cerrar con:
- **Pruebas unitarias** de servicios críticos (mínimo 70% cobertura).
- **Pruebas de integración** del flujo principal del sprint (un caso end-to-end probado contra BD real).
- **Pruebas de aislamiento multi-tenant**: crear datos en empresa A y verificar que un usuario de empresa B no los ve.
- **Pruebas de rollback**: en operaciones transaccionales (recepción de compra, dispensación, conciliación de glosa), forzar fallos intermedios y verificar que la BD queda consistente.

### Pruebas de carga (final de fase 2)
Antes de declarar fase 2 cerrada:
- Stock con 50.000 lotes y 500.000 movimientos: ¿la consulta de kardex y FEFO se mantiene <500ms?
- HC consolidada de un paciente con 100 atenciones, 500 notas, 1000 dispensaciones: ¿carga en <2s?
- Concurrencia: 10 dispensaciones simultáneas del mismo lote, verificar que no hay sobre-descuento.

---

## 4. Hitos por sprint

| Sprint | Hito de cierre |
|--------|----------------|
| 8 | BD con 142 tablas y seeds cargados |
| 9 | Una factura radicada puede ciclo completo de glosa hasta cartera |
| 10 | Farmacia recibe mercancía y muestra stock |
| 11 | Dispensación end-to-end funciona (médico → enfermera → farmacia → paciente) |
| 12 | HC del paciente captura anamnesis y notas de enfermería firmadas |
| 13 | Epicrisis al egreso se genera con PDF + vista consolidada de HC carga sin lentitud |

---

## 5. Equipo recomendado

Para cumplir el cronograma de 6 sprints (12 semanas):

| Rol | Cantidad | Carga |
|-----|---------|-------|
| Backend Senior (Spring Boot) | 2 | Tiempo completo |
| Backend Mid (Spring Boot) | 1 | Tiempo completo |
| Frontend (Angular 18 + SAKAI) | 1 | Tiempo completo, arranca cuando backend tenga endpoints estables |
| QA | 1 | Medio tiempo (al final de cada sprint) |
| Líder técnico / Arquitecto | 1 | Medio tiempo (revisión de PRs y decisiones) |
| Analista funcional clínico | 1 | Soporte (validación con norma colombiana) |

Si el equipo es más pequeño (ej: 1-2 desarrolladores), el cronograma se duplica a ~24 semanas.

---

## 6. Riesgos y mitigaciones

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Lógica FEFO compleja con concurrencia | Alto | Tests de concurrencia explícitos, lock optimista en stock_lote |
| Cálculo de impacto en cartera incorrecto | Alto | Validación con auditor financiero antes de cerrar Sprint 9 |
| Performance de HC consolidada | Medio | Endpoints paginados por sección, índices específicos, caché por sesión |
| Esquema de la Resolución 3047 cambia | Bajo | Catálogo `motivo_glosa` editable por super-admin |
| Volumen de adjuntos clínicos crece | Medio | Almacenamiento externo (S3/MinIO), no en BD |
| Equipo nuevo en Spring Boot | Medio | Usar el agente backend v3 como referencia obligatoria |

---

## 7. Después de fase 2

Al cerrar fase 2, el sistema cubre:
- Multi-tenant completo (fase 1)
- Admisión, urgencias, hospitalización, citas, facturación, cartera (fase 1)
- Glosas y conciliación (fase 2)
- Farmacia con trazabilidad por lote (fase 2)
- HC avanzada con MAR, balances, escalas, epicrisis (fase 2)

**Lo siguiente (fase 3)** sería:
1. Facturación electrónica DIAN
2. MIPRES completo
3. Interoperabilidad HL7/FHIR
4. Portal del paciente (app móvil)
5. Programación quirúrgica completa
6. Laboratorio con resultados (mensajería HL7)
7. Imágenes diagnósticas con PACS
8. Telemedicina con video
9. BI clínico y financiero (dashboards gerenciales)

---

## 8. Backlog visual de fase 2

```
✅ Fase 1 cerrada (65 HUs)
  └─🔜 Sprint 8 — Bootstrap fase 2 (modelo + seeds)
      └─🔜 Sprint 9 — Glosas (5 HUs)
          └─🔜 Sprint 10 — Farmacia base (5 HUs)
              └─🔜 Sprint 11 — Farmacia operativa (7 HUs)
                  └─🔜 Sprint 12 — HC anamnesis y enfermería (8 HUs)
                      └─🔜 Sprint 13 — HC MAR, escalas, epicrisis, vista (7 HUs)
                          ├─→ Frontend (Angular 18)
                          └─→ Fase 3 (DIAN, MIPRES, HL7, portal paciente)
```
