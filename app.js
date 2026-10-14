const API = 'api/productos.php';

let productos = [];

document.addEventListener('DOMContentLoaded', function () {
  cargarProductos();
  cargarMapa();

  document.getElementById('form-producto').addEventListener('submit', guardarProducto);
  document.getElementById('btn-cancelar').addEventListener('click', limpiarFormulario);
  document.getElementById('filtro-categoria').addEventListener('input', cargarProductos);
  document.getElementById('filtro-q').addEventListener('input', cargarProductos);
  document.getElementById('filtro-bajo').addEventListener('change', cargarProductos);
});

async function cargarProductos() {
  const params = new URLSearchParams();
  const categoria = document.getElementById('filtro-categoria').value.trim();
  const q = document.getElementById('filtro-q').value.trim();

  if (categoria) params.set('categoria', categoria);
  if (q) params.set('q', q);
  if (document.getElementById('filtro-bajo').checked) params.set('bajo_stock', '1');

  try {
    const res = await fetch(API + '?' + params.toString());
    if (!res.ok) {
      mostrarMensaje('No se pudieron cargar los productos (error ' + res.status + ')', true);
      return;
    }
    productos = await res.json();
    mostrarTabla();
  } catch (e) {
    mostrarMensaje('No se pudo conectar con el servidor', true);
  }
}

function mostrarTabla() {
  const cuerpo = document.getElementById('cuerpo-tabla');
  cuerpo.innerHTML = '';

  if (productos.length === 0) {
    cuerpo.innerHTML = '<tr><td colspan="7">No hay productos para mostrar.</td></tr>';
    return;
  }

  for (const p of productos) {
    const fila = document.createElement('tr');
    // se marca en rojo si llegó al stock mínimo
    if (Number(p.stock) <= Number(p.stock_minimo)) {
      fila.className = 'bajo-stock';
    }
    fila.innerHTML = `
      <td>${escapar(p.nombre)}</td>
      <td>${escapar(p.categoria)}</td>
      <td>$${Number(p.precio).toFixed(2)}</td>
      <td>${p.stock}</td>
      <td>${p.stock_minimo}</td>
      <td>${escapar(p.origen || '')}</td>
      <td>
        <button type="button" onclick="editarProducto(${p.id})">Editar</button>
        <button type="button" onclick="borrarProducto(${p.id})">Borrar</button>
      </td>`;
    cuerpo.appendChild(fila);
  }
}

function editarProducto(id) {
  const p = productos.find((x) => Number(x.id) === id);
  if (!p) return;

  document.getElementById('producto-id').value = p.id;
  document.getElementById('nombre').value = p.nombre;
  document.getElementById('categoria').value = p.categoria;
  document.getElementById('precio').value = p.precio;
  document.getElementById('stock').value = p.stock;
  document.getElementById('stock_minimo').value = p.stock_minimo;
  document.getElementById('unidad').value = p.unidad || '';
  document.getElementById('origen').value = p.origen || '';
  document.getElementById('descripcion').value = p.descripcion || '';
  document.getElementById('form-titulo').textContent = 'Editar producto';
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

async function guardarProducto(ev) {
  ev.preventDefault();
  const id = document.getElementById('producto-id').value;

  const datos = {
    nombre: document.getElementById('nombre').value.trim(),
    categoria: document.getElementById('categoria').value.trim(),
    precio: document.getElementById('precio').value,
    stock: document.getElementById('stock').value,
    stock_minimo: document.getElementById('stock_minimo').value,
    unidad: document.getElementById('unidad').value.trim(),
    origen: document.getElementById('origen').value.trim(),
    descripcion: document.getElementById('descripcion').value.trim(),
  };

  // si hay id estamos editando, si no es un producto nuevo
  const metodo = id ? 'PUT' : 'POST';
  const url = id ? API + '?id=' + id : API;

  try {
    const res = await fetch(url, {
      method: metodo,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(datos),
    });
    const cuerpo = await res.json();

    if (!res.ok) {
      mostrarMensaje(textoError(cuerpo, 'No se pudo guardar el producto'), true);
      return;
    }

    mostrarMensaje(id ? 'Producto actualizado' : 'Producto creado', false);
    if (cuerpo.advertencia) mostrarMensaje('Aviso: ' + cuerpo.advertencia, true);
    limpiarFormulario();
    cargarProductos();
  } catch (e) {
    mostrarMensaje('No se pudo conectar con el servidor', true);
  }
}

async function borrarProducto(id) {
  if (!confirm('¿Borrar este producto?')) return;

  try {
    const res = await fetch(API + '?id=' + id, { method: 'DELETE' });
    const cuerpo = await res.json();
    if (!res.ok) {
      mostrarMensaje(textoError(cuerpo, 'No se pudo borrar el producto'), true);
      return;
    }
    mostrarMensaje('Producto borrado', false);
    cargarProductos();
  } catch (e) {
    mostrarMensaje('No se pudo conectar con el servidor', true);
  }
}

function limpiarFormulario() {
  document.getElementById('form-producto').reset();
  document.getElementById('producto-id').value = '';
  document.getElementById('form-titulo').textContent = 'Nuevo producto';
}

// Mapa con la ubicación de la tienda (Leaflet + OpenStreetMap)
function cargarMapa() {
  // si la librería no cargó (sin internet o el CDN caído) avisamos y el resto de la página sigue andando
  if (typeof L === 'undefined') {
    document.getElementById('mapa').textContent = 'No se pudo cargar el mapa.';
    return;
  }

  const famatina = [-28.9167, -67.5167];
  const mapa = L.map('mapa').setView(famatina, 14);

  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap',
  }).addTo(mapa);

  L.marker(famatina).addTo(mapa).bindPopup('Nogalina Regionales').openPopup();
}
