package com.df4l.liftaz.data

import kotlinx.coroutines.flow.Flow

class OfflineExerciceRepository (private val exerciseDao: ExerciceDao
) : ExerciceRepository {

    override fun getAllExercices(): Flow<List<Exercice>> = exerciseDao.getAllExercices()

    override suspend fun addExercice(exercise: Exercice) = exerciseDao.insert(exercise)

    override suspend fun updateExercice(exercise: Exercice) = exerciseDao.update(exercise)

    override suspend fun deleteExercice(exercise: Exercice) = exerciseDao.delete(exercise)
}