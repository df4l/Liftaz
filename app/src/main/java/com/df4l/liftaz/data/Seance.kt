package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class TypeFrequence {
    JOURS_SEMAINE,
    INTERVALLE
}

@Entity(tableName = "seances")
data class Seance (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nom: String,
    val typeFrequence: TypeFrequence,
    val joursSemaine: List<Int>? = null,
    val intervalleJours: Int? = null,
    val dateAjout: Date
)