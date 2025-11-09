package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SerieDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(serie: Serie)

    @Update
    suspend fun update(serie: Serie)

    @Delete
    suspend fun delete(serie: Serie)

    @Query("DELETE FROM series WHERE idExercice = :idExercice")
    suspend fun deleteByExercice(idExercice: Int)

    @Query("SELECT * FROM series")
    suspend fun getAll(): List<Serie>
}