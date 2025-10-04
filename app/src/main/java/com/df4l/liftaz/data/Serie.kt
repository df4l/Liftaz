package com.df4l.liftaz.data

import androidx.room.Entity
@Entity(
    tableName = "series",
    primaryKeys = ["idSeanceHistorique", "idExercice", "numeroSerie"]
)
data class Serie (
    val idSeanceHistorique: Int,
    val idExercice: Int,
    val numeroSerie: Int,
    val poids: Float,
    val nombreReps: Float,
    val elastiqueBitMask: Int
    )