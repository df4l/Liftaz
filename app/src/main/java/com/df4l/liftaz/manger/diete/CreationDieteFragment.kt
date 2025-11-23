package com.df4l.liftaz.manger.diete

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
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
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class CreationDieteFragment : Fragment() {

    private var isUpdating = false
    private lateinit var topSheetBehavior: TopSheetBehavior<MaterialCardView>
    private var poidsUtilisateur: Float? = null

    private var pourcentageProteines = 40
    private var pourcentageGlucides = 40
    private var pourcentageLipides = 20
    private var totalCalories = 2000

    private var objectifProteines = 0
    private var objectifGlucides = 0
    private var objectifLipides = 0

    private var progressProteines: ProgressBar? = null
    private var progressGlucides: ProgressBar? = null
    private var progressLipides: ProgressBar? = null

    private var tvProteines: TextView? = null
    private var tvGlucides: TextView? = null
    private var tvLipides: TextView? = null

    private var tvCaloriesStatus: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_creationdiete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressProteines = view.findViewById(R.id.progressProteines)
        progressGlucides = view.findViewById(R.id.progressGlucides)
        progressLipides = view.findViewById(R.id.progressLipides)
        tvProteines = view.findViewById(R.id.tvProteines)
        tvGlucides = view.findViewById(R.id.tvGlucides)
        tvLipides = view.findViewById(R.id.tvLipides)
        tvCaloriesStatus = view.findViewById(R.id.tvCaloriesStatus)


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

                etProteinesKg.setHint(String.format("%.2f", pKg))
                etGlucidesKg.setHint(String.format("%.2f", gKg))
                etLipidesKg.setHint(String.format("%.2f", lKg))

            } else {
                poidsUtilisateur = null

                etProteinesKg.hint = "N/D"
                etGlucidesKg.hint = "N/D"
                etLipidesKg.hint = "N/D"
            }
        }

        addTextWatcher(etCalories) { ancienneValeur, nouvelleValeur ->
            Log.d("TextWatcher", "Les calories ont changées de '$ancienneValeur' à '$nouvelleValeur'")
            updateMacrosFromCalories(ancienneValeur, nouvelleValeur, etProteinesGr, etGlucidesGr, etLipidesGr, etProteinesKg, etGlucidesKg, etLipidesKg, poidsUtilisateur)
        }

        addTextWatcher(etProteinesGr) { ancienneValeur, nouvelleValeur ->
            Log.d("TextWatcher", "Proteines (g) a changé de '$ancienneValeur' à '$nouvelleValeur'")
            updateMacrosFromEditTextsGrams("proteines", ancienneValeur, nouvelleValeur, etProteinesGr, etGlucidesGr, etLipidesGr, etProteinesKg, etGlucidesKg, etLipidesKg, poidsUtilisateur, sliderProteines, sliderGlucides, sliderLipides, view)
        }

        addTextWatcher(etGlucidesGr) { ancienneValeur, nouvelleValeur ->
            Log.d("TextWatcher", "Glucides (g) a changé de '$ancienneValeur' à '$nouvelleValeur'")
            updateMacrosFromEditTextsGrams("glucides", ancienneValeur, nouvelleValeur, etProteinesGr, etGlucidesGr, etLipidesGr, etProteinesKg, etGlucidesKg, etLipidesKg, poidsUtilisateur, sliderProteines, sliderGlucides, sliderLipides, view)
        }

        addTextWatcher(etLipidesGr) { ancienneValeur, nouvelleValeur ->
            Log.d("TextWatcher", "Lipides (g) a changé de '$ancienneValeur' à '$nouvelleValeur'")
            updateMacrosFromEditTextsGrams("lipides", ancienneValeur, nouvelleValeur, etProteinesGr, etGlucidesGr, etLipidesGr, etProteinesKg, etGlucidesKg, etLipidesKg, poidsUtilisateur, sliderProteines, sliderGlucides, sliderLipides, view)
        }

        val listener = Slider.OnChangeListener { slider, value, fromUser ->
            if (!fromUser) return@OnChangeListener
            if (isUpdating) return@OnChangeListener
            updateMacrosFromSliders(sliderProteines, sliderGlucides, sliderLipides, slider, view, etCalories, etProteinesGr, etGlucidesGr, etLipidesGr, etProteinesKg, etGlucidesKg, etLipidesKg)
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

    private fun addTextWatcher(editText: EditText, onTextChangedAction: (ancienneValeur: String, nouvelleValeur: String) -> Unit) {
        editText.addTextChangedListener(object : TextWatcher {
            private var ancienneValeur: String = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (isUpdating) return
                ancienneValeur = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Pas nécessaire pour ce cas d'usage
            }

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                val nouvelleValeur = s.toString()

                // Évite les rappels infinis si l'action modifie le même EditText
                if (ancienneValeur != nouvelleValeur) {
                    onTextChangedAction(ancienneValeur, nouvelleValeur)
                }
            }
        })
    }

    //TODO: Améliorer tout ce code dégoutant
    //Ce code a deux raisons d'exister : d'une part parce qu'il fonctionne mais de l'autre parce que je ne peux pas passer raisonnablement plus de temps dessus tant que le reste de l'application n'est pas encore développé
    private fun updateMacrosFromCalories(ancienneValeur: String, nouvelleValeur: String, etProteinesGr: EditText, etGlucidesGr: EditText, etLipidesGr: EditText, etProteinesKg: EditText, etGlucidesKg: EditText, etLipidesKg: EditText, poidsUtilisateur: Float?)
    {
        if(isUpdating)
            return

        isUpdating = true

        if(nouvelleValeur != "") {
            val proteinesGr =
                ((pourcentageProteines / 100f) * nouvelleValeur.toInt() / 4f).roundToInt()
            val glucidesGr =
                ((pourcentageGlucides / 100f) * nouvelleValeur.toInt() / 4f).roundToInt()
            val lipidesGr = ((pourcentageLipides / 100f) * nouvelleValeur.toInt() / 9f).roundToInt()

            etProteinesGr.setText(proteinesGr.toString())
            etGlucidesGr.setText(glucidesGr.toString())
            etLipidesGr.setText(lipidesGr.toString())

            if (poidsUtilisateur != null) {
                val newProteinesKg = proteinesGr / poidsUtilisateur
                val newGlucidesKg = glucidesGr / poidsUtilisateur
                val newLipidesKg = lipidesGr / poidsUtilisateur

                etProteinesKg.setHint(String.format("%.2f", newProteinesKg))
                etGlucidesKg.setHint(String.format("%.2f", newGlucidesKg))
                etLipidesKg.setHint(String.format("%.2f", newLipidesKg))
            }

            totalCalories = nouvelleValeur.toInt()

            updateMacrosInFragment(glucidesGr, lipidesGr, proteinesGr)
        }
        else
        {
            etProteinesGr.setText("0")
            etGlucidesGr.setText("0")
            etLipidesGr.setText("0")

            etProteinesKg.setHint("0.00")
            etGlucidesKg.setHint("0.00")
            etLipidesKg.setHint("0.00")

            totalCalories = 0

            updateMacrosInFragment(0, 0, 0)
        }

        isUpdating = false
    }

    //TODO: Améliorer tout ce code dégoutant
    //Ce code a deux raisons d'exister : d'une part parce qu'il fonctionne mais de l'autre parce que je ne peux pas passer raisonnablement plus de temps dessus tant que le reste de l'application n'est pas encore développé
    private fun updateMacrosFromEditTextsGrams(editedMacro: String, ancienneValeur: String, nouvelleValeur: String, etProteinesGr: EditText, etGlucidesGr: EditText, etLipidesGr: EditText, etProteinesKg: EditText, etGlucidesKg: EditText, etLipidesKg: EditText, poidsUtilisateur: Float?, prot: Slider, gluc: Slider, lip: Slider, view: View
    ) {
        if (isUpdating) return
        isUpdating = true

        var proteinesGr = etProteinesGr.text.toString().toIntOrNull() ?: 0
        var glucidesGr = etGlucidesGr.text.toString().toIntOrNull() ?: 0
        var lipidesGr = etLipidesGr.text.toString().toIntOrNull() ?: 0

        when (editedMacro) {
            "proteines" -> {
                proteinesGr = nouvelleValeur.toIntOrNull() ?: 0
            }
            "glucides" -> {
                // faire quelque chose uniquement pour les glucides
                glucidesGr = nouvelleValeur.toIntOrNull() ?: 0
            }
            "lipides" -> {
                // faire quelque chose uniquement pour les lipides
                lipidesGr = nouvelleValeur.toIntOrNull() ?: 0
            }
        }

        val totalCalories = totalCalories

        // Calculer les pourcentages
        var p = ((proteinesGr * 4f / totalCalories) * 100).roundToInt()
        var g = ((glucidesGr * 4f / totalCalories) * 100).roundToInt()
        var l = ((lipidesGr * 9f / totalCalories) * 100).roundToInt()

        // Ajuster si total != 100
        val diff = p + g + l - 100
        if (diff != 0) {
            when (editedMacro) {
                "proteines" -> {
                    val reste = -diff
                    if (diff > 0) {
                        val takeFromLip = minOf(l, diff)
                        l -= takeFromLip
                        val takeFromGluc = minOf(g, diff - takeFromLip)
                        g -= takeFromGluc
                    } else {
                        val addToGluc = minOf(100 - g, reste)
                        g += addToGluc
                        val addToLip = minOf(100 - l, reste - addToGluc)
                        l += addToLip
                    }
                }
                "glucides" -> {
                    if (diff > 0) {
                        val takeFromLip = minOf(l, diff)
                        l -= takeFromLip
                        val takeFromProt = minOf(p, diff - takeFromLip)
                        p -= takeFromProt
                    } else {
                        val reste = -diff
                        val addToProt = minOf(100 - p, reste)
                        p += addToProt
                        val addToLip = minOf(100 - l, reste - addToProt)
                        l += addToLip
                    }
                }
                "lipides" -> {
                    if (diff > 0) {
                        val takeFromGluc = minOf(g, diff)
                        g -= takeFromGluc
                        val takeFromProt = minOf(p, diff - takeFromGluc)
                        p -= takeFromProt
                    } else {
                        val reste = -diff
                        val addToGluc = minOf(100 - g, reste)
                        g += addToGluc
                        val addToProt = minOf(100 - p, reste - addToGluc)
                        p += addToProt
                    }
                }
            }
        }

        p = p.coerceIn(0, 100)
        g = g.coerceIn(0, 100)
        l = l.coerceIn(0, 100)

        // Mise à jour sliders
        prot.value = p.coerceIn(prot.valueFrom.toInt(), prot.valueTo.toInt()).toFloat()
        gluc.value = g.coerceIn(gluc.valueFrom.toInt(), gluc.valueTo.toInt()).toFloat()
        lip.value = l.coerceIn(lip.valueFrom.toInt(), lip.valueTo.toInt()).toFloat()

        // Mise à jour pourcentages visibles
        val tvProteines = view.findViewById<TextView>(R.id.tvProteinesPercentage)
        val tvGlucides = view.findViewById<TextView>(R.id.tvGlucidesPercentage)
        val tvLipides = view.findViewById<TextView>(R.id.tvLipidesPercentage)
        tvProteines.text = "$p%"
        tvGlucides.text = "$g%"
        tvLipides.text = "$l%"

        // Sauvegarder pourcentages globaux
        pourcentageProteines = p
        pourcentageGlucides = g
        pourcentageLipides = l

        val newProteinesGr = ((p / 100f) * totalCalories / 4f).roundToInt()
        val newGlucidesGr = ((g / 100f) * totalCalories / 4f).roundToInt()
        val newLipidesGr = ((l / 100f) * totalCalories / 9f).roundToInt()

        etProteinesGr.setText(newProteinesGr.toString())
        etGlucidesGr.setText(newGlucidesGr.toString())
        etLipidesGr.setText(newLipidesGr.toString())

        updateMacrosInFragment(newGlucidesGr, newLipidesGr, newProteinesGr)

        poidsUtilisateur?.let { poids ->
            etProteinesKg.setHint(String.format("%.2f", newProteinesGr / poids))
            etGlucidesKg.setHint(String.format("%.2f", newGlucidesGr / poids))
            etLipidesKg.setHint(String.format("%.2f", newLipidesGr / poids))
        }


        isUpdating = false
    }

    //TODO: Améliorer tout ce code dégoutant
    //Ce code a deux raisons d'exister : d'une part parce qu'il fonctionne mais de l'autre parce que je ne peux pas passer raisonnablement plus de temps dessus tant que le reste de l'application n'est pas encore développé
    private fun updateMacrosFromSliders(prot: Slider, gluc: Slider, lip: Slider, changed: Slider, view: View, etCalories: EditText, etProteinesGr: EditText, etGlucidesGr: EditText, etLipidesGr: EditText, etProteinesKg: EditText, etGlucidesKg: EditText, etLipidesKg: EditText
    ) {
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

        pourcentageProteines = p
        pourcentageGlucides = g
        pourcentageLipides = l

        // --- Mise à jour des grammes selon les pourcentages ---
        val caloriesTotal = etCalories.text.toString().toIntOrNull() ?: 0

        val proteinesGr = ((p / 100f) * caloriesTotal / 4f).roundToInt()
        val glucidesGr = ((g / 100f) * caloriesTotal / 4f).roundToInt()
        val lipidesGr = ((l / 100f) * caloriesTotal / 9f).roundToInt()

        etProteinesGr.setText(proteinesGr.toString())
        etGlucidesGr.setText(glucidesGr.toString())
        etLipidesGr.setText(lipidesGr.toString())

        updateMacrosInFragment(glucidesGr, lipidesGr, proteinesGr)

        // --- Mise à jour des g/kg SI un poids existe ---
        poidsUtilisateur?.let { poids ->

            val pKg = proteinesGr / poids
            val gKg = glucidesGr / poids
            val lKg = lipidesGr / poids

            etProteinesKg.setHint(String.format("%.2f", pKg))
            etGlucidesKg.setHint(String.format("%.2f", gKg))
            etLipidesKg.setHint(String.format("%.2f", lKg))
        }

        isUpdating = false
    }

    private fun updateMacrosInFragment(glucidesGr: Int, lipidesGr: Int, proteinesGr: Int)
    {
        objectifProteines = proteinesGr
        objectifGlucides = glucidesGr
        objectifLipides = lipidesGr

        tvCaloriesStatus!!.text = "0 / ${totalCalories}\ncalories"

        tvProteines!!.text = "0 / ${objectifProteines}g"
        tvGlucides!!.text = "0 / ${objectifGlucides}g"
        tvLipides!!.text = "0 / ${objectifLipides}g"

        progressProteines!!.max = objectifProteines
        progressGlucides!!.max = objectifGlucides
        progressLipides!!.max = objectifLipides
    }
}

