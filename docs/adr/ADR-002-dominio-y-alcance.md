# ADR-002 · El dominio y su alcance

**Estado y fecha:** aceptada (agosto de 2026) · **Responsables:** Yeilin Moya Baltodano, Nazareth Gómez Gómez

## Contexto

Había que elegir un dominio propio, con al menos dos procesos de negocio con reglas, cálculos y validaciones, y con
una parte que justificara una base documental. Los negocios pequeños de comida (panaderías, sodas) generan
excedente diario que termina desechándose, mientras hay personas que lo comprarían a menor precio y
organizaciones comunitarias que podrían recibirlo. El tiempo del ciclo era limitado y el sistema debía poder
demostrarse de extremo a extremo.

## Decisión

Construir **Savora**: una plataforma donde los negocios publican "paquetes sorpresa" con su excedente, los clientes
los reservan y recogen antes de una hora límite, y los que no se venden se donan a una organización comunitaria o
se marcan como perdidos. El historial de impacto (rachas e insignias) y las reseñas completan el dominio.
Cuatro actores: cliente, negocio, organización comunitaria y administrador.

Quedan **fuera del alcance**: pasarela de pagos real (el pago se simula), reparto a domicilio, notificaciones push
o app móvil nativa, verificación sanitaria de las donaciones y rutas de recolección automatizadas.

## Alternativas consideradas

1. **Un sistema de reservas de citas o de inventario genérico:** se descartó porque tiene menos reglas
   interesantes de concurrencia y de cierre por tiempo, y porque no tiene una parte documental natural.
2. **Incluir pagos reales con una pasarela (por ejemplo Stripe):** se descartó por costo, por los datos
   sensibles que implica manejar y porque no aporta a los objetivos del curso. Se simula un pago con tarjetas de
   prueba y se guarda solo la marca, los últimos cuatro dígitos y una referencia.
3. **Incluir delivery y rutas de recolección:** se descartó por complejidad. La recogida es presencial en el negocio.

## Consecuencias

- **Positivas:** dos procesos claros (reservar y cierre de paquetes no vendidos) con reglas, cálculos y
  validaciones; un caso real de concurrencia (dos clientes sobre el mismo paquete); y un subdominio documental
  natural (impacto y reseñas).
- **Negativas:** el pago simulado no demuestra una integración real. Además, la restricción de una reserva por
  paquete hace que cada paquete publicado equivalga a una unidad vendible, aunque su cantidad sea mayor.
- **Neutras:** el administrador gestiona categorías, organizaciones y la aprobación de negocios desde un panel
  propio, no desde Swagger.