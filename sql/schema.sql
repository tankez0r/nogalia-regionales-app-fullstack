CREATE DATABASE IF NOT EXISTS nogalina CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE nogalina;

CREATE TABLE IF NOT EXISTS productos (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(120) NOT NULL,
  categoria VARCHAR(60) NOT NULL,
  precio DECIMAL(10,2) NOT NULL,
  stock INT UNSIGNED NOT NULL,
  stock_minimo INT UNSIGNED NOT NULL,
  unidad VARCHAR(20) NOT NULL DEFAULT '',
  descripcion TEXT NULL,
  origen VARCHAR(120) NOT NULL DEFAULT '',
  fecha_alta TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ventas (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  total DECIMAL(10,2) NOT NULL DEFAULT 0,
  estado ENUM('completada', 'anulada') NOT NULL DEFAULT 'completada',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS detalle_venta (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  venta_id INT UNSIGNED NOT NULL,
  producto_id INT UNSIGNED NULL,
  producto_nombre VARCHAR(120) NOT NULL,
  cantidad INT UNSIGNED NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (id),
  -- Si se borra una venta se borran también sus detalles (CASCADE). Si se borra un producto, el
  -- detalle queda con producto_id en null (SET NULL) y el historial no se pierde porque ya tiene
  -- guardado el nombre.
  FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE,
  FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notificaciones (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  producto_id INT UNSIGNED NULL,
  mensaje VARCHAR(255) NOT NULL,
  leida TINYINT(1) NOT NULL DEFAULT 0,
  fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO productos (nombre, categoria, precio, stock, stock_minimo, unidad, descripcion, origen) VALUES
('Miel de la sierra', 'Miel', 2500.00, 12, 5, 'kg', 'Miel pura de la zona', 'Famatina'),
('Nueces peladas', 'Frutos secos', 4200.00, 8, 3, 'kg', 'Nueces de la cosecha', 'Famatina'),
('Aceite de oliva', 'Aceites', 3800.00, 3, 5, 'botella', 'Primera prensada', 'Chilecito'),
('Dulce de membrillo', 'Dulces', 1800.00, 20, 6, 'unidad', 'Casero', 'Famatina'),
('Vino patero', 'Vinos', 1500.00, 2, 4, 'botella', 'Artesanal de la finca', 'Famatina');
