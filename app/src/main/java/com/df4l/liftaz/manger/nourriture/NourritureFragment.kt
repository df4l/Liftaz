package com.df4l.liftaz.manger.nourriture

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Aliment
import com.df4l.liftaz.data.AlimentDao
import com.df4l.liftaz.data.AppDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class NourritureFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NourritureAdapter

    private lateinit var tabAliments: TextView
    private lateinit var tabRecettes: TextView

    private var ongletActif = 0 // 0 = aliments, 1 = recettes

    // On récupérera les données depuis la BDD
    private lateinit var alimentDao: AlimentDao
    private var aliments = listOf<Aliment>()
    private var recettes = listOf<RecetteAffichee>() // tu peux gérer tes recettes plus tard

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_nourriture, container, false)

        // DAO
        alimentDao = AppDatabase.getDatabase(requireContext()).alimentDao()

        tabAliments = view.findViewById(R.id.tabAliments)
        tabRecettes = view.findViewById(R.id.tabRecettes)
        recyclerView = view.findViewById(R.id.recyclerViewNourriture)

        adapter = NourritureAdapter(emptyList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Par défaut : Aliments
        setActiveTab(tabAliments)
        loadAliments()

        tabAliments.setOnClickListener {
            setActiveTab(tabAliments)
            loadAliments()
        }

        tabRecettes.setOnClickListener {
            setActiveTab(tabRecettes)
            loadRecettes()
        }

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_manger)
        fab.setOnClickListener {
            if (ongletActif == 0) {
                ouvrirDialogAjoutAliment()
            } else {
                ouvrirDialogAjoutRecette()
            }
        }

        return view
    }

    private fun loadAliments() {
        lifecycleScope.launch {
            aliments = alimentDao.getAll() // méthode suspendue
            adapter.updateData(aliments)
        }
    }

    private fun loadRecettes() {
        adapter.updateData(recettes) // futur traitement réel
    }

    private fun ouvrirDialogAjoutAliment() {
        DialogAjoutAliment { aliment ->
            // Sauvegarde dans la BDD
            lifecycleScope.launch {
                alimentDao.insert(aliment)
                loadAliments()
            }
        }.show(parentFragmentManager, "dialogAliment")
    }

    private fun ouvrirDialogAjoutRecette() {
        Toast.makeText(requireContext(), "Dialog ajout recette", Toast.LENGTH_SHORT).show()
    }

    private fun setActiveTab(active: TextView) {
        tabAliments.setTextColor(Color.BLACK)
        tabRecettes.setTextColor(Color.BLACK)
        active.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
        ongletActif = if (active == tabAliments) 0 else 1
    }
}

