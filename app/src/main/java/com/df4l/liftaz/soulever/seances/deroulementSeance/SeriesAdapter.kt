package com.df4l.liftaz.soulever.seances.entrainement

import android.app.AlertDialog
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.CheckedTextView
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Elastique
import com.df4l.liftaz.soulever.fioul.RandomFioulDialog
import com.df4l.liftaz.soulever.seances.deroulementSeance.AssistanceElastiqueView
import com.df4l.liftaz.soulever.seances.deroulementSeance.getCouleursForBitmask

class SeriesAdapter(
    private val series: MutableList<SerieUi>,
    private val elastiques: List<Elastique>,
    private val onSeriesChanged: () -> Unit,
    private val onFlemmeTriggered : () -> Unit
):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun Float.toHint() = if (this == 0f) "" else this.toString()

    inner class FonteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textSerieNumber = itemView.findViewById<TextView>(R.id.textSerieNumber)
        private val editWeight = itemView.findViewById<EditText>(R.id.editPoids)
        private val editReps = itemView.findViewById<EditText>(R.id.editReps)
        private val checkboxFlemme = itemView.findViewById<CheckBox>(R.id.checkboxFlemme)
        private var alreadyHadFlemme = 0

        fun bind(serie: SerieUi.Fonte, index: Int) {
            textSerieNumber.text = "Série $index"

            editWeight.setHint(serie.poids.toHint())
            editReps.setHint(serie.reps.toHint())
            //checkboxFlemme.isChecked = serie.flemme

            // Update model when user edits values
            editWeight.addTextChangedListener {
                serie.poids = it.toString().toFloatOrNull() ?: 0f
                onSeriesChanged()
            }
            editReps.addTextChangedListener {
                serie.reps = it.toString().toFloatOrNull() ?: 0f
                onSeriesChanged()
            }
            checkboxFlemme.setOnCheckedChangeListener { _, checked ->
                if (checked && alreadyHadFlemme == 0) {
                    // Annuler le check sans relancer le listener
                    checkboxFlemme.setOnCheckedChangeListener(null)
                    checkboxFlemme.isChecked = false

                    // Réassigner proprement le listener
                    checkboxFlemme.setOnCheckedChangeListener { _, checkedInner ->
                        handleFlemmeChecked(checkedInner, serie)
                    }

                    onFlemmeTriggered()

                    alreadyHadFlemme = 1
                } else {
                handleFlemmeChecked(checked, serie)
                }
            }
        }

        private fun handleFlemmeChecked(checked: Boolean, serie: SerieUi.Fonte) {
            serie.flemme = checked
            editWeight.isEnabled = !checked
            editReps.isEnabled = !checked
            onSeriesChanged()
        }
    }



    inner class PoidsCorpsViewHolder(itemView: View, val elastiques: List<Elastique>) :
        RecyclerView.ViewHolder(itemView) {

        private val textSerieNumber = itemView.findViewById<TextView>(R.id.textSerieNumber)
        private val editReps = itemView.findViewById<EditText>(R.id.editReps)
        private val checkboxFlemme = itemView.findViewById<CheckBox>(R.id.checkboxFlemme)
        private val viewElastiques = itemView.findViewById<AssistanceElastiqueView>(R.id.viewElastiques)

        fun bind(serie: SerieUi.PoidsDuCorps, index: Int) {

            textSerieNumber.text = "Série $index"

            editReps.setText("")
            editReps.setHint(serie.reps.toHint())
            //checkboxFlemme.isChecked = serie.flemme
            viewElastiques.couleurs = getCouleursForBitmask(elastiques, serie.bitmaskElastiques)

            // Met à jour les données
            editReps.addTextChangedListener {
                serie.reps = it.toString().toFloatOrNull() ?: 0f
                onSeriesChanged()

            }
            checkboxFlemme.setOnCheckedChangeListener { _, checked ->
                serie.flemme = checked

                editReps.isEnabled = !checked

                onSeriesChanged()
            }

            // Clique -> ouvre le dialogue multi sélection
            viewElastiques.setOnClickListener {
                // Adapter custom pour afficher les couleurs
                val adapter = object : ArrayAdapter<Elastique>(
                    itemView.context,
                    android.R.layout.simple_list_item_multiple_choice,
                    elastiques
                ) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val row = convertView ?: LayoutInflater.from(context)
                            .inflate(android.R.layout.simple_list_item_multiple_choice, parent, false)

                        val checkBox = row.findViewById<CheckedTextView>(android.R.id.text1)

                        // Texte vide, couleur de fond = couleur de l'élastique
                        checkBox.text = ""
                        checkBox.setBackgroundColor(elastiques[position].couleur)
                        checkBox.isChecked = (elastiques[position].valeurBitmask and serie.bitmaskElastiques) != 0

                        return row
                    }
                }

                // Création du dialog
                val dialog = AlertDialog.Builder(itemView.context)
                    .setTitle("Élastiques utilisés")
                    .setAdapter(adapter, null) // On gère le click via ListView
                    .setPositiveButton("OK") { _, _ ->
                        // Appliquer les couleurs sélectionnées à la vue
                        viewElastiques.couleurs = getCouleursForBitmask(elastiques, serie.bitmaskElastiques)
                    }
                    .setNegativeButton("Annuler", null)
                    .show()

                // Gestion du click sur les items
                dialog.listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
                dialog.listView.setOnItemClickListener { _, view, position, _ ->
                    val bit = elastiques[position].valeurBitmask
                    val checked = (view as CheckedTextView).isChecked

                    serie.bitmaskElastiques = if (checked)
                        serie.bitmaskElastiques or bit
                    else
                        serie.bitmaskElastiques and bit.inv()
                }
            }

        }
    }


    companion object {
        private const val TYPE_CHARGE = 0
        private const val TYPE_POIDS_CORPS = 1
    }

    override fun getItemViewType(position: Int) = when(series[position]) {
        is SerieUi.Fonte -> TYPE_CHARGE
        is SerieUi.PoidsDuCorps -> TYPE_POIDS_CORPS
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            TYPE_CHARGE -> FonteViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_serie_fonte, parent, false)
            )
            else -> PoidsCorpsViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_serie_pdc, parent, false),
                elastiques
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is FonteViewHolder -> holder.bind(series[position] as SerieUi.Fonte, position + 1)
            is PoidsCorpsViewHolder -> holder.bind(series[position] as SerieUi.PoidsDuCorps, position + 1)
        }
    }

    override fun getItemCount() = series.size
}


sealed class SerieUi {
    data class Fonte(var poids: Float, var reps: Float, var flemme: Boolean) : SerieUi()
    data class PoidsDuCorps(var reps: Float, var bitmaskElastiques: Int, var flemme: Boolean) : SerieUi()
}