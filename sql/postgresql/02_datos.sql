SET client_encoding TO 'UTF8';
CREATE SCHEMA IF NOT EXISTS movilidad_digital;
SET search_path TO movilidad_digital, public;
START TRANSACTION;
INSERT INTO usuario (nombre, telefono, email, licencia_conducir) VALUES
('Juan Perez', '+549117363900', 'juan.perez@email.com', 'A13-B1-D1-E1'),
('Maria Gomez', '+549112933912', 'maria.gomez@email.com', NULL),
('Carlos Rodriguez', '+549112209805', 'carlos.rodriguez@email.com', NULL),
('Ana Martinez', '+549118220576', 'ana.martinez@email.com', 'B1-D1'),
('Luis Lopez', '+549114307113', 'luis.lopez@email.com', NULL),
('Laura Gonzalez', '+549114054301', 'laura.gonzalez@email.com', NULL),
('Diego Sanchez', '+549113872427', 'diego.sanchez@email.com', 'B1-D1'),
('Sofia Alvarez', '+549113170528', 'sofia.alvarez@email.com', NULL),
('Javier Fernandez', '+549118177968', 'javier.fernandez@email.com', NULL),
('Lucia Garcia', '+549112859791', 'lucia.garcia@email.com', NULL),
('Andres Romero', '+549117676566', 'andres.romero@email.com', NULL),
('Elena Diaz', '+549118213171', 'elena.diaz@email.com', NULL),
('Fernando Heredia', '+549116574866', 'fernando.heredia@email.com', NULL),
('Marta Torres', '+549112729295', 'marta.torres@email.com', NULL),
('Gabriel Suarez', '+549116953410', 'gabriela.suarez@email.com', NULL),
('Paula Vazquez', '+549115539336', 'paula.vazquez@email.com', 'A12'),
('Santiago Castro', '+549112266612', 'santiago.castro@email.com', NULL),
('Valentina Ramirez', '+549112249957', 'valentina.ramirez@email.com', NULL),
('Ricardo Silva', '+549112785972', 'ricardo.silva@email.com', 'B1-D1-G'),
('Camila Benitez', '+549113834068', 'camila.benitez@email.com', NULL),
('Alejandro Mendoza', '+549113951701', 'alejandro.mendoza@email.com', NULL),
('Gabriela Flores', '+549116239227', 'gabriela.flores@email.com', 'B1-D1'),
('Martin Morales', '+549117050099', 'martin.morales@email.com', NULL),
('Natalia Acosta', '+549112222599', 'natalia.acosta@email.com', NULL),
('Manuel Rios', '+549116708064', 'manuel.rios@email.com', 'A13'),
('Isabel Gimenez', '+549113667971', 'isabel.gimenez@email.com', NULL),
('Jorge Medina', '+549118006407', 'jorge.medina@email.com', NULL),
('Clara Luna', '+549117451625', 'clara.luna@email.com', NULL),
('Francisco Herrera', '+549117883140', 'francisco.herrera@email.com', NULL),
('Beatriz Dominguez', '+549116571300', 'beatriz.dominguez@email.com', NULL),
('Gonzalo Perez', '+549115519187', 'gonzalo.perez@email.com', 'B1-A13'),
('Daniela Gomez', '+549113849189', 'daniela.gomez@email.com', NULL),
('Roberto Rodriguez', '+549115768238', 'roberto.rodriguez@email.com', NULL),
('Victoria Martinez', '+549116943118', 'victoria.martinez@email.com', 'B1-D1-D2'),
('Esteban Lopez', '+549114333632', 'esteban.lopez@email.com', NULL),
('Patricia Gonzalez', '+549118789994', 'patricia.gonzalez@email.com', NULL),
('Hugo Sanchez', '+549112054515', 'hugo.sanchez@email.com', NULL),
('Irene Alvarez', '+549118365337', 'irene.alvarez@email.com', NULL),
('Federico Fernandez', '+549118759701', 'federico.fernandez@email.com', NULL),
('Alicia Garcia', '+549113339319', 'alicia.garcia@email.com', 'B1-D1'),
('Mateo Romero', '+549117856417', 'mateo.romero@email.com', NULL),
('Sara Diaz', '+549115545146', 'sara.diaz@email.com', NULL),
('Lucas Heredia', '+549114854228', 'lucas.heredia@email.com', NULL),
('Julia Torres', '+549114330953', 'julia.torres@email.com', NULL),
('Bruno Suarez', '+549113304256', 'bruno.suarez@email.com', NULL),
('Olivia Vazquez', '+549113806182', 'olivia.vazquez@email.com', NULL),
('Joaquin Castro', '+549118404652', 'joaquin.castro@email.com', NULL),
('Emilia Ramirez', '+549114823559', 'emilia.ramirez@email.com', NULL),
('Tomas Silva', '+549112857401', 'tomas.silva@email.com', NULL),
('Florencia Benitez', '+549112778008', 'florencia.benitez@email.com', NULL);



