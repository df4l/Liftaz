package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Dao
interface RecetteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(recette: Recette)

    @Update
    suspend fun update(recette: Recette)

    @Delete
    suspend fun delete(recette: Recette)
}