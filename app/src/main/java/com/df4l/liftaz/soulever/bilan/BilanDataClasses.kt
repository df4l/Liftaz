package com.df4l.liftaz.soulever.bilan

data class SerieBilan(
    val numero: Int,
    val ancienPoids: Float?,
    val ancienReps: Float?,
    val nouveauPoids: Float,
    val nouveauReps: Float,
    val progressionKg: Float?,
    val progressionReps: Float?,
    val ancienBitMaskElastique: Int,
    val nouveauBitMaskElastique: Int
)

data class ExerciceBilan(
    val idExercice: Int,
    val nom: String,
    val muscle: String,
    val series: List<SerieBilan>,
    val poidsDuCorps: Boolean,
    val totalVolume: Float,
    val totalVolumeAncien: Float
)