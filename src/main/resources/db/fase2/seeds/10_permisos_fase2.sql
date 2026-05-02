-- ============================================================
-- SEED: permisos fase 2
-- Sprint 8 — Módulos: GLOSAS, FARMACIA, HISTORIA_CLINICA
-- Fuente: trazabilidad_glosas.md, trazabilidad_farmacia.md,
--         trazabilidad_historia_clinica.md
-- Idempotente: usa INSERT … WHERE NOT EXISTS.
-- No modifica ni elimina permisos existentes de fase 1.
-- ============================================================

-- ──────────────────────────────────────────────────────────
-- Módulo: GLOSAS
-- HUs: HU-FASE2-061 a HU-FASE2-066
-- ──────────────────────────────────────────────────────────

-- HU-FASE2-061: gestión del catálogo (solo super-admin)
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'GLOSAS_GESTIONAR_MOTIVOS',
       'Gestionar motivos de glosa',
       'Crear, editar y desactivar los motivos de glosa del catálogo oficial (Res. 3047). Solo super-admin.',
       'GLOSAS', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'GLOSAS_GESTIONAR_MOTIVOS');

-- HU-FASE2-062: recepción de glosa del pagador
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'GLOSAS_RECIBIR',
       'Recibir glosa',
       'Registrar la glosa recibida del pagador y asociarla a una factura radicada.',
       'GLOSAS', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'GLOSAS_RECIBIR');

-- HU-FASE2-063: detallar ítems glosados
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'GLOSAS_DETALLAR',
       'Detallar ítems de glosa',
       'Registrar y editar los ítems glosados de una factura con su motivo oficial.',
       'GLOSAS', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'GLOSAS_DETALLAR');

-- HU-FASE2-064: respuesta institucional a la glosa
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'GLOSAS_RESPONDER',
       'Responder glosa',
       'Redactar y enviar la respuesta institucional a los ítems glosados por el pagador.',
       'GLOSAS', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'GLOSAS_RESPONDER');

-- HU-FASE2-065: concertación final
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'GLOSAS_CONCILIAR',
       'Conciliar glosa',
       'Registrar el acuerdo de conciliación entre la institución y el pagador con valores aceptados y recuperados.',
       'GLOSAS', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'GLOSAS_CONCILIAR');

-- HU-FASE2-066: impacto en cartera
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'GLOSAS_APLICAR_CARTERA',
       'Aplicar impacto de glosa en cartera',
       'Registrar el movimiento contable que ajusta la cuenta por cobrar según el resultado de la concertación.',
       'GLOSAS', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'GLOSAS_APLICAR_CARTERA');

-- Consulta transversal (lectura de todo el módulo)
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'GLOSAS_CONSULTAR',
       'Consultar glosas',
       'Ver el listado de glosas, detalles, respuestas y concertaciones sin poder modificarlas.',
       'GLOSAS', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'GLOSAS_CONSULTAR');

-- ──────────────────────────────────────────────────────────
-- Módulo: FARMACIA
-- HUs: HU-FASE2-067 a HU-FASE2-078
-- ──────────────────────────────────────────────────────────

-- HU-FASE2-067: bodegas
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'FARMACIA_GESTIONAR_BODEGAS',
       'Gestionar bodegas',
       'Crear, editar y desactivar bodegas (farmacia central, botiquines, quirófanos) dentro de una sede.',
       'FARMACIA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'FARMACIA_GESTIONAR_BODEGAS');

-- HU-FASE2-068: proveedores
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'FARMACIA_GESTIONAR_PROVEEDORES',
       'Gestionar proveedores',
       'Registrar y administrar proveedores de medicamentos e insumos asociados a un tercero.',
       'FARMACIA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'FARMACIA_GESTIONAR_PROVEEDORES');

-- HU-FASE2-069: recepción de compra
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'FARMACIA_RECIBIR_COMPRA',
       'Recibir compra de proveedor',
       'Registrar la recepción de mercancía, crear o actualizar lotes y actualizar el stock de la bodega receptora.',
       'FARMACIA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'FARMACIA_RECIBIR_COMPRA');

-- HU-FASE2-070: gestión de lotes
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'FARMACIA_GESTIONAR_LOTES',
       'Gestionar lotes',
       'Consultar, registrar y actualizar lotes con trazabilidad INVIMA y control de vencimiento.',
       'FARMACIA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'FARMACIA_GESTIONAR_LOTES');

