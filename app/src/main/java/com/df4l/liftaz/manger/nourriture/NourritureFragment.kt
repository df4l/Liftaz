package com.df4l.liftaz.manger.nourriture

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R

class NourritureFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NourritureAdapter

    private lateinit var tabAliments: TextView
    private lateinit var tabRecettes: TextView

    private val aliments = listOf("Pomme", "Poulet", "Riz", "Carotte")
    private val recettes = listOf("Salade composée", "Poulet au riz", "Soupe de légumes")

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

        return view
    }

    private fun setActiveTab(active: TextView) {
        // Désactive tous
        tabAliments.paintFlags = tabAliments.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
        tabRecettes.paintFlags = tabRecettes.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()

        tabAliments.setTextColor(Color.BLACK)
        tabRecettes.setTextColor(Color.BLACK)

        // Active l’onglet cliqué
        active.paintFlags = active.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        active.setTextColor(Color.parseColor("#3F51B5")) // couleur accent (bleu Material)
    }

    private fun showAliments() {
        adapter.updateData(aliments)
    }

    private fun showRecettes() {
        adapter.updateData(recettes)
    }
}
