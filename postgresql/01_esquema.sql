CREATE SCHEMA IF NOT EXISTS movilidad_digital;
SET search_path TO movilidad_digital, public;
CREATE TABLE IF NOT EXISTS usuario(
    email VARCHAR(100) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(30) NOT NULL,
    licencia_conducir VARCHAR(30) NULL,      -- NULL representa que está en blanco (solo cliente)
    
    -- Restricción de Clave Primaria solicitada
    CONSTRAINT pk_usuario PRIMARY KEY (email)
);

CREATE TABLE IF NOT EXISTS servicio (
    nombre VARCHAR(50) NOT NULL,
    tarifa_base NUMERIC(10, 2) NOT NULL,
    precio_km NUMERIC(10, 2) NOT NULL,
    precio_minuto NUMERIC(10, 2) NOT NULL,
    tipo_vehiculo INT NOT NULL,       -- Representación entera del enum TipoVehiculo
    categoria_vehiculo INT NOT NULL,  -- Representación entera del enum CategoriaVehiculo
    tipo_servicio INT NOT NULL,       -- Representación entera del enum TipoServicio
    
    -- Restricción de Clave Primaria
    CONSTRAINT pk_servicio PRIMARY KEY (nombre),
    
    -- Validaciones de rango para los enteros de los Enums (Basado en los ordinales de Java)
    CONSTRAINT chk_tipo_vehiculo CHECK (tipo_vehiculo IN (0, 1)),                  -- 0: AUTO, 1: MOTO
    CONSTRAINT chk_categoria_vehiculo CHECK (categoria_vehiculo IN (0, 1, 2)),     -- 0: ESTANDAR, 1: CONFORT, 2: PREMIUM
    CONSTRAINT chk_tipo_servicio CHECK (tipo_servicio IN (0, 1)),                  -- 0: PASAJEROS, 1: ENVIOS
    
    -- Validaciones de seguridad para importes comerciales
    CONSTRAINT chk_tarifa_base CHECK (tarifa_base >= 0),
    CONSTRAINT chk_precio_km CHECK (precio_km >= 0),
    CONSTRAINT chk_precio_minuto CHECK (precio_minuto >= 0)
);

COMMENT ON COLUMN servicio.tipo_vehiculo IS 'Mapeo de TipoVehiculo: 0 = AUTO, 1 = MOTO';
COMMENT ON COLUMN servicio.categoria_vehiculo IS 'Mapeo de CategoriaVehiculo: 0 = ESTANDAR, 1 = CONFORT, 2 = PREMIUM';
COMMENT ON COLUMN servicio.tipo_servicio IS 'Mapeo de TipoServicio: 0 = PASAJEROS, 1 = ENVIOS';



CREATE TABLE IF NOT EXISTS vehiculo (
    patente VARCHAR(10) NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    capacidad_pasajeros INT NOT NULL,
    tipo_vehiculo INT NOT NULL,          -- 0 = AUTO, 1 = MOTO
    categoria_vehiculo INT NOT NULL,     -- 0 = ESTANDAR, 1 = CONFORT, 2 = PREMIUM
    tipo_servicios INT NOT NULL,         -- Máscara de bits: 1 = PASAJEROS, 2 = ENVIOS, 3 = AMBOS
    
    -- Restricción de Clave Primaria solicitada
    CONSTRAINT pk_vehiculo PRIMARY KEY (patente),
    
    -- Validaciones de rango para los enteros de los Enums
    CONSTRAINT chk_vehiculo_tipo CHECK (tipo_vehiculo IN (0, 1)),
    CONSTRAINT chk_vehiculo_categoria CHECK (categoria_vehiculo IN (0, 1, 2)),
    -- El valor máximo actual con ambos servicios es 3 (1 | 2). Si sumás un 3er servicio (4), este check subiría a 7.
    CONSTRAINT chk_vehiculo_servicios CHECK (tipo_servicios BETWEEN 1 AND 3), 
    
    -- Validación de capacidad física razonable
    CONSTRAINT chk_capacidad_pasajeros CHECK (capacidad_pasajeros >= 1)
);


--. Documentación técnica de la máscara de bits mediante comentarios en pgAdmin
COMMENT ON TABLE vehiculo IS 'Tabla que almacena la flota de rodados simulados y sus habilitaciones comerciales.';
COMMENT ON COLUMN vehiculo.tipo_servicios IS 'Máscara Bitwise: 1 = Solo Pasajeros (bit 0), 2 = Solo Envíos (bit 1), 3 = Ambos servicios habilitados';


CREATE TABLE IF NOT EXISTS conductor_vehiculo (
    email VARCHAR(100) NOT NULL,
    patente VARCHAR(10) NOT NULL,
    
    -- Clave primaria compuesta para evitar duplicar el mismo vehículo en el mismo conductor
    CONSTRAINT pk_conductor_vehiculo PRIMARY KEY (email, patente),
    
    -- Clave foránea que conecta con la tabla de usuarios
    CONSTRAINT fk_conductor_vehiculo_usuario FOREIGN KEY (email) 
        REFERENCES usuario (email) ON DELETE CASCADE,
        
    -- Clave foránea que conecta con la tabla de vehículos
    CONSTRAINT fk_conductor_vehiculo_vehiculo FOREIGN KEY (patente) 
        REFERENCES vehiculo (patente) ON DELETE CASCADE
);
COMMENT ON TABLE conductor_vehiculo IS 'Tabla intermedia normalizada que asocia a los conductores con sus rodados habilitados.';
COMMENT ON COLUMN conductor_vehiculo.email IS 'Email del usuario que posee el rol de conductor.';
COMMENT ON COLUMN conductor_vehiculo.patente IS 'Patente del vehículo asignado (El primer registro insertado por email actúa como principal).';


