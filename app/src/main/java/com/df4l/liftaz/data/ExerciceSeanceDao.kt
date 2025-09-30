package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Dao
interface ExerciceSeanceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exerciceSeance: ExerciceSeance)

    @Update
    suspend fun update(exerciceSeance: ExerciceSeance)

    @Delete
    suspend fun delete(exerciceSeance: ExerciceSeance)
}