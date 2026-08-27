-- Índices en las FK que se consultan seguido: catálogo por negocio, filtro por categoría,
-- paquetes asignados a una organización, y el historial de reservas de un cliente.
CREATE INDEX idx_paquete_negocio ON paquete_sorpresa(negocio_id);
CREATE INDEX idx_paquete_categoria ON paquete_sorpresa(categoria_id);
CREATE INDEX idx_paquete_organizacion ON paquete_sorpresa(organizacion_comunitaria_id);
CREATE INDEX idx_reserva_cliente ON reserva(cliente_id);

-- Índice parcial: el catálogo público solo lista paquetes "disponible", es la consulta
-- más frecuente de la aplicación. No se indexa por el resto de estados (poco consultados).
CREATE INDEX idx_paquete_disponible ON paquete_sorpresa(estado) WHERE estado = 'disponible';

-- Nota: cliente.correo y categoria.nombre ya tienen índice implícito por su restricción UNIQUE (V1).