-- categoria (id 1-4)
INSERT INTO categoria (nombre) VALUES
    ('Panadería'),
    ('Comida preparada'),
    ('Frutas y verduras'),
    ('Otros');

-- negocio (id 1-3)
INSERT INTO negocio (nombre, tipo_negocio, ubicacion, horario_cierre) VALUES
    ('Panadería La Espiga', 'Panadería', 'Liberia, Guanacaste', '19:00'),
    ('Soda Doña Marta', 'Soda', 'Nicoya, Guanacaste', '20:00'),
    ('Restaurante El Mirador', 'Restaurante', 'Santa Cruz, Guanacaste', '21:30');

-- cliente (id 1-3)
INSERT INTO cliente (nombre, correo, telefono) VALUES
    ('Ana Rojas', 'ana.rojas@example.com', '8888-1111'),
    ('Luis Fernández', 'luis.fernandez@example.com', '8888-2222'),
    ('Marta Solano', 'marta.solano@example.com', '8888-3333');

-- organizacion_comunitaria (id 1-2)
INSERT INTO organizacion_comunitaria (nombre, tipo, capacidad_recoleccion, contacto) VALUES
    ('Comedor Esperanza', 'Comedor comunitario', 20, '2666-0000'),
    ('Banco de Alimentos Guanacaste', 'ONG', 50, '2666-1111');

-- paquete_sorpresa (id 1-6): cubre los 6 estados del CHECK para que los seeds sean realistas
INSERT INTO paquete_sorpresa
    (negocio_id, categoria_id, descripcion, cantidad, precio_original, precio_con_descuento,
     hora_limite_recogida, estado, organizacion_comunitaria_id)
VALUES
    (1, 1, 'Pan del día variado', 5, 6000.00, 2500.00, '2026-08-20 19:00:00-06', 'recogido', NULL),
    (1, 1, 'Repostería surtida', 3, 9000.00, 4000.00, '2026-08-22 19:00:00-06', 'donado', 1),
    (2, 2, 'Casado del día', 8, 4000.00, 1800.00, '2026-08-23 20:00:00-06', 'recogido', NULL),
    (2, 2, 'Sopa y arroz sobrante', 4, 3500.00, 1500.00, '2026-08-19 20:00:00-06', 'perdido', NULL),
    (3, 3, 'Caja de vegetales frescos', 10, 7000.00, 3000.00, '2026-08-27 21:30:00-06', 'disponible', NULL),
    (3, 4, 'Combo del día', 6, 8000.00, 3500.00, '2026-08-27 21:30:00-06', 'reservado', NULL);

-- reserva (id 1-4): una reserva por paquete (respeta uq_reserva_paquete)
INSERT INTO reserva (cliente_id, paquete_sorpresa_id, fecha_hora_reserva, estado) VALUES
    (1, 1, '2026-08-20 17:00:00-06', 'recogido'),
    (2, 3, '2026-08-23 18:30:00-06', 'recogido'),
    (3, 4, '2026-08-19 19:00:00-06', 'no_recogido'),
    (1, 6, '2026-08-26 10:00:00-06', 'pendiente');