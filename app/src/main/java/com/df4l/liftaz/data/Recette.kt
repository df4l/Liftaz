package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recettes")
data class Recette (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nom: String,
    val quantitePortion: Int? = null,
    val imageUri: String? = null
)