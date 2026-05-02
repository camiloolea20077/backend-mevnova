-- ============================================================
-- SEED: motivo_glosa
-- Sprint 8 — Fase 2 — Módulo Glosas
-- Fuente: Resolución 3047 de 2008 (Manual de Glosas,
--         Devoluciones y Respuestas) y actualizaciones.
-- Catálogo global: sin empresa_id.
-- Idempotente: usa INSERT … WHERE NOT EXISTS.
-- ============================================================

-- ──────────────────────────────────────────────────────────
-- Grupo FACTURACION
-- Causales relacionadas con errores de facturación,
-- soportes incompletos y cobros indebidos.
-- ──────────────────────────────────────────────────────────
INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'F01',
       'Datos de identificación del beneficiario incompletos o incorrectos',
       'El número de documento, nombre o datos del beneficiario no corresponden o están incompletos en la factura.',
       'FACTURACION', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'F01');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'F02',
       'Datos del prestador incompletos o incorrectos',
       'NIT, razón social, habilitación o datos del prestador no coinciden con los registros del pagador.',
       'FACTURACION', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'F02');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'F03',
       'Inconsistencia entre servicios facturados e historia clínica',
       'Los servicios o tecnologías cobrados no tienen respaldo en los registros clínicos del paciente.',
       'FACTURACION', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'F03');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'F04',
       'Cobro de servicios no autorizados',
       'Se facturan servicios para los cuales el pagador no emitió autorización.',
       'FACTURACION', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'F04');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'F05',
       'Duplicidad en la facturación de servicios',
       'Un mismo servicio o tecnología aparece cobrado más de una vez en la misma cuenta o en cuentas previas.',
       'FACTURACION', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'F05');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'F06',
       'Servicios del plan de beneficios cobrados como no cubiertos',
       'Tecnologías incluidas en el plan de beneficios se facturan por mecanismo diferente (ej. medicamentos POS cobrados como No POS).',
       'FACTURACION', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'F06');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'F07',
       'Soportes de la cuenta incompletos o ausentes',
       'La cuenta no incluye los anexos, epicrisis, órdenes médicas u otros documentos exigidos por el contrato.',
       'FACTURACION', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'F07');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'F08',
       'Factura con enmendaduras o tachones',
       'El documento de cobro presenta alteraciones que afectan su validez como soporte fiscal.',
       'FACTURACION', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'F08');

-- ──────────────────────────────────────────────────────────
-- Grupo TARIFAS
-- Diferencias entre lo cobrado y lo pactado contractualmente.
-- ──────────────────────────────────────────────────────────
INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'T01',
       'Tarifa cobrada no pactada en contrato',
       'El valor unitario facturado difiere de la tarifa acordada en el contrato vigente.',
       'TARIFAS', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'T01');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'T02',
       'Error en la liquidación de la cuenta',
       'Errores aritméticos o de aplicación de fórmulas en la liquidación total de la cuenta.',
       'TARIFAS', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'T02');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'T03',
       'Descuento contractual no aplicado',
       'La factura no refleja los descuentos por pronto pago, volumen u otros beneficios pactados.',
       'TARIFAS', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'T03');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'T04',
       'Cobro de copago o cuota moderadora incorrecto',
       'El valor de copago o cuota moderadora descontado al usuario no corresponde a la normativa vigente.',
       'TARIFAS', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'T04');

-- ──────────────────────────────────────────────────────────
-- Grupo COBERTURA
-- Servicios prestados fuera del plan de beneficios
-- o a beneficiarios sin cobertura vigente.
-- ──────────────────────────────────────────────────────────
INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'C01',
       'Beneficiario sin cobertura vigente en la fecha de atención',
       'El paciente no estaba afiliado, se encontraba en mora o su cobertura había terminado a la fecha de la prestación.',
       'COBERTURA', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'C01');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'C02',
       'Servicio no cubierto por el plan de beneficios aplicable',
       'La tecnología o servicio cobrado está expresamente excluido del plan de beneficios del beneficiario.',
       'COBERTURA', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'C02');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'C03',
       'Servicio prestado en período de carencia',
       'El usuario solicitó una prestación sujeta a período de carencia sin haberlo cumplido.',
       'COBERTURA', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'C03');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'C04',
       'Servicio prestado en IPS no habilitada para esa tecnología',
       'La institución no contaba con habilitación vigente para el servicio facturado en la fecha de la prestación.',
       'COBERTURA', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'C04');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'C05',
       'Atención no urgente fuera de la red contratada sin autorización',
       'El paciente accedió a un prestador fuera de red para un servicio que no era urgencia y sin previa autorización.',
       'COBERTURA', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'C05');

