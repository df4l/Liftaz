package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diete_elements")
data class DieteElements(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var idDiete: Int,
    var idElement: Int,
    var typeElement: Int, // (0 = aliment, 1 = recette)
    var typeRepas: Int,
    var quantiteGrammes: Float
)
