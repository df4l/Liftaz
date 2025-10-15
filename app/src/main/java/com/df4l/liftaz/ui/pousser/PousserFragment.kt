package com.df4l.liftaz.ui.pousser

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.MuscleDao
import com.df4l.liftaz.databinding.FragmentPousserBinding
import com.google.android.material.snackbar.Snackbar
import androidx.lifecycle.lifecycleScope
import com.df4l.liftaz.data.Exercice
import com.df4l.liftaz.data.ExerciceDao
import com.df4l.liftaz.data.Muscle
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PousserFragment : Fragment() {

    private var _binding: FragmentPousserBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var muscleDao: MuscleDao
    private lateinit var exerciceDao: ExerciceDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val pousserViewModel =
            ViewModelProvider(this).get(PousserViewModel::class.java)

        _binding = FragmentPousserBinding.inflate(inflater, container, false)
        val root: View = binding.root

        database = AppDatabase.getDatabase(requireContext())
        muscleDao = database.muscleDao()
        exerciceDao = database.exerciceDao()

        lifecycleScope.launch {
            if (muscleDao.count() == 0) {
                val defaultMuscles = listOf(
                    Muscle(nom = "Dos"),
                    Muscle(nom = "Épaules"),
                    Muscle(nom = "Biceps"),
                    Muscle(nom = "Fessiers"),
                    Muscle(nom = "Triceps"),
                    Muscle(nom = "Avant-bras"),
                    Muscle(nom = "Quadriceps"),
                    Muscle(nom = "Ischio-jambiers"),
                    Muscle(nom = "Mollets"),
                    Muscle(nom = "Abdominaux")
                )
                defaultMuscles.forEach { muscleDao.insert(it) }
            }
        }

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
                    //Snackbar.make(anchor, "Création d'une séance", Snackbar.LENGTH_SHORT).show()
                    showExercisesListDialog(anchor)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun showExercisesListDialog(anchor: View) {
        lifecycleScope.launch {
            // 🔹 Récupérer tous les exercices depuis la BDD
            val exercices = exerciceDao.getAllExercices()

            if (exerciceDao.count() == 0) {
                Snackbar.make(anchor, "Aucun exercice créé pour le moment", Snackbar.LENGTH_SHORT).show()
                return@launch
            }

            // 2️⃣ Récupérer tous les muscles pour pouvoir afficher leur nom
            val muscles = muscleDao.getAllMuscles()

            // 3️⃣ Créer la liste de chaînes
            val exercicesStrings = exercices.map { ex ->
                val type = if (ex.poidsDuCorps) "PDC" else "PO"
                val muscleName = muscles.find { it.id == ex.idMuscleCible }?.nom ?: "Muscle inconnu"
                "${ex.nom} ($muscleName) - $type"
            }

            // 4️⃣ Afficher le dialog
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Exercices créés")
            builder.setItems(exercicesStrings.toTypedArray(), null)
            builder.setPositiveButton("Fermer") { dialog, _ -> dialog.dismiss() }
            builder.show()
        }
    }

    private fun showAddExerciseDialog() {
        // 1️⃣ On "inflate" la vue personnalisée
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_exercice, null)

        val editName = dialogView.findViewById<android.widget.EditText>(R.id.editTextExerciseName)
        val editNotes = dialogView.findViewById<android.widget.EditText>(R.id.editTextExerciseNotes)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val checkBoxPDC = dialogView.findViewById<CheckBox>(R.id.checkBoxPDC)

        // 2️⃣ On construit le dialogue
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Nouvel exercice")
            .setView(dialogView)
            .setPositiveButton("Ajouter", null)  // ⚠️ null pour éviter la fermeture automatique
            .setNegativeButton("Annuler") { d, _ ->
                d.dismiss()
            }
            .create()

        // 3️⃣ On affiche d'abord le dialog
        dialog.show()

        lifecycleScope.launch {
            val muscles = muscleDao.getAllMuscles()
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                muscles.map { it.nom }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter

            // 4️⃣ On gère le clic du bouton "Ajouter" manuellement
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = editName.text.toString().trim()
                val description = editNotes.text.toString().trim()
                val selectedMuscleName = spinnerCategory.selectedItem?.toString()
                val selectedMuscle = muscles.find{it.nom == selectedMuscleName}
                val isPDC = checkBoxPDC.isChecked

                if (name.isEmpty()) {
                    editName.error = "Le nom est obligatoire"
                    editName.requestFocus()
                    return@setOnClickListener  // ❌ ne ferme pas le dialog
                }

                if (selectedMuscle == null) {
                    Snackbar.make(requireView(), "Veuillez sélectionner un muscle", Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // ✅ Si tout est bon
                addExercise(name, selectedMuscle.id, isPDC, description)
                dialog.dismiss()
        }
        }
    }

    private fun addExercise(name: String, idMuscleCible: Int, exercicePdC: Boolean, notes: String) {
        // Ici tu peux enregistrer dans une base locale ou envoyer au ViewModel
        // Pour l'instant on affiche juste un message :
        Snackbar.make(requireView(), "Exercice ajouté : $name", Snackbar.LENGTH_LONG).show()

        val exercice = Exercice(
            nom = name,
            idMuscleCible = idMuscleCible,
            poidsDuCorps = exercicePdC,
            notes = notes
        )
        lifecycleScope.launch {
            exerciceDao.insert(exercice)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}