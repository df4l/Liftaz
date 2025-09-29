package com.df4l.liftaz.data

import kotlinx.coroutines.flow.Flow

interface ExerciceRepository {
    fun getAllExercices(): Flow<List<Exercice>>
    suspend fun addExercice(exercise: Exercice)
    suspend fun updateExercice(exercise: Exercice)
    suspend fun deleteExercice(exercise: Exercice)
}