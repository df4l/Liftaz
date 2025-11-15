package com.df4l.liftaz.manger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
