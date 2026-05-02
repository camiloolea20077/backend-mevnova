# Bloque 11 Historia Clinica

> Archivo modular extraído de `backlog_fase2.md`.

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
