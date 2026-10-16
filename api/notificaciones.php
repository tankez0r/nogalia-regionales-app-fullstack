<?php
require 'config.php';

$metodo = $_SERVER['REQUEST_METHOD'];
$id = isset($_GET['id']) ? $_GET['id'] : null;

if ($id !== null && !ctype_digit($id)) {
    responder(['error' => 'El id no es válido'], 400);
}

function buscarNotificacion($pdo, $id)
{
    $stmt = $pdo->prepare('SELECT * FROM notificaciones WHERE id = ?');
    $stmt->execute([$id]);
    return $stmt->fetch();
}

try {
    $pdo = conectar();

    if ($metodo === 'GET') {
        $sql = 'SELECT * FROM notificaciones';
        if (!empty($_GET['no_leidas'])) {
            $sql .= ' WHERE leida = 0';
        }
        $sql .= ' ORDER BY id DESC';
        responder($pdo->query($sql)->fetchAll());
    }

    // Marcar como leída no borra la notificación, solo deja de mostrarse en la página. Así el
    // registro completo sigue en la base.
    if ($metodo === 'PUT') {
        if ($id === null) {
            responder(['error' => 'Falta el id de la notificación'], 400);
        }
        if (!buscarNotificacion($pdo, $id)) {
            responder(['error' => 'Notificación no encontrada'], 404);
        }
        $stmt = $pdo->prepare('UPDATE notificaciones SET leida = 1 WHERE id = ?');
        $stmt->execute([$id]);
        responder(['notificacion' => buscarNotificacion($pdo, $id)]);
    }

    responder(['error' => 'Método no permitido'], 405);
} catch (Exception $e) {
    error_log('notificaciones.php: ' . $e->getMessage());
    responder(['error' => 'Error en el servidor'], 500);
}
