package com.df4l.liftaz.pousser.elastiques

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.Elastique
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ElastiquesFragment : Fragment() {

    private lateinit var viewModel: ElastiqueViewModel
    private lateinit var adapter: ElastiqueAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_elastiques, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dao = AppDatabase.getDatabase(requireContext()).elastiqueDao()
        val factory = ElastiqueViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[ElastiqueViewModel::class.java]

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerElastiques)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = ElastiqueAdapter(mutableListOf()) { elastique ->
            viewModel.delete(elastique)
        }

        recycler.adapter = adapter

        // Gestion du drag & drop
        val touchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                adapter.swapItems(from, to)
                return true
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewModel.updateBitmasks(adapter.getList())
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })

        touchHelper.attachToRecyclerView(recycler)

        // Observation du flux d’élastiques
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.elastiques.collectLatest { list ->
                adapter = ElastiqueAdapter(list.toMutableList()) { elastique ->
                    viewModel.delete(elastique)
                }
                recycler.adapter = adapter

                if (list.isEmpty()) populateTestElastiques()
            }
        }
    }

    private fun populateTestElastiques() {
        val colors = listOf(
            0xFFE57373.toInt(), // Rouge
            0xFF64B5F6.toInt(), // Bleu
            0xFFFFD54F.toInt(), // Jaune
            0xFF81C784.toInt()  // Vert
        )

        val testElastiques = colors.mapIndexed { index, color ->
            Elastique(
                couleur = color,
                valeurBitmask = 1 shl index,
                label = listOf("Rouge", "Bleu", "Jaune", "Vert")[index],
                resistanceMinKg = 5 + index * 5,
                resistanceMaxKg = 15 + index * 5
            )
        }

        testElastiques.forEach { viewModel.insert(it) }
    }
}