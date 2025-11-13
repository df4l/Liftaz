package com.df4l.liftaz.soulever.programmes

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.Programme
import com.df4l.liftaz.data.Seance
import kotlinx.coroutines.launch

class ModifierProgrammeDialog(
    private val programme: Programme,
    private val onProgrammeModifie: () -> Unit
) : DialogFragment() {

    private lateinit var database: AppDatabase
    private lateinit var listView: ListView
    private lateinit var seances: List<Seance>
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_creation_programme, null)

        val editNom = view.findViewById<EditText>(R.id.editNomProgramme)
        val editDescription = view.findViewById<EditText>(R.id.editDescriptionProgramme)
        listView = view.findViewById(R.id.listViewSeances)
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        editNom.setText(programme.nom)
        editDescription.setText(programme.description ?: "")

        database = AppDatabase.getDatabase(requireContext())

        lifecycleScope.launch {
            seances = database.seanceDao().getAllSeances()
            adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_multiple_choice,
                seances.map { it.nom }
            )
            listView.adapter = adapter

            // Sélectionner les séances déjà associées au programme
            seances.forEachIndexed { index, seance ->
                if (seance.idProgramme == programme.id) {
                    listView.setItemChecked(index, true)
                }
            }
        }

        builder.setView(view)
            .setTitle("Modifier le programme")
            .setPositiveButton("Enregistrer", null)
            .setNegativeButton("Annuler", null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            val btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnPositive.setOnClickListener {
                val nom = editNom.text.toString().trim()
                val description = editDescription.text.toString().trim()

                if (nom.isEmpty()) {
                    editNom.error = "Nom requis"
                    return@setOnClickListener
                }

                val selectedIds = mutableListOf<Int>()
                for (i in 0 until listView.count) {
                    if (listView.isItemChecked(i)) {
                        selectedIds.add(seances[i].id)
                    }
                }

                if (selectedIds.isEmpty()) {
                    Toast.makeText(requireContext(), "Sélectionne au moins une séance", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    // Mettre à jour le programme
                    database.programmeDao().update(
                        programme.copy(
                            nom = nom,
                            description = description.ifEmpty { null }
                        )
                    )

                    // Détacher toutes les séances du programme
                    seances.forEach {
                        if (it.idProgramme == programme.id) {
                            database.seanceDao().updateProgrammeId(it.id, null)
                        }
                    }

                    // Associer les séances sélectionnées
                    selectedIds.forEach { seanceId ->
                        database.seanceDao().updateProgrammeId(seanceId, programme.id)
                    }

                    onProgrammeModifie()
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Programme modifié ✅", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return dialog
    }
}
