package com.reserva.sensor_queda.dados;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {EventoEntidade.class}, version = 1, exportSchema = false)
public abstract class BancoDados extends RoomDatabase {

    private static volatile BancoDados instancia;

    public abstract EventoDao eventoDao();

    public static BancoDados obterInstancia(Context context) {
        if (instancia == null) {
            synchronized (BancoDados.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(
                            context.getApplicationContext(),
                            BancoDados.class,
                            "me_arriba.db"
                    ).build();
                }
            }
        }
        return instancia;
    }
}
