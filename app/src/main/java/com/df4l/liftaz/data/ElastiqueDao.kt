package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Dao
interface ElastiqueDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(elastique: Elastique)

    @Update
    suspend fun update(elastique: Elastique)

    @Delete
    suspend fun delete(elastique: Elastique)
}