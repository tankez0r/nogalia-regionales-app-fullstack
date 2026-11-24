package com.nogalina.regionales;

import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NuevaVentaActivity extends AppCompatActivity {

    public static class Datos extends AndroidViewModel {
        final Pedido<List<Producto>> productos;
        final Pedido<Venta.Respuesta> registrar;
        final MutableLiveData<List<Venta.Item>> carrito = new MutableLiveData<>(new ArrayList<>());

        public Datos(@NonNull Application app) {
            super(app);
            productos = new Pedido<>(app);
            registrar = new Pedido<>(app);
        }

        int enCarrito(int productoId) {
            for (Venta.Item item : carrito.getValue()) {
                if (item.productoId == productoId) {
                    return item.cantidad;
                }
            }
            return 0;
        }

        void agregar(Producto p, int cantidad) {
            List<Venta.Item> lista = carrito.getValue();
            for (Venta.Item item : lista) {
                if (item.productoId.equals(p.id)) {
                    item.cantidad += cantidad;
                    carrito.setValue(lista);
                    return;
                }
            }
            Venta.Item item = new Venta.Item(p.id, cantidad);
            item.productoNombre = p.nombre;
            item.precioUnitario = p.precio;
            lista.add(item);
            carrito.setValue(lista);
        }

        void quitar(Venta.Item item) {
            List<Venta.Item> lista = carrito.getValue();
            lista.remove(item);
            carrito.setValue(lista);
        }
    }

    private Datos datos;
    private final List<Producto> disponibles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_venta);
        setTitle(R.string.nueva_venta);

        datos = new ViewModelProvider(this).get(Datos.class);

        Spinner selector = findViewById(R.id.producto);
        EditText cantidad = findViewById(R.id.cantidad);
        TextView total = findViewById(R.id.total);
        TextView error = findViewById(R.id.error);
        TextView estado = findViewById(R.id.estado);
        Button reintentar = findViewById(R.id.reintentar);
        Button registrar = findViewById(R.id.registrar);
        View progreso = findViewById(R.id.progreso);

        CarritoAdapter adapter = new CarritoAdapter(datos::quitar);
        RecyclerView lista = findViewById(R.id.carrito);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adapter);

        reintentar.setOnClickListener(v -> datos.productos.recargar(Api.nogalina.productos(null, null, null)));

        findViewById(R.id.agregar).setOnClickListener(v -> {
            int posicion = selector.getSelectedItemPosition();
            if (posicion <= 0) {
                Toast.makeText(this, R.string.elegir_producto, Toast.LENGTH_SHORT).show();
                return;
            }
            int n;
            try {
                n = Integer.parseInt(cantidad.getText().toString().trim());
            } catch (NumberFormatException e) {
                n = 0;
            }
            if (n <= 0) {
                cantidad.setError(getString(R.string.cantidad_invalida));
                return;
            }
            Producto p = disponibles.get(posicion - 1);
            if (datos.enCarrito(p.id) + n > p.stock) {
                cantidad.setError(getString(R.string.sin_stock_suficiente, p.nombre, p.stock));
                return;
            }
            datos.agregar(p, n);
            cantidad.setText("");
        });

        registrar.setOnClickListener(v -> {
            if (datos.carrito.getValue().isEmpty()) {
                Toast.makeText(this, R.string.carrito_vacio, Toast.LENGTH_SHORT).show();
                return;
            }
            datos.registrar.enviar(Api.nogalina.registrarVenta(new Venta.Nueva(datos.carrito.getValue())));
        });

        datos.productos.cargando.observe(this, c -> progreso.setVisibility(c ? View.VISIBLE : View.GONE));
        datos.productos.datos.observe(this, productos -> {
            if (productos == null) {
                return;
            }
            disponibles.clear();
            List<String> nombres = new ArrayList<>();
            nombres.add(getString(R.string.elegir_producto));
            for (Producto p : productos) {
                if (p.stock > 0) {
                    disponibles.add(p);
                    nombres.add(getString(R.string.producto_con_stock, p.nombre, p.stock));
                }
            }
            int seleccion = selector.getSelectedItemPosition();
            ArrayAdapter<String> opciones = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombres);
            opciones.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            selector.setAdapter(opciones);
            if (seleccion > 0 && seleccion < nombres.size()) {
                selector.setSelection(seleccion);
            }
        });
        datos.productos.error.observe(this, e -> {
            estado.setText(e);
            estado.setVisibility(e != null ? View.VISIBLE : View.GONE);
            reintentar.setVisibility(e != null ? View.VISIBLE : View.GONE);
        });

        datos.carrito.observe(this, items -> {
            adapter.mostrar(items);
            double suma = 0;
            for (Venta.Item item : items) {
                suma += item.precioUnitario * item.cantidad;
            }
            total.setText(getString(R.string.total, suma));
        });

        datos.registrar.cargando.observe(this, c -> {
            registrar.setEnabled(!c);
            progreso.setVisibility(c ? View.VISIBLE : View.GONE);
        });
        datos.registrar.error.observe(this, e -> {
            error.setText(e);
            error.setVisibility(e != null ? View.VISIBLE : View.GONE);
        });
        datos.registrar.datos.observe(this, r -> {
            if (r != null) {
                Toast.makeText(this, R.string.venta_registrada, Toast.LENGTH_SHORT).show();
                if (r.advertencia != null) {
                    Toast.makeText(this, getString(R.string.aviso, r.advertencia), Toast.LENGTH_LONG).show();
                }
                setResult(RESULT_OK);
                finish();
            }
        });

        datos.productos.primeraVez(Api.nogalina.productos(null, null, null));
    }
}
