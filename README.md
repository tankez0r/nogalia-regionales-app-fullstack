# Nogalina Regionales

App para manejar el stock de una tienda de productos regionales de Famatina, La Rioja.
TP Integrador N.º 1 de Aplicaciones Móviles y Cloud Computing.

- App: https://REEMPLAZAR-CON-LA-URL
- API: https://REEMPLAZAR-CON-LA-URL/api/productos.php
- Integrantes: Torres, Martin Alberto y Torres, Carlos Emanuel

## Cómo correrla en local (XAMPP)

1. Copiar la carpeta `nogalina-regionales` dentro de `C:\xampp\htdocs`.
2. Prender Apache y MySQL desde el panel de XAMPP.
3. Entrar a phpMyAdmin (`http://localhost/phpmyadmin`), ir a la pestaña SQL y pegar el
   contenido de `sql/schema.sql`. Crea la base `nogalina`, las tablas y algunos productos
   de ejemplo.
4. Abrir `http://localhost/nogalina-regionales/`.

En local no hace falta configurar nada: si no hay variables de entorno, `api/config.php` usa
los datos por defecto de XAMPP (usuario `root` sin contraseña). El mail de stock bajo no se
manda porque no está la clave de Resend, pero el resto funciona igual.

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
En XAMPP se pueden cargar con `SetEnv` en la configuración de Apache (`httpd.conf`), por
ejemplo para probar el mail de stock bajo con la clave de Resend.

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

Esta rama es la versión para correr en local con XAMPP. La versión desplegada está en la rama
`main`, que usa Docker en la PC servidor y un túnel para publicarla por HTTPS (los pasos están
en el README de esa rama).

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
- El aviso de stock bajo solo se manda al crear o editar un producto o al registrar una
  venta. No hay un chequeo periódico.
- El clima se pide una vez al abrir la página, no se actualiza solo.

## Aportes

Cada uno hizo sus partes en commits separados:

| Integrante | Commit | Qué hizo | Fecha |
|---|---|---|---|
| Torres, Martin Alberto (tankez0r) | `5d6466f` abm de productos y mapa | estructura del proyecto, `api/config.php`, `api/productos.php`, `api/servicios.php` (Resend), tabla de productos en `sql/schema.sql`, `index.html`, `app.js`, `comun.js`, `styles.css` y el mapa | 14/10/2026 |
| Torres, Martin Alberto (tankez0r) | `d205ff7` agrego docker | `Dockerfile`, `docker-compose.yml` y el despliegue en la PC con el túnel | 14/10/2026 |
| Torres, Carlos Emanuel (puchito456) | `1f98617` api de ventas | `api/ventas.php` y las tablas `ventas` y `detalle_venta` | 15/10/2026 |
| Torres, Carlos Emanuel (puchito456) | `0a8025c` pagina de ventas | `ventas.html`, `ventas.js` y el link a ventas en `index.html` | 15/10/2026 |
