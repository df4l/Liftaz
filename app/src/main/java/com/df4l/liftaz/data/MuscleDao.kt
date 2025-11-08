package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MuscleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(muscle: Muscle)

    @Update
    suspend fun update(muscle: Muscle)

    @Delete
    suspend fun delete(muscle: Muscle)

    @Query("SELECT COUNT(*) FROM muscles")
    suspend fun count(): Int

    @Query("SELECT * FROM muscles ORDER BY nom ASC")
    suspend fun getAllMuscles(): List<Muscle>

    @Query("SELECT * FROM muscles WHERE id = :id")
    fun getMuscle(id: Int): Flow<Muscle>

    @Query("SELECT nom FROM muscles WHERE id = :id LIMIT 1")
    suspend fun getNomMuscleById(id: Int): String

}