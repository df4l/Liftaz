package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SeanceHistoriqueDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(seanceHistorique: SeanceHistorique): Long

    @Update
    suspend fun update(seanceHistorique: SeanceHistorique)

    @Delete
    suspend fun delete(seanceHistorique: SeanceHistorique)

    @Query("SELECT * FROM seances_historique WHERE idSeance = :idSeance ORDER BY date DESC LIMIT 1")
    suspend fun getLastSeanceHistorique(idSeance: Int): SeanceHistorique?

    @Query("""
    SELECT * FROM seances_historique 
    WHERE idSeance = :idSeance 
    ORDER BY date DESC 
    LIMIT 1 OFFSET 1
""")
    suspend fun getPreviousSeanceHistorique(idSeance: Int): SeanceHistorique?
}