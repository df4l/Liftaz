package com.df4l.liftaz.ui.pousser.creationSeance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.ExerciceDao
import com.df4l.liftaz.data.MuscleDao
import com.df4l.liftaz.databinding.FragmentCreationseanceBinding
import com.example.myapp.ui.dialogs.CreateExerciceDialog
import com.example.myapp.ui.dialogs.ShowExercicesListDialog
import com.google.android.material.snackbar.Snackbar

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
            showFabMenu(view)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAddExercice: ImageButton = view.findViewById(R.id.btnAddExercice)

        btnAddExercice.setOnClickListener {
            ShowExercicesListDialog(
                context = requireContext(),
                lifecycleScope = viewLifecycleOwner.lifecycleScope,
                exerciceDao = exerciceDao,
                muscleDao = muscleDao,
                parentView = requireView()
            ) {
                    exercice ->
                // 👉 Tu récupères ici directement l'exercice choisi
                // Tu peux ouvrir une autre vue, afficher des détails, lancer une édition, etc.
                Snackbar.make(requireView(), "Exercice sélectionné : ${exercice.nom}", Snackbar.LENGTH_LONG).show()
            }.show()
        }

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        exerciceSeanceList.add(ExerciceSeanceUi(1, "Développé-couché", "Pectoraux",4,8,12))

        exerciceSeanceAdapter = ExerciceSeanceAdapter(exerciceSeanceList)
        recyclerView.adapter = exerciceSeanceAdapter

    }

    private fun showFabMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_creationseance_options, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_add_exercice -> {
                    ShowExercicesListDialog(
                        context = requireContext(),
                        lifecycleScope = viewLifecycleOwner.lifecycleScope,
                        exerciceDao = exerciceDao,
                        muscleDao = muscleDao,
                        parentView = requireView()
                    ) {
                            exercice ->
                        // 👉 Tu récupères ici directement l'exercice choisi
                        // Tu peux ouvrir une autre vue, afficher des détails, lancer une édition, etc.
                        Snackbar.make(requireView(), "Exercice sélectionné : ${exercice.nom}", Snackbar.LENGTH_LONG).show()
                    }.show()
                    true
                }
                R.id.action_create_exercice -> {
                    CreateExerciceDialog(
                        context = requireContext(),
                        lifecycleScope = lifecycleScope,
                        exerciceDao = exerciceDao,
                        muscleDao = muscleDao,
                        parentView = requireView()
                    ).show()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}