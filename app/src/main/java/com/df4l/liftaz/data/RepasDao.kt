package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Dao
interface RepasDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(repas: Repas)

    @Update
    suspend fun update(repas: Repas)

    @Delete
    suspend fun delete(repas: Repas)
}