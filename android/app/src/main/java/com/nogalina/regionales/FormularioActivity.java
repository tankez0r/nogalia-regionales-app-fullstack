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

import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

public class FormularioActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCTO = "producto";

    public static class Datos extends AndroidViewModel {
        final Pedido<Api.RespuestaProducto> guardar;

        public Datos(@NonNull Application app) {
            super(app);
            guardar = new Pedido<>(app);
        }
    }

    private Datos datos;
    private Producto original;
    private final Map<String, TextInputLayout> campos = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);

        datos = new ViewModelProvider(this).get(Datos.class);
        original = (Producto) getIntent().getSerializableExtra(EXTRA_PRODUCTO);
        setTitle(original == null ? R.string.nuevo_producto : R.string.editar_producto);

        campos.put("nombre", findViewById(R.id.campoNombre));
        campos.put("categoria", findViewById(R.id.campoCategoria));
        campos.put("precio", findViewById(R.id.campoPrecio));
        campos.put("stock", findViewById(R.id.campoStock));
        campos.put("stock_minimo", findViewById(R.id.campoStockMinimo));
        campos.put("unidad", findViewById(R.id.campoUnidad));
        campos.put("origen", findViewById(R.id.campoOrigen));
        campos.put("descripcion", findViewById(R.id.campoDescripcion));

        if (savedInstanceState == null && original != null) {
            poner("nombre", original.nombre);
            poner("categoria", original.categoria);
            poner("precio", String.valueOf(original.precio));
            poner("stock", String.valueOf(original.stock));
            poner("stock_minimo", String.valueOf(original.stockMinimo));
            poner("unidad", original.unidad);
            poner("origen", original.origen);
            poner("descripcion", original.descripcion);
        }

        Button guardar = findViewById(R.id.guardar);
        View progreso = findViewById(R.id.progreso);
        TextView error = findViewById(R.id.error);

        guardar.setOnClickListener(v -> {
            Producto p = leer();
            if (p != null) {
                datos.guardar.enviar(original == null
                        ? Api.nogalina.crearProducto(p)
                        : Api.nogalina.editarProducto(original.id, p));
            }
        });

        datos.guardar.cargando.observe(this, c -> {
            guardar.setEnabled(!c);
            progreso.setVisibility(c ? View.VISIBLE : View.GONE);
        });
        datos.guardar.detalle.observe(this, detalle -> {
            if (detalle != null) {
                for (Map.Entry<String, String> e : detalle.entrySet()) {
                    TextInputLayout campo = campos.get(e.getKey());
                    if (campo != null) {
                        campo.setError(e.getValue());
                    }
                }
            }
        });
        datos.guardar.error.observe(this, e -> {
            error.setText(e);
            error.setVisibility(e != null ? View.VISIBLE : View.GONE);
        });
        datos.guardar.datos.observe(this, r -> {
            if (r != null) {
                Toast.makeText(this, original == null ? R.string.producto_creado : R.string.producto_actualizado,
                        Toast.LENGTH_SHORT).show();
                if (r.advertencia != null) {
                    Toast.makeText(this, getString(R.string.aviso, r.advertencia), Toast.LENGTH_LONG).show();
                }
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private Producto leer() {
        for (TextInputLayout campo : campos.values()) {
            campo.setError(null);
        }

        Producto p = new Producto();
        p.nombre = texto("nombre");
        p.categoria = texto("categoria");
        p.unidad = texto("unidad");
        p.origen = texto("origen");
        p.descripcion = texto("descripcion");
        boolean ok = true;

        if (p.nombre.isEmpty()) {
            campos.get("nombre").setError(getString(R.string.falta_nombre));
            ok = false;
        }
        if (p.categoria.isEmpty()) {
            campos.get("categoria").setError(getString(R.string.falta_categoria));
            ok = false;
        }
        try {
            p.precio = Double.parseDouble(texto("precio").replace(',', '.'));
            if (p.precio < 0 || !Double.isFinite(p.precio)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            campos.get("precio").setError(getString(R.string.precio_invalido));
            ok = false;
        }
        Integer stock = entero("stock");
        if (stock == null) {
            campos.get("stock").setError(getString(R.string.stock_invalido));
            ok = false;
        } else {
            p.stock = stock;
        }
        Integer minimo = entero("stock_minimo");
        if (minimo == null) {
            campos.get("stock_minimo").setError(getString(R.string.stock_minimo_invalido));
            ok = false;
        } else {
            p.stockMinimo = minimo;
        }

        return ok ? p : null;
    }

    private Integer entero(String campo) {
        try {
            int valor = Integer.parseInt(texto(campo));
            return valor >= 0 ? valor : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String texto(String campo) {
        return campos.get(campo).getEditText().getText().toString().trim();
    }

    private void poner(String campo, String valor) {
        campos.get(campo).getEditText().setText(valor == null ? "" : valor);
    }
}
