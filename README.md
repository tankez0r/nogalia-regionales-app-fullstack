# Nogalina Regionales

App para manejar el stock de una tienda de productos regionales de Famatina, La Rioja.
TP Integrador N.º 1 de Aplicaciones Móviles y Cloud Computing.

- App: https://mu-chilecito.playit.plus/nogalina/
- API: https://mu-chilecito.playit.plus/nogalina/api/productos.php
- Integrantes: Torres, Martin Alberto y Torres, Carlos Emanuel

## Cómo correrla en local

### Con XAMPP

1. Copiar la carpeta `nogalina-regionales` dentro de `C:\xampp\htdocs`.
2. Prender Apache y MySQL desde el panel de XAMPP.
3. Entrar a phpMyAdmin (`http://localhost/phpmyadmin`), ir a la pestaña SQL y pegar el
   contenido de `sql/schema.sql`. Crea la base `nogalina`, las tablas y algunos productos
   de ejemplo.
4. Abrir `http://localhost/nogalina-regionales/`.

En local no hace falta configurar nada: si no hay variables de entorno, `api/config.php` usa
los datos por defecto de XAMPP (usuario `root` sin contraseña). El mail de stock bajo no se
manda porque no está la clave de Resend, pero el resto funciona igual.

### Con Docker

1. Instalar Docker Desktop y dejarlo abierto.
2. En la carpeta del proyecto copiar `.env.example` a `.env` y completarlo, por ejemplo:
   ```
   APP_ENV=local
   PUERTO=8080
   DB_HOST=db
   DB_PORT=3306
   DB_NAME=nogalina
   DB_USER=nogalina
   DB_PASS=una_contraseña
   MYSQL_ROOT_PASSWORD=otra_contraseña
   ALLOWED_ORIGIN=http://localhost:8080
   ```
   `DB_NAME` tiene que ser `nogalina` porque es la base que crea `sql/schema.sql`. `RESEND_API_KEY`
   y `MAIL_TO` se pueden dejar vacías si no se quiere probar el mail.
3. Levantar los contenedores: `docker compose up -d --build`
4. Abrir `http://localhost:8080` (o el puerto que se haya puesto en `PUERTO`). La primera vez
   MySQL tarda unos segundos en crear la base; si aparece un error al cargar los productos,
   esperar un poco y recargar.
5. Para apagarla: `docker compose down`. Con `docker compose down -v` también se borra la base,
   y la próxima vez se vuelve a crear con los productos de ejemplo.

Si se cambia algo del código hay que repetir el paso 3, porque los archivos se copian dentro de
la imagen al construirla.

### Carpetas

```
api/          backend en PHP (config.php, productos.php, ventas.php, servicios.php)
sql/          script para crear la base
img/          imagen de fondo
index.html    productos (CRUD), clima y mapa
ventas.html   registrar ventas e historial
app.js, ventas.js, comun.js, styles.css
```

## Endpoints de la API

| Método | Ruta | Recibe | Devuelve | Códigos |
|---|---|---|---|---|
| GET | `api/productos.php` | `?categoria=`, `?q=`, `?bajo_stock=1` (opcionales) | lista de productos | 200, 500 |
| GET | `api/productos.php?id=1` | id | un producto | 200, 400, 404, 500 |
| POST | `api/productos.php` | JSON del producto | `{producto}` | 201, 400, 422, 500 |
| PUT | `api/productos.php?id=1` | JSON del producto | `{producto}` | 200, 400, 404, 422, 500 |
| DELETE | `api/productos.php?id=1` | id | `{ok: true}` | 200, 400, 404, 500 |
| GET | `api/ventas.php` | nada | historial de ventas con sus productos | 200, 500 |
| GET | `api/ventas.php?id=1` | id | una venta con sus productos | 200, 400, 404, 500 |
| POST | `api/ventas.php` | JSON `{items: [{producto_id, cantidad}]}` | `{venta}` | 201, 400, 422, 500 |
| PUT | `api/ventas.php?id=1` | id | anula la venta y devuelve `{venta}` | 200, 400, 404, 409, 500 |

Ejemplo de JSON para crear o editar un producto:

```json
{ "nombre": "Miel", "categoria": "Miel", "precio": 2500, "stock": 10, "stock_minimo": 3,
  "unidad": "kg", "origen": "Famatina", "descripcion": "Miel de la sierra" }
```

Si faltan datos o son incorrectos la API responde 422 con el detalle de cada campo.

## Variables de entorno

| Variable | Para qué sirve |
|---|---|
| APP_ENV | en `production` se apaga `display_errors` |
| DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASS | conexión a MySQL |
| ALLOWED_ORIGIN | origen permitido para CORS (no usamos `*`) |
| RESEND_API_KEY | clave de Resend para mandar los mails |
| MAIL_TO | a quién le llegan los avisos de stock bajo |

Los valores nunca están en el repo. `.env.example` tiene solo los nombres.

## Servicios externos

| Servicio | Quién lo llama | Por qué | Datos que salen | Si falla |
|---|---|---|---|---|
| Resend (email) | backend (`api/servicios.php`) | avisar cuando un producto queda con stock bajo | nombre del producto y cantidades, a `MAIL_TO` | el producto o la venta se guardan igual y la API devuelve `advertencia` |
| Leaflet + OpenStreetMap (mapa) | navegador (`app.js`) | mostrar dónde está la tienda | ningún dato de la app, solo se piden los mapas de la zona | se muestra "No se pudo cargar el mapa" y el resto de la página funciona |
| Open-Meteo (clima) | navegador (`app.js`) | mostrar el clima del día en Famatina | solo las coordenadas de Famatina | se muestra "No se pudo cargar el clima" y el resto de la página funciona |

