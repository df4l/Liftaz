package com.df4l.liftaz.data

import androidx.room.Entity

@Entity(tableName = "exercices_seance",
    primaryKeys = ["idSeance", "idExercice"])
data class ExerciceSeance (
    var idSeance: Int,
    var idExercice: Int,
    var indexOrdre: Int,
    var nbSeries: Int,
    var minReps: Int,
    var maxReps: Int
)