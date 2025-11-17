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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Aliment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NourritureFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NourritureAdapter

    private lateinit var tabAliments: TextView
    private lateinit var tabRecettes: TextView

    private var ongletActif = 0 //0 = aliments, 1 = recettes

    val alimentsFake = mutableListOf(
        Aliment(nom="Pomme", code=111, marque="Bio", calories=52, proteines=0.3f, lipides=0.2f, glucides=14f),
        Aliment(nom="Poulet", code=222, marque="Fermier", calories=165, proteines=31f, lipides=3.6f, glucides=0f),
        Aliment(nom="Riz cuit", code=333, marque="Uncle Ben’s", calories=130, proteines=2.4f, lipides=0.3f, glucides=28f, quantiteParDefaut = 150),
        Aliment(nom="Carotte", code=444, marque="Bio", calories=41, proteines=0.9f, lipides=0.24f, glucides=10f)
    )
    val recettePouletRiz = RecetteAffichee(
        nom = "Poulet au riz",
        proteines = 31f + 2.4f,
        glucides = 0f + 28f,
        lipides = 3.6f + 0.3f,
        calories = 165 + 130,
        quantiteTotale = 250f
    )

    val recettesFake = listOf(recettePouletRiz)

    private val aliments = alimentsFake
    private val recettes = recettesFake

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_nourriture, container, false)

        tabAliments = view.findViewById(R.id.tabAliments)
        tabRecettes = view.findViewById(R.id.tabRecettes)
        recyclerView = view.findViewById(R.id.recyclerViewNourriture)

        adapter = NourritureAdapter(emptyList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Par défaut : Aliments
        setActiveTab(tabAliments)
        showAliments()

        tabAliments.setOnClickListener {
            setActiveTab(tabAliments)
            showAliments()
        }

        tabRecettes.setOnClickListener {
            setActiveTab(tabRecettes)
            showRecettes()
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

    private fun ouvrirDialogAjoutAliment() {
        DialogAjoutAliment { aliment ->
            alimentsFake.add(aliment)   // ⚠️ alimentsFake doit être MutableList
            showAliments()
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

    private fun showAliments() {
        adapter.updateData(aliments)
    }

    private fun showRecettes() {
        adapter.updateData(recettes)
    }
}
