package com.nogalina.regionales;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static class Datos extends AndroidViewModel {
        final Pedido<List<Producto>> productos;
        final Pedido<Clima> clima;
        final Pedido<List<Notificacion>> nuevas;

        public Datos(@NonNull Application app) {
            super(app);
            productos = new Pedido<>(app);
            clima = new Pedido<>(app);
            nuevas = new Pedido<>(app);
        }
    }

    private Datos datos;
    private EditText filtroCategoria;
    private EditText filtroBusqueda;
    private CheckBox filtroBajo;
    private TextView estado;
    private Button reintentar;
    private int cantidadNuevas;

    private final ActivityResultLauncher<Intent> abrir = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == RESULT_OK) {
                    cargar();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        datos = new ViewModelProvider(this).get(Datos.class);
        filtroCategoria = findViewById(R.id.filtroCategoria);
        filtroBusqueda = findViewById(R.id.filtroBusqueda);
        filtroBajo = findViewById(R.id.filtroBajo);
        estado = findViewById(R.id.estado);
        reintentar = findViewById(R.id.reintentar);
        View progreso = findViewById(R.id.progreso);
        TextView clima = findViewById(R.id.clima);

        ProductosAdapter adapter = new ProductosAdapter(producto -> abrir.launch(
                new Intent(this, DetalleActivity.class).putExtra(DetalleActivity.EXTRA_ID, producto.id)));
        RecyclerView lista = findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adapter);

        findViewById(R.id.buscar).setOnClickListener(v -> cargar());
        filtroBajo.setOnClickListener(v -> cargar());
        reintentar.setOnClickListener(v -> cargar());
        TextView.OnEditorActionListener alBuscar = (v, accion, evento) -> {
            cargar();
            return false;
        };
        filtroCategoria.setOnEditorActionListener(alBuscar);
        filtroBusqueda.setOnEditorActionListener(alBuscar);

        datos.productos.cargando.observe(this, c -> progreso.setVisibility(c ? View.VISIBLE : View.GONE));
        datos.productos.datos.observe(this, productos -> {
            adapter.mostrar(productos);
            mostrarEstado();
        });
        datos.productos.error.observe(this, e -> mostrarEstado());

        datos.clima.datos.observe(this, c -> {
            if (c != null) {
                clima.setText(textoClima(c));
            }
        });
        datos.clima.error.observe(this, e -> {
            if (e != null) {
                clima.setText(R.string.clima_error);
            }
        });

        datos.nuevas.datos.observe(this, nuevas -> {
            cantidadNuevas = nuevas == null ? 0 : nuevas.size();
            invalidateOptionsMenu();
        });

        datos.productos.primeraVez(Api.nogalina.productos(null, null, null));
        datos.clima.primeraVez(Api.openMeteo.clima());
        datos.nuevas.primeraVez(Api.nogalina.notificaciones(1, 1));
    }

    private void cargar() {
        String categoria = filtroCategoria.getText().toString().trim();
        String busqueda = filtroBusqueda.getText().toString().trim();
        datos.productos.recargar(Api.nogalina.productos(
                categoria.isEmpty() ? null : categoria,
                busqueda.isEmpty() ? null : busqueda,
                filtroBajo.isChecked() ? 1 : null));
        datos.nuevas.recargar(Api.nogalina.notificaciones(1, 1));
    }

    private void mostrarEstado() {
        String error = datos.productos.error.getValue();
        List<Producto> productos = datos.productos.datos.getValue();

        if (error != null) {
            estado.setText(error);
        } else if (productos != null && productos.isEmpty()) {
            estado.setText(R.string.sin_productos);
        }
        estado.setVisibility(error != null || (productos != null && productos.isEmpty()) ? View.VISIBLE : View.GONE);
        reintentar.setVisibility(error != null ? View.VISIBLE : View.GONE);
    }

    private String textoClima(Clima c) {
        if (c.current == null || c.daily == null || c.daily.minima == null || c.daily.minima.length == 0
                || c.daily.maxima == null || c.daily.maxima.length == 0) {
            return getString(R.string.clima_error);
        }
        return getString(R.string.clima_texto,
                getString(descripcionClima(c.current.codigo, c.current.esDeDia == 1)),
                Math.round(c.current.temperatura),
                Math.round(c.daily.minima[0]),
                Math.round(c.daily.maxima[0]),
                Math.round(c.current.viento));
    }

    private int descripcionClima(int codigo, boolean esDeDia) {
        if (codigo == 0) return esDeDia ? R.string.clima_despejado_dia : R.string.clima_despejado_noche;
        if (codigo <= 2) return esDeDia ? R.string.clima_parcial_dia : R.string.clima_parcial_noche;
        if (codigo == 3) return R.string.clima_nublado;
        if (codigo <= 48) return R.string.clima_niebla;
        if (codigo <= 57) return R.string.clima_llovizna;
        if (codigo <= 67) return R.string.clima_lluvia;
        if (codigo <= 77) return R.string.clima_nieve;
        if (codigo <= 82) return R.string.clima_chaparrones;
        if (codigo <= 86) return R.string.clima_nevadas;
        return R.string.clima_tormenta;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menuNotificaciones).setTitle(cantidadNuevas == 0
                ? getString(R.string.notificaciones)
                : getResources().getQuantityString(R.plurals.notificaciones_nuevas, cantidadNuevas, cantidadNuevas));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuNuevo) {
            abrir.launch(new Intent(this, FormularioActivity.class));
        } else if (id == R.id.menuVentas) {
            abrir.launch(new Intent(this, VentasActivity.class));
        } else if (id == R.id.menuNotificaciones) {
            abrir.launch(new Intent(this, NotificacionesActivity.class));
        } else if (id == R.id.menuMapa) {
            startActivity(new Intent(this, MapaActivity.class));
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
