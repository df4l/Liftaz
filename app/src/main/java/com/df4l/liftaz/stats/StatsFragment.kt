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
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

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

                // Sauvegarde en BDD et mise à jour de l'UI
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = AppDatabase.getDatabase(requireContext()).entreePoidsDao()
                    dao.insert(entree)
                    updateLatestEntry() // Met à jour le poids actuel affiché

                    // --- AJOUT IMPORTANT ---
                    // On revient sur le thread principal pour mettre à jour le graphique
                    withContext(Dispatchers.Main) {
                        setupWeightChart() // Recharge et redessine le graphique et la tendance
                    }
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
        CoroutineScope(Dispatchers.IO).launch {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -6)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startDate = cal.time

            val dao = AppDatabase.getDatabase(requireContext()).entreePoidsDao()
            val entriesFromDb = dao.getEntriesSince(startDate)

            val chartEntries = entriesFromDb.mapIndexed { index, entreePoids ->
                // Utiliser l'index comme valeur X
                Entry(index.toFloat(), entreePoids.poids, entreePoids.date) // Stocker la date dans l'objet "data"
            }

            withContext(Dispatchers.Main) {
                updateChartUI(chartEntries)
            }
        }
    }

    private fun updateChartUI(entries: List<Entry>) {
        val chart: LineChart = binding.lineChartWeight

        if (entries.size >= 2) {
            val firstWeight = entries.first().y
            val lastWeight = entries.last().y

            val weightDiffKg = lastWeight - firstWeight
            val weightDiffGrams = (weightDiffKg * 1000).roundToInt()
            val sign = if (weightDiffGrams >= 0) "+" else ""

            binding.tvPoidsTendance.text = "Tendance sur la semaine : %s%d g".format(sign, weightDiffGrams)
            binding.tvPoidsTendance.setTextColor(appBlack) // Utilisation d'une couleur neutre
            binding.tvPoidsTendance.visibility = View.VISIBLE
        } else {
            binding.tvPoidsTendance.visibility = View.GONE
        }

        chart.setBackgroundColor(appWhite)
        chart.setDrawGridBackground(false)
        chart.xAxis.setDrawGridLines(false)
        chart.axisLeft.setDrawGridLines(false)
        chart.axisRight.isEnabled = false

        chart.xAxis.apply {
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            textColor = appBlack
            textSize = 12f
            labelRotationAngle = -45f
            setDrawGridLines(false)

            // --- MODIFICATIONS IMPORTANTES ---

            // 1. Définir le nombre exact de labels que nous attendons.
            // S'il y a 7 jours, il y aura 7 labels.
            // Utilisez la taille de la liste d'entrées pour être dynamique.
            labelCount = entries.size
            isGranularityEnabled = true // S'assurer que la granularité est active

            // 2. Définir l'espacement minimum. Un jour.
            granularity = TimeUnit.DAYS.toMillis(1).toFloat()

            // 3. Spécifier les valeurs min/max de l'axe pour lui donner un cadre clair
            if (entries.isNotEmpty()) {
                axisMinimum = entries.first().x
                axisMaximum = entries.last().x
            }

            valueFormatter = object : ValueFormatter() {
                private val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())

                override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase?): String {
                    // L'index de notre entrée
                    val index = value.toInt()

                    // Récupérer la liste des entrées du premier dataset
                    val chartEntries = (chart.data.getDataSetByIndex(0) as? LineDataSet)?.values

                    // Vérifier si l'index est valide et récupérer la date depuis l'objet "data"
                    return if (chartEntries != null && index >= 0 && index < chartEntries.size) {
                        val entryData = chartEntries[index].data
                        if (entryData is Date) {
                            sdf.format(entryData)
                        } else {
                            "" // Pas de date trouvée
                        }
                    } else {
                        "" // Index hors limites
                    }
                }
            }
        }

        if (entries.isNotEmpty()) {
            val minPoids = entries.minOf { it.y } - 2f
            val maxPoids = entries.maxOf { it.y } + 2f
            chart.axisLeft.axisMinimum = minPoids
            chart.axisLeft.axisMaximum = maxPoids
        } else {
            chart.axisLeft.axisMinimum = 73f
            chart.axisLeft.axisMaximum = 77f
        }

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

        chart.invalidate() // Rafraîchir le graphique
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
