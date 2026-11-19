# ADR-006 · Plataforma y estrategia de despliegue

**Estado y fecha:** aceptada (octubre de 2026) · **Responsables:** Yeilin Moya Baltodano, Nazareth Gómez Gómez

## Contexto

Había que publicar la API, la SPA y dos bases de datos sin presupuesto, con HTTPS, con secretos fuera del
repositorio y con la garantía de que no llegue a producción un cambio que no pasó las pruebas.

## Decisión

- **Render** (plan gratuito): la API como contenedor Docker y la SPA como sitio estático.
- **Neon** para PostgreSQL y **MongoDB Atlas** (M0) para MongoDB.
- **Resend** (API HTTP) para el correo de recuperación de contraseña.
- **Infraestructura como código:** `render.yaml` (con `autoDeploy: false`), donde los secretos van con `sync: false`
  y el secreto JWT se genera en la plataforma.
- **Despliegue desde la integración continua:** GitHub Actions compila, prueba y verifica la cobertura
  (JaCoCo ≥ 70 % en la capa de negocio); solo si todo pasa y el cambio está en `main`, dispara los deploy hooks
  de Render. Los hooks viven en GitHub Secrets.
- **Perfiles `dev`, `test` y `prod`:** en `prod` todo viene de variables de entorno. Las tareas programadas corren
  solo en `dev` y `prod`.
- **Observabilidad mínima:** `/actuator/health` sin detalles y registros estructurados con los eventos de seguridad.

## Alternativas consideradas

1. **Un VPS propio (por ejemplo una máquina en la nube con Docker Compose):** se descartó por el costo y el trabajo
   de operar el servidor, los certificados y las actualizaciones.
2. **Heroku, Railway o Fly.io:** se descartaron porque su plan gratuito era limitado o inexistente en el momento
   de decidir.
3. **Despliegue automático de Render en cada push:** se descartó porque saltaría la verificación de la integración
   continua. Por eso `autoDeploy` está desactivado.
4. **Servidor SMTP para el correo:** se descartó porque el plan gratuito de Render bloquea los puertos SMTP; con
   una API HTTP funciona.

## Consecuencias

- **Positivas:** costo cero, HTTPS por defecto, despliegue reproducible y controlado por la integración continua.
- **Negativas:** el servicio gratuito de Render se duerme tras unos 15 minutos sin tráfico, y la primera petición
  tarda cerca de un minuto. Las bases gratuitas tienen límites, Atlas queda abierto a `0.0.0.0/0` porque Render no
  tiene IP fija, y Render agrega un sufijo al nombre del servicio si ya está tomado: la API quedó en
  `savora-api-ff0z`.
- **Neutras:** las URLs reales deben anotarse en la documentación cuando se conocen.