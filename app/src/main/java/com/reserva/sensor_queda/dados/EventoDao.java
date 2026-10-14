package com.reserva.sensor_queda.dados;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EventoDao {

    @Insert
    long inserir(EventoEntidade evento);

    @Query("SELECT * FROM eventos ORDER BY timestamp DESC")
    List<EventoEntidade> listarTodos();

    @Query("SELECT * FROM eventos WHERE type IN (:tipos) ORDER BY timestamp DESC")
    List<EventoEntidade> buscarPorTipos(List<String> tipos);
}
