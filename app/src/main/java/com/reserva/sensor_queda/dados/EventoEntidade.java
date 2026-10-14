package com.reserva.sensor_queda.dados;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Evento registrado no historico do aplicativo. */
@Entity(tableName = "eventos")
public class EventoEntidade {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String type;

    public long timestamp;

    public String title;

    public String detail;

    public EventoEntidade(String type, long timestamp, String title, String detail) {
        this.type = type;
        this.timestamp = timestamp;
        this.title = title;
        this.detail = detail;
    }
}
