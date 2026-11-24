package com.nogalina.regionales;

import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;

public class NotificacionesActivity extends AppCompatActivity {

    public static class Datos extends AndroidViewModel {
        final Pedido<List<Notificacion>> notificaciones;
        final Pedido<Object> marcar;
        boolean verTodas;
        boolean huboCambios;

        public Datos(@NonNull Application app) {
            super(app);
            notificaciones = new Pedido<>(app);
            marcar = new Pedido<>(app);
        }

        Call<List<Notificacion>> pedir() {
            return verTodas
                    ? Api.nogalina.notificaciones(null, null)
                    : Api.nogalina.notificaciones(1, 1);
        }
    }

    private Datos datos;
    private Button cambiarVista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);
        setTitle(R.string.notificaciones);

        datos = new ViewModelProvider(this).get(Datos.class);
        if (datos.huboCambios) {
            setResult(RESULT_OK);
        }

        View progreso = findViewById(R.id.progreso);
        TextView estado = findViewById(R.id.estado);
        Button reintentar = findViewById(R.id.reintentar);
        cambiarVista = findViewById(R.id.cambiarVista);
        mostrarBoton();

        NotificacionesAdapter adapter = new NotificacionesAdapter(
                n -> datos.marcar.enviar(Api.nogalina.marcarLeida(n.id)));
        RecyclerView lista = findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adapter);

        cambiarVista.setOnClickListener(v -> {
            datos.verTodas = !datos.verTodas;
            mostrarBoton();
            datos.notificaciones.recargar(datos.pedir());
        });
        reintentar.setOnClickListener(v -> datos.notificaciones.recargar(datos.pedir()));

        datos.notificaciones.cargando.observe(this, c -> progreso.setVisibility(c ? View.VISIBLE : View.GONE));
        datos.notificaciones.datos.observe(this, notificaciones -> {
            adapter.mostrar(notificaciones);
            if (notificaciones != null && notificaciones.isEmpty()) {
                estado.setText(datos.verTodas ? R.string.sin_notificaciones : R.string.sin_notificaciones_nuevas);
            }
            estado.setVisibility(notificaciones != null && notificaciones.isEmpty() ? View.VISIBLE : View.GONE);
        });
        datos.notificaciones.error.observe(this, e -> {
            if (e != null) {
                estado.setText(e);
                estado.setVisibility(View.VISIBLE);
            }
            reintentar.setVisibility(e != null ? View.VISIBLE : View.GONE);
        });

        datos.marcar.datos.observe(this, r -> {
            if (r != null) {
                datos.marcar.limpiar();
                datos.huboCambios = true;
                setResult(RESULT_OK);
                datos.notificaciones.recargar(datos.pedir());
            }
        });
        datos.marcar.error.observe(this, e -> {
            if (e != null) {
                Toast.makeText(this, e, Toast.LENGTH_LONG).show();
                datos.marcar.limpiar();
            }
        });

        datos.notificaciones.primeraVez(datos.pedir());
    }

    private void mostrarBoton() {
        cambiarVista.setText(datos.verTodas ? R.string.ver_recientes : R.string.ver_todas);
    }
}
