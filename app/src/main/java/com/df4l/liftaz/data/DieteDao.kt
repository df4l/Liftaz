package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Dao
interface DieteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(diete: Diete): Long

    @Update
    suspend fun update(diete: Diete)

    @Delete
    suspend fun delete(diete: Diete)
}