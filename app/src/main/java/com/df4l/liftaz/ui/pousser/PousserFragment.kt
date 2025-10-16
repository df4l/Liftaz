package com.df4l.liftaz.ui.pousser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.MuscleDao
import com.df4l.liftaz.databinding.FragmentPousserBinding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.df4l.liftaz.data.ExerciceDao
import com.df4l.liftaz.data.Muscle
import com.example.myapp.ui.dialogs.CreateExerciceDialog
import kotlinx.coroutines.launch

class PousserFragment : Fragment() {

    private var _binding: FragmentPousserBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var muscleDao: MuscleDao
    private lateinit var exerciceDao: ExerciceDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPousserBinding.inflate(inflater, container, false)
        val root: View = binding.root

        database = AppDatabase.getDatabase(requireContext())
        muscleDao = database.muscleDao()
        exerciceDao = database.exerciceDao()

        lifecycleScope.launch {
            if (muscleDao.count() == 0) {
                val defaultMuscles = listOf(
                    Muscle(nom = "Dos"),
                    Muscle(nom = "Épaules"),
                    Muscle(nom = "Biceps"),
                    Muscle(nom = "Fessiers"),
                    Muscle(nom = "Triceps"),
                    Muscle(nom = "Avant-bras"),
                    Muscle(nom = "Quadriceps"),
                    Muscle(nom = "Ischio-jambiers"),
                    Muscle(nom = "Mollets"),
                    Muscle(nom = "Abdominaux"),
                    Muscle(nom = "Cou"),
                    Muscle(nom = "Trapèzes")
                )
                defaultMuscles.forEach { muscleDao.insert(it) }
            }
        }

        binding.fabAdd.setOnClickListener { view ->
            showFabMenu(view)
        }

        return root
    }

    private fun showFabMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_pousser_options, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_add_exercice -> {
                    CreateExerciceDialog(
                        context = requireContext(),
                        lifecycleScope = lifecycleScope,
                        exerciceDao = exerciceDao,
                        muscleDao = muscleDao,
                        parentView = requireView()
                    ).show()
                    true
                }
                R.id.action_create_seance -> {
                    createNewSeanceView()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun createNewSeanceView()
    {
        val navController = findNavController()
        navController.navigate(R.id.action_pousserFragment_to_createSeanceFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}