package com.nogalina.regionales;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.Fila> {

    public interface AlQuitar {
        void quitar(Venta.Item item);
    }

    private List<Venta.Item> items = new ArrayList<>();
    private final AlQuitar alQuitar;

    public CarritoAdapter(AlQuitar alQuitar) {
        this.alQuitar = alQuitar;
    }

    public void mostrar(List<Venta.Item> lista) {
        items = lista == null ? new ArrayList<>() : lista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Fila(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carrito, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        Venta.Item item = items.get(posicion);
        Context c = fila.itemView.getContext();

        fila.nombre.setText(item.productoNombre);
        fila.subtotal.setText(c.getString(R.string.subtotal, item.cantidad, item.precioUnitario,
                item.cantidad * item.precioUnitario));
        fila.quitar.setOnClickListener(v -> alQuitar.quitar(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final TextView nombre;
        final TextView subtotal;
        final Button quitar;

        Fila(View vista) {
            super(vista);
            nombre = vista.findViewById(R.id.nombre);
            subtotal = vista.findViewById(R.id.subtotal);
            quitar = vista.findViewById(R.id.quitar);
        }
    }
}
