package com.nogalina.regionales;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VentasActivity extends AppCompatActivity {

    public static class Datos extends AndroidViewModel {
        final Pedido<List<Venta>> ventas;
        final Pedido<Venta.Respuesta> anular;
        boolean huboCambios;

        public Datos(@NonNull Application app) {
            super(app);
            ventas = new Pedido<>(app);
            anular = new Pedido<>(app);
        }
    }

    private Datos datos;

    private final ActivityResultLauncher<Intent> nuevaVenta = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == RESULT_OK) {
                    huboCambios();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventas);
        setTitle(R.string.ventas);

        datos = new ViewModelProvider(this).get(Datos.class);
        if (datos.huboCambios) {
            setResult(RESULT_OK);
        }

        View progreso = findViewById(R.id.progreso);
        TextView estado = findViewById(R.id.estado);
        Button reintentar = findViewById(R.id.reintentar);

        VentasAdapter adapter = new VentasAdapter(venta -> new AlertDialog.Builder(this)
                .setMessage(R.string.confirmar_anular)
                .setPositiveButton(R.string.anular, (d, w) -> datos.anular.enviar(Api.nogalina.anularVenta(venta.id)))
                .setNegativeButton(R.string.cancelar, null)
                .show());
        RecyclerView lista = findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adapter);

        findViewById(R.id.nuevaVenta).setOnClickListener(v ->
                nuevaVenta.launch(new Intent(this, NuevaVentaActivity.class)));
        reintentar.setOnClickListener(v -> datos.ventas.recargar(Api.nogalina.ventas()));

        datos.ventas.cargando.observe(this, c -> progreso.setVisibility(c ? View.VISIBLE : View.GONE));
        datos.ventas.datos.observe(this, ventas -> {
            adapter.mostrar(ventas);
            if (ventas != null && ventas.isEmpty()) {
                estado.setText(R.string.sin_ventas);
            }
            estado.setVisibility(ventas != null && ventas.isEmpty() ? View.VISIBLE : View.GONE);
        });
        datos.ventas.error.observe(this, e -> {
            if (e != null) {
                estado.setText(e);
                estado.setVisibility(View.VISIBLE);
            }
            reintentar.setVisibility(e != null ? View.VISIBLE : View.GONE);
        });

        datos.anular.cargando.observe(this, adapter::setBloqueado);
        datos.anular.datos.observe(this, r -> {
            if (r != null) {
                Toast.makeText(this, R.string.venta_anulada, Toast.LENGTH_SHORT).show();
                datos.anular.limpiar();
                huboCambios();
            }
        });
        datos.anular.error.observe(this, e -> {
            if (e != null) {
                Toast.makeText(this, e, Toast.LENGTH_LONG).show();
                datos.anular.limpiar();
            }
        });

        datos.ventas.primeraVez(Api.nogalina.ventas());
    }

    private void huboCambios() {
        datos.huboCambios = true;
        setResult(RESULT_OK);
        datos.ventas.recargar(Api.nogalina.ventas());
    }
}
