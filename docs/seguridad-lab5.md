# Seguridad (JWT) — Lab 5

## Modelo de roles
No existía tabla de usuarios, así que se agregó `usuario` (migración V6) con dos roles:
`NEGOCIO` y `CLIENTE`, cada uno enlazado a un `negocio` o a un `cliente` ya existentes.
Las contraseñas de prueba se siembran en código (`UsuarioSeeder`, no en SQL) porque deben
pasar por el mismo `PasswordEncoder` (BCrypt) que usa el login.

Usuarios de prueba: `negocio1`/`negocio123` (negocio 1), `negocio2`/`negocio123` (negocio 2),
`cliente1`/`cliente123` (cliente 1).

## Flujo
`POST /auth/login` valida usuario y contraseña y devuelve un JWT firmado con HMAC-SHA256. El
token lleva como claims el rol y el id del negocio o cliente dueño de la cuenta. Un filtro
(`JwtAuthFilter`) lee el header `Authorization: Bearer ...` en cada petición y arma la
autenticación de Spring Security a partir de esos claims. No hay sesión ni cookie: todo es
stateless.

## Autorización por endpoint
Solo dos rutas exigen rol `NEGOCIO`: crear paquetes (`POST /api/v1/paquetes`) y cerrarlos
(`POST /api/v1/paquetes/{id}/cierre`). El resto de la API (reservas, negocios, etc., que es la
parte de Nazareth) queda abierta como estaba, para no romper sus pruebas existentes.

## Verificación de propiedad (OWASP API1)
Dos decisiones evitan que un negocio opere sobre recursos de otro:
- Al crear un paquete, el `negocioId` no viene del cuerpo de la petición: se toma siempre del
  claim del token. Así un negocio no puede publicar a nombre de otro.
- Al cerrar un paquete, el controlador compara el negocio dueño del paquete (consultado en BD)
  contra el negocio del token, y responde 403 si no coinciden, antes de llamar a
  `CierreDePaquetesService`.

## N+1 en el listado de paquetes
Siguiendo la lección del Lab 3, `listar()` compone un `Specification` adicional
(`conNegocioYCategoria`) que hace `JOIN FETCH` de negocio y categoría, para no disparar una
consulta extra por cada paquete de la página.