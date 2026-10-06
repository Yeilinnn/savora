CREATE TABLE usuario (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    rol TEXT NOT NULL,
    negocio_id BIGINT REFERENCES negocio(id),
    cliente_id BIGINT REFERENCES cliente(id),
    CONSTRAINT chk_usuario_rol CHECK (rol IN ('NEGOCIO', 'CLIENTE')),
    CONSTRAINT chk_usuario_propietario CHECK (
        (rol = 'NEGOCIO' AND negocio_id IS NOT NULL AND cliente_id IS NULL)
        OR (rol = 'CLIENTE' AND cliente_id IS NOT NULL AND negocio_id IS NULL)
    )
);