package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "muscles")
data class Muscle (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nom: String,
    val nomImage: String
)