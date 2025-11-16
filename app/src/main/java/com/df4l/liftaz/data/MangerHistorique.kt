package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "manger_historique")
data class MangerHistorique (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Date,
    val typeRepas: Int,
    val idElement: Int,     // aliment ou recette
    val typeElement: Int,   // 0 = aliment, 1 = recette
    val quantiteGrammes: Float
    )