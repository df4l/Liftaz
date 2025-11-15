package com.df4l.liftaz.soulever.bilan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.df4l.liftaz.data.ExerciceDao
import com.df4l.liftaz.data.MuscleDao
import com.df4l.liftaz.data.SeanceHistoriqueDao
import com.df4l.liftaz.data.SerieDao
import kotlinx.coroutines.launch

class BilanViewModel(
    private val seanceHistoriqueDao: SeanceHistoriqueDao,
    private val serieDao: SerieDao,
    private val exerciceDao: ExerciceDao,
    private val muscleDao: MuscleDao,
    private val idSeance: Int,
    private val idSeanceHistoriqueActuelle: Int
) : ViewModel() {

    val bilan = MutableLiveData<List<ExerciceBilan>>()

    init {
        viewModelScope.launch {
            loadBilan()
        }
    }

    private suspend fun loadBilan() {
        val seancePrecedente =
            seanceHistoriqueDao.getPreviousSeanceHistorique(idSeance)

        val seriesActuelles =
            serieDao.getSeriesForSeanceHistorique(idSeanceHistoriqueActuelle)

        val seriesPrecedentes =
            seancePrecedente?.let {
                serieDao.getSeriesForSeanceHistorique(it.id)
            } ?: emptyList()

        // Regroupement par exercice
        val groupesActuels = seriesActuelles.groupBy { it.idExercice }
        val groupesAnciens = seriesPrecedentes.groupBy { it.idExercice }

        val resultat = mutableListOf<ExerciceBilan>()

        for ((idExercice, seriesAct) in groupesActuels) {

            val anc = groupesAnciens[idExercice]

            val exercice = exerciceDao.getExerciceById(idExercice)
            val muscleNom = muscleDao.getNomMuscleById(exercice.idMuscleCible)

            val series = seriesAct.map { serieAct ->
                val serieAnc = anc?.find { it.numeroSerie == serieAct.numeroSerie }

                val progressionKg = (serieAct.poids * serieAct.nombreReps) - (serieAnc!!.poids * serieAnc.nombreReps)
                val progressionReps = serieAct.nombreReps - serieAnc.nombreReps

                SerieBilan(
                    numero = serieAct.numeroSerie,
                    ancienPoids = serieAnc.poids,
                    ancienReps = serieAnc.nombreReps,
                    nouveauPoids = serieAct.poids,
                    nouveauReps = serieAct.nombreReps,
                    progressionKg = if(!exercice.poidsDuCorps) { progressionKg } else 0f,
                    progressionReps = if(exercice.poidsDuCorps) { progressionReps } else 0f,
                    ancienBitMaskElastique = if(exercice.poidsDuCorps) { serieAnc.elastiqueBitMask } else 0,
                    nouveauBitMaskElastique = if(exercice.poidsDuCorps) { serieAct.elastiqueBitMask } else 0
                )
            }

            resultat += ExerciceBilan(
                idExercice = idExercice,
                nom = exercice.nom,
                muscle = muscleNom,
                series = series,
                poidsDuCorps = exercice.poidsDuCorps
            )
        }

        bilan.postValue(resultat)
    }
}