-- HU-FASE2-071: consulta de stock
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'FARMACIA_CONSULTAR_STOCK',
       'Consultar stock de inventario',
       'Ver la disponibilidad de medicamentos e insumos por lote y bodega (solo lectura).',
       'FARMACIA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'FARMACIA_CONSULTAR_STOCK');

-- HU-FASE2-072: solicitud de medicamentos
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'FARMACIA_SOLICITAR_MEDICAMENTO',
       'Solicitar medicamentos',
       'Crear solicitudes de medicamentos e insumos desde un servicio hacia farmacia central.',
       'FARMACIA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'FARMACIA_SOLICITAR_MEDICAMENTO');

-- HU-FASE2-073: despacho de solicitudes
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'FARMACIA_DESPACHAR_SOLICITUD',
       'Despachar solicitud de medicamentos',
       'Atender solicitudes internas, actualizar stock y registrar el movimiento de traslado entre bodegas.',
       'FARMACIA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'FARMACIA_DESPACHAR_SOLICITUD');

-- HU-FASE2-074: dispensación al paciente
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'FARMACIA_DISPENSAR',
       'Dispensar medicamentos al paciente',
       'Registrar la entrega de medicamentos al paciente con trazabilidad de lote (FEFO) y actualización de stock.',
       'FARMACIA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'FARMACIA_DISPENSAR');

-- HU-FASE2-075: devolución de dispensación
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'FARMACIA_REGISTRAR_DEVOLUCION',
       'Registrar devolución de medicamentos',
       'Procesar devoluciones de medicamentos no administrados al paciente o al proveedor.',
       'FARMACIA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'FARMACIA_REGISTRAR_DEVOLUCION');

-- HU-FASE2-076: traslado entre bodegas
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'FARMACIA_TRASLADAR_INVENTARIO',
       'Trasladar inventario entre bodegas',
       'Mover existencias de lotes entre bodegas de la misma sede con registro de movimiento de inventario.',
       'FARMACIA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'FARMACIA_TRASLADAR_INVENTARIO');

-- HU-FASE2-077: ajuste de inventario (creación)
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'FARMACIA_AJUSTAR_INVENTARIO',
       'Crear ajuste de inventario',
       'Registrar ajustes de inventario físico, por deterioro, vencimiento o error de captura.',
       'FARMACIA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'FARMACIA_AJUSTAR_INVENTARIO');

-- HU-FASE2-077: aprobación de ajuste
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'FARMACIA_APROBAR_AJUSTE',
       'Aprobar ajuste de inventario',
       'Aprobar o rechazar ajustes de inventario en estado BORRADOR para que pasen a APLICADO.',
       'FARMACIA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'FARMACIA_APROBAR_AJUSTE');

-- HU-FASE2-078: kardex y reportes
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'FARMACIA_CONSULTAR_KARDEX',
       'Consultar kardex de inventario',
       'Ver el historial completo de movimientos de un lote o producto por bodega (solo lectura).',
       'FARMACIA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'FARMACIA_CONSULTAR_KARDEX');

-- ──────────────────────────────────────────────────────────
-- Módulo: HISTORIA_CLINICA
-- HUs: HU-FASE2-079 a HU-FASE2-093
-- ──────────────────────────────────────────────────────────

-- HU-FASE2-079 / 080: antecedentes
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'HC_REGISTRAR_ANTECEDENTES',
       'Registrar antecedentes del paciente',
       'Crear, editar y consultar antecedentes personales y familiares del paciente.',
       'HISTORIA_CLINICA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'HC_REGISTRAR_ANTECEDENTES');

-- HU-FASE2-081: hábitos
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'HC_REGISTRAR_HABITOS',
       'Registrar hábitos del paciente',
       'Registrar y actualizar hábitos de vida del paciente (alcohol, tabaco, ejercicio, etc.).',
       'HISTORIA_CLINICA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'HC_REGISTRAR_HABITOS');

-- HU-FASE2-082: revisión por sistemas
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'HC_REGISTRAR_REVISION_SISTEMAS',
       'Registrar revisión por sistemas',
       'Registrar los hallazgos de la revisión por sistemas en una atención.',
       'HISTORIA_CLINICA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'HC_REGISTRAR_REVISION_SISTEMAS');

-- HU-FASE2-083: esquema de vacunación
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'HC_REGISTRAR_VACUNAS',
       'Registrar vacunas del paciente',
       'Registrar dosis aplicadas, lote y fechas del esquema de vacunación del paciente.',
       'HISTORIA_CLINICA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'HC_REGISTRAR_VACUNAS');

