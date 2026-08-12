# Propuesta de Dominio

Laboratorio 1 · EIF509 · II Ciclo 2026

**Savora**

## 1 · Identificación

### Equipo y sistema

**Integrantes:** Yeilin Moya Baltodano (504550568) y Nazareth Gómez Gómez (504430491).

**Nombre del sistema propuesto:** Savora

## 2 · El negocio

### Descripción del negocio

Muchos negocios pequeños de comida —panaderías, sodas y restaurantes de barrio— generan diariamente un excedente de alimentos en buen estado que termina desechándose al cierre, ya sea por falta de tiempo, de canal de venta o de forma de darle salida antes de que pierda valor comercial. Al mismo tiempo, existen personas dispuestas a comprar esa comida a un precio reducido, y organizaciones comunitarias (comedores, ONG) que podrían aprovecharla cuando no se vende a tiempo.

Savora es una plataforma que permite a estos negocios publicar "paquetes sorpresa" con su excedente diario a precio reducido. Los clientes reservan y recogen el paquete antes de una hora límite. Si un paquete no se reserva a tiempo, el sistema prioriza ofrecerlo a una organización comunitaria registrada para donación, en vez de que se pierda.

### Actores

- **Cliente:** navega el catálogo de paquetes disponibles, reserva y recoge sus pedidos, y consulta su historial de compras.
- **Negocio:** publica y administra sus paquetes sorpresa, define precios y horarios de recogida.
- **Organización comunitaria:** recibe paquetes donados cuando no se venden a tiempo, indicando su capacidad de recolección.
- **Administrador:** supervisa negocios y usuarios registrados, y gestiona incidencias de la plataforma.

## 3 · Entidades de negocio

### Listado de entidades

- **Negocio:** comercio que publica excedente de comida. Datos: nombre, tipo de negocio, ubicación, horario de cierre.
- **Cliente:** usuario que compra paquetes. Datos: nombre, correo, teléfono, historial de compras.
- **PaqueteSorpresa:** excedente publicado por un negocio. Datos: descripción, cantidad, precio original, precio con descuento, hora límite de recogida, estado (disponible, reservado, recogido, donado, perdido).
- **Reserva:** vínculo entre un cliente y un paquete que aparta. Datos: fecha y hora de reserva, estado (pendiente, recogido, no recogido).
- **Categoría:** clasificación del tipo de comida del paquete (panadería, comida preparada, frutas y verduras, otros).
- **OrganizaciónComunitaria:** comedor u ONG que puede recibir donaciones. Datos: nombre, tipo, capacidad de recolección, contacto.

### Relaciones entre entidades

- Un Negocio publica muchos PaqueteSorpresa.
- Un PaqueteSorpresa pertenece a una Categoría.
- Un Cliente realiza muchas Reservas.
- Una Reserva corresponde exactamente a un PaqueteSorpresa.
- Un PaqueteSorpresa no reservado antes de su hora límite puede asignarse a una OrganizaciónComunitaria como donación.

### Subdominio documental candidato

El historial de impacto de cada negocio y cliente (kilogramos de comida rescatados, número de donaciones realizadas, bitácora de paquetes por día) es información flexible y de crecimiento variable, más parecida a un documento que a un registro rígido. Se identifica aquí como candidata para modelarse en MongoDB en el Laboratorio 2.

## 4 · Procesos de negocio

### Proceso 1: Reservar un paquete sorpresa

Un cliente selecciona un PaqueteSorpresa disponible y lo reserva. El proceso involucra:

- **Reglas:** solo se puede reservar un paquete si su estado es "disponible" y aún no se llegó a la hora límite de recogida. Un cliente no puede tener más de N reservas activas simultáneas, para evitar acaparamiento.
- **Cálculos:** el precio final se obtiene aplicando el porcentaje de descuento definido por el negocio sobre el precio original; si el cliente reserva más de un paquete, se calcula el total acumulado.
- **Validaciones:** se valida que exista stock del paquete, que la hora actual sea anterior a la hora límite, y que el cliente no exceda su límite de reservas activas.

Este proceso involucra varios cambios que deben ocurrir todos juntos o ninguno: crear el registro de Reserva y, al mismo tiempo, actualizar el estado (y el stock) del PaqueteSorpresa correspondiente. Si el paquete llega a cero unidades disponibles, su estado cambia a "agotado" como parte de la misma operación. Este será el punto que se trabajará como transacción en el Laboratorio 4.

### Proceso 2: Cierre de paquetes no reservados

Al llegar la hora límite de un PaqueteSorpresa que no fue reservado por ningún cliente, el sistema evalúa si puede asignarse como donación.

- **Reglas:** un paquete no reservado se ofrece primero a las organizaciones comunitarias registradas con capacidad de recolección disponible en ese horario; si ninguna acepta, el paquete se marca como "perdido".
- **Cálculos:** se actualiza el acumulado de kilogramos de comida rescatados del negocio y, si aplica, de la organización receptora, para efectos de reportes de impacto.
- **Validaciones:** se valida que la organización tenga capacidad de recolección disponible y que el paquete no haya sido reservado por un cliente mientras se procesaba la asignación (evitar condición de carrera entre una reserva de último minuto y el cierre automático).

## 5 · Alcance

### Dentro del alcance

- Catálogo de negocios y paquetes sorpresa, con búsqueda y filtro por categoría.
- Reserva de paquetes por parte de clientes, con control de disponibilidad y hora límite.
- Asignación de paquetes no reservados a organizaciones comunitarias registradas.
- Historial de compras del cliente e indicador de comida "salvada".
- Gestión básica de negocios, categorías y organizaciones comunitarias.

### Fuera del alcance

- Procesamiento de pagos reales (pasarela de pago); se simula el checkout.
- Logística de reparto o delivery; la recogida es siempre presencial en el negocio.
- Notificaciones push reales o app móvil nativa.
- Verificación legal o sanitaria de donaciones ante entidades reguladoras.
- Rutas de recolección automatizadas para organizaciones comunitarias.
