package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dietes")
data class Diete(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var nom: String,
    var objProteines: Int,
    var objGlucides: Int,
    var objLipides: Int,
    var objCalories: Int,
    val actif: Boolean = false //Une seule diète active à la fois
)
