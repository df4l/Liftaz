package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dietes")
data class Diete(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var nom: String
)
