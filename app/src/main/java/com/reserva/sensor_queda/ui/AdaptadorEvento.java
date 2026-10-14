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
import com.reserva.sensor_queda.dados.EventoEntidade;

import java.util.ArrayList;
import java.util.List;

/** Lista de eventos do historico. */
public class AdaptadorEvento extends RecyclerView.Adapter<AdaptadorEvento.VisaoEvento> {

    public interface AoClicarEvento {
        void onEventClick(EventoEntidade evento);
    }

    private final List<EventoEntidade> itens = new ArrayList<>();
    private final AoClicarEvento listener;

    public AdaptadorEvento(AoClicarEvento listener) {
        this.listener = listener;
    }

    public void setItens(List<EventoEntidade> novos) {
        itens.clear();
        if (novos != null) {
            itens.addAll(novos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VisaoEvento onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_evento, parent, false);
        return new VisaoEvento(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VisaoEvento holder, int position) {
        EventoEntidade evento = itens.get(position);
        holder.titulo.setText(UtilitariosEvento.tituloRes(evento.type));
        holder.data.setText(UtilitariosEvento.formatarDataHora(evento.timestamp));
        holder.img.setImageResource(UtilitariosEvento.iconeRes(evento.type));
        holder.img.setColorFilter(
                ContextCompat.getColor(holder.itemView.getContext(), UtilitariosEvento.corRes(evento.type)));

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

    static class VisaoEvento extends RecyclerView.ViewHolder {
        final ImageView img;
        final TextView titulo;
        final TextView data;
        final TextView resultado;

        VisaoEvento(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgEvento);
            titulo = itemView.findViewById(R.id.txtEventoTitulo);
            data = itemView.findViewById(R.id.txtEventoData);
            resultado = itemView.findViewById(R.id.txtEventoResultado);
        }
    }
}
