package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "aliments")
data class Aliment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nom: String,
    val marque: String, //Tout ce qui suit compte pour 100 grammes
    val calories: Int,
    val proteines: Float,
    val lipides: Float,
    val glucides: Float,
    val quantiteParDefaut: Int? = null //Exprimée en grammes
    )
