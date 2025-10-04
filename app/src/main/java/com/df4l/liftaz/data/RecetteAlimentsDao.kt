package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Dao
interface RecetteAlimentsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(recetteAliments: RecetteAliments)

    @Update
    suspend fun update(recetteAliments: RecetteAliments)

    @Delete
    suspend fun delete(recetteAliments: RecetteAliments)
}