CREATE TABLE categoria (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre TEXT NOT NULL UNIQUE
);

CREATE TABLE negocio (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre TEXT NOT NULL,
    tipo_negocio TEXT NOT NULL,
    ubicacion TEXT NOT NULL,
    horario_cierre TIME NOT NULL
);

CREATE TABLE cliente (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre TEXT NOT NULL,
    correo TEXT NOT NULL UNIQUE,
    telefono TEXT NOT NULL,
    CONSTRAINT chk_cliente_correo CHECK (length(trim(correo)) > 0)
);

CREATE TABLE organizacion_comunitaria (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre TEXT NOT NULL,
    tipo TEXT NOT NULL,
    capacidad_recoleccion INT NOT NULL,
    contacto TEXT NOT NULL,
    CONSTRAINT chk_org_capacidad CHECK (capacidad_recoleccion >= 0)
);
