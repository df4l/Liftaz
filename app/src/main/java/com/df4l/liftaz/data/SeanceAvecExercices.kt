package com.df4l.liftaz.data

import androidx.room.Embedded
import androidx.room.Relation

data class SeanceAvecExercices(
    @Embedded val seance: Seance,
    @Relation(
        parentColumn = "id",
        entityColumn = "idSeance"
    )
    val exercices: List<ExerciceSeance>
)
