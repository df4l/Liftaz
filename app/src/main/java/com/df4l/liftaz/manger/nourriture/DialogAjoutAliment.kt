package com.df4l.liftaz.manger.nourriture

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Aliment

class DialogAjoutAliment(
    private val onAdd: (Aliment) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_ajout_aliment, null)

        // Champs
        val inputNom = view.findViewById<EditText>(R.id.inputNom)
        val inputMarque = view.findViewById<EditText>(R.id.inputMarque)
        val inputCalories = view.findViewById<EditText>(R.id.inputCalories)
        val inputProteines = view.findViewById<EditText>(R.id.inputProteines)
        val inputGlucides = view.findViewById<EditText>(R.id.inputGlucides)
        val inputLipides = view.findViewById<EditText>(R.id.inputLipides)
        val inputQuantite = view.findViewById<EditText>(R.id.inputQuantite)

        val btnScan = view.findViewById<ImageButton>(R.id.btnScan)

        // Créer un ripple programmatique
        val rippleColor = Color.parseColor("#FFFFFF") // couleur du ripple
        val contentDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_circle_purple)
        val mask = ShapeDrawable(OvalShape()).apply {
            paint.color = Color.WHITE // couleur de masque (pour limiter le ripple au cercle)
        }

        val rippleDrawable = RippleDrawable(
            ColorStateList.valueOf(rippleColor),
            contentDrawable,
            mask
        )

        btnScan.background = rippleDrawable

        // === BOUTON SCANNER ===
        btnScan.setOnClickListener {
            Toast.makeText(requireContext(), "Scan code-barres bientôt disponible 🔍📷", Toast.LENGTH_SHORT).show()

            // Plus tard :
            // openScannerCamera()
        }

        builder.setView(view)
            .setTitle("Ajouter un aliment")
            .setPositiveButton("Ajouter") { _, _ ->

                val nom = inputNom.text.toString().trim()
                if (nom.isEmpty()) {
                    Toast.makeText(requireContext(), "Le nom est obligatoire", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val aliment = Aliment(
                    nom = nom,
                    marque = inputMarque.text.toString(),
                    code = 0, // sera remplacé par le scan plus tard
                    calories = inputCalories.text.toString().toIntOrNull() ?: 0,
                    proteines = inputProteines.text.toString().toFloatOrNull() ?: 0f,
                    glucides = inputGlucides.text.toString().toFloatOrNull() ?: 0f,
                    lipides = inputLipides.text.toString().toFloatOrNull() ?: 0f,
                    quantiteParDefaut = inputQuantite.text.toString().toIntOrNull()
                )

                onAdd(aliment)
            }
            .setNegativeButton("Annuler", null)

        return builder.create()
    }
}
