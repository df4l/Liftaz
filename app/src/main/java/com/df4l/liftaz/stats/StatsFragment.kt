package com.df4l.liftaz.stats

import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.EntreePoids
import com.df4l.liftaz.databinding.FragmentStatsBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatsFragment : Fragment(R.layout.fragment_stats) {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val appBlack by lazy { requireContext().getColor(R.color.black) }
    private val appWhite by lazy { requireContext().getColor(R.color.white) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatsBinding.bind(view)

        binding.cardPoids.setOnClickListener {
            showAddWeightDialog()
        }

        setupWeightChart()
        setupCorrelationChart()

        CoroutineScope(Dispatchers.IO).launch {
            updateLatestEntry()
        }
    }

    private fun showAddWeightDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_poids, null)
        val inputPoids = view.findViewById<EditText>(R.id.etPoids)
        val inputBodyFat = view.findViewById<EditText>(R.id.etBodyFat)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Ajouter une mesure")
            .setView(view)
            .setPositiveButton("Ajouter") { _, _ ->
                val poidsText = inputPoids.text.toString().trim()
                if (poidsText.isEmpty()) {
                    Toast.makeText(requireContext(), "Le poids est obligatoire", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val poids = poidsText.toFloatOrNull()
                if (poids == null) {
                    Toast.makeText(requireContext(), "Poids invalide", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val bodyFat = inputBodyFat.text.toString().toFloatOrNull()

                val entree = EntreePoids(
                    date = Date(),
                    poids = poids,
                    bodyFat = bodyFat
                )

                // Sauvegarde en BDD
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = AppDatabase.getDatabase(requireContext()).entreePoidsDao()
                    dao.insert(entree)
                    updateLatestEntry()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private suspend fun updateLatestEntry() {
        val dao = AppDatabase.getDatabase(requireContext()).entreePoidsDao()
        val allEntries = dao.getAll()
        val latest = allEntries.lastOrNull()

        requireActivity().runOnUiThread {
            if (latest != null) {
                val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                binding.tvPoids.text = "${latest.poids} kg"
                binding.tvDatePoids.text = "Dernière mesure : ${sdf.format(latest.date)}"

                if (latest.bodyFat != null) {
                    binding.tvBodyfat.text = "${latest.bodyFat}%"
                    binding.tvDateBodyfat.text = "Dernière mesure : ${sdf.format(latest.date)}"
                } else {
                    binding.tvBodyfat.text = "N/D"
                    binding.tvDateBodyfat.text = "Dernière mesure : -"
                }
            } else {
                binding.tvPoids.text = "N/D"
                binding.tvDatePoids.text = "Dernière mesure : -"
                binding.tvBodyfat.text = "N/D"
                binding.tvDateBodyfat.text = "Dernière mesure : -"
            }
        }
    }

    private fun setupCorrelationChart() {
        val chart: LineChart = binding.correlationChart

        chart.setBackgroundColor(appWhite)
        chart.setDrawGridBackground(false)
        chart.xAxis.setDrawGridLines(false)
        chart.axisLeft.setDrawGridLines(false)
        chart.axisRight.setDrawGridLines(false)
        chart.description.isEnabled = false

        // Pas de légende sur le deuxième graphique
        chart.legend.isEnabled = false

        val dates = generateLastSevenDays()

        chart.xAxis.apply {
            textSize = 16f
            textColor = appBlack
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase?): String {
                    val i = value.toInt() - 1
                    return if (i in dates.indices) dates[i] else ""
                }
            }
        }

        chart.axisLeft.apply {
            textSize = 16f
            textColor = appBlack
            axisMinimum = 73f
            axisMaximum = 77f
        }

        chart.axisRight.apply {
            textSize = 16f
            textColor = appBlack
            axisMinimum = 1500f
            axisMaximum = 2500f
        }

        val weightData = listOf(75.5f, 75.8f, 75.2f, 75.0f, 74.8f, 75.1f, 74.9f)
        val calData = listOf(2200f, 2400f, 1900f, 2000f, 2100f, 2300f, 1950f)

        val wEntries = weightData.mapIndexed { i, v -> Entry((i + 1).toFloat(), v) }
        val cEntries = calData.mapIndexed { i, v -> Entry((i + 1).toFloat(), v) }

        val weightSet = LineDataSet(wEntries, "Poids (kg)").apply {
            color = requireContext().getColor(R.color.purple_500)
            setCircleColor(requireContext().getColor(R.color.purple_500))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            axisDependency = com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT
        }

        val calSet = LineDataSet(cEntries, "Calories (kcal)").apply {
            color = Color.RED
            setCircleColor(Color.RED)
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            axisDependency = com.github.mikephil.charting.components.YAxis.AxisDependency.RIGHT
        }

        chart.data = LineData(weightSet, calSet)

        val marker = StatsMarker(requireContext())
        marker.chartView = chart
        chart.marker = marker

        // Désactivation du zoom et du drag
        chart.setTouchEnabled(true)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)

        chart.invalidate()
    }

    private fun setupWeightChart() {
        val chart: LineChart = binding.lineChartWeight

        chart.setBackgroundColor(appWhite)
        chart.setDrawGridBackground(false)
        chart.xAxis.setDrawGridLines(false)
        chart.axisLeft.setDrawGridLines(false)
        chart.axisRight.isEnabled = false

        val dates = generateLastSevenDays()

        chart.xAxis.apply {
            textSize = 16f
            textColor = appBlack
            granularity = 1f
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase?): String {
                    val i = value.toInt() - 1
                    return if (i in dates.indices) dates[i] else ""
                }
            }
        }

        val weights = listOf(75.5f, 75.8f, 75.2f, 75.0f, 74.8f, 75.1f, 74.9f)
        val entries = weights.mapIndexed { i, v -> Entry((i + 1).toFloat(), v) }

        val dataSet = LineDataSet(entries, "Poids (kg)").apply {
            color = requireContext().getColor(R.color.purple_500)
            setCircleColor(requireContext().getColor(R.color.purple_500))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
        }

        chart.data = LineData(dataSet)

        chart.axisLeft.apply {
            textSize = 16f
            textColor = appBlack
            axisMinimum = 73f
            axisMaximum = 77f
        }

        chart.description.isEnabled = false
        chart.legend.isEnabled = false

        val marker = StatsMarker(requireContext())
        marker.chartView = chart
        chart.marker = marker

        // Désactivation du zoom et du drag
        chart.setTouchEnabled(true)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)

        // Fix cropping bottom
        chart.setExtraOffsets(0f, 0f, 28f, 18f)

        chart.invalidate()
    }

    private fun generateLastSevenDays(): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        val f = SimpleDateFormat("dd/MM", Locale.getDefault())
        cal.add(Calendar.DAY_OF_YEAR, -6)
        repeat(7) {
            list.add(f.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
