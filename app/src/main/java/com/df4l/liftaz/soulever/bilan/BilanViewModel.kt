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

                // Valeurs précédentes (0 si aucune séance précédente)
                val ancienPoids = serieAnc?.poids ?: 0f
                val ancienReps = serieAnc?.nombreReps ?: 0f
                val ancienMask = serieAnc?.elastiqueBitMask ?: 0

                // Progressions
                val progressionKg =
                    if (!exercice.poidsDuCorps) {
                        (serieAct.poids * serieAct.nombreReps) - (ancienPoids * ancienReps)
                    } else 0f

                val progressionReps =
                    if (exercice.poidsDuCorps) {
                        serieAct.nombreReps - ancienReps
                    } else 0f

                SerieBilan(
                    numero = serieAct.numeroSerie,
                    ancienPoids = ancienPoids,
                    ancienReps = ancienReps,
                    nouveauPoids = serieAct.poids,
                    nouveauReps = serieAct.nombreReps,
                    progressionKg = progressionKg,
                    progressionReps = progressionReps,
                    ancienBitMaskElastique = if (exercice.poidsDuCorps) ancienMask else 0,
                    nouveauBitMaskElastique = if (exercice.poidsDuCorps) serieAct.elastiqueBitMask else 0
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
