package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MotivationFioulDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuel(fuel: MotivationFioul)

    @Query("SELECT * FROM motivation_fioul ORDER BY dateAdded DESC")
    suspend fun getAllFioulsOnce(): List<MotivationFioul>

    @Delete
    suspend fun deleteFioul(fioul: MotivationFioul)
}