Resend lo llamamos desde el backend porque necesita la API key, y si lo hiciéramos desde el
navegador la clave quedaría expuesta. El mapa y el clima no necesitan clave y no mandan datos
de la app, así que se cargan directo en el navegador.

## Despliegue

La app corre en un servidor nuestro con Docker, en dos contenedores: uno con PHP 8.2 y Apache (la
app) y otro con MySQL 8 (la base). El servidor ya tenía un dominio con HTTPS: un túnel de
playit.gg llega a un nginx que tiene el certificado de Let's Encrypt. A ese nginx le agregamos la
ruta `/nogalina/`, que manda las peticiones al contenedor de la app, así que no hizo falta un
túnel aparte.

Es un modelo parecido a IaaS: nosotros administramos la máquina, Docker, la base y las
actualizaciones. Lo elegimos porque ya teníamos el servidor y el dominio funcionando, y así no
dependemos de los límites de los planes gratis de las plataformas.

Cómo se actualiza:

1. Con cada push a `main`, GitHub Actions (`.github/workflows/docker.yml`) construye la imagen,
   prueba que levante y la publica en GitHub Container Registry como `ghcr.io/tankez0r/nogalina`.
2. En el servidor, un cron revisa cada 20 minutos si hay un commit nuevo en `main`. Si lo hay,
   baja la imagen nueva y recrea solo el contenedor de la app. La base no se toca y los datos
   quedan guardados en el servidor.

En el servidor no está el código, solo `docker-compose.yml`, `.env` y `sql/schema.sql`. El código
viene dentro de la imagen.

Pasos para volver a desplegar desde cero:

1. Tener Docker en el servidor y hacer `docker login ghcr.io` con una cuenta que tenga acceso al
   paquete, porque la imagen es privada.
2. Copiar `docker-compose.yml` y `sql/schema.sql`. En `docker-compose.yml` cambiar `build: .` por
   `image: ghcr.io/tankez0r/nogalina:latest`.
3. Armar el `.env` a partir de `.env.example` con `APP_ENV=production`, `DB_HOST` con el nombre
   del servicio de MySQL, `ALLOWED_ORIGIN=https://mu-chilecito.playit.plus` y contraseñas nuevas
   para la base.
4. `docker compose up -d`. La primera vez MySQL ejecuta solo `sql/schema.sql` y crea las tablas.
5. En nginx, mandar `/nogalina/` al puerto de la app y redirigir `/nogalina` a `/nogalina/`. Las
   rutas de la app son relativas, así que sin la barra final no cargan los estilos ni la API.
6. Para que se actualice sola, programar en cron un script que haga `docker compose pull web` y
   `docker compose up -d web` cuando haya una imagen nueva.

## Ventas

En `ventas.html` se arma una venta con varios productos (tipo carrito) y abajo se ve el
historial.

- Al registrar la venta se descuenta el stock dentro de una transacción. Si algún producto
  no tiene stock suficiente se cancela toda la venta.
- Cada item guarda el nombre y el precio que tenía el producto en ese momento, así el
  historial no cambia si después se edita o se borra el producto.
- Una venta se puede anular: vuelve el stock pero la venta queda en el historial.
- Si después de una venta algún producto queda en el mínimo o por debajo, se manda el aviso
  por mail igual que al crear o editar un producto.

## Limitaciones conocidas

- No hay login, cualquiera puede usar el CRUD.
- La app depende de que el servidor esté prendido y con internet. Si se corta la luz o la
  conexión, la app no responde hasta que vuelva.
- Un cambio subido a `main` puede tardar hasta unos 20 minutos, más lo que tarda GitHub
  Actions, en verse en la URL pública. Si la actualización falla no se vuelve atrás sola: queda
  anotado en el servidor y hay que hacerlo a mano.
- El aviso de stock bajo solo se manda al crear o editar un producto o al registrar una
  venta. No hay un chequeo periódico.
- El clima se pide una vez al abrir la página, no se actualiza solo.

## Aportes

Cada uno hizo sus partes en commits separados:

| Integrante | Commit | Qué hizo | Fecha |
|---|---|---|---|
| Torres, Martin Alberto (tankez0r) | `5d6466f` abm de productos y mapa | estructura del proyecto, `api/config.php`, `api/productos.php`, `api/servicios.php` (Resend), tabla de productos en `sql/schema.sql`, `index.html`, `app.js`, `comun.js`, `styles.css` y el mapa | 14/10/2026 |
| Torres, Martin Alberto (tankez0r) | `d205ff7` agrego docker | `Dockerfile`, `docker-compose.yml` y los pasos de despliegue | 14/10/2026 |
| Torres, Carlos Emanuel (puchito456) | `1f98617` api de ventas | `api/ventas.php` y las tablas `ventas` y `detalle_venta` | 15/10/2026 |
| Torres, Carlos Emanuel (puchito456) | `0a8025c` pagina de ventas | `ventas.html`, `ventas.js` y el link a ventas en `index.html` | 15/10/2026 |