-- ──────────────────────────────────────────────────────────
-- Grupo AUTORIZACIONES
-- Deficiencias o ausencias en el proceso de autorización.
-- ──────────────────────────────────────────────────────────
INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'A01',
       'Servicio prestado sin autorización previa',
       'El servicio requería autorización del pagador y fue entregado sin haberla obtenido.',
       'AUTORIZACIONES', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'A01');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'A02',
       'Autorización vencida a la fecha de prestación',
       'El servicio se prestó cuando la autorización emitida ya había expirado.',
       'AUTORIZACIONES', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'A02');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'A03',
       'Código de servicio autorizado difiere del facturado',
       'El código CUPS u otro código del servicio en la autorización no coincide con el cobrado.',
       'AUTORIZACIONES', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'A03');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'A04',
       'Cantidad de servicios excede la autorizada',
       'Se factura un número de sesiones, días o unidades mayor al autorizado por el pagador.',
       'AUTORIZACIONES', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'A04');

-- ──────────────────────────────────────────────────────────
-- Grupo PERTINENCIA
-- Cuestionamiento a la necesidad clínica del servicio.
-- ──────────────────────────────────────────────────────────
INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'P01',
       'Diagnóstico no soporta la tecnología facturada',
       'El diagnóstico registrado en la historia clínica no justifica el procedimiento, medicamento o dispositivo cobrado.',
       'PERTINENCIA', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'P01');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'P02',
       'Procedimiento o tecnología no pertinente según diagnóstico',
       'Según guías de práctica clínica o evidencia, el servicio no tiene indicación para el diagnóstico presentado.',
       'PERTINENCIA', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'P02');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'P03',
       'Estancia hospitalaria prolongada sin soporte clínico',
       'Los días de hospitalización cobrados superan lo médicamente justificado según los registros clínicos.',
       'PERTINENCIA', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'P03');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'P04',
       'Servicio ordenado por profesional no habilitado',
       'La orden que generó el servicio fue emitida por un profesional sin habilitación para ese tipo de prescripción.',
       'PERTINENCIA', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'P04');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'P05',
       'Cantidad de insumos o medicamentos excede la dosis establecida',
       'La cantidad facturada de un medicamento o insumo supera la dosis o la duración clínicamente justificada.',
       'PERTINENCIA', false, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'P05');

-- ──────────────────────────────────────────────────────────
-- Grupo DEVOLUCION
-- Causales de devolución total de la cuenta;
-- aplica_devolucion = true.
-- ──────────────────────────────────────────────────────────
INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'D01',
       'Factura sin requisitos legales para ser considerada como tal',
       'El documento de cobro no cumple los requisitos del Estatuto Tributario ni la normativa de facturación electrónica.',
       'DEVOLUCION', true, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'D01');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'D02',
       'Radicación extemporánea',
       'La cuenta se presentó fuera del plazo máximo pactado en el contrato o establecido por la norma.',
       'DEVOLUCION', true, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'D02');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'D03',
       'Cuenta radicada ante pagador incorrecto',
       'La factura fue enviada a una entidad que no es responsable del pago según el régimen o contrato del paciente.',
       'DEVOLUCION', true, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'D03');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'D04',
       'Cuenta ya pagada anteriormente',
       'La prestación ya fue reconocida y pagada en una radicación anterior, constituyendo cobro doble.',
       'DEVOLUCION', true, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'D04');

INSERT INTO motivo_glosa (codigo, nombre, descripcion, grupo, aplica_devolucion, activo)
SELECT 'D05',
       'Soportes obligatorios ausentes que impiden la auditoría',
       'La ausencia de documentos esenciales (historia clínica, autorización, consentimiento) hace imposible auditar la cuenta.',
       'DEVOLUCION', true, true
WHERE NOT EXISTS (SELECT 1 FROM motivo_glosa WHERE codigo = 'D05');
