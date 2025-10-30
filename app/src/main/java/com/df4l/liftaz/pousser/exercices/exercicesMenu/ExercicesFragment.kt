package com.df4l.liftaz.pousser.exercices.exercicesMenu

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.ExerciceDao
import com.df4l.liftaz.data.MuscleDao
import com.df4l.liftaz.pousser.exercices.creationExercice.CreateExerciceDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ExercicesFragment : Fragment(R.layout.fragment_exercices) {

    private lateinit var viewModel: ExercicesViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton

    private lateinit var database: AppDatabase
    private lateinit var muscleDao: MuscleDao
    private lateinit var exerciceDao: ExerciceDao

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerExercices)
        fab = view.findViewById(R.id.fab_exercicesMenu)

        database = AppDatabase.getDatabase(requireContext())
        muscleDao = database.muscleDao()
        exerciceDao = database.exerciceDao()

        val db = AppDatabase.getDatabase(requireContext())
        viewModel = ExercicesViewModel(db.muscleDao(), db.exerciceDao())

        viewModel.exercicesParMuscle.observe(viewLifecycleOwner) { data ->
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = ExercicesAdapter(data)

        }

        fab.setOnClickListener {
            CreateExerciceDialog(
                context = requireContext(),
                lifecycleScope = lifecycleScope,
                exerciceDao = exerciceDao,
                muscleDao = muscleDao,
                parentView = requireView()
            ).show()
        }
    }
}
