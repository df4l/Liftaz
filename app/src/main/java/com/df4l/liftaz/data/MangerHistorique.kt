package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "manger_historique")
data class MangerHistorique(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var date: Date,
    var heuresEtMinutes: Int, //TODO: A MODIFIER UN JOUR
    var nomElement: String,
    var calories: Int,
    var proteines: Float,
    var glucides: Float,
    var lipides: Float
)
