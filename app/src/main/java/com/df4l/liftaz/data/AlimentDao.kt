package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AlimentDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(aliment: Aliment)

    @Update
    suspend fun update(aliment: Aliment)

    @Delete
    suspend fun delete(aliment: Aliment)

    @Query("SELECT * FROM aliments ORDER BY nom ASC")
    suspend fun getAll(): List<Aliment>
}