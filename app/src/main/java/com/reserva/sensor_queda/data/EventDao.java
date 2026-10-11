package com.reserva.sensor_queda.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EventDao {

    @Insert
    long insert(EventEntity event);

    @Query("SELECT * FROM eventos ORDER BY timestamp DESC")
    List<EventEntity> getAll();

    @Query("SELECT * FROM eventos WHERE type IN (:types) ORDER BY timestamp DESC")
    List<EventEntity> findByTypes(List<String> types);
}
