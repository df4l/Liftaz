package com.df4l.liftaz.pousser.exercices.exercicesMenu

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.df4l.liftaz.data.ExerciceDao
import com.df4l.liftaz.data.MuscleDao
import kotlinx.coroutines.Dispatchers

class ExercicesViewModel(
    private val muscleDao: MuscleDao,
    private val exerciceDao: ExerciceDao
) : ViewModel() {

    val exercicesParMuscle = liveData(Dispatchers.IO) {
        val muscles = muscleDao.getAllMuscles()
        Log.d("DEBUG_VM", "Muscles trouvés: ${muscles.size}")
        muscles.forEach { Log.d("DEBUG_VM", "Muscle = ${it.id} / ${it.nom}") }

        val data = muscles.map { muscle ->
            val exercices = exerciceDao.getExercicesByMuscle(muscle.id)
            Log.d("DEBUG_VM", "  -> ${muscle.nom} : ${exercices.size} exercices")
            exercices.forEach { Log.d("DEBUG_VM", "       Exo = ${it.nom}") }
            muscle to exercices
        }
        emit(data)
    }
}
