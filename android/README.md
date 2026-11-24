# Nogalina Regionales (app Android)

App Android para manejar el stock y las ventas de una tienda de productos regionales de Famatina,
La Rioja. Usa la misma API REST del TP N.º 1.
TP Integrador N.º 2 de Aplicaciones Móviles y Cloud Computing.

- Integrantes: Torres, Martin Alberto y Torres, Carlos Emanuel
- API: https://mu-chilecito.playit.plus/nogalina/api/
- Frontend web: https://mu-chilecito.playit.plus/nogalina/
- Proveedor: servidor propio con Docker, publicado por HTTPS con un túnel de playit.gg y nginx
  con certificado de Let's Encrypt (está explicado en el README principal del repositorio).
- La app no pide usuario ni contraseña.

## Cómo correrla

1. Abrir la carpeta `android/` en Android Studio (File > Open). La primera vez Gradle baja solo
   las dependencias.
2. Elegir un emulador o un celular con Android 8.0 o superior y darle a Run.

También se puede compilar la APK por consola con `gradlew assembleDebug`. Queda en
`app/build/outputs/apk/debug/app-debug.apk`.

No hace falta ningún archivo de configuración ni `google-services.json`. La URL base de la API
está en un solo lugar, en `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "API_URL", "\"https://mu-chilecito.playit.plus/nogalina/api/\"")
```

Para probar contra el backend en local, se cambia esa línea (por ejemplo
`http://10.0.2.2:8080/api/` desde el emulador, que además necesita permitir HTTP sin cifrar).

## Pantallas

| Pantalla | Qué hace |
|---|---|
| `MainActivity` | lista de productos con el clima de Famatina arriba, filtros por categoría, búsqueda y solo bajo stock, y el menú para ir al resto |
| `DetalleActivity` | abre un producto por su `id` (llega como extra), lo muestra, y permite editarlo o borrarlo |
| `FormularioActivity` | crear o editar un producto; al guardar devuelve `RESULT_OK` y la pantalla anterior recarga |
| `VentasActivity` | historial de ventas y anular una venta |
| `NuevaVentaActivity` | arma una venta con varios productos (carrito) y la registra; devuelve `RESULT_OK` |
| `NotificacionesActivity` | avisos de stock bajo: los nuevos de las últimas 24 horas o todos, y marcar como leído |
| `MapaActivity` | mapa de OpenStreetMap con la ubicación de la tienda |

La navegación es con Intents. Detalle recibe el `id` y Formulario recibe el `Producto` como extra.
Las pantallas que cambian datos devuelven un resultado con `registerForActivityResult`, así la
lista se actualiza sola al volver.

## Endpoints que usa la app

| Método | Ruta | Dónde |
|---|---|---|
| GET | `productos.php` (`?categoria=`, `?q=`, `?bajo_stock=1`) | lista de productos y selector de la venta |
| GET | `productos.php?id=N` | detalle |
| POST | `productos.php` | nuevo producto |
| PUT | `productos.php?id=N` | editar producto |
| DELETE | `productos.php?id=N` | borrar producto |
| GET | `ventas.php` | historial |
| POST | `ventas.php` | registrar venta |
| PUT | `ventas.php?id=N` | anular venta |
| GET | `notificaciones.php` (`?no_leidas=1&recientes=1`) | contador del menú y lista de notificaciones |
| PUT | `notificaciones.php?id=N` | marcar como leída |

## Servicios cloud

| Servicio | Para qué | Clave | Si falla |
|---|---|---|---|
| Open-Meteo | clima actual de Famatina en la pantalla principal | no usa | se muestra "No se pudo cargar el clima." y la lista sigue andando |
| OpenStreetMap (con osmdroid) | mapa de la tienda | no usa | el mapa queda sin cargar y el resto de la app sigue andando |

Son los mismos servicios externos que usa el frontend web del TP N.º 1. No usamos Firestore ni
Storage, así que no hay reglas de seguridad que configurar. Como ninguno de los dos necesita clave,
en el repositorio y en la APK no hay contraseñas, claves ni cuentas de servicio.

## Decisiones de diseño

- **Retrofit + Gson** para la API. `enqueue` hace el pedido en otro hilo y devuelve la respuesta
  en el principal. Gson pasa los nombres de la API (`stock_minimo`) a los de Java (`stockMinimo`).
- **`Pedido<T>`** junta lo que se repetía en todas las pantallas: el estado de carga, los datos y
  el error, en `LiveData`. Si la API responde 400, 404, 409 o 422 se muestra el mensaje que manda
  la API. En el 422 cada error va debajo de su campo. Si no hay internet o el servidor no responde
  se muestra un mensaje y la app no se cierra.
- **ViewModel en cada pantalla**: al girar el celular los datos siguen en memoria y no se vuelven a
  pedir. Mientras un envío está en curso el botón queda deshabilitado y `Pedido` no deja mandar
  otro, así no se repite un POST.
- **Validación antes de enviar**: el formulario y el carrito revisan los datos (campos
  obligatorios, números, stock disponible) antes de llamar a la API. La API vuelve a validar igual.
- **minSdk 26** (Android 8.0): el certificado de Let's Encrypt del servidor depende de ISRG Root
  X1, que Android 7.0 no trae, y con minSdk 24 la app no podía conectarse en esos celulares.
- **targetSdk 34**: con 35 o más, Android 15 obliga a dibujar la app de borde a borde y la barra de
  título tapaba el principio de cada pantalla. Con 34 la app se ve igual en todas las versiones.
- En horizontal la pantalla principal pone los filtros a la izquierda y la lista a la derecha.

## Limitaciones conocidas

- No hay login: cualquiera con la app puede modificar los productos, igual que en la web.
- La app depende de que el servidor esté prendido. Si no responde, se ve el mensaje de error y el
  botón Reintentar.
- Las notificaciones no llegan como push: se ven al abrir la app o al volver a la lista.
- El clima y el mapa necesitan internet y no se guardan para verlos sin conexión.
- Las fechas se muestran como las manda la API (`2026-10-16 16:31:43`).

## Aportes

Los commits se subieron desde una sola cuenta. Esto es lo que hizo cada uno:

| Integrante | Commit | Qué hizo |
|---|---|---|
| Torres, Martin Alberto | `eed22f1` proyecto android y conexion con la api | proyecto de Android Studio, Gradle, `Api.java` (Retrofit), `Pedido.java` (carga, errores y envíos), los modelos, `strings.xml`, el tema y el ícono |
| Torres, Martin Alberto | `ff50019` productos, clima y filtros en la app | `MainActivity` con la lista, los filtros y el clima de Open-Meteo, `DetalleActivity`, `FormularioActivity` y la vista horizontal |
| Torres, Carlos Emanuel | `8cbe731` ventas en la app | `VentasActivity` (historial y anular) y `NuevaVentaActivity` (carrito y registrar venta) |
| Torres, Carlos Emanuel | `d41e3a5` notificaciones en la app | `NotificacionesActivity`: recientes o todas y marcar como leída |
| Torres, Martin Alberto | `283f3ad` mapa en la app | `MapaActivity` con OpenStreetMap |
| Torres, Martin Alberto | readme de la app | este README |
