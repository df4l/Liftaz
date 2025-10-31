package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExerciceSeanceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exerciceSeance: ExerciceSeance)

    @Update
    suspend fun update(exerciceSeance: ExerciceSeance)

    @Delete
    suspend fun delete(exerciceSeance: ExerciceSeance)

    @Query("SELECT * FROM exercices_seance WHERE idExercice = :idExercice")
    suspend fun getByExercice(idExercice: Int): List<ExerciceSeance>

    @Query("SELECT COUNT(*) FROM exercices_seance WHERE idExercice = :idExercice")
    suspend fun countByExerciceId(idExercice: Int): Int

    @Query("UPDATE exercices_seance SET indexOrdre = indexOrdre - 1 WHERE idSeance = :idSeance AND indexOrdre > :indexSupprime")
    suspend fun reordonnerApresSuppression(idSeance: Int, indexSupprime: Int)
}
