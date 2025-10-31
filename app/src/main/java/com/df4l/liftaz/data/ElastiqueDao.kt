package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ElastiqueDao {
    @Query("SELECT * FROM elastiques ORDER BY valeurBitmask ASC")
    suspend fun getAll(): List<Elastique>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(elastique: Elastique)

    // Ajout : insertion multiple (liste)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(elastiques: List<Elastique>)

    @Update
    suspend fun update(elastique: Elastique)

    @Delete
    suspend fun delete(elastique: Elastique)

    @Query("DELETE FROM elastiques")
    suspend fun deleteAll()

    // Ajout : nombre d'éléments
    @Query("SELECT COUNT(*) FROM elastiques")
    suspend fun count(): Int
}
