package com.df4l.liftaz.manger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.df4l.liftaz.R
import com.df4l.liftaz.databinding.FragmentMangerBinding

class MangerFragment : Fragment() {

    private var _binding: FragmentMangerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMangerBinding.inflate(inflater, container, false)

        setupEmptyStates()

        binding.fabManger.setOnClickListener { view ->
            showFabMenu(view)
        }

        return binding.root
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
