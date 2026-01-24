package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercices_seance")
data class ExerciceSeance(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var idSeance: Int,
    var idExercice: Int,
    var indexOrdre: Int,
    var nbSeries: Int,
    var minReps: Int,
    var maxReps: Int,
    var idSuperset: Int? = null
)
