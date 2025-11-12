package com.df4l.liftaz.soulever

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.MuscleDao
import com.df4l.liftaz.databinding.FragmentSouleverBinding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.data.Exercice
import com.df4l.liftaz.data.ExerciceDao
import com.df4l.liftaz.data.Muscle
import com.df4l.liftaz.data.Seance
import com.df4l.liftaz.data.SeanceAvecExercices
import com.df4l.liftaz.data.SeanceDao
import com.df4l.liftaz.data.TypeFrequence
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class SouleverFragment : Fragment() {

    private var _binding: FragmentSouleverBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var muscleDao: MuscleDao
    private lateinit var exerciceDao: ExerciceDao
    private lateinit var seanceDao: SeanceDao

    var seanceDuJour: Seance? = null
        private set

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSouleverBinding.inflate(inflater, container, false)
        val root: View = binding.root

        database = AppDatabase.getDatabase(requireContext())
        muscleDao = database.muscleDao()
        exerciceDao = database.exerciceDao()
        seanceDao = database.seanceDao()

        lifecycleScope.launch {
            if (muscleDao.count() == 0) {
                val defaultMuscles = listOf(
                    Muscle(nom = "Dos", nomImage = "dos"),
                    Muscle(nom = "Épaules", nomImage = "epaules"),
                    Muscle(nom = "Biceps", nomImage = "biceps"),
                    Muscle(nom = "Fessiers", nomImage = "fessiers"),
                    Muscle(nom = "Triceps", nomImage="triceps"),
                    Muscle(nom = "Avant-bras", nomImage = "avantbras"),
                    Muscle(nom = "Quadriceps", nomImage = "quadriceps"),
                    Muscle(nom = "Ischio-jambiers", nomImage = "ischiojambiers"),
                    Muscle(nom = "Mollets", nomImage = "mollets"),
                    Muscle(nom = "Abdominaux", nomImage = "abdominaux"),
                    Muscle(nom = "Cou", nomImage = "cou"),
                    Muscle(nom = "Trapèzes", nomImage="trapezes"),
                    Muscle(nom = "Pectoraux", nomImage="pectoraux")
                )
                defaultMuscles.forEach { muscleDao.insert(it) }
            }
        }

        lifecycleScope.launch {
            val seances = seanceDao.getAllSeances()

            seanceDuJour = seances.firstOrNull { seance ->
                evaluateSeanceForToday(seance)
            }
            updateUI()
        }


        binding.fabAdd.setOnClickListener { view ->
            showFabMenu(view)
        }

        val listener = View.OnClickListener {
            lifecycleScope.launch {
                val seances = seanceDao.getSeancesAvecExercices()

                val musclesList = muscleDao.getAllMuscles()
                val exercicesList = exerciceDao.getAllExercices()

                val musclesMap = musclesList.associateBy({ it.id }, { it.nom })
                val exerciceMap = exercicesList.associateBy { it.id }

                showSeancePickerDialog(seances, musclesMap, exerciceMap)
            }
        }

        binding.btnChooseSeance.setOnClickListener(listener)
        binding.btnStartAnotherSeance.setOnClickListener(listener)

        return root
    }

    private fun evaluateSeanceForToday(seance: Seance): Boolean
    {
        val today = LocalDate.now()

        return when (seance.typeFrequence) {
            TypeFrequence.JOURS_SEMAINE -> {
                val todayIndex = today.dayOfWeek.value
                seance.joursSemaine?.contains(todayIndex) == true
            }

            TypeFrequence.INTERVALLE -> {
                val dateAjout = seance.dateAjout.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

                val daysSince = ChronoUnit.DAYS.between(
                    dateAjout,
                    today
                ).toInt()

                daysSince % (seance.intervalleJours ?: 1) == 0 && today != dateAjout
            }
        }
    }

    private fun updateUI() {
        if (seanceDuJour == null) {
            binding.SeanceTodayContainer.visibility = View.GONE
            binding.NoSeanceTodayContainer.visibility = View.VISIBLE

        } else {
            binding.SeanceTodayContainer.visibility = View.VISIBLE
            binding.NoSeanceTodayContainer.visibility = View.GONE

            // Afficher le nom
            binding.textNomSeance.text = seanceDuJour!!.nom

            // RÉCUPÉRER LES EXERCICES LIÉS À LA SÉANCE
            lifecycleScope.launch {
                val exercicesSeance = database.exerciceSeanceDao().getExercicesForSeance(seanceDuJour!!.id)

                val previewItems = exercicesSeance.map { se ->
                    val exercice = exerciceDao.getExerciceById(se.idExercice)
                    val muscleName = muscleDao.getNomMuscleById(exercice.idMuscleCible)

                    val repsText = if (exercice.poidsDuCorps) {
                        // Ex : 4 x 12
                        "${se.nbSeries} x ${se.minReps}"
                    } else {
                        // Ex : 4 x 8-12
                        "${se.nbSeries} x ${se.minReps}-${se.maxReps}"
                    }

                    ExercicePreviewItem(
                        nomExercice = exercice.nom,
                        nomMuscle = muscleName,
                        texteSeriesEtReps = repsText
                    )
                }

                binding.recyclerPreviewExercices.layoutManager = LinearLayoutManager(requireContext())
                binding.recyclerPreviewExercices.adapter = PreviewExerciceAdapter(previewItems)
            }

            binding.btnStart.setOnClickListener {
                val bundle = bundleOf("SEANCE_ID" to seanceDuJour!!.id)
                findNavController().navigate(R.id.action_souleverFragment_to_entrainementFragment, bundle)
            }

        }

    }

    private fun showSeancePickerDialog(
        seances: List<SeanceAvecExercices>,
        musclesMap: Map<Int, String>,
        exerciceMap: Map<Int, Exercice>
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_seance_picker, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerSeances)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setNegativeButton("Annuler", null)
            .create()

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = SeancePickerAdapter(seances, musclesMap, exerciceMap) { seance ->
            val bundle = bundleOf("SEANCE_ID" to seance.seance.id)

            dialog.dismiss()

            findNavController().navigate(
                R.id.action_souleverFragment_to_entrainementFragment,
                bundle
            )
        }

        dialog.show()
    }


    private fun showFabMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_soulever_options, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_exercices -> {
                    goToExercicesView()
                    true
                }
                R.id.action_create_seance -> {
                    goToSeancesView()
                    true
                }
                R.id.action_motivationfioul -> {
                    goToMotivationFioulView()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun goToMotivationFioulView()
    {
        val navController = findNavController()
        navController.navigate(R.id.action_souleverFragment_to_motivationFioulFragment)
    }


    private fun goToExercicesView()
    {
        val navController = findNavController()
        navController.navigate(R.id.action_souleverFragment_to_exercicesFragment)
    }

    private fun goToSeancesView()
    {
        val navController = findNavController()
        navController.navigate(R.id.action_souleverFragment_to_seancesFragment)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}