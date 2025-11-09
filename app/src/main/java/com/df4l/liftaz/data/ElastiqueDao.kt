package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface ElastiqueDao {
    @Query("SELECT * FROM elastiques ORDER BY id")
    suspend fun getAll(): List<Elastique>

    @Insert
    suspend fun insert(elastique: Elastique)

    @Insert
    suspend fun insertAll(elastiques: List<Elastique>)

    @Update
    suspend fun update(elastique: Elastique)

    @Delete
    suspend fun delete(elastique: Elastique)

    @Query("SELECT COUNT(*) FROM elastiques")
    suspend fun count(): Int

    @Transaction
    suspend fun deleteAndRecalculate(
        elastiqueToDelete: Elastique,
        serieDao: SerieDao
    ) {
        // 1️⃣ Supprimer l'élastique
        delete(elastiqueToDelete)

        // 2️⃣ Récupérer la liste restante triée
        val remaining = getAll().sortedBy { it.id }

        // 3️⃣ Recalculer les bitmasks des élastiques
        remaining.forEachIndexed { index, e ->
            update(e.copy(valeurBitmask = 1 shl index))
        }

        // 4️⃣ Recalculer les bitmasks dans Series
        val allSeries = serieDao.getAll()
        allSeries.forEach { serie ->
            var newMask = 0
            remaining.forEachIndexed { index, elastique ->
                // Si le bit ancien est présent dans la série, on ajoute le nouveau bit
                //TODO: Sans doute de la bêtise ce que j'ai fait ici présent
                //TODO: Faudrait avertir plutôt l'utilisateur qu'il a déjà utilisé l'élastique dans une série auparavant, faire le check avant, bref
                if ((serie.elastiqueBitMask and elastiqueToDelete.valeurBitmask) != elastiqueToDelete.valeurBitmask) {
                    // Rien à faire ici, car on supprime seulement le bit de l'élastique supprimé
                }
                if ((serie.elastiqueBitMask and elastique.valeurBitmask) != 0) {
                    newMask = newMask or (1 shl index)
                }
            }
            if (newMask != serie.elastiqueBitMask) {
                serieDao.update(serie.copy(elastiqueBitMask = newMask))
            }
        }
    }
}

