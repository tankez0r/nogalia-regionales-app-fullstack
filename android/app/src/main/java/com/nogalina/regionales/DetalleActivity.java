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

public class DetalleActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";

    public static class Datos extends AndroidViewModel {
        final Pedido<Producto> producto;
        final Pedido<Object> borrar;
        boolean huboCambios;

        public Datos(@NonNull Application app) {
            super(app);
            producto = new Pedido<>(app);
            borrar = new Pedido<>(app);
        }
    }

    private Datos datos;
    private int id;

    private final ActivityResultLauncher<Intent> editar = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == RESULT_OK) {
                    datos.huboCambios = true;
                    setResult(RESULT_OK);
                    datos.producto.recargar(Api.nogalina.producto(id));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        id = getIntent().getIntExtra(EXTRA_ID, -1);
        datos = new ViewModelProvider(this).get(Datos.class);
        if (datos.huboCambios) {
            setResult(RESULT_OK);
        }

        View contenido = findViewById(R.id.contenido);
        View progreso = findViewById(R.id.progreso);
        TextView estado = findViewById(R.id.estado);
        Button reintentar = findViewById(R.id.reintentar);
        Button botonEditar = findViewById(R.id.editar);
        Button botonBorrar = findViewById(R.id.borrar);

        reintentar.setOnClickListener(v -> datos.producto.recargar(Api.nogalina.producto(id)));
        botonEditar.setOnClickListener(v -> editar.launch(new Intent(this, FormularioActivity.class)
                .putExtra(FormularioActivity.EXTRA_PRODUCTO, datos.producto.datos.getValue())));
        botonBorrar.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setMessage(R.string.confirmar_borrar)
                .setPositiveButton(R.string.borrar, (d, w) -> datos.borrar.enviar(Api.nogalina.borrarProducto(id)))
                .setNegativeButton(R.string.cancelar, null)
                .show());

        datos.producto.cargando.observe(this, c -> progreso.setVisibility(c ? View.VISIBLE : View.GONE));
        datos.producto.datos.observe(this, p -> {
            contenido.setVisibility(p != null ? View.VISIBLE : View.GONE);
            if (p != null) {
                mostrar(p);
            }
        });
        datos.producto.error.observe(this, e -> {
            estado.setText(e);
            estado.setVisibility(e != null ? View.VISIBLE : View.GONE);
            reintentar.setVisibility(e != null ? View.VISIBLE : View.GONE);
        });

        datos.borrar.cargando.observe(this, c -> {
            botonEditar.setEnabled(!c);
            botonBorrar.setEnabled(!c);
        });
        datos.borrar.datos.observe(this, r -> {
            if (r != null) {
                Toast.makeText(this, R.string.producto_borrado, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
        datos.borrar.error.observe(this, e -> {
            if (e != null) {
                Toast.makeText(this, e, Toast.LENGTH_LONG).show();
                datos.borrar.limpiar();
            }
        });

        datos.producto.primeraVez(Api.nogalina.producto(id));
    }

    private void mostrar(Producto p) {
        setTitle(p.nombre);
        ((TextView) findViewById(R.id.nombre)).setText(p.nombre);
        ((TextView) findViewById(R.id.categoria)).setText(getString(R.string.detalle_categoria, p.categoria));
        ((TextView) findViewById(R.id.precio)).setText(getString(R.string.detalle_precio, p.precio));
        ((TextView) findViewById(R.id.stock)).setText(getString(R.string.detalle_stock, p.stock));
        ((TextView) findViewById(R.id.stockMinimo)).setText(getString(R.string.detalle_stock_minimo, p.stockMinimo));
        ((TextView) findViewById(R.id.unidad)).setText(getString(R.string.detalle_unidad, vacio(p.unidad)));
        ((TextView) findViewById(R.id.origen)).setText(getString(R.string.detalle_origen, vacio(p.origen)));
        ((TextView) findViewById(R.id.descripcion)).setText(vacio(p.descripcion));
        findViewById(R.id.avisoBajo).setVisibility(p.bajoStock() ? View.VISIBLE : View.GONE);
    }

    private String vacio(String texto) {
        return texto == null || texto.isEmpty() ? getString(R.string.sin_dato) : texto;
    }
}
