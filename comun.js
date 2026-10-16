function mostrarMensaje(texto, esError) {
  const div = document.createElement('div');
  div.className = esError ? 'error' : 'ok';
  div.textContent = texto;
  document.getElementById('mensajes').appendChild(div);
  setTimeout(function () {
    div.remove();
  }, 6000);
}

// Los productos se muestran con innerHTML, así que si alguien guarda un nombre con un <script> el
// navegador lo ejecutaría. escapar() lo pasa a texto común y se ve tal cual, sin ejecutarse. Esto es
// para evitar ataques XSS.
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
