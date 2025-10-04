package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diete_elements")
data class DieteElements(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var idDiete: Int,
    var idRecette: Int? = null,
    var idAliment: Int? = null,
    var typeRepas: Int,
    var quantiteGrammes: Float
)
