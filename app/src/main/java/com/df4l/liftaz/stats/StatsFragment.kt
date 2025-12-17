package com.df4l.liftaz.stats

import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.bold
import androidx.fragment.app.Fragment
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.EntreePoids
import com.df4l.liftaz.databinding.FragmentStatsBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
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
import kotlin.math.abs
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

        AlertDialog.Builder(requireContext())
            .setTitle("Ajouter une mesure")
            .setView(view)
            .setPositiveButton("Ajouter") { _, _ ->
                val poidsText = inputPoids.text.toString().trim()
                if (poidsText.isEmpty()) {
                    Toast.makeText(requireContext(), "Le poids est obligatoire", Toast.LENGTH_SHORT)
                        .show()
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
                        setupCorrelationChart() // Met également à jour le graphique de corrélation
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

        // Initialisation du style du graphique (inchangé)
        chart.setBackgroundColor(appWhite)
        chart.setDrawGridBackground(false)
        chart.xAxis.setDrawGridLines(false)
        chart.axisLeft.setDrawGridLines(false)
        chart.axisRight.setDrawGridLines(false)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)

        // Lancement de la coroutine pour charger les données
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Définir la période (derniers 14 jours)
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -13) // Changé de -6 à -13 pour avoir 14 jours
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startDate = cal.time

            // 2. Récupérer les données de la BDD
            val db = AppDatabase.getDatabase(requireContext())
            val weightEntriesFromDb = db.entreePoidsDao().getEntriesSince(startDate)
            val caloriesPerDayFromDb = db.mangerHistoriqueDao().getCaloriesSumSince(startDate)

            calculateAndDisplayMaintenance(startDate)

            // 3. Mapper les données pour le graphique
            // Créons une map pour accéder facilement aux calories par jour
            val caloriesMap = caloriesPerDayFromDb.associateBy(
                { TimeUnit.MILLISECONDS.toDays(it.date.time) }, // Clé: le jour
                { it.totalCalories.toFloat() } // Valeur: les calories
            )

            val weightChartEntries = mutableListOf<Entry>()
            val caloriesChartEntries = mutableListOf<Entry>()

            weightEntriesFromDb.forEachIndexed { index, entreePoids ->
                val xValue = index.toFloat()
                // Ajout de l'entrée de poids
                weightChartEntries.add(Entry(xValue, entreePoids.poids, entreePoids.date))

                // Recherche des calories correspondantes pour le même jour
                val day = TimeUnit.MILLISECONDS.toDays(entreePoids.date.time)
                val caloriesForDay = caloriesMap[day]
                if (caloriesForDay != null) {
                    caloriesChartEntries.add(Entry(xValue, caloriesForDay, entreePoids.date))
                }
            }

            // 4. Mettre à jour l'UI sur le thread principal
            withContext(Dispatchers.Main) {
                updateCorrelationChartUI(weightChartEntries, caloriesChartEntries)
            }
        }
    }

    private suspend fun calculateAndDisplayMaintenance(startDate14DaysAgo: Date) {
        val db = AppDatabase.getDatabase(requireContext())
        val weightDao = db.entreePoidsDao()
        val foodDao = db.mangerHistoriqueDao()

        // Définir les périodes S1 et S2
        val cal = Calendar.getInstance()
        cal.time = startDate14DaysAgo
        val week1_start = cal.time
        cal.add(Calendar.DAY_OF_YEAR, 6)
        val week1_end = cal.time
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val week2_start = cal.time
        cal.add(Calendar.DAY_OF_YEAR, 6)
        val week2_end = cal.time

        // Récupérer les données
        val week1_weights = weightDao.getEntriesBetween(week1_start, week1_end).map { it.poids }
        val week2_weights = weightDao.getEntriesBetween(week2_start, week2_end).map { it.poids }

        val totalCalories14Days = foodDao.getCaloriesSumBetween(week1_start, week2_end)
        // --- CORRECTION ---
        // Compter le nombre de jours où des calories ont été enregistrées
        val daysWithCaloriesCount = foodDao.countDaysWithCaloriesBetween(week1_start, week2_end)


        // Calculs
        val avgWeight1 = if (week1_weights.isNotEmpty()) week1_weights.average().toFloat() else 0f
        val avgWeight2 = if (week2_weights.isNotEmpty()) week2_weights.average().toFloat() else 0f

        // --- CORRECTION ---
        // Diviser par le nombre de jours réels avec données, au lieu de 14
        val avgCalories = if (totalCalories14Days != null && totalCalories14Days > 0 && daysWithCaloriesCount > 0) {
            (totalCalories14Days / daysWithCaloriesCount).toInt()
        } else {
            0
        }

        val resultText = SpannableStringBuilder()

        if (avgWeight1 > 0 && avgWeight2 > 0 && avgCalories > 0) {
            val weightChange = avgWeight2 - avgWeight1
            val maintenanceEstimation: String

            resultText.append("Poids S1: ${"%.1f".format(avgWeight1)} kg | Poids S2: ${"%.1f".format(avgWeight2)} kg\n")
            resultText.append("Calories moy./jour: $avgCalories kcal\n\n")

            when {
                // Poids stable
                abs(weightChange) < 0.25 -> {
                    maintenanceEstimation = "~ $avgCalories kcal"
                    resultText.append("Votre poids est stable. Maintien estimé : ")
                }
                // Perte de poids
                weightChange < -0.25 -> {
                    // val surplus = if (abs(weightChange) in 0.25..0.45) "200-500" else "500+" // La variable n'est pas utilisée, on peut la commenter
                    maintenanceEstimation = "~ ${avgCalories + 200} kcal"
                    resultText.append("Vous avez perdu du poids. Maintien estimé : ")
                }
                // Prise de poids
                weightChange > 0.25 -> {
                    // val deficit = if (weightChange in 0.25..0.45) "200-500" else "500+" // La variable n'est pas utilisée, on peut la commenter
                    maintenanceEstimation = "~ ${avgCalories - 200} kcal"
                    resultText.append("Vous avez pris du poids. Maintien estimé : ")
                }
                else -> maintenanceEstimation = "N/A"
            }

            resultText.bold { append(maintenanceEstimation) }

        } else {
            resultText.append("Données insuffisantes sur les 14 derniers jours pour estimer le maintien calorique.")
        }

        withContext(Dispatchers.Main) {
            binding.tvCaloriesTendance.text = resultText
            binding.tvCaloriesTendance.visibility = View.VISIBLE
        }
    }

    // Nouvelle fonction pour mettre à jour l'UI du graphique de corrélation
    private fun updateCorrelationChartUI(wEntries: List<Entry>, cEntries: List<Entry>) {
        val chart: LineChart = binding.correlationChart

        // Configuration des axes
        chart.xAxis.apply {
            textSize = 12f
            textColor = appBlack
            position = XAxis.XAxisPosition.BOTTOM
            labelRotationAngle = -45f
            granularity = 1f
            labelCount = wEntries.size
            isGranularityEnabled = true

            if (wEntries.isNotEmpty()) {
                axisMinimum = wEntries.first().x
                axisMaximum = wEntries.last().x
            }

            valueFormatter = object : ValueFormatter() {
                private val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
                override fun getAxisLabel(
                    value: Float,
                    axis: AxisBase?
                ): String {
                    val index = value.toInt()
                    return if (index >= 0 && index < wEntries.size) {
                        val entryData = wEntries[index].data
                        if (entryData is Date) sdf.format(entryData) else ""
                    } else {
                        ""
                    }
                }
            }
        }

        val minPoids = if (wEntries.isNotEmpty()) wEntries.minOf { it.y } - 1f else 70f
        val maxPoids = if (wEntries.isNotEmpty()) wEntries.maxOf { it.y } + 1f else 80f
        chart.axisLeft.apply {
            textSize = 12f
            textColor = appBlack
            axisMinimum = minPoids
            axisMaximum = maxPoids
        }

        val minCals = if (cEntries.isNotEmpty()) cEntries.minOf { it.y } - 100f else 1500f
        val maxCals = if (cEntries.isNotEmpty()) cEntries.maxOf { it.y } + 100f else 2500f
        chart.axisRight.apply {
            textSize = 12f
            textColor = appBlack
            axisMinimum = minCals
            axisMaximum = maxCals
        }

        // Configuration des datasets
        val weightSet = LineDataSet(wEntries, "Poids (kg)").apply {
            color = requireContext().getColor(R.color.purple_500)
            setCircleColor(requireContext().getColor(R.color.purple_500))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            axisDependency = YAxis.AxisDependency.LEFT
        }

        val calSet = LineDataSet(cEntries, "Calories (kcal)").apply {
            color = Color.RED
            setCircleColor(Color.RED)
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            axisDependency = YAxis.AxisDependency.RIGHT
        }

        // Légende
        chart.legend.isEnabled = true
        chart.legend.textColor = appBlack

        chart.data = LineData(weightSet, calSet)

        val marker = StatsMarker(requireContext())
        marker.chartView = chart
        chart.marker = marker

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
                Entry(
                    index.toFloat(),
                    entreePoids.poids,
                    entreePoids.date
                ) // Stocker la date dans l'objet "data"
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

            binding.tvPoidsTendance.text =
                "Tendance sur la semaine : %s%d g".format(sign, weightDiffGrams)
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
            position = XAxis.XAxisPosition.BOTTOM
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
            granularity = 1f

            // 3. Spécifier les valeurs min/max de l'axe pour lui donner un cadre clair
            if (entries.isNotEmpty()) {
                axisMinimum = entries.first().x
                axisMaximum = entries.last().x
            }

            valueFormatter = object : ValueFormatter() {
                private val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())

                override fun getAxisLabel(
                    value: Float,
                    axis: AxisBase?
                ): String {
                    // L'index de notre entrée
                    val index = value.toInt()

                    // Récupérer la liste des entrées du premier dataset
                    val chartEntries = (chart.data?.getDataSetByIndex(0) as? LineDataSet)?.values

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