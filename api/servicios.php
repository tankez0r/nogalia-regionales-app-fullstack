<?php

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
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);

    $respuesta = curl_exec($ch);
    $codigo = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    return $respuesta !== false && $codigo >= 200 && $codigo < 300;
}

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
