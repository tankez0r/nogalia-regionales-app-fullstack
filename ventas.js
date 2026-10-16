const API_VENTAS = 'api/ventas.php';
const API_PRODUCTOS = 'api/productos.php';

let productos = [];
let carrito = [];

document.addEventListener('DOMContentLoaded', function () {
  cargarProductos();
  cargarVentas();
  document.getElementById('btn-agregar').addEventListener('click', agregarAlCarrito);
  document.getElementById('btn-registrar').addEventListener('click', registrarVenta);
});

async function cargarProductos() {
  try {
    const res = await fetch(API_PRODUCTOS);
    if (!res.ok) {
      mostrarMensaje('No se pudieron cargar los productos', true);
      return;
    }
    productos = await res.json();

    const select = document.getElementById('producto-select');
    select.innerHTML = '<option value="">Elegir producto</option>';
    for (const p of productos) {
      const opcion = document.createElement('option');
      opcion.value = p.id;
      opcion.textContent = p.nombre + ' (stock ' + p.stock + ')';
      if (Number(p.stock) <= 0) opcion.disabled = true;
      select.appendChild(opcion);
    }
  } catch (e) {
    mostrarMensaje('No se pudo conectar con el servidor', true);
  }
}

function agregarAlCarrito() {
  const id = Number(document.getElementById('producto-select').value);
  const cantidad = Number(document.getElementById('cantidad').value);

  if (!id) {
    mostrarMensaje('Elegí un producto', true);
    return;
  }
  if (!Number.isInteger(cantidad) || cantidad <= 0) {
    mostrarMensaje('La cantidad tiene que ser un número entero mayor a 0', true);
    return;
  }

  const producto = productos.find((p) => Number(p.id) === id);
  const enCarrito = carrito.find((c) => c.producto_id === id);

  if (enCarrito) {
    enCarrito.cantidad += cantidad;
  } else {
    carrito.push({
      producto_id: id,
      nombre: producto.nombre,
      precio: Number(producto.precio),
      cantidad: cantidad,
    });
  }

  document.getElementById('cantidad').value = '';
  mostrarCarrito();
}

function quitarDelCarrito(id) {
  carrito = carrito.filter((c) => c.producto_id !== id);
  mostrarCarrito();
}

function mostrarCarrito() {
  const cuerpo = document.getElementById('cuerpo-carrito');
  cuerpo.innerHTML = '';
  let total = 0;

  for (const item of carrito) {
    const subtotal = item.precio * item.cantidad;
    total += subtotal;
    const fila = document.createElement('tr');
    fila.innerHTML = `
      <td>${escapar(item.nombre)}</td>
      <td>${item.cantidad}</td>
      <td>$${item.precio.toFixed(2)}</td>
      <td>$${subtotal.toFixed(2)}</td>
      <td><button type="button" onclick="quitarDelCarrito(${item.producto_id})">Quitar</button></td>`;
    cuerpo.appendChild(fila);
  }

  document.getElementById('total').textContent = '$' + total.toFixed(2);
}

async function registrarVenta() {
  if (carrito.length === 0) {
    mostrarMensaje('El carrito está vacío', true);
    return;
  }

  const items = carrito.map((c) => ({ producto_id: c.producto_id, cantidad: c.cantidad }));

  try {
    const res = await fetch(API_VENTAS, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ items: items }),
    });
    const cuerpo = await res.json();

    if (!res.ok) {
      mostrarMensaje(textoError(cuerpo, 'No se pudo registrar la venta'), true);
      return;
    }

    mostrarMensaje('Venta registrada', false);
    if (cuerpo.advertencia) mostrarMensaje('Aviso: ' + cuerpo.advertencia, true);
    carrito = [];
    mostrarCarrito();
    cargarProductos();
    cargarVentas();
  } catch (e) {
    mostrarMensaje('No se pudo conectar con el servidor', true);
  }
}

async function cargarVentas() {
  try {
    const res = await fetch(API_VENTAS);
    if (!res.ok) {
      mostrarMensaje('No se pudo cargar el historial de ventas', true);
      return;
    }
    const ventas = await res.json();
    const cuerpo = document.getElementById('cuerpo-ventas');
    cuerpo.innerHTML = '';

    if (ventas.length === 0) {
      cuerpo.innerHTML = '<tr><td colspan="5">Todavía no hay ventas.</td></tr>';
      return;
    }

    for (const v of ventas) {
      const detalle = v.items.map((i) => escapar(i.producto_nombre) + ' x' + i.cantidad).join(', ');
      const fila = document.createElement('tr');
      if (v.estado === 'anulada') fila.className = 'bajo-stock';
      fila.innerHTML = `
        <td>${escapar(v.fecha)}</td>
        <td>$${Number(v.total).toFixed(2)}</td>
        <td>${escapar(v.estado)}</td>
        <td>${detalle}</td>
        <td>${v.estado === 'completada' ? `<button type="button" onclick="anularVenta(${v.id})">Anular</button>` : ''}</td>`;
      cuerpo.appendChild(fila);
    }
  } catch (e) {
    mostrarMensaje('No se pudo conectar con el servidor', true);
  }
}

async function anularVenta(id) {
  if (!confirm('¿Anular esta venta? El stock vuelve a los productos.')) return;

  try {
    const res = await fetch(API_VENTAS + '?id=' + id, { method: 'PUT' });
    const cuerpo = await res.json();
    if (!res.ok) {
      mostrarMensaje(textoError(cuerpo, 'No se pudo anular la venta'), true);
      return;
    }
    mostrarMensaje('Venta anulada', false);
    cargarProductos();
    cargarVentas();
  } catch (e) {
    mostrarMensaje('No se pudo conectar con el servidor', true);
  }
}
