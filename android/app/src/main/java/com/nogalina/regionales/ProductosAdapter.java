package com.nogalina.regionales;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProductosAdapter extends RecyclerView.Adapter<ProductosAdapter.Fila> {

    public interface AlTocar {
        void tocar(Producto producto);
    }

    private List<Producto> productos = new ArrayList<>();
    private final AlTocar alTocar;

    public ProductosAdapter(AlTocar alTocar) {
        this.alTocar = alTocar;
    }

    public void mostrar(List<Producto> lista) {
        productos = lista == null ? new ArrayList<>() : lista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Fila(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        Producto p = productos.get(posicion);
        Context c = fila.itemView.getContext();

        fila.nombre.setText(p.nombre);
        fila.categoria.setText(p.origen == null || p.origen.isEmpty()
                ? p.categoria : c.getString(R.string.categoria_origen, p.categoria, p.origen));
        fila.precio.setText(c.getString(R.string.precio, p.precio));
        fila.stock.setText(c.getString(R.string.stock_fila, p.stock, p.stockMinimo));
        fila.itemView.setBackgroundResource(p.bajoStock() ? R.color.bajo_stock : android.R.color.transparent);
        fila.itemView.setOnClickListener(v -> alTocar.tocar(p));
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final TextView nombre;
        final TextView categoria;
        final TextView precio;
        final TextView stock;

        Fila(View vista) {
            super(vista);
            nombre = vista.findViewById(R.id.nombre);
            categoria = vista.findViewById(R.id.categoria);
            precio = vista.findViewById(R.id.precio);
            stock = vista.findViewById(R.id.stock);
        }
    }
}
