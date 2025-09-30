package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "elastiques")
data class Elastique (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val couleur: Int,
    val valeurBitmask: Int
)