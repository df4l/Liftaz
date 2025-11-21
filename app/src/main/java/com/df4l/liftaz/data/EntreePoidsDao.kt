package com.df4l.liftaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface EntreePoidsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entreePoids: EntreePoids)

    @Update
    suspend fun update(entreePoids: EntreePoids)

    @Delete
    suspend fun delete(entreePoids: EntreePoids)

    @Query("SELECT * FROM entree_poids ORDER BY id ASC")
    suspend fun getAll(): List<EntreePoids>
}