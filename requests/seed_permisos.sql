-- ══════════════════════════════════════════════════════════════════════
-- SEED DATA: Roles y Permisos por defecto
-- ══════════════════════════════════════════════════════════════════════
-- Ejecutar después de crear una empresa nueva

-- Parámetros: cambiar estos valores según la empresa
\set EMPRESA_ID 4
\set ROL_ID 1
\set USUARIO_ADMIN_ID 2
\set SEDE_PRINCIPAL_ID 1

-- 1. Crear permisos base si no existen
INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'USUARIOS_VER', 'Ver usuarios', 'Ver lista de usuarios', 'USUARIOS', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'USUARIOS_VER');

INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'USUARIOS_CREAR', 'Crear usuario', 'Crear nuevos usuarios', 'USUARIOS', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'USUARIOS_CREAR');

INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'USUARIOS_EDITAR', 'Editar usuario', 'Editar datos de usuarios', 'USUARIOS', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'USUARIOS_EDITAR');

INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'USUARIOS_ELIMINAR', 'Eliminar usuario', 'Eliminar usuarios', 'USUARIOS', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'USUARIOS_ELIMINAR');

INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'ROLES_VER', 'Ver roles', 'Ver lista de roles', 'ROLES', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'ROLES_VER');

INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'ROLES_CREAR', 'Crear rol', 'Crear nuevos roles', 'ROLES', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'ROLES_CREAR');

INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'ROLES_EDITAR', 'Editar rol', 'Editar datos de roles', 'ROLES', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'ROLES_EDITAR');

INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'ROLES_ELIMINAR', 'Eliminar rol', 'Eliminar roles', 'ROLES', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'ROLES_ELIMINAR');

INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'SEDES_VER', 'Ver sedes', 'Ver lista de sedes', 'SEDES', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'SEDES_VER');

INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'SEDES_CREAR', 'Crear sede', 'Crear nuevas sedes', 'SEDES', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'SEDES_CREAR');

INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'SEDES_EDITAR', 'Editar sede', 'Editar datos de sedes', 'SEDES', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'SEDES_EDITAR');

INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'SEDES_ELIMINAR', 'Eliminar sede', 'Eliminar sedes', 'SEDES', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'SEDES_ELIMINAR');

INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'SERVICIOS_VER', 'Ver servicios', 'Ver lista de servicios', 'SERVICIOS', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'SERVICIOS_VER');

INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'SERVICIOS_CREAR', 'Crear servicio', 'Crear nuevos servicios', 'SERVICIOS', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'SERVICIOS_CREAR');

INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'SERVICIOS_EDITAR', 'Editar servicio', 'Editar datos de servicios', 'SERVICIOS', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'SERVICIOS_EDITAR');

INSERT INTO permiso (codigo, nombre, descripcion, modulo, activo, created_at)
SELECT 'SERVICIOS_ELIMINAR', 'Eliminar servicio', 'Eliminar servicios', 'SERVICIOS', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'SERVICIOS_ELIMINAR');

-- 2. Asignar todos los permisos al rol ADMIN-CLINICA (ID 1)
INSERT INTO rol_permiso (rol_id, permiso_id, activo, created_at, usuario_creacion)
SELECT :ROL_ID, p.id, true, NOW(), :USUARIO_ADMIN_ID
FROM permiso p
WHERE p.activo = true
AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = :ROL_ID AND rp.permiso_id = p.id
);

-- 3. Asignar rol al usuario admin_hospital
INSERT INTO usuario_rol (usuario_id, empresa_id, sede_id, rol_id, activo, created_at, usuario_creacion)
VALUES (:USUARIO_ADMIN_ID, :EMPRESA_ID, :SEDE_PRINCIPAL_ID, :ROL_ID, true, NOW(), :USUARIO_ADMIN_ID)
ON CONFLICT DO NOTHING;