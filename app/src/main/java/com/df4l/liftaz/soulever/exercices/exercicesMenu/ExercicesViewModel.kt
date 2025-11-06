package com.df4l.liftaz.soulever.exercices.exercicesMenu

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.df4l.liftaz.data.Exercice
import com.df4l.liftaz.data.ExerciceDao
import com.df4l.liftaz.data.Muscle
import com.df4l.liftaz.data.MuscleDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExercicesViewModel(
    private val muscleDao: MuscleDao,
    private val exerciceDao: ExerciceDao
) : ViewModel() {

    private val _exercicesParMuscle = MutableLiveData<List<Pair<Muscle, List<Exercice>>>>()
    val exercicesParMuscle: LiveData<List<Pair<Muscle, List<Exercice>>>> = _exercicesParMuscle

    init {
        reloadData()
    }

    fun reloadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val muscles = muscleDao.getAllMuscles()
            val data = muscles.map { muscle ->
                val exercices = exerciceDao.getExercicesByMuscle(muscle.id)
                muscle to exercices
            }
            _exercicesParMuscle.postValue(data)
        }
    }
}