-- . Inserción inicial traduciendo los literales de servicios.txt a sus ordinales enteros
INSERT INTO servicio (nombre, tarifa_base, precio_km, precio_minuto, tipo_vehiculo, categoria_vehiculo, tipo_servicio) VALUES
('Estandar', 2000.00, 200.00, 100.00, 0, 0, 0), -- AUTO (0), ESTANDAR (0), PASAJEROS (0)
('Confort', 2500.00, 250.00, 125.00, 0, 1, 0),  -- AUTO (0), CONFORT (1), PASAJEROS (0)
('Premium', 3000.00, 300.00, 150.00, 0, 2, 0),  -- AUTO (0), PREMIUM (2), PASAJEROS (0)
('Envios', 1500.00, 150.00, 75.00, 0, 0, 1),    -- AUTO (0), ESTANDAR (0), ENVIOS (1)
('Mototaxi', 1000.00, 100.00, 50.00, 1, 0, 0),  -- MOTO (1), ESTANDAR (0), PASAJEROS (0)
('Mensajería', 500.00, 50.00, 25.00, 1, 0, 1);  -- MOTO (1), ESTANDAR (0), ENVIOS (1)


--. Inserción inicial traduciendo el archivo vehiculos.txt
INSERT INTO vehiculo (patente, modelo, capacidad_pasajeros, tipo_vehiculo, categoria_vehiculo, tipo_servicios) VALUES
('AD123BC', 'Toyota Corolla', 4, 0, 2, 1), -- AUTO (0), PREMIUM (2), PASAJEROS (1)
('AB456DE', 'Ford Focus', 4, 0, 0, 3),     -- AUTO (0), ESTANDAR (0), PASAJEROS+ENVIOS (1+2=3)
('AC789FG', 'Chevrolet Cruze', 4, 0, 1, 3),  -- AUTO (0), CONFORT (1), PASAJEROS+ENVIOS (1+2=3)
('AD012HI', 'Volkswagen Gol', 4, 0, 0, 2),   -- AUTO (0), ESTANDAR (0), ENVIOS (2)
('AE345JK', 'Fiat Cronos', 4, 0, 0, 3),      -- AUTO (0), ESTANDAR (0), PASAJEROS+ENVIOS (1+2=3)
('AF678LM', 'Peugeot 208', 4, 0, 0, 3),      -- AUTO (0), ESTANDAR (0), PASAJEROS+ENVIOS (1+2=3)
('AG901NO', 'Renault Sandero', 4, 0, 0, 2),   -- AUTO (0), ESTANDAR (0), ENVIOS (2)
('AH234PQ', 'Honda Civic', 4, 0, 1, 1),      -- AUTO (0), CONFORT (1), PASAJEROS (1)
('AI567RS', 'Nissan Versa', 4, 0, 2, 1),      -- AUTO (0), PREMIUM (2), PASAJEROS (1)
('AJ890TU', 'Toyota Yaris', 4, 0, 1, 1),      -- AUTO (0), CONFORT (1), PASAJEROS (1)
('123ABC', 'Honda Wave', 1, 1, 0, 2),        -- MOTO (1), ESTANDAR (0), ENVIOS (2)
('456DEF', 'Yamaha YBR', 2, 1, 0, 3),        -- MOTO (1), ESTANDAR (0), PASAJEROS+ENVIOS (1+2=3)
('789GHI', 'Motomel S2', 1, 1, 0, 2),        -- MOTO (1), ESTANDAR (0), ENVIOS (2)
('012JKL', 'Zanella Custom', 1, 1, 0, 2),    -- MOTO (1), ESTANDAR (0), ENVIOS (2)
('345MNO', 'Bajaj Rouser', 2, 1, 0, 3);      -- MOTO (1), ESTANDAR (0), PASAJEROS+ENVIOS (1+2=3)



-- 3. Inserción de las relaciones en la tabla intermedia 'conductor_vehiculo'
-- (Mapea secuencialmente todas las patentes secundarias distribuidas en el archivo de texto)
INSERT INTO conductor_vehiculo (email, patente) VALUES
('juan.perez@email.com', 'AD123BC'),
('juan.perez@email.com', '123ABC'),
('juan.perez@email.com', 'AD012HI'),
('ana.martinez@email.com', 'AB456DE'),
('diego.sanchez@email.com', 'AC789FG'),
('paula.vazquez@email.com', '456DEF'),
('ricardo.silva@email.com', 'AE345JK'),
('ricardo.silva@email.com', 'AJ890TU'),
('gabriela.flores@email.com', 'AF678LM'),
('manuel.rios@email.com', '789GHI'),
('gonzalo.perez@email.com', '345MNO'),
('gonzalo.perez@email.com', '012JKL'),
('victoria.martinez@email.com', 'AG901NO'),
('victoria.martinez@email.com', 'AH234PQ'),
('alicia.garcia@email.com', 'AI567RS');

COMMIT;
SELECT (SELECT COUNT(*) FROM usuario) AS usuarios, (SELECT COUNT(*) FROM servicio) AS servicios, (SELECT COUNT(*) FROM vehiculo) AS vehiculos, (SELECT COUNT(*) FROM conductor_vehiculo) AS relaciones;
