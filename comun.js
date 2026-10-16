function mostrarMensaje(texto, esError) {
  const div = document.createElement('div');
  div.className = esError ? 'error' : 'ok';
  div.textContent = texto;
  document.getElementById('mensajes').appendChild(div);
  setTimeout(function () {
    div.remove();
  }, 6000);
}

function escapar(texto) {
  const div = document.createElement('div');
  div.textContent = texto;
  return div.innerHTML;
}

function textoError(cuerpo, porDefecto) {
  if (cuerpo.detalle) {
    return Object.values(cuerpo.detalle).join('. ');
  }
  return cuerpo.error || porDefecto;
}
