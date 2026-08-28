-- La donación deja de ser solo un estado + una FK suelta en paquete_sorpresa,
-- y pasa a ser su propia entidad, con fecha y estado propios (sugerencia del profe
-- en la retroalimentación del Lab 1).
ALTER TABLE paquete_sorpresa DROP COLUMN organizacion_comunitaria_id;

CREATE TABLE donacion (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    paquete_sorpresa_id BIGINT NOT NULL UNIQUE REFERENCES paquete_sorpresa(id),
    organizacion_comunitaria_id BIGINT NOT NULL REFERENCES organizacion_comunitaria(id),
    fecha_donacion TIMESTAMPTZ NOT NULL DEFAULT now(),
    estado TEXT NOT NULL DEFAULT 'pendiente'
        CHECK (estado IN ('pendiente', 'aceptada', 'rechazada'))
);

CREATE INDEX idx_donacion_organizacion ON donacion(organizacion_comunitaria_id);

INSERT INTO donacion (paquete_sorpresa_id, organizacion_comunitaria_id, fecha_donacion, estado)
VALUES (2, 1, '2026-08-22 19:05:00-06', 'aceptada');