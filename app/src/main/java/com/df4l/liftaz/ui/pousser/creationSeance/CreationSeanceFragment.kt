package com.df4l.liftaz.ui.pousser.creationSeance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.ExerciceDao
import com.df4l.liftaz.data.MuscleDao
import com.df4l.liftaz.databinding.FragmentCreationseanceBinding
import com.example.myapp.ui.dialogs.CreateExerciceDialog

class CreationSeanceFragment : Fragment() {

    private var _binding: FragmentCreationseanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var muscleDao: MuscleDao
    private lateinit var exerciceDao: ExerciceDao

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
    }

    private fun showFabMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_creationseance_options, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_add_exercice -> {

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