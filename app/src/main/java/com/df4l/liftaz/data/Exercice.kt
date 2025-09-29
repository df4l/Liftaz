package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercices")
data class Exercice (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var nom: String,
    var idMuscleCible: Int,
    var notes: String
    )