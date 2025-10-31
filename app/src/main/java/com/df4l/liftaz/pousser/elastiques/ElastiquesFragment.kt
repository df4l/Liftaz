package com.df4l.liftaz.pousser.elastiques

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        val addButton = view.findViewById<View>(R.id.addButton)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = ElastiqueAdapter(mutableListOf()) { elastique ->
            viewModel.delete(elastique)
        }
        recycler.adapter = adapter

        // 1) Vérifier la DB directement (IO) et n'insérer que si vide
        viewLifecycleOwner.lifecycleScope.launch {
            // passe en IO pour les accès DB
            val count = withContext(Dispatchers.IO) {
                dao.count()
            }
            if (count == 0) {
                // préparer la liste de test puis insérer en bloc
                val colors = listOf(
                    0xFFE57373.toInt(), 0xFF64B5F6.toInt(),
                    0xFFFFD54F.toInt(), 0xFF81C784.toInt()
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
                // insertion en une seule opération (évite plusieurs émissions intermédiaires)
                viewModel.insertAll(testElastiques)
            }
        }

        // 2) Observer la liste (UI)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.elastiques.collectLatest { list ->
                adapter.updateList(list)
            }
        }

        addButton.setOnClickListener {
            showAddDialog()
        }
    }



    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_elastique, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val minInput = dialogView.findViewById<EditText>(R.id.minInput)
        val maxInput = dialogView.findViewById<EditText>(R.id.maxInput)
        val colorPreview = dialogView.findViewById<View>(R.id.colorPreview)
        val selectColorButton = dialogView.findViewById<Button>(R.id.selectColorButton)

        var selectedColor = Color.rgb((50..255).random(), (50..255).random(), (50..255).random())
        colorPreview.setBackgroundColor(selectedColor)

        selectColorButton.setOnClickListener {
            // Utilisation du ColorPicker d'Android natif (API 29+) ou une simple alternative
            val colorPicker = ColorPickerDialog(requireContext(), selectedColor) { color ->
                selectedColor = color
                colorPreview.setBackgroundColor(color)
            }
            colorPicker.show()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Nouvel élastique")
            .setView(dialogView)
            .setPositiveButton("Ajouter", null) // on override ensuite
            .setNegativeButton("Annuler", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val label = nameInput.text.toString().trim()
                val min = minInput.text.toString().toIntOrNull()
                val max = maxInput.text.toString().toIntOrNull()

                when {
                    label.isEmpty() -> {
                        nameInput.error = "Nom obligatoire"
                    }
                    min == null || max == null -> {
                        Toast.makeText(requireContext(), "Entrez des valeurs valides", Toast.LENGTH_SHORT).show()
                    }
                    min >= max -> {
                        Toast.makeText(requireContext(), "La résistance minimale doit être inférieure à la maximale", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val elastique = Elastique(
                            couleur = selectedColor,
                            valeurBitmask = 1 shl (viewModel.elastiques.value.size),
                            label = label,
                            resistanceMinKg = min,
                            resistanceMaxKg = max
                        )
                        viewModel.insert(elastique)
                        dialog.dismiss()
                    }
                }
            }
        }

        dialog.show()
    }


    private fun populateTestElastiques() {
        val colors = listOf(
            0xFFE57373.toInt(), 0xFF64B5F6.toInt(),
            0xFFFFD54F.toInt(), 0xFF81C784.toInt()
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

