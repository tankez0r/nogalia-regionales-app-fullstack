package com.nogalina.regionales;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VentasAdapter extends RecyclerView.Adapter<VentasAdapter.Fila> {

    public interface AlAnular {
        void anular(Venta venta);
    }

    private List<Venta> ventas = new ArrayList<>();
    private final AlAnular alAnular;
    private boolean bloqueado;

    public VentasAdapter(AlAnular alAnular) {
        this.alAnular = alAnular;
    }

    public void mostrar(List<Venta> lista) {
        ventas = lista == null ? new ArrayList<>() : lista;
        notifyDataSetChanged();
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Fila(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venta, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        Venta v = ventas.get(posicion);
        Context c = fila.itemView.getContext();

        List<String> productos = new ArrayList<>();
        if (v.items != null) {
            for (Venta.Item item : v.items) {
                productos.add(c.getString(R.string.item_venta, item.productoNombre, item.cantidad));
            }
        }

        fila.titulo.setText(c.getString(R.string.venta_titulo, v.id, v.fecha));
        fila.total.setText(c.getString(R.string.venta_total, v.total,
                c.getString(v.anulada() ? R.string.estado_anulada : R.string.estado_completada)));
        fila.productos.setText(TextUtils.join(", ", productos));
        fila.anular.setVisibility(v.anulada() ? View.GONE : View.VISIBLE);
        fila.anular.setEnabled(!bloqueado);
        fila.anular.setOnClickListener(b -> alAnular.anular(v));
        fila.itemView.setBackgroundResource(v.anulada() ? R.color.anulada : android.R.color.transparent);
    }

    @Override
    public int getItemCount() {
        return ventas.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final TextView titulo;
        final TextView total;
        final TextView productos;
        final Button anular;

        Fila(View vista) {
            super(vista);
            titulo = vista.findViewById(R.id.titulo);
            total = vista.findViewById(R.id.total);
            productos = vista.findViewById(R.id.productos);
            anular = vista.findViewById(R.id.anular);
        }
    }
}
