package com.df4l.liftaz.soulever.seances.entrainement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Elastique
import com.df4l.liftaz.data.Exercice

class EntrainementExerciceAdapter(
    private var elastiques: List<Elastique>,
    private val onSeriesChanged: () -> Unit,
    private val onFlemmeTriggered : () -> Unit
) : RecyclerView.Adapter<EntrainementExerciceAdapter.ExerciceViewHolder>() {

    private val items = mutableListOf<ExerciceSeanceItem>()

    fun submitList(list: List<ExerciceSeanceItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun setElastiques(newList: List<Elastique>) {
        elastiques = newList
        notifyDataSetChanged()
    }

    inner class ExerciceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textNom = itemView.findViewById<TextView>(R.id.textExerciceName)
        private val textMuscle = itemView.findViewById<TextView>(R.id.textExerciceMuscle)
        private val textProgression = itemView.findViewById<TextView>(R.id.textExerciceProgression)
        private val recyclerSeries = itemView.findViewById<RecyclerView>(R.id.recyclerSeries)
        private val btnAddPlus = itemView.findViewById<ImageButton>(R.id.btnAddPlus)

        private lateinit var seriesAdapter: SeriesAdapter

        fun bind(item: ExerciceSeanceItem) {
            textNom.text = item.exerciceName
            textMuscle.text = item.muscleName

            updateProgression(item)

            seriesAdapter = SeriesAdapter(item.series, elastiques, onSeriesChanged = {
                updateProgression(item) // Met à jour le texte de progression (+2kg etc)
                onSeriesChanged()       // Appelle la fonction mettreAJourEtatBouton du fragment
            }, onFlemmeTriggered)
            recyclerSeries.layoutManager = LinearLayoutManager(itemView.context)
            recyclerSeries.adapter = seriesAdapter

            btnAddPlus.setOnClickListener {
                // Ajouter une nouvelle série par défaut
                val newSerie = if (item.series.firstOrNull() is SerieUi.Fonte) {
                    SerieUi.Fonte(0f, 0f, false)
                } else {
                    SerieUi.PoidsDuCorps(0f, 0, false)
                }
                item.series.add(newSerie)
                seriesAdapter.notifyItemInserted(item.series.size - 1)
                onSeriesChanged()
            }
        }

        private fun updateProgression(item: ExerciceSeanceItem) {
            val actuel = item.series.sumOf { serie ->
                // 1. Vérification si la série doit être comptée
                val isReady = when (serie) {
                    is SerieUi.Fonte -> {
                        // Pour la Fonte : on attend que poids ET reps soient > 0
                        serie.touchedByUser && serie.poids > 0f && serie.reps > 0f
                    }
                    is SerieUi.PoidsDuCorps -> {
                        // Pour le PDC : on attend juste que les reps soient > 0
                        serie.touchedByUser && serie.reps > 0f
                    }
                }

                val hasFlemme = when(serie) {
                    is SerieUi.Fonte -> serie.flemme
                    is SerieUi.PoidsDuCorps -> serie.flemme
                }

                if (hasFlemme || !isReady) {
                    0.0
                } else {
                    when (serie) {
                        is SerieUi.Fonte -> (serie.poids * serie.reps).toDouble()
                        is SerieUi.PoidsDuCorps -> {
                            val elastiquesChoisis = decodeElastiques(serie.bitmaskElastiques, elastiques)
                            val poidsEffectif = computeEffectiveLoad(item.poidsUtilisateur, elastiquesChoisis)
                            (poidsEffectif * serie.reps).toDouble()
                        }
                    }
                }
            }.toFloat()

            val precedent = item.poidsSouleve

            if (precedent > 0f) {
                // Si le volume actuel est encore à 0 (car aucune série n'est "Ready"), on reste discret
                if (actuel == 0f) {
                    textProgression.text = "--"
                    textProgression.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                    return
                }

                val diff = actuel - precedent
                val sign = if (diff >= 0) "+" else ""

                // Affichage complet avec pourcentage
                textProgression.text = String.format("%s%.1f kg", sign, diff)

                if (diff >= 0) {
                    textProgression.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                } else {
                    textProgression.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                }
            } else {
                textProgression.text = "Première séance"
                textProgression.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
            }
        }
    }

    private fun decodeElastiques(bitmask: Int, allElastiques: List<Elastique>): List<Elastique> {
        return allElastiques.filter { elast ->
            (bitmask and elast.valeurBitmask) != 0
        }
    }

    private fun computeEffectiveLoad(userWeight: Float, elastiques: List<Elastique>): Float {
        // Somme des résistances (Assistance)
        val elastiqueEffect = elastiques.sumOf { it.resistanceMinKg.toDouble() }.toFloat()
        // Poids effectif = Poids du corps - Aide des élastiques
        return (userWeight - elastiqueEffect).coerceAtLeast(0f)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercice_seance, parent, false)
        return ExerciceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size



    fun getItems(): List<ExerciceSeanceItem> = items
}

data class ExerciceSeanceItem(
    val exerciceName: String,
    val muscleName: String,
    val series: MutableList<SerieUi>,
    val poidsSouleve: Float,
    val poidsUtilisateur: Float,
    val supersetData: SupersetOrigins? = null
)

data class SupersetOrigins(
    val exercices: List<Exercice>,
    val poidsSouleveParExercices: List<Float>,
    val idSuperset: Int,
    val nbTours: Int
)