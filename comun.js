// Funciones que usan las dos páginas (productos y ventas)

function mostrarMensaje(texto, esError) {
  const div = document.createElement('div');
  div.className = esError ? 'error' : 'ok';
  div.textContent = texto;
  document.getElementById('mensajes').appendChild(div);
  setTimeout(function () {
    div.remove();
  }, 6000);
}

// para no meter HTML que venga de la base directo en la página
function escapar(texto) {
  const div = document.createElement('div');
  div.textContent = texto;
  return div.innerHTML;
}

// arma el mensaje de error a partir de lo que devuelve la API
function textoError(cuerpo, porDefecto) {
  if (cuerpo.detalle) {
    return Object.values(cuerpo.detalle).join('. ');
  }
  return cuerpo.error || porDefecto;
}
