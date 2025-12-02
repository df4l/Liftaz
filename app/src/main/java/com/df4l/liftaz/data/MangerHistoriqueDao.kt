package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.util.Date

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
    suspend fun getTopTenFavoriteFoods(): List<String>

    @Query("""
        SELECT *
        FROM manger_historique
        WHERE date / (1000 * 60 * 60 * 24) = :date / (1000 * 60 * 60 * 24)
    """)
    suspend fun getHistoriqueForDate(date: Date): List<MangerHistorique>

    @Query("DELETE FROM manger_historique WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("""
    SELECT date, SUM(calories) as totalCalories
    FROM manger_historique
    WHERE date >= :startDate
    GROUP BY date / (1000 * 60 * 60 * 24)
    ORDER BY date ASC
""")
    suspend fun getCaloriesSumSince(startDate: Date): List<CaloriesPerDay>
}

data class CaloriesPerDay(
    val date: Date,
    val totalCalories: Int
)