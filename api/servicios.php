<?php

// Se hace lectura de las variables de entorno de RESEND_API_KEY y MAIL_TO. Resend se llama desde el
// servidor porque la clave da permiso para mandar mails a nombre de nuestra cuenta. Si estuviera en
// JavaScript, cualquiera podría verla con herramientas de navegación y usarla para crear spam a
// nuestro nombre. Si falta la clave o el destinatario la función devuelve false antes de pedir nada
// a Resend. Así no hace un pedido que puede fallar con seguridad, quien llamó a la función lo toma
// como "no se pudo enviar. El resto de la función usa curl, que es la forma que tiene PHP de hacer un
// pedido HTTP a otro servidor. Manda un POST con el mail en formato JSON y la clave en el encabezado
// como un Bearer token, que es como Resend sabe que somos nosotros.
function enviarEmail($asunto, $html)
{
    $apiKey = getenv('RESEND_API_KEY');
    $destino = getenv('MAIL_TO');

    if (!$apiKey || !$destino) {
        return false;
    }

    $cuerpo = json_encode([
        'from' => 'Nogalina Regionales <onboarding@resend.dev>',
        'to' => [$destino],
        'subject' => $asunto,
        'html' => $html,
    ]);

    $ch = curl_init('https://api.resend.com/emails');
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $cuerpo);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Authorization: Bearer ' . $apiKey,
        'Content-Type: application/json',
    ]);
    // Sin este límite, si Resend está caído o muy lento, la app se queda esperando mucho tiempo.
    // Mientras tanto, el usuario que ve "guardar" o "registrar venta" no termina. Con 10 segundos de
    // reintentar, curl abandona y enviarEmail() devuelve un false. Y con esto la API responde de igual
    // manera: guardar el producto o venta y manda la advertencia. Esto lo aplicamos de esta es parte
    // del requisito del requisito 5, que nos pide degradación elegante. Si los servicios externos
    // fallan, la app no se cae.
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);

    $respuesta = curl_exec($ch);
    $codigo = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    return $respuesta !== false && $codigo >= 200 && $codigo < 300;
}

// El stock está por encima del mínimo. Devuelve null y no manda nada porque no hay nada que avisar.
// Si en cambio está en el mínimo o por debajo, arma el mail en HTML. El nombre del producto pasa por
// htmlspecialchars, así un nombre con caracteres como < o > no rompe el HTML del mail ni mete código.
// Devuelve un aviso que es un texto cuando el mail no salió y null cuando salió bien o no hacía
// falta. Lo hicimos así, en lugar de devolver un boolean, para que productos.php y ventas.php puedan
// poner ese texto directamente en el campo advertencia de la respuesta. El frontend lo muestra sin
// tener que armar el mensaje.
function avisarStockBajo($nombre, $stock, $minimo)
{
    if ($stock > $minimo) {
        return null;
    }

    $html = '<h2>Stock bajo: ' . htmlspecialchars($nombre) . '</h2>'
        . '<p>Quedan ' . $stock . ' unidades (el mínimo es ' . $minimo . ').</p>';

    if (!enviarEmail('Stock bajo: ' . $nombre, $html)) {
        return 'No se pudo enviar el aviso de stock bajo por mail';
    }
    return null;
}
