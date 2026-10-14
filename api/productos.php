<?php
require 'config.php';
require 'servicios.php';

$metodo = $_SERVER['REQUEST_METHOD'];
$id = isset($_GET['id']) ? $_GET['id'] : null;

if ($id !== null && !ctype_digit($id)) {
    responder(['error' => 'El id no es válido'], 400);
}

function buscarProducto($pdo, $id)
{
    $stmt = $pdo->prepare('SELECT * FROM productos WHERE id = ?');
    $stmt->execute([$id]);
    return $stmt->fetch();
}

function validarProducto($d)
{
    $errores = [];

    if (trim($d['nombre'] ?? '') === '') {
        $errores['nombre'] = 'El nombre es obligatorio';
    }
    if (trim($d['categoria'] ?? '') === '') {
        $errores['categoria'] = 'La categoría es obligatoria';
    }
    if (!isset($d['precio']) || !is_numeric($d['precio']) || $d['precio'] < 0) {
        $errores['precio'] = 'El precio tiene que ser un número mayor o igual a 0';
    }
    if (!isset($d['stock']) || filter_var($d['stock'], FILTER_VALIDATE_INT) === false || $d['stock'] < 0) {
        $errores['stock'] = 'El stock tiene que ser un entero mayor o igual a 0';
    }
    if (!isset($d['stock_minimo']) || filter_var($d['stock_minimo'], FILTER_VALIDATE_INT) === false || $d['stock_minimo'] < 0) {
        $errores['stock_minimo'] = 'El stock mínimo tiene que ser un entero mayor o igual a 0';
    }

    return $errores;
}

// arma los parámetros para el INSERT y el UPDATE
function datosProducto($d)
{
    return [
        ':nombre' => trim($d['nombre']),
        ':categoria' => trim($d['categoria']),
        ':precio' => $d['precio'],
        ':stock' => (int) $d['stock'],
        ':stock_minimo' => (int) $d['stock_minimo'],
        ':unidad' => trim($d['unidad'] ?? ''),
        ':descripcion' => trim($d['descripcion'] ?? ''),
        ':origen' => trim($d['origen'] ?? ''),
    ];
}

try {
    $pdo = conectar();

    if ($metodo === 'GET') {
        if ($id !== null) {
            $producto = buscarProducto($pdo, $id);
            if (!$producto) {
                responder(['error' => 'Producto no encontrado'], 404);
            }
            responder($producto);
        }

        // listado con filtros opcionales
        $sql = 'SELECT * FROM productos WHERE 1 = 1';
        $parametros = [];

        if (!empty($_GET['categoria'])) {
            $sql .= ' AND categoria = ?';
            $parametros[] = $_GET['categoria'];
        }
        if (!empty($_GET['q'])) {
            $sql .= ' AND (nombre LIKE ? OR descripcion LIKE ? OR origen LIKE ?)';
            $busqueda = '%' . $_GET['q'] . '%';
            $parametros[] = $busqueda;
            $parametros[] = $busqueda;
            $parametros[] = $busqueda;
        }
        if (!empty($_GET['bajo_stock'])) {
            $sql .= ' AND stock <= stock_minimo';
        }
        $sql .= ' ORDER BY id DESC';

        $stmt = $pdo->prepare($sql);
        $stmt->execute($parametros);
        responder($stmt->fetchAll());
    }

    if ($metodo === 'POST') {
        $datos = leerJson();
        $errores = validarProducto($datos);
        if ($errores) {
            responder(['error' => 'Datos inválidos', 'detalle' => $errores], 422);
        }

        $stmt = $pdo->prepare('INSERT INTO productos (nombre, categoria, precio, stock, stock_minimo, unidad, descripcion, origen)
            VALUES (:nombre, :categoria, :precio, :stock, :stock_minimo, :unidad, :descripcion, :origen)');
        $stmt->execute(datosProducto($datos));

        $respuesta = ['producto' => buscarProducto($pdo, $pdo->lastInsertId())];
        $aviso = avisarStockBajo(trim($datos['nombre']), (int) $datos['stock'], (int) $datos['stock_minimo']);
        if ($aviso) {
            $respuesta['advertencia'] = $aviso;
        }
        responder($respuesta, 201);
    }

    if ($metodo === 'PUT') {
        if ($id === null) {
            responder(['error' => 'Falta el id del producto'], 400);
        }
        $datos = leerJson();
        $errores = validarProducto($datos);
        if ($errores) {
            responder(['error' => 'Datos inválidos', 'detalle' => $errores], 422);
        }
        if (!buscarProducto($pdo, $id)) {
            responder(['error' => 'Producto no encontrado'], 404);
        }

        $parametros = datosProducto($datos);
        $parametros[':id'] = $id;
        $stmt = $pdo->prepare('UPDATE productos SET nombre = :nombre, categoria = :categoria, precio = :precio,
            stock = :stock, stock_minimo = :stock_minimo, unidad = :unidad, descripcion = :descripcion, origen = :origen
            WHERE id = :id');
        $stmt->execute($parametros);

        $respuesta = ['producto' => buscarProducto($pdo, $id)];
        $aviso = avisarStockBajo(trim($datos['nombre']), (int) $datos['stock'], (int) $datos['stock_minimo']);
        if ($aviso) {
            $respuesta['advertencia'] = $aviso;
        }
        responder($respuesta);
    }

    if ($metodo === 'DELETE') {
        if ($id === null) {
            responder(['error' => 'Falta el id del producto'], 400);
        }
        if (!buscarProducto($pdo, $id)) {
            responder(['error' => 'Producto no encontrado'], 404);
        }
        $stmt = $pdo->prepare('DELETE FROM productos WHERE id = ?');
        $stmt->execute([$id]);
        responder(['ok' => true]);
    }

    responder(['error' => 'Método no permitido'], 405);
} catch (Exception $e) {
    error_log('productos.php: ' . $e->getMessage());
    responder(['error' => 'Error en el servidor'], 500);
}
