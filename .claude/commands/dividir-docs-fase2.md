# Dividir documentación fase 2 por módulos

Objetivo:
Separar los documentos grandes de fase 2 en archivos pequeños por módulo para reducir tokens.

Archivos fuente:
- docs/agentes/agente_modelo_datos_fase2.md
- docs/fase2_original/backlog_fase2.md
- docs/fase2_original/matriz_trazabilidad_fase2.md
- docs/fase2_original/sprints_fase2.md

Crear o actualizar:

1. docs/fase2/00_contexto_minimo_fase2.md
   Debe contener:
   - Resumen de fase 2.
   - Bloques 9, 10 y 11.
   - Reglas transversales.
   - Forma de trabajo por HU.

2. docs/fase2/backlog/bloque_09_glosas.md
   Extraer del backlog:
   - Bloque 9 — Glosas y conciliación.
   - HU-FASE2-061 a HU-FASE2-066.
   - No incluir Farmacia ni Historia clínica.

3. docs/fase2/backlog/bloque_10_farmacia.md
   Extraer del backlog:
   - Bloque 10 — Farmacia e inventario.
   - HU-FASE2-067 a HU-FASE2-078.
   - No incluir Glosas ni Historia clínica.

4. docs/fase2/backlog/bloque_11_historia_clinica.md
   Extraer del backlog:
   - Bloque 11 — Historia clínica avanzada.
   - HU-FASE2-079 a HU-FASE2-093.
   - No incluir Glosas ni Farmacia.

5. docs/fase2/modelo/modelo_glosas.md
   Extraer del modelo:
   - Contexto de Glosas.
   - Tablas existentes reutilizadas.
   - motivo_glosa.
   - concertacion_glosa.
   - ALTER TABLE detalle_glosa.
   - Resumen del módulo.

6. docs/fase2/modelo/modelo_farmacia.md
   Extraer del modelo:
   - Contexto de Farmacia e inventario.
   - Tablas existentes reutilizadas.
   - bodega.
   - proveedor.
   - compra.
   - lote.
   - detalle_compra.
   - stock_lote.
   - movimiento_inventario.
   - solicitud_medicamento.
   - detalle_solicitud_medicamento.
   - dispensacion.
   - detalle_dispensacion.
   - ajuste_inventario.
   - detalle_ajuste_inventario.
   - Resumen del módulo.

7. docs/fase2/modelo/modelo_historia_clinica.md
   Extraer del modelo:
   - Contexto de Historia clínica avanzada.
   - tipo_antecedente.
   - antecedente_personal.
   - antecedente_familiar.
   - habito_paciente.
   - revision_sistemas.
   - vacuna_paciente.
   - medicacion_habitual.
   - plan_cuidados_enfermeria.
   - nota_enfermeria.
   - administracion_medicamento.
   - balance_liquidos.
   - detalle_balance_liquidos.
   - escala_clinica.
   - interconsulta.
   - epicrisis.
   - adjunto_clinico.
   - consentimiento_informado.
   - Resumen del módulo.

8. docs/fase2/trazabilidad/trazabilidad_glosas.md
   Extraer de la matriz:
   - Filas HU-FASE2-061 a HU-FASE2-066.
   - Matriz inversa de tablas de Glosas.
   - Dependencias de Glosas.
   - Permisos de Glosas.

9. docs/fase2/trazabilidad/trazabilidad_farmacia.md
   Extraer de la matriz:
   - Filas HU-FASE2-067 a HU-FASE2-078.
   - Matriz inversa de tablas de Farmacia.
   - Dependencias de Farmacia.
   - Permisos de Farmacia.

10. docs/fase2/trazabilidad/trazabilidad_historia_clinica.md
   Extraer de la matriz:
   - Filas HU-FASE2-079 a HU-FASE2-093.
   - Matriz inversa de tablas de Historia clínica.
   - Dependencias de Historia clínica.
   - Permisos de Historia clínica.

Reglas:
- No resumir las HUs.
- No cambiar el sentido funcional.
- No eliminar criterios de aceptación.
- No mezclar módulos.
- Mantener nombres técnicos exactos.
- Al final, reportar archivos creados y qué rango de HUs contiene cada uno.