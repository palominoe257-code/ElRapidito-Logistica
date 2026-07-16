-- ============================================================
--  EL RAPIDITO EXPRESS  —  Script de Base de Datos MySQL
--  Archivo : elrapiditos.sql
--  Version : 1.0
--  Descripcion: Schema completo + datos de ejemplo pre-cargados
-- ============================================================

-- Crear y usar la base de datos
DROP DATABASE IF EXISTS elrapiditos_db;
CREATE DATABASE elrapiditos_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_spanish_ci;

USE elrapiditos_db;

-- ============================================================
-- TABLA: usuarios
-- Almacena las credenciales del personal de la agencia.
-- ============================================================
CREATE TABLE usuarios (
    id           INT           NOT NULL AUTO_INCREMENT,
    username     VARCHAR(50)   NOT NULL UNIQUE,
    password     VARCHAR(255)  NOT NULL,          -- En produccion: hash bcrypt
    nombre       VARCHAR(100)  NOT NULL,
    rol          VARCHAR(30)   NOT NULL DEFAULT 'OPERADOR',
    activo       TINYINT(1)    NOT NULL DEFAULT 1,
    fecha_alta   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: agencias
-- Catalogo de sucursales / agencias del sistema.
-- ============================================================
CREATE TABLE agencias (
    id       INT          NOT NULL AUTO_INCREMENT,
    nombre   VARCHAR(100) NOT NULL UNIQUE,
    zona     VARCHAR(30)  NOT NULL,   -- LIMA_METRO | NORTE | SUR_COSTA | SIERRA | SELVA
    activa   TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: envios
-- Registro principal de cada paquete/envio en el sistema.
-- ============================================================
CREATE TABLE envios (
    id_interno         INT            NOT NULL AUTO_INCREMENT,
    codigo_seguridad   CHAR(8)        NOT NULL UNIQUE,
    fecha_registro     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Datos del paquete
    descripcion        VARCHAR(200)   NOT NULL,
    peso_kg            DECIMAL(8,2)   NOT NULL,
    tiene_seguro       TINYINT(1)     NOT NULL DEFAULT 0,
    tiene_express      TINYINT(1)     NOT NULL DEFAULT 0,
    costo_total        DECIMAL(10,2)  NOT NULL,

    -- Ruta
    agencia_origen     VARCHAR(100)   NOT NULL,
    agencia_destino    VARCHAR(100)   NOT NULL,
    zona_nivel         VARCHAR(20)    NOT NULL,   -- LOCAL | CORTO | MEDIO | LARGO

    -- Remitente
    remitente          VARCHAR(100)   NOT NULL,
    dni_remitente      VARCHAR(20)    NOT NULL,
    cel_remitente      VARCHAR(20)    NOT NULL,

    -- Destinatario
    destinatario       VARCHAR(100)   NOT NULL,
    dni_destinatario   VARCHAR(20)    NOT NULL,
    cel_destinatario   VARCHAR(20)    NOT NULL,

    -- Estado y pago
    estado             ENUM(
                           'Pendiente de Recojo',
                           'En Almacen',
                           'En Transito',
                           'Entregado',
                           'Cancelado'
                       ) NOT NULL DEFAULT 'Pendiente de Recojo',
    metodo_pago        ENUM(
                           'Efectivo',
                           'Yape / Plin',
                           'Tarjeta de Debito/Credito',
                           'Transferencia Bancaria'
                       ) NOT NULL DEFAULT 'Efectivo',

    -- Auditoria
    atendido_por        VARCHAR(50)   NOT NULL DEFAULT 'sistema',
    fecha_actualizacion DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                     ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id_interno),
    INDEX idx_codigo   (codigo_seguridad),
    INDEX idx_estado   (estado),
    INDEX idx_dni_rem  (dni_remitente),
    INDEX idx_dni_dest (dni_destinatario),
    INDEX idx_fecha    (fecha_registro)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: historial_estados
-- Auditoria de cada cambio de estado de un envio.
-- ============================================================
CREATE TABLE historial_estados (
    id              INT          NOT NULL AUTO_INCREMENT,
    id_envio        INT          NOT NULL,
    estado_anterior VARCHAR(50)  NOT NULL,
    estado_nuevo    VARCHAR(50)  NOT NULL,
    operador        VARCHAR(50)  NOT NULL,
    fecha_cambio    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (id_envio) REFERENCES envios(id_interno) ON DELETE CASCADE,
    INDEX idx_envio (id_envio)
) ENGINE=InnoDB;

-- ============================================================
-- DATOS DE EJEMPLO: usuarios
-- ============================================================
INSERT INTO usuarios (username, password, nombre, rol) VALUES
('jleonel',   'admin123',  'Jose Leonel',   'ADMINISTRADOR'),
('operador1',  'oper2024',  'Maria Condori', 'OPERADOR'),
('operador2',  'oper2024',  'Carlos Ruiz',   'OPERADOR');

-- ============================================================
-- DATOS DE EJEMPLO: agencias
-- ============================================================
INSERT INTO agencias (nombre, zona) VALUES
('Lima Central (CAPITAL)', 'LIMA_METRO'),
('Lima Norte',             'LIMA_METRO'),
('Lima Sur',               'LIMA_METRO'),
('Lima Este',              'LIMA_METRO'),
('Callao',                 'LIMA_METRO'),
('Trujillo',               'NORTE'),
('Chiclayo',               'NORTE'),
('Piura',                  'NORTE'),
('Arequipa',               'SUR_COSTA'),
('Tacna',                  'SUR_COSTA'),
('Cusco',                  'SIERRA'),
('Puno',                   'SIERRA'),
('Iquitos',                'SELVA');

-- ============================================================
-- DATOS DE EJEMPLO: envios (12 pedidos pre-cargados)
-- Tarifas:
--   LOCAL : 0.80/kg + 5.00 base, min 7.00
--   CORTO : 1.00/kg + 10.00 base, min 13.00
--   MEDIO : 1.30/kg + 18.00 base, min 22.00
--   LARGO : 1.60/kg + 30.00 base, min 38.00
--   Seguro +15% | Express +S/.20.00
-- ============================================================
INSERT INTO envios (
    codigo_seguridad, fecha_registro,
    descripcion, peso_kg, tiene_seguro, tiene_express, costo_total,
    agencia_origen, agencia_destino, zona_nivel,
    remitente, dni_remitente, cel_remitente,
    destinatario, dni_destinatario, cel_destinatario,
    estado, metodo_pago, atendido_por
) VALUES

-- 1. Lima Central -> Lima Norte | LOCAL | ENTREGADO
-- Costo: 3*0.80+5=7.40
('A1B2C3D4', '2026-06-10 09:15:00',
 'Ropa deportiva', 3.00, 0, 0, 7.40,
 'Lima Central (CAPITAL)', 'Lima Norte', 'LOCAL',
 'Juan Perez',  '45123456', '987001001',
 'Maria Lopez', '48234567', '987002002',
 'Entregado', 'Efectivo', 'jleonel'),

-- 2. Lima Central -> Trujillo | MEDIO | EN TRANSITO (+Seguro)
-- Costo base: 8*1.30+18=28.40 | +15%=32.66
('B2C3D4E5', '2026-06-14 11:30:00',
 'Libros universitarios [+Seguro]', 8.00, 1, 0, 32.66,
 'Lima Central (CAPITAL)', 'Trujillo', 'MEDIO',
 'Carlos Ruiz', '43456789', '987003003',
 'Ana Torres',  '46789012', '987004004',
 'En Transito', 'Yape / Plin', 'jleonel'),

-- 3. Lima Central -> Cusco | LARGO | EN ALMACEN
-- Costo: 12*1.60+30=49.20
('C3D4E5F6', '2026-06-18 14:00:00',
 'Electrodomesticos', 12.00, 0, 0, 49.20,
 'Lima Central (CAPITAL)', 'Cusco', 'LARGO',
 'Pedro Mamani', '42567890', '987005005',
 'Rosa Quispe',  '45678901', '987006006',
 'En Almacen', 'Transferencia Bancaria', 'operador1'),

-- 4. Lima Norte -> Arequipa | MEDIO | EN TRANSITO
-- Costo: 20*1.30+18=44.00
('D4E5F6G7', '2026-06-20 08:45:00',
 'Repuestos de auto', 20.00, 0, 0, 44.00,
 'Lima Norte', 'Arequipa', 'MEDIO',
 'Luis Garcia',   '41890123', '987007007',
 'Carmen Flores', '44901234', '987008008',
 'En Transito', 'Efectivo', 'operador1'),

-- 5. Lima Central -> Lima Sur | LOCAL | ENTREGADO
-- Costo minimo: 7.00
('E5F6G7H8', '2026-06-22 16:20:00',
 'Documentos legales', 0.50, 0, 0, 7.00,
 'Lima Central (CAPITAL)', 'Lima Sur', 'LOCAL',
 'Sandra Vega',  '47012345', '987009009',
 'Miguel Salas', '40123456', '987010010',
 'Entregado', 'Yape / Plin', 'jleonel'),

-- 6. Callao -> Piura | MEDIO | EN TRANSITO
-- Costo: 5*1.30+18=24.50
('F6G7H8I9', '2026-06-25 10:00:00',
 'Muestras comerciales', 5.00, 0, 0, 24.50,
 'Callao', 'Piura', 'MEDIO',
 'Fernando Cruz', '47234567', '987011011',
 'Lucia Mendez',  '40345678', '987012012',
 'En Transito', 'Efectivo', 'operador2'),

-- 7. Lima Central -> Iquitos | LARGO | PENDIENTE
-- Costo: 2*1.60+30=33.20 -> min 38.00
('G7H8I9J0', '2026-07-01 09:00:00',
 'Medicamentos refrigerados', 2.00, 0, 0, 38.00,
 'Lima Central (CAPITAL)', 'Iquitos', 'LARGO',
 'Dr. Hugo Silva',   '44567890', '987013013',
 'Paciente Ramirez', '43678901', '987014014',
 'Pendiente de Recojo', 'Transferencia Bancaria', 'jleonel'),

-- 8. Lima Central -> Arequipa | MEDIO | EN TRANSITO (+Express)
-- Costo base: 15*1.30+18=37.50 | +20=57.50
('H8I9J0K1', '2026-07-03 11:15:00',
 'Pesas de gimnasio [+Express]', 15.00, 0, 1, 57.50,
 'Lima Central (CAPITAL)', 'Arequipa', 'MEDIO',
 'Gym PowerFit Lima', '20456789', '987015015',
 'Jorge Condori',     '47890123', '987016016',
 'En Transito', 'Tarjeta de Debito/Credito', 'operador1'),

-- 9. Lima Central -> Chiclayo | MEDIO | ENTREGADO
-- Costo: 7*1.30+18=27.10
('I9J0K1L2', '2026-07-05 13:30:00',
 'Equipo de computo', 7.00, 0, 0, 27.10,
 'Lima Central (CAPITAL)', 'Chiclayo', 'MEDIO',
 'TechStore Lima', '20567890', '987017017',
 'Oficina Norte',  '20678901', '987018018',
 'Entregado', 'Efectivo', 'operador2'),

-- 10. Lima Central -> Puno | LARGO | CANCELADO
-- Costo: 4*1.60+30=36.40 -> min 38.00
('J0K1L2M3', '2026-07-06 15:00:00',
 'Ropa de abrigo', 4.00, 0, 0, 38.00,
 'Lima Central (CAPITAL)', 'Puno', 'LARGO',
 'Moda Lima',       '20789012', '987019019',
 'Boutique Andina', '20890123', '987020020',
 'Cancelado', 'Efectivo', 'jleonel'),

-- 11. Arequipa -> Cusco | CORTO | EN ALMACEN
-- Costo: 6*1.00+10=16.00
('K1L2M3N4', '2026-07-08 10:30:00',
 'Artesanias textiles', 6.00, 0, 0, 16.00,
 'Arequipa', 'Cusco', 'CORTO',
 'Artesanos Arequipa', '23456789', '987021021',
 'Galeria Cusco',      '24567890', '987022022',
 'En Almacen', 'Yape / Plin', 'operador1'),

-- 12. Lima Central -> Tacna | MEDIO | ENTREGADO (+Express +Seguro)
-- Costo base: 18*1.30+18=41.40 | +15%=47.61 | +20=67.61
('L2M3N4O5', '2026-07-10 08:00:00',
 'Insumos industriales [+Express][+Seguro]', 18.00, 1, 1, 67.61,
 'Lima Central (CAPITAL)', 'Tacna', 'MEDIO',
 'Industrias Lima S.A.', '20901234', '987023023',
 'Fabrica Tacna',        '20012345', '987024024',
 'Entregado', 'Transferencia Bancaria', 'jleonel');

-- ============================================================
-- VISTAS UTILES PARA REPORTES
-- ============================================================

CREATE VIEW v_ingresos_por_estado AS
SELECT
    estado,
    COUNT(*)          AS cantidad,
    SUM(costo_total)  AS ingreso_total,
    AVG(costo_total)  AS promedio
FROM envios
GROUP BY estado;

CREATE VIEW v_envios_completo AS
SELECT
    id_interno,
    codigo_seguridad,
    DATE_FORMAT(fecha_registro, '%d/%m/%Y %H:%i') AS fecha,
    descripcion,
    peso_kg,
    CONCAT('S/. ', FORMAT(costo_total, 2))         AS importe,
    agencia_origen,
    agencia_destino,
    zona_nivel,
    remitente,
    destinatario,
    estado,
    metodo_pago,
    atendido_por
FROM envios
ORDER BY fecha_registro DESC;

-- ============================================================
-- CONSULTAS DE REFERENCIA (para uso del equipo)
-- ============================================================

-- Buscar por codigo:
--   SELECT * FROM envios WHERE codigo_seguridad = 'A1B2C3D4';

-- Buscar por DNI:
--   SELECT * FROM envios WHERE dni_remitente='45123456' OR dni_destinatario='45123456';

-- Ingresos brutos totales:
--   SELECT SUM(costo_total) FROM envios;

-- Ingresos netos (sin cancelados):
--   SELECT SUM(costo_total) FROM envios WHERE estado <> 'Cancelado';

-- Conteo por estado:
--   SELECT estado, COUNT(*) total FROM envios GROUP BY estado;

-- Avanzar estado manualmente:
--   UPDATE envios SET estado='En Almacen'
--   WHERE codigo_seguridad='G7H8I9J0' AND estado='Pendiente de Recojo';

-- ============================================================
-- FIN DEL SCRIPT
-- ============================================================
