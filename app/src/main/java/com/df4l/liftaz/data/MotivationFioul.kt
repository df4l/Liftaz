package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "motivation_fioul")
data class MotivationFioul(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val type: FioulType,
    val contentUri: String?,
    val textContent: String?,
    val dateAdded: Date,

    val muscleId: Int? = null   // ⭐ Facultatif
)

enum class FioulType {
    IMAGE,
    VIDEO,
    TEXTE
}
