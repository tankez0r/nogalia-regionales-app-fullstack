<?php

// Si PHP falla y display_errors está prendido, el error sale en la respuesta con rutas de archivos,
// nombres de tablas y a veces con datos de la conexión. Esto puede dar pistas a un atacante, por ende
// en producción es apagado y en local se deja prendido. Esto sirve para debuguear mientras
// programamos. Es para satisfacer el requisito 8 de las consignas.
$entorno = getenv('APP_ENV') ?: 'local';

if ($entorno === 'production') {
    ini_set('display_errors', '0');
} else {
    ini_set('display_errors', '1');
}
error_reporting(E_ALL);

// El navegador bloquea por defecto que una página con otro dominio llame a nuestra API. El
// encabezado de control de acceso de origen le avisa al navegador qué dominio sí puede. Esto lo
// hicimos para respetar el requisito número 8 de la consigna. Si esta variable no está, como en
// XAMPP, no se manda el encabezado y solo puede usarla la misma página. Cuando el pedido es un PUT o
// un DELETE, o lleva JSON, el navegador manda un OPTIONS para preguntar si tiene permiso. Si
// contestamos un 204 (que obviamente no va a tener contenido) y corta ahí, sin tocar la base.
$origenPermitido = getenv('ALLOWED_ORIGIN');
if ($origenPermitido) {
    header('Access-Control-Allow-Origin: ' . $origenPermitido);
}
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

// Los datos de la base de datos no están escritos en el código. En cambio se leen de variables de
// entorno, así esto no queda dentro de GitHub (requisito número 7). Si una variable no existe, el ?:
// usa el valor por defecto de XAMPP (localhost, root sin contraseña). Es por eso que en local
// funciona sin configurar nada y en el servidor usa lo que está en el archivo de entorno (.env).
function conectar()
{
    $host = getenv('DB_HOST') ?: 'localhost';
    $puerto = getenv('DB_PORT') ?: '3306';
    $base = getenv('DB_NAME') ?: 'nogalina';
    $usuario = getenv('DB_USER') ?: 'root';
    $clave = getenv('DB_PASS') ?: '';

    $pdo = new PDO("mysql:host=$host;port=$puerto;dbname=$base;charset=utf8mb4", $usuario, $clave);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
    return $pdo;
}

function responder($datos, $codigo = 200)
{
    http_response_code($codigo);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode($datos, JSON_UNESCAPED_UNICODE);
    exit;
}

function leerJson()
{
    $datos = json_decode(file_get_contents('php://input'), true);
    if (!is_array($datos)) {
        responder(['error' => 'El cuerpo de la petición no es un JSON válido'], 400);
    }
    return $datos;
}
