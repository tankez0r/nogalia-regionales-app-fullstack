<?php

$entorno = getenv('APP_ENV') ?: 'local';

if ($entorno === 'production') {
    ini_set('display_errors', '0');
} else {
    ini_set('display_errors', '1');
}
error_reporting(E_ALL);

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
