package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface GraphiqueDao {

    @Query("SELECT * FROM graphiques ORDER BY id ASC")
    suspend fun getAllGraphiques(): List<Graphique>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(graphique: Graphique)

    @Update
    suspend fun update(graphique: Graphique)

    @Delete
    suspend fun delete(graphique: Graphique)

}