package com.df4l.liftaz.pousser

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
import com.df4l.liftaz.pousser.exercices.creationExercice.CreateExerciceDialog
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
                R.id.action_exercices -> {
                    goToExercicesView()
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

    private fun goToExercicesView()
    {
        val navController = findNavController()
        navController.navigate(R.id.action_pousserFragment_to_elastiquesFragment)
    }

    private fun createNewSeanceView()
    {
        val navController = findNavController()
        navController.navigate(R.id.action_pousserFragment_to_creationSeanceFragment)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}