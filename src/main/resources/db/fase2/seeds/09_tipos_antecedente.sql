-- ============================================================
-- SEED: tipo_antecedente
-- Sprint 8 — Fase 2 — Módulo Historia Clínica
-- Catálogo global de tipos de antecedente clínico.
-- Valores estándar para historia clínica colombiana.
-- Idempotente: usa INSERT … WHERE NOT EXISTS.
-- ============================================================

INSERT INTO tipo_antecedente (codigo, nombre, descripcion, activo)
SELECT 'PATOLOGICO',
       'Patológico',
       'Enfermedades o condiciones médicas previas diagnosticadas al paciente (HTA, DM, cardiopatías, etc.).',
       true
WHERE NOT EXISTS (SELECT 1 FROM tipo_antecedente WHERE codigo = 'PATOLOGICO');

INSERT INTO tipo_antecedente (codigo, nombre, descripcion, activo)
SELECT 'QUIRURGICO',
       'Quirúrgico',
       'Intervenciones quirúrgicas previas, incluyendo fecha aproximada y complicaciones si las hubo.',
       true
WHERE NOT EXISTS (SELECT 1 FROM tipo_antecedente WHERE codigo = 'QUIRURGICO');

INSERT INTO tipo_antecedente (codigo, nombre, descripcion, activo)
SELECT 'TRAUMATICO',
       'Traumático',
       'Traumas, accidentes o lesiones físicas previas con impacto en la salud actual del paciente.',
       true
WHERE NOT EXISTS (SELECT 1 FROM tipo_antecedente WHERE codigo = 'TRAUMATICO');

INSERT INTO tipo_antecedente (codigo, nombre, descripcion, activo)
SELECT 'ALERGICO',
       'Alérgico',
       'Alergias conocidas a medicamentos, alimentos, látex, contrastes u otras sustancias.',
       true
WHERE NOT EXISTS (SELECT 1 FROM tipo_antecedente WHERE codigo = 'ALERGICO');

INSERT INTO tipo_antecedente (codigo, nombre, descripcion, activo)
SELECT 'TOXICO',
       'Tóxico',
       'Exposición a sustancias tóxicas, pesticidas, metales pesados u otros agentes químicos.',
       true
WHERE NOT EXISTS (SELECT 1 FROM tipo_antecedente WHERE codigo = 'TOXICO');

INSERT INTO tipo_antecedente (codigo, nombre, descripcion, activo)
SELECT 'FARMACOLOGICO',
       'Farmacológico',
       'Medicamentos que el paciente ha consumido de forma crónica o que han generado reacciones adversas relevantes.',
       true
WHERE NOT EXISTS (SELECT 1 FROM tipo_antecedente WHERE codigo = 'FARMACOLOGICO');

INSERT INTO tipo_antecedente (codigo, nombre, descripcion, activo)
SELECT 'GINECO_OBSTETRICO',
       'Gineco-obstétrico',
       'Antecedentes de embarazos, partos, abortos, métodos anticonceptivos y patología ginecológica (aplica a mujeres).',
       true
WHERE NOT EXISTS (SELECT 1 FROM tipo_antecedente WHERE codigo = 'GINECO_OBSTETRICO');

INSERT INTO tipo_antecedente (codigo, nombre, descripcion, activo)
SELECT 'PSIQUIATRICO',
       'Psiquiátrico',
       'Diagnósticos o tratamientos psiquiátricos previos: depresión, ansiedad, esquizofrenia, intentos de suicidio, etc.',
       true
WHERE NOT EXISTS (SELECT 1 FROM tipo_antecedente WHERE codigo = 'PSIQUIATRICO');

INSERT INTO tipo_antecedente (codigo, nombre, descripcion, activo)
SELECT 'HOSPITALARIO',
       'Hospitalario',
       'Hospitalizaciones previas: motivo, duración y complicaciones relevantes para la atención actual.',
       true
WHERE NOT EXISTS (SELECT 1 FROM tipo_antecedente WHERE codigo = 'HOSPITALARIO');

INSERT INTO tipo_antecedente (codigo, nombre, descripcion, activo)
SELECT 'TRANSFUSIONAL',
       'Transfusional',
       'Antecedentes de transfusiones de sangre o hemoderivados, incluyendo reacciones adversas si las hubo.',
       true
WHERE NOT EXISTS (SELECT 1 FROM tipo_antecedente WHERE codigo = 'TRANSFUSIONAL');