-- HU-FASE2-084: medicación habitual
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'HC_REGISTRAR_MEDICACION_HABITUAL',
       'Registrar medicación habitual',
       'Registrar los medicamentos crónicos que el paciente consume fuera del episodio de atención actual.',
       'HISTORIA_CLINICA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'HC_REGISTRAR_MEDICACION_HABITUAL');

-- HU-FASE2-085: plan de cuidados de enfermería
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'HC_GESTIONAR_PLAN_CUIDADOS',
       'Gestionar plan de cuidados de enfermería',
       'Crear, actualizar y cerrar planes de cuidados NANDA/NIC/NOC durante la hospitalización.',
       'HISTORIA_CLINICA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'HC_GESTIONAR_PLAN_CUIDADOS');

-- HU-FASE2-086: nota de enfermería
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'HC_REGISTRAR_NOTA_ENFERMERIA',
       'Registrar nota de enfermería',
       'Crear notas clínicas de turno: ingreso, evolución, novedad, entrega de turno y post-procedimiento.',
       'HISTORIA_CLINICA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'HC_REGISTRAR_NOTA_ENFERMERIA');

-- HU-FASE2-087: MAR — administración de medicamentos
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'HC_REGISTRAR_ADMINISTRACION_MED',
       'Registrar administración de medicamentos (MAR)',
       'Registrar la administración real de cada dosis programada: hora, dosis, vía, estado y observaciones.',
       'HISTORIA_CLINICA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'HC_REGISTRAR_ADMINISTRACION_MED');

-- HU-FASE2-088: balance de líquidos
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'HC_REGISTRAR_BALANCE_LIQUIDOS',
       'Registrar balance de líquidos',
       'Registrar ingresos y egresos hídricos del paciente por turno con cálculo automático de balance.',
       'HISTORIA_CLINICA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'HC_REGISTRAR_BALANCE_LIQUIDOS');

-- HU-FASE2-089: escalas clínicas
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'HC_APLICAR_ESCALAS_CLINICAS',
       'Aplicar escalas clínicas',
       'Registrar la aplicación de escalas de valoración (Glasgow, EVA, Braden, Morse, Barthel, etc.).',
       'HISTORIA_CLINICA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'HC_APLICAR_ESCALAS_CLINICAS');

-- HU-FASE2-090: interconsultas (solicitud)
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'HC_SOLICITAR_INTERCONSULTA',
       'Solicitar interconsulta',
       'Crear solicitudes de interconsulta a otra especialidad desde una atención activa.',
       'HISTORIA_CLINICA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'HC_SOLICITAR_INTERCONSULTA');

-- HU-FASE2-090: interconsultas (respuesta)
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'HC_RESPONDER_INTERCONSULTA',
       'Responder interconsulta',
       'Registrar la respuesta del especialista a una interconsulta recibida, incluyendo recomendaciones.',
       'HISTORIA_CLINICA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'HC_RESPONDER_INTERCONSULTA');

-- HU-FASE2-091: epicrisis
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'HC_GENERAR_EPICRISIS',
       'Generar epicrisis',
       'Redactar y firmar el resumen de egreso hospitalario (epicrisis) al dar de alta al paciente.',
       'HISTORIA_CLINICA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'HC_GENERAR_EPICRISIS');

-- HU-FASE2-092: adjuntos clínicos
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'HC_CARGAR_ADJUNTOS',
       'Cargar adjuntos clínicos',
       'Subir y gestionar documentos adjuntos a la historia clínica del paciente (resultados, imágenes, consentimientos).',
       'HISTORIA_CLINICA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'HC_CARGAR_ADJUNTOS');

-- Adjuntos confidenciales (rol diferenciado)
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'HC_VER_ADJUNTOS_CONFIDENCIALES',
       'Ver adjuntos clínicos confidenciales',
       'Acceder a documentos marcados como confidenciales en la historia clínica del paciente.',
       'HISTORIA_CLINICA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'HC_VER_ADJUNTOS_CONFIDENCIALES');

-- HU-FASE2-093: vista consolidada de historia clínica
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'HC_CONSULTAR_HISTORIA_CLINICA',
       'Consultar historia clínica completa',
       'Acceder a la vista consolidada de la historia clínica: antecedentes, notas, MAR, balances, escalas, interconsultas y epicrisis.',
       'HISTORIA_CLINICA', true, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'HC_CONSULTAR_HISTORIA_CLINICA');
