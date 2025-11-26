package com.df4l.liftaz.manger.diete.dietesMenu

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Diete
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class DieteAdapter(
    private var dietes: List<Diete>,
    private val onActivate: (Diete) -> Unit,
    private val onDelete: (Diete) -> Unit,
    private val onLongClic: (Diete) -> Unit
) : RecyclerView.Adapter<DieteAdapter.DieteViewHolder>() {

    inner class DieteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // IDs provenant de item_diete.xml
        val nom: TextView = view.findViewById(R.id.textNomDiete)
        val calories: TextView = view.findViewById(R.id.tvCalories)
        val proteines: TextView = view.findViewById(R.id.tvProteinesDiete)
        val glucides: TextView = view.findViewById(R.id.tvGlucidesDiete)
        val lipides: TextView = view.findViewById(R.id.tvLipidesDiete)
        val badge: TextView = view.findViewById(R.id.textActifBadge)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteDiete)
        val pieChart: PieChart = view.findViewById(R.id.pieChartMini)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DieteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_diete, parent, false)
        return DieteViewHolder(view)
    }

    override fun onBindViewHolder(holder: DieteViewHolder, position: Int) {
        val diete = dietes[position]

        holder.btnDelete.setOnClickListener {
            onDelete(diete)
        }

        // Textes
        holder.nom.text = diete.nom
        holder.calories.text = "${diete.objCalories} calories"
        holder.proteines.text = "${diete.objProteines} g"
        holder.glucides.text = "${diete.objGlucides} g"
        holder.lipides.text = "${diete.objLipides} g"
        holder.badge.visibility = if (diete.actif) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener { onActivate(diete) }
        holder.btnDelete.setOnClickListener { onDelete(diete) }

        // Calcul des pourcentages
        val kcalProteines = diete.objProteines * 4
        val kcalGlucides = diete.objGlucides * 4
        val kcalLipides = diete.objLipides * 9

        val entries = listOf(
            PieEntry(kcalProteines.toFloat()),
            PieEntry(kcalGlucides.toFloat()),
            PieEntry(kcalLipides.toFloat())
        )

        val colors = listOf(
            Color.parseColor("#ec99b5"), // protéines
            Color.parseColor("#86e8cd"), // glucides
            Color.parseColor("#f2d678")  // lipides
        )

        // DataSet
        val dataSet = PieDataSet(entries, "").apply {
            setColors(colors)
            setDrawValues(false)  // Pas de texte sur les parts
            sliceSpace = 6f
        }

        // PieData
        holder.pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            isDrawHoleEnabled = false  // Pas de cercle blanc au milieu
            setDrawEntryLabels(false)  // Pas de texte sur les parts
            invalidate()
        }

        holder.itemView.setOnLongClickListener {
            onLongClic(diete)
            true
        }
    }


    override fun getItemCount() = dietes.size

    fun updateData(newList: List<Diete>) {
        dietes = newList
        notifyDataSetChanged()
    }
}