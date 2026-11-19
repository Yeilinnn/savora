ALTER TABLE negocio ADD COLUMN estado TEXT NOT NULL DEFAULT 'aprobado';
ALTER TABLE negocio ADD CONSTRAINT chk_negocio_estado CHECK (estado IN ('pendiente', 'aprobado', 'suspendido'));

ALTER TABLE usuario ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE usuario DROP CONSTRAINT chk_usuario_rol;
ALTER TABLE usuario DROP CONSTRAINT chk_usuario_propietario;
ALTER TABLE usuario ADD CONSTRAINT chk_usuario_rol CHECK (rol IN ('NEGOCIO', 'CLIENTE', 'ADMIN'));
ALTER TABLE usuario ADD CONSTRAINT chk_usuario_propietario CHECK (
    (rol = 'NEGOCIO' AND negocio_id IS NOT NULL AND cliente_id IS NULL)
    OR (rol = 'CLIENTE' AND cliente_id IS NOT NULL AND negocio_id IS NULL)
    OR (rol = 'ADMIN' AND negocio_id IS NULL AND cliente_id IS NULL)
);