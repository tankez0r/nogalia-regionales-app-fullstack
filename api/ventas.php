<?php
require 'config.php';
require 'servicios.php';

$metodo = $_SERVER['REQUEST_METHOD'];
$id = isset($_GET['id']) ? $_GET['id'] : null;

if ($id !== null && !ctype_digit($id)) {
    responder(['error' => 'El id no es válido'], 400);
}

function buscarVenta($pdo, $id)
{
    $stmt = $pdo->prepare('SELECT * FROM ventas WHERE id = ?');
    $stmt->execute([$id]);
    $venta = $stmt->fetch();
    if (!$venta) {
        return null;
    }

    $stmt = $pdo->prepare('SELECT * FROM detalle_venta WHERE venta_id = ?');
    $stmt->execute([$id]);
    $venta['items'] = $stmt->fetchAll();
    return $venta;
}

try {
    $pdo = conectar();

    if ($metodo === 'GET') {
        if ($id !== null) {
            $venta = buscarVenta($pdo, $id);
            if (!$venta) {
                responder(['error' => 'Venta no encontrada'], 404);
            }
            responder($venta);
        }

        $ventas = $pdo->query('SELECT id FROM ventas ORDER BY id DESC')->fetchAll();
        $resultado = [];
        foreach ($ventas as $v) {
            $resultado[] = buscarVenta($pdo, $v['id']);
        }
        responder($resultado);
    }

    if ($metodo === 'POST') {
        $datos = leerJson();
        $items = $datos['items'] ?? [];

        if (!is_array($items) || count($items) === 0) {
            responder(['error' => 'Datos inválidos', 'detalle' => ['items' => 'La venta tiene que tener al menos un producto']], 422);
        }
        foreach ($items as $item) {
            if (!is_array($item)) {
                responder(['error' => 'Datos inválidos', 'detalle' => ['items' => 'Formato de items incorrecto']], 422);
            }
            $productoId = filter_var($item['producto_id'] ?? null, FILTER_VALIDATE_INT);
            $cantidad = filter_var($item['cantidad'] ?? null, FILTER_VALIDATE_INT);
            if (!$productoId || $productoId <= 0 || !$cantidad || $cantidad <= 0) {
                responder(['error' => 'Datos inválidos', 'detalle' => ['items' => 'Cada producto necesita un id y una cantidad mayor a 0']], 422);
            }
        }

        $pdo->beginTransaction();
        $total = 0;
        $vendidos = [];

        foreach ($items as $item) {
            $productoId = (int) $item['producto_id'];
            $cantidad = (int) $item['cantidad'];

            $stmt = $pdo->prepare('SELECT * FROM productos WHERE id = ?');
            $stmt->execute([$productoId]);
            $producto = $stmt->fetch();
            if (!$producto) {
                $pdo->rollBack();
                responder(['error' => 'Datos inválidos', 'detalle' => ['items' => 'No existe el producto ' . $productoId]], 422);
            }

            $stmt = $pdo->prepare('UPDATE productos SET stock = stock - ? WHERE id = ? AND stock >= ?');
            $stmt->execute([$cantidad, $productoId, $cantidad]);
            if ($stmt->rowCount() === 0) {
                $pdo->rollBack();
                responder(['error' => 'Stock insuficiente', 'detalle' => ['items' => 'No hay stock suficiente de ' . $producto['nombre']]], 422);
            }

            $total += $producto['precio'] * $cantidad;
            $vendidos[] = [
                'producto' => $producto,
                'cantidad' => $cantidad,
            ];
        }

        $stmt = $pdo->prepare('INSERT INTO ventas (total) VALUES (?)');
        $stmt->execute([$total]);
        $ventaId = $pdo->lastInsertId();

        $stmt = $pdo->prepare('INSERT INTO detalle_venta (venta_id, producto_id, producto_nombre, cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?)');
        foreach ($vendidos as $v) {
            $stmt->execute([$ventaId, $v['producto']['id'], $v['producto']['nombre'], $v['cantidad'], $v['producto']['precio']]);
        }

        $pdo->commit();

        $advertencias = [];
        foreach ($vendidos as $v) {
            $stockNuevo = $v['producto']['stock'] - $v['cantidad'];
            $aviso = avisarStockBajo($v['producto']['nombre'], $stockNuevo, $v['producto']['stock_minimo']);
            if ($aviso) {
                $advertencias[] = $v['producto']['nombre'] . ': ' . $aviso;
            }
        }

        $respuesta = ['venta' => buscarVenta($pdo, $ventaId)];
        if ($advertencias) {
            $respuesta['advertencia'] = implode('. ', $advertencias);
        }
        responder($respuesta, 201);
    }

    if ($metodo === 'PUT') {
        if ($id === null) {
            responder(['error' => 'Falta el id de la venta'], 400);
        }
        $venta = buscarVenta($pdo, $id);
        if (!$venta) {
            responder(['error' => 'Venta no encontrada'], 404);
        }
        if ($venta['estado'] === 'anulada') {
            responder(['error' => 'La venta ya estaba anulada'], 409);
        }

        $pdo->beginTransaction();
        $stmt = $pdo->prepare('UPDATE productos SET stock = stock + ? WHERE id = ?');
        foreach ($venta['items'] as $item) {
            if ($item['producto_id'] !== null) {
                $stmt->execute([$item['cantidad'], $item['producto_id']]);
            }
        }
        $stmt = $pdo->prepare("UPDATE ventas SET estado = 'anulada' WHERE id = ?");
        $stmt->execute([$id]);
        $pdo->commit();

        responder(['venta' => buscarVenta($pdo, $id)]);
    }

    responder(['error' => 'Método no permitido'], 405);
} catch (Exception $e) {
    if (isset($pdo) && $pdo->inTransaction()) {
        $pdo->rollBack();
    }
    error_log('ventas.php: ' . $e->getMessage());
    responder(['error' => 'Error en el servidor'], 500);
}
