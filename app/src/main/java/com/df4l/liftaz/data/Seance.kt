package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seances")
data class Seance (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nom: String
)