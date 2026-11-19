CREATE TABLE pago (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reserva_id BIGINT NOT NULL REFERENCES reserva(id),
    monto NUMERIC(10, 2) NOT NULL,
    estado TEXT NOT NULL,
    marca TEXT NOT NULL,
    ultimos4 TEXT NOT NULL,
    referencia TEXT NOT NULL,
    fecha TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_pago_reserva UNIQUE (reserva_id),
    CONSTRAINT chk_pago_estado CHECK (estado IN ('aprobado'))
);