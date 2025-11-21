package com.df4l.liftaz.data

import androidx.room.*
import java.util.Date

@Entity(tableName = "entree_poids")
data class EntreePoids (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Date,
    val poids: Float,
    val bodyFat: Float? = null
)