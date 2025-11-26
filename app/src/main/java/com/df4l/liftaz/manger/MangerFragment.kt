package com.df4l.liftaz.manger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.Diete
import com.df4l.liftaz.data.DieteDao
import com.df4l.liftaz.databinding.FragmentMangerBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MangerFragment : Fragment() {

    private var _binding: FragmentMangerBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var dieteDao: DieteDao

    private var dieteActive: Diete? = null
    private var caloriesConsommees: Int = 0
    private var proteinesConsommees: Int = 0
    private var glucidesConsommees: Int = 0
    private var lipidesConsommees: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMangerBinding.inflate(inflater, container, false)

        database = AppDatabase.getDatabase(requireContext())
        dieteDao = database.dieteDao()

        lifecycleScope.launch {
            dieteActive = dieteDao.getActiveDiete()

            updateCaloriesAndMacros()
        }

        setupCurrentDate()
        setupEmptyStates()

        binding.fabManger.setOnClickListener { view ->
            showFabMenu(view)
        }

        return binding.root
    }

    suspend private fun updateCaloriesAndMacros() {
        val diete = dieteActive // Crée une copie locale pour la capture par la lambda
        if (diete != null) {
            val caloriesRestantes = diete.objCalories - caloriesConsommees
            binding.tvCaloriesRestantes.text = "$caloriesRestantes\ncalories\nrestantes"
            binding.cpbCalories.progressMax = diete.objCalories.toFloat()

            binding.tvProteinesManger.text = "${proteinesConsommees} / ${diete.objProteines}g"
            binding.tvGlucidesManger.text = "${glucidesConsommees} / ${diete.objGlucides}g"
            binding.tvLipidesManger.text = "${lipidesConsommees} / ${diete.objLipides}g"
        } else {
            binding.tvCaloriesRestantes.text = "${caloriesConsommees}\ncalories\nconsommées"
            binding.cpbCalories.progressMax = 100f // Valeur par défaut si aucune diète n'est active

            // Récupère le dernier poids pour un affichage plus informatif
            val dernierPoids =
                AppDatabase.getDatabase(requireContext()).entreePoidsDao().getLatestWeight()?.poids
            if (dernierPoids != null && dernierPoids > 0) {
                val proteinesParKg = "%.1f".format(proteinesConsommees / dernierPoids)
                val glucidesParKg = "%.1f".format(glucidesConsommees / dernierPoids)
                val lipidesParKg = "%.1f".format(lipidesConsommees / dernierPoids)

                binding.tvProteinesManger.text = "${proteinesConsommees}g (${proteinesParKg}g/kg)"
                binding.tvGlucidesManger.text = "${glucidesConsommees}g (${glucidesParKg}g/kg)"
                binding.tvLipidesManger.text = "${lipidesConsommees}g (${lipidesParKg}g/kg)"
            } else {
                binding.tvProteinesManger.text = "${proteinesConsommees}g"
                binding.tvGlucidesManger.text = "${glucidesConsommees}g"
                binding.tvLipidesManger.text = "${lipidesConsommees}g"
            }
        }

        binding.cpbCalories.progress = caloriesConsommees.toFloat()
    }

    private fun setupCurrentDate() {
        val dateFormat = SimpleDateFormat("EEEE dd LLLL", Locale.getDefault())
        val today = dateFormat.format(Date())

        binding.tvDate.text = today
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            }
    }


    /**
     * Comme il n’y a PAS d’adapter ni de repas,
     * on affiche juste "Rien pour l'instant" partout
     */
    private fun setupEmptyStates() {
        binding.emptyMatin.visibility = View.VISIBLE
        binding.rvMatin.visibility = View.GONE

        binding.emptyMidi.visibility = View.VISIBLE
        binding.rvMidi.visibility = View.GONE

        binding.emptyApresMidi.visibility = View.VISIBLE
        binding.rvApresMidi.visibility = View.GONE

        binding.emptySoir.visibility = View.VISIBLE
        binding.rvSoir.visibility = View.GONE
    }

    private fun showFabMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_manger_options, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_nourriture -> {
                    goToNourritureView()
                    true
                }
                R.id.action_dietes -> {
                    goToDieteView()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun goToDieteView()
    {
        val navController = findNavController()
        navController.navigate(R.id.action_mangerFragment_to_dieteFragment)
    }

    private fun goToNourritureView()
    {
        val navController = findNavController()
        navController.navigate(R.id.action_mangerFragment_to_nourritureFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
