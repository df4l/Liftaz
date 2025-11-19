package com.df4l.liftaz.manger.nourriture

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Aliment
import com.df4l.liftaz.data.AlimentDao
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.Recette
import com.df4l.liftaz.data.RecetteAlimentsDao
import com.df4l.liftaz.data.RecetteDao
import com.df4l.liftaz.manger.nourriture.aliments.DialogCreationAliment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class NourritureFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NourritureAdapter

    private lateinit var tabAliments: TextView
    private lateinit var tabRecettes: TextView

    private var ongletActif = 0 // 0 = aliments, 1 = recettes

    private lateinit var alimentDao: AlimentDao
    private lateinit var recetteDao: RecetteDao
    private lateinit var recetteAlimentsDao: RecetteAlimentsDao

    private var aliments = listOf<Aliment>()
    private var recettes = listOf<Recette>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_nourriture, container, false)

        // DAO
        val db = AppDatabase.getDatabase(requireContext())
        alimentDao = db.alimentDao()
        recetteDao = db.recetteDao()
        recetteAlimentsDao = db.recetteAlimentsDao()

        tabAliments = view.findViewById(R.id.tabAliments)
        tabRecettes = view.findViewById(R.id.tabRecettes)
        recyclerView = view.findViewById(R.id.recyclerViewNourriture)

        adapter = NourritureAdapter(emptyList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter.setOnItemClickListener { item ->
            when (item) {
                is Aliment -> {
                    DialogCreationAliment(item) { updatedAliment ->
                        lifecycleScope.launch {
                            alimentDao.update(updatedAliment)
                            loadAliments()
                        }
                    }.show(parentFragmentManager, "dialogEditAliment")
                }
                is RecetteAffichee -> {
                    // Récupérer l'objet Recette complet
                    val bundle = Bundle().apply {
                        putInt("recetteId", item.id)
                    }
                    findNavController().navigate(R.id.action_nourritureFragment_to_creationRecetteFragment, bundle)
                }
            }
        }

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
            if (ongletActif == 0) ouvrirDialogAjoutAliment()
            else ouvrirDialogAjoutRecette()
        }

        parentFragmentManager.setFragmentResultListener("ongletResult", viewLifecycleOwner) { _, bundle ->
            val onglet = bundle.getInt("ongletActif", 0)
            if (onglet == 1) {
                setActiveTab(tabRecettes)
                loadRecettes()
            }
        }

        return view
    }

    private fun loadAliments() {
        lifecycleScope.launch {
            aliments = alimentDao.getAll()
            adapter.updateData(aliments)
        }
    }

    private fun loadRecettes() {
        lifecycleScope.launch {
            recettes = recetteDao.getAll()

            val recettesAffichees = mutableListOf<RecetteAffichee>()

            for (recette in recettes) {
                val recAliments = recetteAlimentsDao.getAllForRecette(recette.id)
                var totalProteines = 0f
                var totalGlucides = 0f
                var totalLipides = 0f
                var totalCalories = 0

                var quantiteTotale = 0f

                for (ra in recAliments) {
                    val aliment = alimentDao.getById(ra.idAliment) ?: continue
                    val coef = ra.coefAliment
                    totalProteines += aliment.proteines * coef
                    totalGlucides += aliment.glucides * coef
                    totalLipides += aliment.lipides * coef
                    totalCalories += (aliment.calories * coef).toInt()
                    quantiteTotale += 100f * coef
                }

                recettesAffichees.add(
                    RecetteAffichee(
                        id = recette.id,
                        nom = recette.nom,
                        proteines = totalProteines,
                        glucides = totalGlucides,
                        lipides = totalLipides,
                        calories = totalCalories,
                        quantiteTotale = quantiteTotale,
                        quantitePortion = recette.quantitePortion?.toFloat()
                    )
                )
            }

            adapter.updateData(recettesAffichees)
        }
    }

    private fun ouvrirDialogAjoutAliment() {
        DialogCreationAliment { aliment ->
            lifecycleScope.launch {
                alimentDao.insert(aliment)
                loadAliments()
            }
        }.show(parentFragmentManager, "dialogAliment")
    }

    private fun ouvrirDialogAjoutRecette() {
        findNavController().navigate(R.id.action_nourritureFragment_to_creationRecetteFragment)
    }

    private fun setActiveTab(active: TextView) {
        tabAliments.setTextColor(Color.BLACK)
        tabRecettes.setTextColor(Color.BLACK)
        active.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
        ongletActif = if (active == tabAliments) 0 else 1
    }
}


