package com.df4l.liftaz.data

import androidx.room.Entity
@Entity(
    tableName = "recette_aliments",
    primaryKeys = ["idRecette", "idAliment"]
)
data class RecetteAliments(
    var idRecette: Int,
    var idAliment: Int,
    var coefAliment: Float
)
