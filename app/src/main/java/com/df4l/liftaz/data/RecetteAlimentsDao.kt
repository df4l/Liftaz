package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecetteAlimentsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(recetteAliments: RecetteAliments): Long

    @Update
    suspend fun update(recetteAliments: RecetteAliments)

    @Delete
    suspend fun delete(recetteAliments: RecetteAliments)

    @Query("SELECT * FROM recette_aliments WHERE idRecette = :recetteId")
    suspend fun getAllForRecette(recetteId: Int): List<RecetteAliments>

}