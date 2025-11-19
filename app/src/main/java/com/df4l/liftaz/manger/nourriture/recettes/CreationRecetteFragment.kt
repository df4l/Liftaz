package com.df4l.liftaz.manger.nourriture.recettes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.Recette
import com.df4l.liftaz.data.RecetteAliments
import com.df4l.liftaz.manger.nourriture.aliments.DialogCreationAliment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class CreationRecetteFragment : Fragment() {

    private lateinit var adapter: AlimentRecetteAdapter
    private val items = mutableListOf<AlimentRecetteAdapter.AlimentRecetteItem>()

    private lateinit var tvProteines: TextView
    private lateinit var tvGlucides: TextView
    private lateinit var tvLipides: TextView

    private var recetteId: Int? = null

    override fun onDestroyView() {
        super.onDestroyView()
        // On prévient NourritureFragment de passer à l'onglet Recettes
        parentFragmentManager.setFragmentResult("ongletResult", Bundle().apply {
            putInt("ongletActif", 1) // 1 = recettes
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_creationrecette, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerIngredients)
        val btnCreateAliment = view.findViewById<FloatingActionButton>(R.id.btnCreateAliment)
        val btnAddAliment = view.findViewById<ImageButton>(R.id.btnAddPlus)
        val btnSave = view.findViewById<FloatingActionButton>(R.id.btnSauvegarderRecette)

        tvProteines = view.findViewById(R.id.tvProteinesTotal)
        tvGlucides = view.findViewById(R.id.tvGlucidesTotal)
        tvLipides = view.findViewById(R.id.tvLipidesTotal)

        adapter = AlimentRecetteAdapter(
            items,
            onQuantityChanged = { updateTotalMacros() },
            onRemove = { removeIngredient(it) }
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        btnCreateAliment.setOnClickListener {
            DialogCreationAliment { aliment ->
                // Sauvegarde dans la BDD
                lifecycleScope.launch {
                    AppDatabase.getDatabase(requireContext()).alimentDao().insert(aliment)
                }
            }.show(parentFragmentManager, "dialogAliment")
        }

        btnSave.setOnClickListener {
            saveRecette()
        }

        btnAddAliment.setOnClickListener {
            addAlimentToRecette()
        }

        recetteId = arguments?.getInt("recetteId")
        recetteId?.let { loadRecette(it) }
    }

    private fun loadRecette(id: Int) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val recette = db.recetteDao().getById(id) ?: return@launch
            val recAliments = db.recetteAlimentsDao().getAllForRecette(id)

            // Pré-remplir le nom et la portion
            view?.findViewById<TextInputEditText>(R.id.editNomRecette)?.setText(recette.nom)
            view?.findViewById<TextInputEditText>(R.id.editQuantitePortion)
                ?.setText(recette.quantitePortion?.toString() ?: "")

            items.clear()
            for (ra in recAliments) {
                val aliment = db.alimentDao().getById(ra.idAliment) ?: continue
                val item = AlimentRecetteAdapter.AlimentRecetteItem(aliment, ra.coefAliment * 100)
                items.add(item)
            }
            adapter.notifyDataSetChanged()
            updateTotalMacros()
        }
    }

    private fun addAlimentToRecette() {
        lifecycleScope.launch {
            val aliments = AppDatabase.getDatabase(requireContext()).alimentDao().getAll()
            if (aliments.isEmpty()) {
                Toast.makeText(requireContext(), "Aucun aliment disponible", Toast.LENGTH_SHORT).show()
                return@launch
            }

            DialogSelectAliment(aliments) { aliment ->
                val item = AlimentRecetteAdapter.AlimentRecetteItem(aliment)
                items.add(item)
                adapter.notifyItemInserted(items.size - 1)
                updateTotalMacros()
            }.show(parentFragmentManager, "dialogSelectAliment")
        }
    }

    private fun removeIngredient(item: AlimentRecetteAdapter.AlimentRecetteItem) {
        adapter.removeItem(item)
        updateTotalMacros()
    }

    // --- CALCUL TOTAL ---
    private fun updateTotalMacros() {
        var totalProteines = 0f
        var totalGlucides = 0f
        var totalLipides = 0f

        var sommeQuantites = 0f

        for (item in items) {
            val coef = item.quantite / 100f
            totalProteines += item.aliment.proteines * coef
            totalGlucides += item.aliment.glucides * coef
            totalLipides += item.aliment.lipides * coef
            sommeQuantites += item.quantite
        }

        val portionText = view?.findViewById<TextInputEditText>(R.id.editQuantitePortion)?.text?.toString()
        val portion = portionText?.toFloatOrNull()

        if (portion != null && portion > 0f && sommeQuantites > 0f) {
            val coefPortion = portion / sommeQuantites
            tvProteines.text = "${"%.1f".format(totalProteines)}g\n${"%.1f".format(totalProteines * coefPortion)}g par portion"
            tvGlucides.text = "${"%.1f".format(totalGlucides)}g\n${"%.1f".format(totalGlucides * coefPortion)}g par portion"
            tvLipides.text = "${"%.1f".format(totalLipides)}g\n${"%.1f".format(totalLipides * coefPortion)}g par portion"
        } else {
            // Si portion invalide ou quantité totale = 0, on n'affiche que le total
            tvProteines.text = "${"%.1f".format(totalProteines)}g"
            tvGlucides.text = "${"%.1f".format(totalGlucides)}g"
            tvLipides.text = "${"%.1f".format(totalLipides)}g"
        }
    }

    private fun saveRecette() {
        val nom = view?.findViewById<TextInputEditText>(R.id.editNomRecette)?.text?.toString()?.trim()
        val portionText = view?.findViewById<TextInputEditText>(R.id.editQuantitePortion)?.text?.toString()
        val portion = portionText?.toIntOrNull()  // Peut rester null

        if (nom.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Nom de la recette manquant", Toast.LENGTH_SHORT).show()
            return
        }

        if (items.isEmpty()) {
            Toast.makeText(requireContext(), "Ajoutez au moins un aliment", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())

            val idRecetteFinal = recetteId?.also { id ->
                // ✅ Modification d'une recette existante
                val recetteExistante = db.recetteDao().getById(id) ?: return@launch

                db.recetteDao().update(
                    Recette(
                        id = id,
                        nom = nom,
                        quantitePortion = portion
                    )
                )

                // Supprimer les anciens ingrédients
                db.recetteAlimentsDao().deleteForRecette(id)

            } ?: run {
                // ✅ Création d'une nouvelle recette
                db.recetteDao().insert(
                    Recette(
                        nom = nom,
                        quantitePortion = portion
                    )
                ).toInt()
            }

            // Ajouter tous les ingrédients
            for (item in items) {
                db.recetteAlimentsDao().insert(
                    RecetteAliments(
                        idRecette = idRecetteFinal,
                        idAliment = item.aliment.id,
                        coefAliment = item.quantite / 100f
                    )
                )
            }

            Toast.makeText(requireContext(), "Recette sauvegardée !", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
        }
    }



}
