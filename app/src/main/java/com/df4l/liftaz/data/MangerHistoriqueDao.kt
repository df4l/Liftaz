package com.df4l.liftaz.data

import androidx.room.*
import java.util.Date

@Dao
interface MangerHistoriqueDao {

    // Insérer une entrée
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(h: MangerHistorique): Long

    // Mettre à jour
    @Update
    suspend fun update(h: MangerHistorique)

    // Supprimer
    @Delete
    suspend fun delete(h: MangerHistorique)

    // Récupérer par ID
    @Query("SELECT * FROM manger_historique WHERE id = :id")
    suspend fun getById(id: Int): MangerHistorique?

    // Récupérer tout l’historique
    @Query("SELECT * FROM manger_historique ORDER BY date DESC")
    suspend fun getAll(): List<MangerHistorique>

    // Récupérer l’historique d’un jour
    @Query("""
        SELECT * FROM manger_historique 
        WHERE date(date / 1000, 'unixepoch') = date(:day / 1000, 'unixepoch')
        ORDER BY typeRepas ASC, date ASC
    """)
    suspend fun getByDate(day: Date): List<MangerHistorique>

    // Récupérer par jour + type repas (petit-déj, déjeuner...)
    @Query("""
        SELECT * FROM manger_historique 
        WHERE date(date / 1000, 'unixepoch') = date(:day / 1000, 'unixepoch')
        AND typeRepas = :typeRepas
        ORDER BY date ASC
    """)
    suspend fun getByDateAndTypeRepas(day: Date, typeRepas: Int): List<MangerHistorique>

    // Récupérer entre deux dates (statistiques)
    @Query("""
        SELECT * FROM manger_historique
        WHERE date BETWEEN :start AND :end
        ORDER BY date ASC
    """)
    suspend fun getBetweenDates(start: Date, end: Date): List<MangerHistorique>
}
