package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "series")
data class Serie (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val idSeanceHistorique: Int,
    val idExercice: Int,
    val index: Int,
    val poids: Int,
    val nombreReps: Int,
    val elastiqueBitMask: Int
    )