# Guía de despliegue — Laboratorio 6

**Sistema publicado**

| Qué | Dirección |
|---|---|
| API | <https://savora-api-ff0z.onrender.com> |
| SPA (clientes y negocios) | <https://savora-spa.onrender.com> |
| Inicio de sesión SPA | <https://savora-spa.onrender.com/#/login> |
| Swagger UI | <https://savora-api-ff0z.onrender.com/swagger-ui.html> |
| Salud | <https://savora-api-ff0z.onrender.com/actuator/health> |
| Panel administrativo (ADMIN) | <https://savora-api-ff0z.onrender.com/login> |

Render agregó el sufijo `-ff0z` al nombre del servicio de la API porque `savora-api` ya estaba tomado.

Orden recomendado: bases de datos → API → comprobar → SPA → conectar CORS → pruebas finales.
Nunca pegue contraseñas, URLs de conexión ni secretos en archivos del repositorio: solo en los
paneles de las plataformas.

## 1. PostgreSQL en Neon

1. Cree una cuenta en <https://neon.tech> y un proyecto (región más cercana, p. ej. `us-east`).
2. En **Connection Details** copie host, base, usuario y contraseña.
3. Arme el valor de `DB_URL` así (cambie el host y la base por los suyos):
   `jdbc:postgresql://<host>/<base>?sslmode=require`
4. `DB_USER` y `DB_PASSWORD` son el usuario y la contraseña de Neon (por separado, no dentro de la URL).

Flyway crea las tablas y carga los datos de ejemplo la primera vez que arranca la API.

## 2. MongoDB en Atlas

1. Cree una cuenta en <https://www.mongodb.com/cloud/atlas> y un clúster gratuito **M0**.
2. **Database Access** → cree un usuario con contraseña propia.
3. **Network Access** → agregue `0.0.0.0/0` (Render usa IPs variables en el plan gratuito).
4. **Connect → Drivers** → copie la cadena `mongodb+srv://…` y agregue el nombre de la base antes
   del `?`, por ejemplo `.../savora?retryWrites=true&w=majority`. Ese es el valor de `MONGODB_URI`.

## 3. API en Render

1. Suba el código a GitHub (rama `main`). Revise antes que no haya secretos (sección 7).
2. En <https://render.com> → **New → Blueprint** → elija el repositorio. Render lee `render.yaml`
   y crea `savora-api` y `savora-spa`.
3. Cuando pida los valores `sync: false`, complete (solo para `savora-api` por ahora):
   - `DB_URL`, `DB_USER`, `DB_PASSWORD` (Neon)
   - `MONGODB_URI` (Atlas)
   - `SAVORA_CORS_ORIGINS`: déjelo con un valor provisional (`https://localhost`) y corríjalo en el paso 5
   - `SAVORA_SEED_NEGOCIO_PASSWORD`, `SAVORA_SEED_CLIENTE_PASSWORD` y `SAVORA_SEED_ADMIN_PASSWORD`:
     contraseñas **nuevas** que usted elija (son las credenciales de prueba que entregará en el aula virtual)
   - `SAVORA_SPA_URL` (para los enlaces de los correos), y opcionalmente `SAVORA_RESEND_API_KEY` y
     `SAVORA_MAIL_FROM` (correo de recuperación de contraseña)
   - `VITE_API_URL` (de la SPA): provisional por ahora
   `SAVORA_JWT_SECRET` lo genera Render solo; es distinto al de desarrollo.
4. Espere el primer despliegue (5–10 minutos). Compruebe:
   - `https://savora-api-ff0z.onrender.com/actuator/health` → `{"status":"UP"}`
   - `https://savora-api-ff0z.onrender.com/swagger-ui.html` → abre Swagger UI
   - `https://savora-api-ff0z.onrender.com/login` → pantalla del módulo administrativo

> El plan gratuito de Render suspende el servicio tras 15 minutos sin tráfico; la primera
> petición tarda ~1 minuto en despertarlo. Ábralo unos minutos antes de entregar y antes de
> cualquier presentación.

## 4. SPA

1. Copie la URL pública de la API (`https://savora-api-ff0z.onrender.com`, sin `/` al final).
2. Render → `savora-spa` → **Environment** → `VITE_API_URL` = esa URL.
3. **Manual Deploy → Deploy latest commit** (la variable se incorpora al compilar).

## 5. CORS con el dominio real de la SPA

1. Copie la URL pública de la SPA (`https://savora-spa.onrender.com`, sin `/` al final).
2. Render → `savora-api` → **Environment** → `SAVORA_CORS_ORIGINS` = esa URL → guardar (reinicia la API).
3. Abra la SPA e inicie sesión con `cliente1` y la contraseña que eligió.

## 6. Qué entregar en el aula virtual

- API: <https://savora-api-ff0z.onrender.com>, SPA: <https://savora-spa.onrender.com/#/login>, Swagger:
  <https://savora-api-ff0z.onrender.com/swagger-ui.html>, panel ADMIN:
  <https://savora-api-ff0z.onrender.com/login> (mismas direcciones en README y `docs/diagrama-despliegue.md`).
- Credenciales de prueba, una por rol: `admin` (rol ADMIN, entra al panel `/login`), `negocio1` (rol
  NEGOCIO, entra a la SPA) y `cliente1` (rol CLIENTE, entra a la SPA), con las contraseñas que definió
  en Render.

## 7. Revisar que no haya secretos en el historial

```bash
git log -p --all | grep -i -E "mongodb\+srv|neon\.tech|BEGIN PRIVATE|password=" 
```

Si aparece algo que está en uso en producción, **cambie ese secreto** en su plataforma (nueva
contraseña de base de datos, nuevo usuario de Atlas, etc.). Esto es obligatorio: un secreto
expuesto en el historial anula el criterio de perfiles y secretos.

## 8. Problemas frecuentes

| Síntoma | Causa probable |
|---|---|
| La API no arranca y el log dice `Could not resolve placeholder 'DB_URL'` | Falta una variable de entorno en `savora-api` |
| `FATAL: password authentication failed` | `DB_USER`/`DB_PASSWORD` mal copiados |
| Timeout contra Mongo | Falta `0.0.0.0/0` en Network Access de Atlas, o falta el nombre de la base en la URI |
| La SPA muestra "No se pudo conectar con el servidor" | `VITE_API_URL` mal escrita, o la API está dormida |
| El navegador muestra error de CORS | `SAVORA_CORS_ORIGINS` no coincide exactamente con la URL de la SPA (con `https://` y sin `/` final) |
| La SPA muestra la página en blanco | `VITE_API_URL` no estaba definida al compilar: redespliegue manual de la SPA |