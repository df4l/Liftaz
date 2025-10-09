package com.df4l.liftaz.ui.pousser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.df4l.liftaz.R
import com.df4l.liftaz.databinding.FragmentPousserBinding
import com.google.android.material.snackbar.Snackbar

class PousserFragment : Fragment() {

    private var _binding: FragmentPousserBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val pousserViewModel =
            ViewModelProvider(this).get(PousserViewModel::class.java)

        _binding = FragmentPousserBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.fabAdd.setOnClickListener { view ->
            showFabMenu(view)
        }

        return root
    }

    private fun showFabMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_pousser_options, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_add_exercice -> {
                    showAddExerciseDialog()
                    true
                }
                R.id.action_add_series -> {
                    Snackbar.make(anchor, "Ajout d'une série", Snackbar.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun showAddExerciseDialog() {
        // 1️⃣ On "inflate" la vue personnalisée
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_exercice, null)

        val editName = dialogView.findViewById<android.widget.EditText>(R.id.editTextExerciseName)
        val editDescription = dialogView.findViewById<android.widget.EditText>(R.id.editTextExerciseDescription)

        // 2️⃣ On construit le dialogue
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Nouvel exercice")
            .setView(dialogView)
            .setPositiveButton("Ajouter") { d, _ ->
                val name = editName.text.toString().trim()
                val description = editDescription.text.toString().trim()

                if (name.isNotEmpty()) {
                    addExercise(name, description)
                } else {
                    Snackbar.make(requireView(), "Le nom est obligatoire", Snackbar.LENGTH_SHORT).show()
                }
                d.dismiss()
            }
            .setNegativeButton("Annuler") { d, _ ->
                d.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun addExercise(name: String, description: String) {
        // Ici tu peux enregistrer dans une base locale ou envoyer au ViewModel
        // Pour l'instant on affiche juste un message :
        Snackbar.make(requireView(), "Exercice ajouté : $name", Snackbar.LENGTH_LONG).show()

        // TODO : enregistrer l'exercice dans une liste ou DB
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}