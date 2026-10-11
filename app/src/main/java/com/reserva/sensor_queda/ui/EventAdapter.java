package com.reserva.sensor_queda.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.reserva.sensor_queda.R;
import com.reserva.sensor_queda.data.EventEntity;

import java.util.ArrayList;
import java.util.List;

/** Lista de eventos do historico. */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(EventEntity evento);
    }

    private final List<EventEntity> itens = new ArrayList<>();
    private final OnEventClickListener listener;

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void setItens(List<EventEntity> novos) {
        itens.clear();
        if (novos != null) {
            itens.addAll(novos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventEntity evento = itens.get(position);
        holder.titulo.setText(EventUtils.tituloRes(evento.type));
        holder.data.setText(EventUtils.formatarDataHora(evento.timestamp));
        holder.img.setImageResource(EventUtils.iconeRes(evento.type));
        holder.img.setColorFilter(
                ContextCompat.getColor(holder.itemView.getContext(), EventUtils.corRes(evento.type)));

        if (evento.detail != null && !evento.detail.isEmpty()) {
            holder.resultado.setText(evento.detail);
            holder.resultado.setVisibility(View.VISIBLE);
        } else {
            holder.resultado.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(evento);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        final ImageView img;
        final TextView titulo;
        final TextView data;
        final TextView resultado;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgEvento);
            titulo = itemView.findViewById(R.id.txtEventoTitulo);
            data = itemView.findViewById(R.id.txtEventoData);
            resultado = itemView.findViewById(R.id.txtEventoResultado);
        }
    }
}
