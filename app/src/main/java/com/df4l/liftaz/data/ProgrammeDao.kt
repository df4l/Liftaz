package com.df4l.liftaz.data

import androidx.room.*

@Dao
interface ProgrammeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(programme: Programme): Long

    @Update
    suspend fun update(programme: Programme)

    @Delete
    suspend fun delete(programme: Programme)

    @Transaction
    @Query("SELECT * FROM programmes ORDER BY dateCreation DESC")
    suspend fun getAllProgrammesAvecSeances(): List<ProgrammeAvecSeances>

    @Transaction
    @Query("SELECT * FROM programmes WHERE id = :id")
    suspend fun getProgrammeAvecSeances(id: Int): ProgrammeAvecSeances

    // ✅ Gestion du programme actif
    @Query("UPDATE programmes SET actif = 0")
    suspend fun desactiverTous()

    @Query("UPDATE programmes SET actif = 1 WHERE id = :programmeId")
    suspend fun activer(programmeId: Int)

    @Query("SELECT * FROM programmes WHERE actif = 1 LIMIT 1")
    suspend fun getProgrammeActif(): Programme?
}

