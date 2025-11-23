package com.df4l.liftaz.manger.diete

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class CreationDieteFragment : Fragment() {

    private var isUpdating = false
    private lateinit var topSheetBehavior: TopSheetBehavior<MaterialCardView>

    private var poidsUtilisateur: Float? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_creationdiete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val topSheet: MaterialCardView = view.findViewById(R.id.topSheetLayout)
        topSheetBehavior = TopSheetBehavior.from(topSheet)

        // Rendre le top sheet complètement invisible au démarrage
        topSheetBehavior.setHideable(true)
        topSheetBehavior.setPeekHeight(0)
        topSheetBehavior.state = TopSheetBehavior.STATE_EXPANDED

        // Bouton pour ouvrir/fermer
        val btnShowTopSheet = view.findViewById<FloatingActionButton>(R.id.btnShowTopSheet)
        btnShowTopSheet.setOnClickListener {
            toggleTopSheet()
        }

        // FAB secondaire
        val fabToggle = view.findViewById<FloatingActionButton>(R.id.fabToggleTopSheet)
        fabToggle.setOnClickListener { toggleTopSheet() }

        // Récupérer layoutObjectif pour appliquer la marge
        val layoutObjectif = topSheet.findViewById<LinearLayout>(R.id.layoutObjectif)

        ViewCompat.setOnApplyWindowInsetsListener(topSheet) { _, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            layoutObjectif.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBarsInsets.top
            }
            insets
        }

        val etCalories = view.findViewById<EditText>(R.id.etCalories)

        val etProteinesGr = view.findViewById<EditText>(R.id.etProteinesGr)
        val etGlucidesGr = view.findViewById<EditText>(R.id.etGlucidesGr)
        val etLipidesGr = view.findViewById<EditText>(R.id.etLipidesGr)

        val sliderProteines = view.findViewById<Slider>(R.id.sliderProteines)
        val sliderGlucides  = view.findViewById<Slider>(R.id.sliderGlucides)
        val sliderLipides   = view.findViewById<Slider>(R.id.sliderLipides)

        val etProteinesKg = view.findViewById<EditText>(R.id.etProteinesKg)
        val etGlucidesKg = view.findViewById<EditText>(R.id.etGlucidesKg)
        val etLipidesKg = view.findViewById<EditText>(R.id.etLipidesKg)

        lifecycleScope.launch {
            val dernierPoidsUtilisateur =
                AppDatabase.getDatabase(requireContext()).entreePoidsDao().getLatestWeight()?.poids

            if (dernierPoidsUtilisateur != null) {

                // On enregistre le poids pour les calculs g/kg
                poidsUtilisateur = dernierPoidsUtilisateur

                val pKg = 200 / dernierPoidsUtilisateur
                val gKg = 200 / dernierPoidsUtilisateur
                val lKg = 40 / dernierPoidsUtilisateur

                etProteinesKg.setText(String.format("%.2f", pKg))
                etGlucidesKg.setText(String.format("%.2f", gKg))
                etLipidesKg.setText(String.format("%.2f", lKg))

                // Les champs deviennent actifs
                etProteinesKg.isEnabled = true
                etGlucidesKg.isEnabled = true
                etLipidesKg.isEnabled = true

            } else {
                poidsUtilisateur = null

                etProteinesKg.hint = "N/D"
                etGlucidesKg.hint = "N/D"
                etLipidesKg.hint = "N/D"
                etProteinesKg.isEnabled = false
                etGlucidesKg.isEnabled = false
                etLipidesKg.isEnabled = false
            }
        }

        val listener = Slider.OnChangeListener { slider, value, fromUser ->
            if (!fromUser) return@OnChangeListener
            if (isUpdating) return@OnChangeListener
            updateSliders(sliderProteines, sliderGlucides, sliderLipides, slider, view, etCalories, etProteinesGr, etGlucidesGr, etLipidesGr, etProteinesKg, etGlucidesKg, etLipidesKg)
        }

        sliderProteines.addOnChangeListener(listener)
        sliderGlucides.addOnChangeListener(listener)
        sliderLipides.addOnChangeListener(listener)

    }

    private fun toggleTopSheet() {
        topSheetBehavior.state = if (topSheetBehavior.state == TopSheetBehavior.STATE_EXPANDED) {
            TopSheetBehavior.STATE_COLLAPSED // au lieu de STATE_HIDDEN
        } else {
            TopSheetBehavior.STATE_EXPANDED
        }
    }

    private fun updateSliders(
        prot: Slider,
        gluc: Slider,
        lip: Slider,
        changed: Slider,
        view: View,
        etCalories: EditText,
        etProteinesGr: EditText,
        etGlucidesGr: EditText,
        etLipidesGr: EditText,
        etProteinesKg: EditText,
        etGlucidesKg: EditText,
        etLipidesKg: EditText
    )
    {
        isUpdating = true

        var p = prot.value.roundToInt()
        var g = gluc.value.roundToInt()
        var l = lip.value.roundToInt()

        val total = p + g + l
        val diff = total - 100

        if (diff == 0) {
            isUpdating = false
            return
        }

        when (changed) {

            // ----------------------------------------------------
            // PROTÉINES MODIFIÉES
            // ----------------------------------------------------
            prot -> {
                if (diff > 0) {
                    // ----------------------------------------------------
                    // CAS : On augmente les protéines → il faut retirer
                    // ----------------------------------------------------
                    var reste = diff

                    // D'abord retirer des lipides
                    val takeFromLip = minOf(l, reste)
                    l -= takeFromLip
                    reste -= takeFromLip

                    // Ensuite retirer des glucides
                    if (reste > 0) {
                        val takeFromGluc = minOf(g, reste)
                        g -= takeFromGluc
                        reste -= takeFromGluc
                    }

                } else if (diff < 0) {
                    // ----------------------------------------------------
                    // CAS : On diminue les protéines → il faut ajouter
                    // (glucides en priorité)
                    // ----------------------------------------------------
                    var reste = -diff

                    // Ajouter aux glucides en priorité
                    val addToGluc = minOf(100 - g, reste)
                    g += addToGluc
                    reste -= addToGluc

                    // Puis aux lipides
                    if (reste > 0) {
                        val addToLip = minOf(100 - l, reste)
                        l += addToLip
                        reste -= addToLip
                    }
                }
            }

            // ----------------------------------------------------
            // GLUCIDES MODIFIÉS
            // ----------------------------------------------------
            gluc -> {
                var reste = diff

                // D'abord retirer des lipides
                val takeFromLip = minOf(l, reste)
                l -= takeFromLip
                reste -= takeFromLip

                // Ensuite retirer des protéines
                if (reste > 0) {
                    val takeFromProt = minOf(p, reste)
                    p -= takeFromProt
                    reste -= takeFromProt
                }
            }

            // ----------------------------------------------------
            // LIPIDES MODIFIÉS
            // ----------------------------------------------------
            lip -> {
                var reste = diff

                // D'abord retirer des glucides
                val takeFromGluc = minOf(g, reste)
                g -= takeFromGluc
                reste -= takeFromGluc

                // Ensuite retirer des protéines
                if (reste > 0) {
                    val takeFromProt = minOf(p, reste)
                    p -= takeFromProt
                    reste -= takeFromProt
                }
            }
        }

        // Appliquer les nouvelles valeurs
        prot.value = p.toFloat()
        gluc.value = g.toFloat()
        lip.value = l.toFloat()

        val tvProteines = view.findViewById<TextView>(R.id.tvProteinesPercentage)
        val tvGlucides = view.findViewById<TextView>(R.id.tvGlucidesPercentage)
        val tvLipides = view.findViewById<TextView>(R.id.tvLipidesPercentage)

        tvProteines.text = "$p%"
        tvGlucides.text = "$g%"
        tvLipides.text = "$l%"

        // --- Mise à jour des grammes selon les pourcentages ---
        val caloriesTotal = etCalories.text.toString().toIntOrNull() ?: 0

        val proteinesGr = ((p / 100f) * caloriesTotal / 4f).roundToInt()
        val glucidesGr = ((g / 100f) * caloriesTotal / 4f).roundToInt()
        val lipidesGr = ((l / 100f) * caloriesTotal / 9f).roundToInt()

        etProteinesGr.setText(proteinesGr.toString())
        etGlucidesGr.setText(glucidesGr.toString())
        etLipidesGr.setText(lipidesGr.toString())

        // --- Mise à jour des g/kg SI un poids existe ---
        poidsUtilisateur?.let { poids ->

            val pKg = proteinesGr / poids
            val gKg = glucidesGr / poids
            val lKg = lipidesGr / poids

            etProteinesKg.setText(String.format("%.2f", pKg))
            etGlucidesKg.setText(String.format("%.2f", gKg))
            etLipidesKg.setText(String.format("%.2f", lKg))
        }

        isUpdating = false
    }

}

