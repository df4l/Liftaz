package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Dao
interface RepasElementsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(repasElements: RepasElements)

    @Update
    suspend fun update(repasElements: RepasElements)

    @Delete
    suspend fun delete(repasElements: RepasElements)
}