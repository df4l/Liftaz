package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "repas")
data class Repas(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var date: Date,
    var typeRepas: Int
)
