package com.df4l.liftaz.soulever.bilan

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.data.Elastique
import com.df4l.liftaz.databinding.ItemBilanExerciceBinding
import java.text.DecimalFormat

class BilanExerciceAdapter(
    private val exercices: List<ExerciceBilan>,
    private val allElastiques: List<Elastique>       // <-- Nouvel argument
) : RecyclerView.Adapter<BilanExerciceAdapter.ExViewHolder>() {

    class ExViewHolder(val binding: ItemBilanExerciceBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBilanExerciceBinding.inflate(inflater, parent, false)
        return ExViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExViewHolder, position: Int) {
        val ex = exercices[position]
        val b = holder.binding

        b.textExerciceName.text = ex.nom
        b.textExerciceMuscle.text = ex.muscle

        val format = DecimalFormat("#.#")
        val volumeActuel = ex.totalVolume
        val volumeAncien = ex.totalVolumeAncien
        val diff = volumeActuel - volumeAncien

        val sb = SpannableStringBuilder()

        // Partie principale "Total : 1250 kg"
        sb.append("Total : ${format.format(volumeActuel)} kg")

        // Si on a un historique (volumeAncien > 0), on affiche la différence

        Log.d("BILAN_DEBUG", "BilanExerciceAdapter.onBindViewHolder: volumeActuel=$volumeActuel, volumeAncien=$volumeAncien")
        if (volumeAncien > 0f) {
            sb.append(" (")

            val startColor = sb.length
            val sign = if (diff > 0) "+" else "" // Le moins est déjà dans le nombre si négatif
            val diffText = "${sign}${format.format(diff)}"
            sb.append(diffText)
            val endColor = sb.length

            sb.append(")")

            // Choix de la couleur
            val color = when {
                diff > 0 -> Color.parseColor("#4CAF50") // Vert
                diff < 0 -> Color.parseColor("#F44336") // Rouge
                else -> Color.GRAY
            }

            sb.setSpan(
                ForegroundColorSpan(color),
                startColor,
                endColor,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        b.textTotalVolume.text = sb

        b.recyclerSeriesBilan.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = BilanSerieAdapter(ex.series, ex.poidsDuCorps, allElastiques)
        }
    }

    override fun getItemCount(): Int = exercices.size
}