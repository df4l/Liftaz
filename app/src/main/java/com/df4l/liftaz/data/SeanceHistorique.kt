package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "seances_historique")
data class SeanceHistorique (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val idSeance: Int,
    val date: Date,
    val dureeSecondes: Int

)