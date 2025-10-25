package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciceDao {

    @Query("SELECT * FROM exercices ORDER BY nom ASC")
    suspend fun getAllExercices(): List<Exercice>

    @Query("SELECT * FROM exercices WHERE id = :id")
    fun getItem(id: Int): Flow<Exercice>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exercice: Exercice)

    @Update
    suspend fun update(exercice: Exercice)

    @Delete
    suspend fun delete(exercice: Exercice)

    @Query("SELECT COUNT(*) FROM exercices")
    suspend fun count(): Int

    @Query("SELECT * FROM exercices WHERE idMuscleCible = :id")
    fun getExercicesByMuscle(id: Int): List<Exercice>
}