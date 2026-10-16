-- Base de datos de Nogalina Regionales
-- Se puede correr entero desde phpMyAdmin (pestaña SQL) o con:
--   mysql -u root -p < sql/schema.sql

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

-- cabecera de cada venta
CREATE TABLE IF NOT EXISTS ventas (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  total DECIMAL(10,2) NOT NULL DEFAULT 0,
  estado ENUM('completada', 'anulada') NOT NULL DEFAULT 'completada',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- productos de cada venta. Guardamos el nombre y el precio del momento
-- para que el historial no cambie si después se edita o se borra el producto.
CREATE TABLE IF NOT EXISTS detalle_venta (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  venta_id INT UNSIGNED NOT NULL,
  producto_id INT UNSIGNED NULL,
  producto_nombre VARCHAR(120) NOT NULL,
  cantidad INT UNSIGNED NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE,
  FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- algunos productos de ejemplo
INSERT INTO productos (nombre, categoria, precio, stock, stock_minimo, unidad, descripcion, origen) VALUES
('Miel de la sierra', 'Miel', 2500.00, 12, 5, 'kg', 'Miel pura de la zona', 'Famatina'),
('Nueces peladas', 'Frutos secos', 4200.00, 8, 3, 'kg', 'Nueces de la cosecha', 'Famatina'),
('Aceite de oliva', 'Aceites', 3800.00, 3, 5, 'botella', 'Primera prensada', 'Chilecito'),
('Dulce de membrillo', 'Dulces', 1800.00, 20, 6, 'unidad', 'Casero', 'Famatina'),
('Vino patero', 'Vinos', 1500.00, 2, 4, 'botella', 'Artesanal de la finca', 'Famatina');
