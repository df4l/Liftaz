package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "programmes")
data class Programme(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nom: String,
    val description: String?,
    val dateCreation: Date = Date(),
    val actif: Boolean = false // ✅ un seul programme actif à la fois
)