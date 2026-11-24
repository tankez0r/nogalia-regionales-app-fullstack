package com.nogalina.regionales;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificacionesAdapter extends RecyclerView.Adapter<NotificacionesAdapter.Fila> {

    public interface AlMarcar {
        void marcar(Notificacion notificacion);
    }

    private List<Notificacion> notificaciones = new ArrayList<>();
    private final AlMarcar alMarcar;

    public NotificacionesAdapter(AlMarcar alMarcar) {
        this.alMarcar = alMarcar;
    }

    public void mostrar(List<Notificacion> lista) {
        notificaciones = lista == null ? new ArrayList<>() : lista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Fila(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notificacion, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        Notificacion n = notificaciones.get(posicion);
        boolean leida = n.leida == 1;

        fila.fecha.setText(n.fecha);
        fila.mensaje.setText(n.mensaje);
        fila.leida.setVisibility(leida ? View.VISIBLE : View.GONE);
        fila.marcar.setVisibility(leida ? View.GONE : View.VISIBLE);
        fila.marcar.setOnClickListener(v -> alMarcar.marcar(n));
        fila.itemView.setAlpha(leida ? 0.6f : 1f);
    }

    @Override
    public int getItemCount() {
        return notificaciones.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final TextView fecha;
        final TextView mensaje;
        final TextView leida;
        final Button marcar;

        Fila(View vista) {
            super(vista);
            fecha = vista.findViewById(R.id.fecha);
            mensaje = vista.findViewById(R.id.mensaje);
            leida = vista.findViewById(R.id.leida);
            marcar = vista.findViewById(R.id.marcar);
        }
    }
}
