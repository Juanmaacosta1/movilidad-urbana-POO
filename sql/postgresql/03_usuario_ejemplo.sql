
-- 03_usuario_movilidad.sql
-- Ejecutar conectado a bd1 con un usuario administrador.

-- Crear usuario de aplicación
CREATE ROLE movilidad_app
LOGIN PASSWORD 'CAMBIAR_ESTA_CLAVE';

-- Permitir conexión a la base de datos
GRANT CONNECT ON DATABASE bd1 TO movilidad_app;

-- Permitir acceso al esquema
GRANT USAGE ON SCHEMA movilidad_digital TO movilidad_app;

-- Permitir consultas sobre las tablas del esquema
GRANT SELECT ON ALL TABLES IN SCHEMA movilidad_digital
TO movilidad_app;
