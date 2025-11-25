package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DieteElementsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(dieteElements: DieteElements)

    @Update
    suspend fun update(dieteElements: DieteElements)

    @Delete
    suspend fun delete(dieteElements: DieteElements)

    @Query("DELETE FROM diete_elements WHERE idDiete = :idDiete")
    suspend fun deleteByIdDiete(idDiete: Int)
}