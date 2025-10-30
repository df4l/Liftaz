package com.df4l.liftaz.pousser.creationSeance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        //exerciceSeanceList.add( ExerciceSeanceUi(4, "Développé-couché", "Pectoraux",4,8,12))
        //exerciceSeanceList.add(0, ExerciceSeanceUi(3, "Soulevé de terre", "Quoicoubeh",4,8,12))

        exerciceSeanceAdapter = ExerciceSeanceAdapter(exerciceSeanceList) { position ->
            addExerciceToSeance(position)
        }
        recyclerView.adapter = exerciceSeanceAdapter

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
        ) {
            exercice ->
            viewLifecycleOwner.lifecycleScope.launch {
                val muscle = muscleDao.getMuscle(exercice.idMuscleCible).first()
                exerciceSeanceList.add(position, ExerciceSeanceUi(exercice.id, exercice.nom, muscle.nom))
                exerciceSeanceAdapter.notifyItemInserted(position)
            }
        }.show()
    }
}