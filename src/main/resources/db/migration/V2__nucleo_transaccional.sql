CREATE TABLE paquete_sorpresa (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    negocio_id BIGINT NOT NULL REFERENCES negocio(id),
    categoria_id BIGINT NOT NULL REFERENCES categoria(id),
    descripcion TEXT NOT NULL,
    cantidad INT NOT NULL,
    precio_original NUMERIC(10, 2) NOT NULL,
    precio_con_descuento NUMERIC(10, 2) NOT NULL,
    hora_limite_recogida TIMESTAMPTZ NOT NULL,
    estado TEXT NOT NULL,
    organizacion_comunitaria_id BIGINT REFERENCES organizacion_comunitaria(id),
    CONSTRAINT chk_paquete_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_paquete_precios CHECK (
        precio_original > 0
        AND precio_con_descuento >= 0
        AND precio_con_descuento < precio_original
    ),
    CONSTRAINT chk_paquete_estado CHECK (
        estado IN ('disponible', 'reservado', 'recogido', 'donado', 'perdido', 'agotado')
    )
);

CREATE TABLE reserva (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES cliente(id),
    paquete_sorpresa_id BIGINT NOT NULL REFERENCES paquete_sorpresa(id),
    fecha_hora_reserva TIMESTAMPTZ NOT NULL DEFAULT now(),
    estado TEXT NOT NULL,
    CONSTRAINT chk_reserva_estado CHECK (estado IN ('pendiente', 'recogido', 'no_recogido')),
    CONSTRAINT uq_reserva_paquete UNIQUE (paquete_sorpresa_id)
);
