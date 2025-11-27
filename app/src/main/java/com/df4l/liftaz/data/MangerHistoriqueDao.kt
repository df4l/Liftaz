package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MangerHistoriqueDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(mangerHistorique: MangerHistorique)

    @Update
    suspend fun update(mangerHistorique: MangerHistorique)

    @Delete
    suspend fun delete(mangerHistorique: MangerHistorique)

    @Query("""
        SELECT nomElement
        FROM manger_historique
        GROUP BY nomElement
        ORDER BY COUNT(nomElement) DESC
        LIMIT 10
    """)
    fun getTopTenFavoriteFoods(): List<String>
}