package com.df4l.liftaz.pousser.seances.creationSeance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.ExerciceDao
import com.df4l.liftaz.data.MuscleDao
import com.df4l.liftaz.databinding.FragmentCreationseanceBinding
import com.df4l.liftaz.pousser.exercices.creationExercice.CreateExerciceDialog
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class CreationSeanceFragment : Fragment() {

    private var _binding: FragmentCreationseanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var muscleDao: MuscleDao
    private lateinit var exerciceDao: ExerciceDao

    private lateinit var recyclerView: RecyclerView
    private lateinit var exerciceSeanceAdapter: ExerciceSeanceAdapter

    private val exerciceSeanceList = mutableListOf<ExerciceSeanceUi>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreationseanceBinding.inflate(inflater, container, false)

        database = AppDatabase.getDatabase(requireContext())
        muscleDao = database.muscleDao()
        exerciceDao = database.exerciceDao()

        binding.fabAddExercice.setOnClickListener { view ->
            CreateExerciceDialog(
                context = requireContext(),
                lifecycleScope = lifecycleScope,
                exerciceDao = exerciceDao,
                muscleDao = muscleDao,
                parentView = requireView()
            ).show()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAddExercice: ImageButton = view.findViewById(R.id.btnAddExercice)

        btnAddExercice.setOnClickListener {
            addExerciceToSeance(0)
        }

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupFrequence)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupJours)
        val intervalLayout = view.findViewById<LinearLayout>(R.id.layoutIntervalle)
        val numberSlider = view.findViewById<Slider>(R.id.sliderIntervalle)
        val textProchaines = view.findViewById<TextView>(R.id.textProchainesSeances)
        var intervalleTexte = view.findViewById<TextView>(R.id.IntervalleTexte)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioJoursSemaine -> {
                    chipGroup.visibility = View.VISIBLE
                    intervalLayout.visibility = View.INVISIBLE

                }
                R.id.radioIntervalle -> {
                    chipGroup.visibility = View.INVISIBLE
                    intervalLayout.visibility = View.VISIBLE
                    updateNextDates(numberSlider.value.toInt(), textProchaines, intervalleTexte)
                }
            }
        }

        numberSlider.addOnChangeListener { _, value, _ ->
            updateNextDates(value.toInt(), textProchaines, intervalleTexte)
        }

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)



        exerciceSeanceAdapter = ExerciceSeanceAdapter(exerciceSeanceList) { position ->
            addExerciceToSeance(position)
        }
        recyclerView.adapter = exerciceSeanceAdapter

    }

    private fun updateNextDates(interval: Int, textView: TextView, textView2: TextView) {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale.FRENCH)

        val nextDates = List(3) { today.plusDays(((it + 1) * interval).toLong()) }

        val todayFormatted = today.format(formatter)
        val nextFormatted = nextDates.joinToString(", ") { it.format(formatter) }

        textView.text = "À compter d’aujourd’hui ($todayFormatted), prochaines séances :\n$nextFormatted"
        textView2.text = "Séance à effectuer tous les $interval jours"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addExerciceToSeance(position: Int) {
        ShowExercicesListDialog(
            context = requireContext(),
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            exerciceDao = exerciceDao,
            muscleDao = muscleDao,
            parentView = requireView()
        ) { exercice ->
            viewLifecycleOwner.lifecycleScope.launch {
                val muscle = muscleDao.getMuscle(exercice.idMuscleCible).first()

                val nouvelExercice = if (exercice.poidsDuCorps) {
                    ExerciceSeanceUi.PoidsDuCorps(
                        idExercice = exercice.id,
                        nom = exercice.nom,
                        muscle = muscle.nom,
                    )
                } else {
                    ExerciceSeanceUi.AvecFonte(
                        idExercice = exercice.id,
                        nom = exercice.nom,
                        muscle = muscle.nom,
                    )
                }

                exerciceSeanceList.add(position, nouvelExercice)
                exerciceSeanceAdapter.notifyItemInserted(position)
            }
        }.show()
    }

}