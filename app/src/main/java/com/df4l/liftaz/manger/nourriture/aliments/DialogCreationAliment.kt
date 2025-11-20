package com.df4l.liftaz.manger.nourriture.aliments

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Aliment
import com.df4l.liftaz.manger.nourriture.OpenFoodFactsAPI
import kotlinx.coroutines.launch

class DialogCreationAliment(
    private val alimentExistant: Aliment? = null, // si null => création
    private val onAddOrUpdate: (Aliment) -> Unit
) : DialogFragment() {

    lateinit var barcodeScanner: BarcodeScanner

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_add_aliment, null)

        // Champs
        val inputNom = view.findViewById<EditText>(R.id.inputNom)
        val inputMarque = view.findViewById<EditText>(R.id.inputMarque)
        val inputCalories = view.findViewById<EditText>(R.id.inputCalories)
        val inputProteines = view.findViewById<EditText>(R.id.inputProteines)
        val inputGlucides = view.findViewById<EditText>(R.id.inputGlucides)
        val inputLipides = view.findViewById<EditText>(R.id.inputLipides)
        val inputQuantite = view.findViewById<EditText>(R.id.inputQuantite)

        alimentExistant?.let {
            inputNom.setText(it.nom)
            inputMarque.setText(it.marque)
            inputCalories.setText(it.calories.toString())
            inputProteines.setText(it.proteines.toString())
            inputGlucides.setText(it.glucides.toString())
            inputLipides.setText(it.lipides.toString())
            inputQuantite.setText(it.quantiteParDefaut?.toString() ?: "")
        }

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

        barcodeScanner = BarcodeScanner(requireContext())

        val offAPI = OpenFoodFactsAPI()

        btnScan.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val scannedCode = barcodeScanner.startScan()
                    if (!scannedCode.isNullOrEmpty()) {
                        val response = offAPI.getProduct(scannedCode)

                        if (response != null && response.status == 1 && response.product != null) {
                            inputNom.setText(response.product.productName ?: "")
                            inputMarque.setText(response.product.brands ?: "")
                            inputCalories.setText(response.product.nutriments?.energyKcal100g?.toInt()?.toString() ?: "0")
                            inputProteines.setText(response.product.nutriments?.proteins100g?.toString() ?: "0")
                            inputGlucides.setText(response.product.nutriments?.carbohydrates100g?.toString() ?: "0")
                            inputLipides.setText(response.product.nutriments?.fat100g?.toString() ?: "0")
                        } else {
                            Toast.makeText(requireContext(), "Produit non trouvé dans OpenFoodFacts.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Scan annulé ou code vide", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Erreur lors du scan ou de la récupération : ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        builder.setView(view)
            .setTitle(if (alimentExistant != null) "Modifier l'aliment" else "Ajouter un aliment")
            .setPositiveButton(if (alimentExistant != null) "Modifier" else "Ajouter") { _, _ ->

                val nom = inputNom.text.toString().trim()
                if (nom.isEmpty()) {
                    Toast.makeText(requireContext(), "Le nom est obligatoire", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val aliment = Aliment(
                    id = alimentExistant?.id ?: 0, // important pour la MAJ
                    nom = nom,
                    marque = inputMarque.text.toString(),
                    calories = inputCalories.text.toString().toIntOrNull() ?: 0,
                    proteines = inputProteines.text.toString().toFloatOrNull() ?: 0f,
                    glucides = inputGlucides.text.toString().toFloatOrNull() ?: 0f,
                    lipides = inputLipides.text.toString().toFloatOrNull() ?: 0f,
                    quantiteParDefaut = inputQuantite.text.toString().toIntOrNull()
                )

                onAddOrUpdate(aliment) // le Fragment s'occupe maintenant de la BDD
            }
            .setNegativeButton("Annuler", null)

        return builder.create()
    }
}