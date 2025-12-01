package com.df4l.liftaz.stats

import android.content.Context
import android.widget.TextView
import java.util.Locale
import com.df4l.liftaz.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlin.text.format

class StatsMarker(context: Context) : MarkerView(context, R.layout.marker_stats) {

    private val tvValue: TextView = findViewById(R.id.tvMarkerValue)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e == null) {
            super.refreshContent(e, highlight)
            return
        }

        // Récupère tous les DataSets du graphique
        val dataSets = chartView.data.dataSets
        // L'abscisse (X) du point sélectionné, qui est commune à toutes les entrées du jour.
        val xValue = e.x

        val stringBuilder = StringBuilder()

        // Itère sur chaque DataSet (ex: Poids, Calories)
        for (dataSet in dataSets) {
            // Trouve l'entrée dans ce DataSet qui a la même valeur X
            val entryForX = dataSet.getEntryForXValue(xValue, Float.NaN)

            // Si une entrée correspondante est trouvée
            if (entryForX != null) {
                val value = entryForX.y
                val label = dataSet.label
                // Extrait l'unité du label, comme avant
                val unit = label.substringAfter('(', "").substringBefore(')')

                // Ajoute la ligne au StringBuilder
                if (stringBuilder.isNotEmpty()) {
                    stringBuilder.append("\n") // Ajoute une nouvelle ligne si ce n'est pas la première entrée
                }

                val formattedValue = if (value == value.toInt().toFloat()) {
                    String.format(Locale.FRENCH, "%d", value.toInt())
                } else {
                    // Formate à une décimale pour la propreté (ex: 75.2)
                    String.format(Locale.FRENCH, "%.1f", value)
                }

                stringBuilder.append("$formattedValue $unit")
            }
        }

        tvValue.text = stringBuilder.toString()
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        // Il faudra peut-être ajuster l'offset si le marker devient plus grand
        return MPPointF(-(width / 2f), -height.toFloat() - 15f)
    }
}