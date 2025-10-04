package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repas_elements")
data class RepasElements(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var idRepas: Int,
    var idAliment: Int? = null,
    var idRecette: Int? = null
)
