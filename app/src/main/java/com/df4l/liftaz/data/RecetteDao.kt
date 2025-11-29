package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecetteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(recette: Recette): Long

    @Update
    suspend fun update(recette: Recette)

    @Delete
    suspend fun delete(recette: Recette)

    @Query("SELECT * FROM recettes ORDER BY nom ASC")
    suspend fun getAll(): List<Recette>

    @Query("SELECT * FROM recettes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Recette?

    @Query("SELECT imageUri FROM recettes WHERE nom = :nom")
    suspend fun getImageUriParNom(nom: String): String?
}