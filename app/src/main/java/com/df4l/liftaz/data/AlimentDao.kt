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
    suspend fun insert(aliment: Aliment): Long

    @Update
    suspend fun update(aliment: Aliment)

    @Delete
    suspend fun delete(aliment: Aliment)

    @Query("SELECT * FROM aliments ORDER BY nom ASC")
    suspend fun getAll(): List<Aliment>

    @Query("SELECT * FROM aliments WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Aliment?

    @Query("SELECT imageUri FROM aliments WHERE nom = :nom")
    suspend fun getImageUriParNom(nom: String): String?

    @Query("SELECT * FROM aliments WHERE nom = :nom LIMIT 1")
    suspend fun getByNom(nom: String): Aliment?
}