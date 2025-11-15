package com.df4l.liftaz.soulever.bilan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.databinding.FragmentBilanBinding
import kotlinx.coroutines.launch

class BilanFragment : Fragment() {

    private var _binding: FragmentBilanBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BilanViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBilanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val ctx = requireContext()
        val db = AppDatabase.getDatabase(ctx)

        val idSeance = requireArguments().getInt("idSeance")
        val idSeanceHistoriqueActuelle = requireArguments().getInt("idSeanceHistorique")

        viewModel = BilanViewModel(
            db.seanceHistoriqueDao(),
            db.serieDao(),
            db.exerciceDao(),
            db.muscleDao(),
            idSeance,
            idSeanceHistoriqueActuelle
        )

        binding.recyclerBilan.layoutManager = LinearLayoutManager(ctx)

        viewModel.bilan.observe(viewLifecycleOwner) { liste ->
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(requireContext())
                val elastiques = db.elastiqueDao().getAll()
                binding.recyclerBilan.adapter = BilanExerciceAdapter(liste, elastiques)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.navigation_soulever, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
