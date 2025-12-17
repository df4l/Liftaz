package com.df4l.liftaz.soulever.bilan

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.data.Elastique
import com.df4l.liftaz.databinding.ItemBilanSerieBinding

class BilanSerieAdapter(
    private val series: List<SerieBilan>,
    private val poidsDuCorps: Boolean,
    private val allElastiques: List<Elastique>
) : RecyclerView.Adapter<BilanSerieAdapter.SerieViewHolder>() {

    class SerieViewHolder(val binding: ItemBilanSerieBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SerieViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBilanSerieBinding.inflate(inflater, parent, false)
        return SerieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SerieViewHolder, position: Int) {
        val s = series[position]
        val b = holder.binding

        b.textSerieNumber.text = "Série ${s.numero}"

        // -------------------------------------------
        // 1) Gestion de la FLEMME
        // -------------------------------------------
        val flemme = s.nouveauReps == 0f

        if (flemme) {
            // Affichage FLEMME
            b.textFlemme.visibility = View.VISIBLE
            b.textFlemme.text = "FLEMME"
            b.textFlemme.setTextColor(Color.parseColor("#F44336"))

            // Barrer les textes du nouveau et progression
            b.textNouveau.paintFlags =
                b.textNouveau.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            // Texte "Nouveau"
            b.textNouveau.text =
                if (!poidsDuCorps)
                    "Nouveau : ${s.nouveauPoids}kg × ${s.nouveauReps}"
                else
                    "Nouveau : ${s.nouveauReps} reps"

        } else {
            // Pas de Flemme → cacher le label et enlever les traits
            b.textFlemme.visibility = View.GONE

            b.textNouveau.paintFlags =
                b.textNouveau.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            b.textProgression.paintFlags =
                b.textProgression.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        // -------------------------------------------
        // 2) Ancien
        // -------------------------------------------
        b.textAncien.text =
            if (s.ancienPoids != null) {
                if (!poidsDuCorps)
                    "Ancien : ${s.ancienPoids}kg × ${s.ancienReps}"
                else
                    "Ancien : ${s.ancienReps} reps"
            } else {
                "Ancien : -"
            }

        if (poidsDuCorps) {
            b.textAncien.append(" ")
            b.textAncien.append(getElastiquesColoredSlashes(s.ancienBitMaskElastique))
        }

        // -------------------------------------------
        // 3) Nouveau (si pas flemme déjà affiché plus haut)
        // -------------------------------------------
        if (!flemme) {
            b.textNouveau.text =
                if (!poidsDuCorps)
                    "Nouveau : ${s.nouveauPoids}kg × ${s.nouveauReps}"
                else
                    "Nouveau : ${s.nouveauReps} reps"
        }

        if (poidsDuCorps) {
            b.textNouveau.append(" ")
            b.textNouveau.append(getElastiquesColoredSlashes(s.nouveauBitMaskElastique))
        }

        // -------------------------------------------
        // 4) Progression
        // -------------------------------------------
        val progKg = s.progressionKg ?: 0f
        val progReps = (s.progressionReps ?: 0f)
        val isSamePerformance = progKg == 0f && progReps == 0f

        val txtProgression: String =
            if (s.ancienPoids == null) {
                "Nouveau"
            } else if (isSamePerformance) {
                "✓" // Coche verte pour une performance égale
            } else if (!poidsDuCorps) {
                when {
                    progKg > 0f -> "+${progKg} kg"
                    progKg < 0f -> "${progKg} kg"
                    else -> "0" // Devrait être couvert par isSamePerformance
                }
            } else {
                when {
                    progKg != 0f -> {
                        // Affiche la progression en kg si elle est non nulle
                        when {
                            progReps > 0f -> "+${progReps.toInt()} reps (+${progKg.toInt()} kg)"
                            // Correction ici : pas de 'else if', juste la condition
                            progReps == 0f && progKg > 0f -> "✓ (+${progKg.toInt()} kg)"
                            else -> "${progReps.toInt()} reps (${progKg.toInt()} kg)"
                        }
                    }

                    else -> {
                        // Sinon fallback sur la progression en reps
                        when {
                            progReps > 0 -> "+${progReps} reps"
                            progReps < 0 -> "${progReps} reps"
                            else -> "0" // Devrait être couvert par isSamePerformance
                        }
                    }
                }
            }

        b.textProgression.text = txtProgression

        // -------------------------------------------
        // 5) Couleur progression (si pas FLEMME)
        // -------------------------------------------
        val color = when{
            s.ancienPoids == null -> Color.GRAY
            // Ajout de la condition pour la coche verte
            isSamePerformance -> Color.parseColor("#4CAF50")
            (!poidsDuCorps && progKg > 0f) || (poidsDuCorps && (progReps > 0 || progKg > 0)) ->
                Color.parseColor("#4CAF50")
            (!poidsDuCorps && progKg < 0f) || (poidsDuCorps && (progReps < 0 || progKg < 0)) ->
                Color.parseColor("#F44336")
            else -> Color.GRAY
        }

        b.textProgression.setTextColor(color)
    }

    private fun decodeElastiques(bitmask: Int): List<Elastique> {
        return allElastiques.filter { elast ->
            (bitmask and elast.valeurBitmask) != 0
        }
    }

    private fun getElastiquesColoredSlashes(bitmask: Int): CharSequence {
        val list = decodeElastiques(bitmask)

        if (list.isEmpty()) return ""

        val sb = android.text.SpannableStringBuilder()

        list.forEach { elast ->
            val start = sb.length
            sb.append("/")

            // Couleur
            sb.setSpan(
                android.text.style.ForegroundColorSpan(elast.couleur),
                start, start + 1,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Gras
            sb.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                start, start + 1,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Taille plus grande (ex: 20sp)
            sb.setSpan(
                android.text.style.AbsoluteSizeSpan(20, true), // true = sp
                start, start + 1,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return sb
    }

    override fun getItemCount(): Int = series.size
}