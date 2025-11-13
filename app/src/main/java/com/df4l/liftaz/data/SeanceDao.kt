package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface SeanceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(seance: Seance): Long

    @Update
    suspend fun update(seance: Seance)

    @Delete
    suspend fun delete(seance: Seance)

    @Transaction
    @Query("SELECT * FROM seances WHERE id = :idSeance")
    suspend fun getSeanceAvecExercices(idSeance: Int): SeanceAvecExercices?

    @Query("SELECT * FROM seances")
    suspend fun getAllSeances(): List<Seance>

    @Query("SELECT * FROM seances WHERE id = :id LIMIT 1")
    suspend fun getSeance(id: Int): Seance

    @Transaction
    @Query("SELECT * FROM seances")
    suspend fun getSeancesAvecExercices(): List<SeanceAvecExercices>

    @Query("UPDATE seances SET idProgramme = :programmeId WHERE id = :seanceId")
    suspend fun updateProgrammeId(seanceId: Int, programmeId: Int?)

    @Query("UPDATE seances SET idProgramme = NULL WHERE idProgramme = :programmeId")
    suspend fun clearProgrammeIdForProgramme(programmeId: Int)
}