package com.df4l.liftaz.manger.diete

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.core.copy
import androidx.compose.ui.semantics.dismiss
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Aliment
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.Recette
import com.df4l.liftaz.data.RecetteAliments
import com.df4l.liftaz.manger.nourriture.NourritureAdapter
import com.df4l.liftaz.manger.nourriture.RecetteAffichee
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class CreationDieteFragment : Fragment() {

    private var isUpdating = false
    private lateinit var topSheetBehavior: TopSheetBehavior<MaterialCardView>
    private var poidsUtilisateur: Float? = null

    // Valeurs de base
    private var pourcentageProteines = 40
    private var pourcentageGlucides = 40
    private var pourcentageLipides = 20
    private var totalCalories = 2000

    private var objectifProteines = 200
    private var objectifGlucides = 200
    private var objectifLipides = 44

    private var usedProteines = 0
    private var usedGlucides = 0
    private var usedLipides = 0
    private var usedCalories = 0

    // Vues du Fragment
    private lateinit var etCalories: EditText
    private lateinit var etProteinesGr: EditText
    private lateinit var etGlucidesGr: EditText
    private lateinit var etLipidesGr: EditText
    private lateinit var etProteinesKg: EditText
    private lateinit var etGlucidesKg: EditText
    private lateinit var etLipidesKg: EditText
    private lateinit var sliderProteines: Slider
    private lateinit var sliderGlucides: Slider
    private lateinit var sliderLipides: Slider
    private lateinit var progressProteines: ProgressBar
    private lateinit var progressGlucides: ProgressBar
    private lateinit var progressLipides: ProgressBar
    private lateinit var tvProteines: TextView
    private lateinit var tvGlucides: TextView
    private lateinit var tvLipides: TextView
    private lateinit var tvCaloriesStatus: TextView
    private lateinit var tvProteinesPercentage: TextView
    private lateinit var tvGlucidesPercentage: TextView
    private lateinit var tvLipidesPercentage: TextView
    private lateinit var btnAddAlimentOuRecette: FloatingActionButton
    private lateinit var cpbCalories: CircularProgressBar

    private lateinit var rvMatin: RecyclerView
    private lateinit var rvMidi: RecyclerView
    private lateinit var rvApresMidi: RecyclerView
    private lateinit var rvSoir: RecyclerView

    private lateinit var matinAdapter: NourritureAdapter
    private lateinit var midiAdapter: NourritureAdapter
    private lateinit var apresMidiAdapter: NourritureAdapter
    private lateinit var soirAdapter: NourritureAdapter

    private val matinItems = mutableListOf<Any>()
    private val midiItems = mutableListOf<Any>()
    private val apresMidiItems = mutableListOf<Any>()
    private val soirItems = mutableListOf<Any>()

    private fun bindViews(view: View) {
        progressProteines = view.findViewById(R.id.progressProteines)
        progressGlucides = view.findViewById(R.id.progressGlucides)
        progressLipides = view.findViewById(R.id.progressLipides)
        tvProteines = view.findViewById(R.id.tvProteines)
        tvGlucides = view.findViewById(R.id.tvGlucides)
        tvLipides = view.findViewById(R.id.tvLipides)
        tvCaloriesStatus = view.findViewById(R.id.tvCaloriesStatus)
        etCalories = view.findViewById(R.id.etCalories)
        etProteinesGr = view.findViewById(R.id.etProteinesGr)
        etGlucidesGr = view.findViewById(R.id.etGlucidesGr)
        etLipidesGr = view.findViewById(R.id.etLipidesGr)
        sliderProteines = view.findViewById(R.id.sliderProteines)
        sliderGlucides = view.findViewById(R.id.sliderGlucides)
        sliderLipides = view.findViewById(R.id.sliderLipides)
        etProteinesKg = view.findViewById(R.id.etProteinesKg)
        etGlucidesKg = view.findViewById(R.id.etGlucidesKg)
        etLipidesKg = view.findViewById(R.id.etLipidesKg)
        tvProteinesPercentage = view.findViewById(R.id.tvProteinesPercentage)
        tvGlucidesPercentage = view.findViewById(R.id.tvGlucidesPercentage)
        tvLipidesPercentage = view.findViewById(R.id.tvLipidesPercentage)
        btnAddAlimentOuRecette = view.findViewById(R.id.btnAddAlimentOuRecette)
        rvMatin = view.findViewById(R.id.rvMatin)
        rvMidi = view.findViewById(R.id.rvMidi)
        rvApresMidi = view.findViewById(R.id.rvApresMidi)
        rvSoir = view.findViewById(R.id.rvSoir)
        cpbCalories = view.findViewById(R.id.cpbCalories)
    }

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

        bindViews(view)
        setupRecyclerViews()

        lifecycleScope.launch {
            val aliments = AppDatabase.getDatabase(requireContext()).alimentDao().getAll()
            val recettes = AppDatabase.getDatabase(requireContext()).recetteDao().getAll()

            val recettesAffichees = recettes.map { recette ->
                val ing = AppDatabase.getDatabase(requireContext()).recetteAlimentsDao()
                    .getAllForRecette(recette.id)
                recetteToAffichee(recette, ing, aliments)
            }

            btnAddAlimentOuRecette.setOnClickListener {
                DialogSelectNourriture(
                    items = aliments + recettesAffichees,
                    onItemSelected = { item ->
                        ajouterItemToDiete(item)
                    }
                ).show(parentFragmentManager, "selectNourriture")
            }
        }

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
            updateMacrosFromCalories(nouvelleValeur, poidsUtilisateur)
        }

        addTextWatcher(etProteinesGr) { ancienneValeur, nouvelleValeur ->
            Log.d("TextWatcher", "Proteines (g) a changé de '$ancienneValeur' à '$nouvelleValeur'")
            updateMacrosFromEditTextsGrams("proteines", nouvelleValeur, poidsUtilisateur)
        }

        addTextWatcher(etGlucidesGr) { ancienneValeur, nouvelleValeur ->
            Log.d("TextWatcher", "Glucides (g) a changé de '$ancienneValeur' à '$nouvelleValeur'")
            updateMacrosFromEditTextsGrams("glucides", nouvelleValeur, poidsUtilisateur)
        }

        addTextWatcher(etLipidesGr) { ancienneValeur, nouvelleValeur ->
            Log.d("TextWatcher", "Lipides (g) a changé de '$ancienneValeur' à '$nouvelleValeur'")
            updateMacrosFromEditTextsGrams("lipides", nouvelleValeur, poidsUtilisateur)
        }

        val listener = Slider.OnChangeListener { slider, value, fromUser ->
            if (!fromUser) return@OnChangeListener
            if (isUpdating) return@OnChangeListener
            updateMacrosFromSliders(slider, poidsUtilisateur)
        }

        sliderProteines.addOnChangeListener(listener)
        sliderGlucides.addOnChangeListener(listener)
        sliderLipides.addOnChangeListener(listener)
    }

    private fun setupRecyclerViews() {
        matinAdapter = NourritureAdapter(matinItems, onItemClick = {}, onDeleteClick = { item -> deleteFromDiete(item, matinItems, matinAdapter) })
        rvMatin.layoutManager = LinearLayoutManager(requireContext())
        rvMatin.adapter = matinAdapter

        midiAdapter = NourritureAdapter(midiItems, onItemClick = {}, onDeleteClick = { item -> deleteFromDiete(item, midiItems, midiAdapter) })
        rvMidi.layoutManager = LinearLayoutManager(requireContext())
        rvMidi.adapter = midiAdapter

        apresMidiAdapter = NourritureAdapter(apresMidiItems, onItemClick = {}, onDeleteClick = { item -> deleteFromDiete(item, apresMidiItems, apresMidiAdapter) })
        rvApresMidi.layoutManager = LinearLayoutManager(requireContext())
        rvApresMidi.adapter = apresMidiAdapter

        soirAdapter = NourritureAdapter(soirItems, onItemClick = {}, onDeleteClick = { item -> deleteFromDiete(item, soirItems, soirAdapter) })
        rvSoir.layoutManager = LinearLayoutManager(requireContext())
        rvSoir.adapter = soirAdapter
    }

    private fun deleteFromDiete(item: Any, listeItems: MutableList<Any>, adapter: NourritureAdapter)
    {
        listeItems.remove(item)
        adapter.updateData(listeItems)
        updateDietTotals()
    }

    private fun ajouterItemToDiete(item: Any)
    {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_diete_item, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Ajouter à la diète")

        // Récupération des vues du dialogue
        val txtName: TextView = dialogView.findViewById(R.id.itemName)
        val txtSub: TextView = dialogView.findViewById(R.id.itemSub)
        val txtNutri: TextView = dialogView.findViewById(R.id.itemNutri)
        val layoutQuantite: TextInputLayout = dialogView.findViewById(R.id.layoutQuantite)
        val etQuantite: EditText = dialogView.findViewById(R.id.etQuantite)
        val radioGroup: RadioGroup = dialogView.findViewById(R.id.radioGroupMoment)
        val rbMatin: RadioButton = dialogView.findViewById(R.id.rbMatin)
        val layoutPortions: LinearLayout = dialogView.findViewById(R.id.layoutPortions)
        val npPortions: NumberPicker = dialogView.findViewById(R.id.numberPickerPortions)

        rbMatin.isChecked = true

        npPortions.minValue = 1
        npPortions.maxValue = 100
        npPortions.value = 1

        when (item) {
            is Aliment -> {
                val q = item.quantiteParDefaut ?: 100
                txtName.text = item.nom
                txtSub.text = if (item.quantiteParDefaut != null)
                    "${item.marque} - $q g"
                else
                    "${item.marque} - Pour 100 g"

                val coef = q / 100f
                txtNutri.text = nutritionalString(
                    item.proteines * coef,
                    item.glucides * coef,
                    item.lipides * coef,
                    (item.calories * coef).toInt()
                )

                if (item.quantiteParDefaut == null) {
                    layoutQuantite.visibility = View.VISIBLE
                    etQuantite.setText(q.toString())
                } else {
                    layoutPortions.visibility = View.VISIBLE
                }
            }

            is RecetteAffichee -> {
                txtName.text = item.nom
                val subText = if (item.quantitePortion != null) {
                    "Portion de ${item.quantitePortion.toInt()}g"
                } else {
                    if (item.quantiteTotale % 1f == 0f)
                        "${item.quantiteTotale.toInt()}g"
                    else
                        "${item.quantiteTotale}g"
                }
                txtSub.text = subText

                val coef = if (item.quantitePortion != null && item.quantiteTotale > 0f)
                    item.quantitePortion / item.quantiteTotale
                else 1f

                txtNutri.text = nutritionalString(
                    item.proteines * coef,
                    item.glucides * coef,
                    item.lipides * coef,
                    (item.calories * coef).toInt()
                )

                layoutQuantite.visibility = View.GONE
            }
        }

        builder.setPositiveButton("Ajouter") { dialog, _ ->
            val selectedMomentId = radioGroup.checkedRadioButtonId

            when (item) {
                is Aliment -> {

                    // ➤ Cas 1 : aliment SANS quantité par défaut → on récupère l'entrée utilisateur
                    if (item.quantiteParDefaut == null) {
                        val quantite = etQuantite.text.toString().toIntOrNull() ?: 100
                        val alimentCopy = item.copy(quantiteParDefaut = quantite)

                        addItemToMoment(selectedMomentId, alimentCopy)
                    }

                    // ➤ Cas 2 : aliment AVEC quantité par défaut → on ajoute N copies
                    else {
                        val portions = npPortions.value

                        repeat(portions) {
                            val alimentCopy = item.copy()   // copie simple
                            addItemToMoment(selectedMomentId, alimentCopy)
                        }
                    }
                }

                is RecetteAffichee -> {
                    addItemToMoment(selectedMomentId, item)
                }
            }

            updateDietTotals()
            dialog.dismiss()
        }

        builder.setNegativeButton("Annuler") { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    private fun addItemToMoment(momentId: Int, item: Any) {
        when (momentId) {
            R.id.rbMatin -> { matinItems.add(item); matinAdapter.updateData(matinItems) }
            R.id.rbMidi -> { midiItems.add(item); midiAdapter.updateData(midiItems) }
            R.id.rbApresMidi -> { apresMidiItems.add(item); apresMidiAdapter.updateData(apresMidiItems) }
            R.id.rbSoir -> { soirItems.add(item); soirAdapter.updateData(soirItems) }
        }
    }

    private fun updateDietTotals()
    {
        var totalProteines = 0f
        var totalGlucides = 0f
        var totalLipides = 0f
        var totalCalories = 0

        var tousLesItems = matinItems + midiItems + apresMidiItems + soirItems

        tousLesItems.forEach { item ->
            when(item) {
                is Aliment -> {
                    if(item.quantiteParDefaut != null) {
                        totalProteines += (item.proteines * item.quantiteParDefaut) / 100
                        totalGlucides += (item.glucides * item.quantiteParDefaut) / 100
                        totalLipides += (item.lipides * item.quantiteParDefaut) / 100
                        totalCalories += (item.calories * item.quantiteParDefaut) / 100
                    }
                    else {
                        totalProteines += item.proteines
                        totalGlucides += item.glucides
                        totalLipides += item.lipides
                        totalCalories += item.calories
                    }
                }
                is RecetteAffichee -> {
                    totalProteines += item.proteines
                    totalGlucides += item.glucides
                    totalLipides += item.lipides
                    totalCalories += item.calories
                }
            }
        }

        usedCalories = totalCalories
        usedGlucides = totalGlucides.roundToInt()
        usedLipides = totalLipides.roundToInt()
        usedProteines = totalProteines.roundToInt()

        updateMacrosObjectivesInFragment(objectifGlucides, objectifLipides, objectifProteines)
    }


    private fun nutritionalString(p: Float, g: Float, l: Float, kcal: Int): SpannableString {
        val protStr = "${"%.1f".format(p)}g"
        val glucStr = "${"%.1f".format(g)}g"
        val lipStr = "${"%.1f".format(l)}g"
        val calStr = "${kcal}kcal"

        val text = "$protStr / $glucStr / $lipStr / $calStr"
        val span = SpannableString(text)

        val protColor = Color.parseColor("#ec99b5")
        val glucColor = Color.parseColor("#86e8cd")
        val fatColor = Color.parseColor("#f2d678")

        val protStart = 0
        val protEnd = protStr.length

        val glucStart = protEnd + 3
        val glucEnd = glucStart + glucStr.length

        val lipStart = glucEnd + 3
        val lipEnd = lipStart + lipStr.length

        span.setSpan(ForegroundColorSpan(protColor), protStart, protEnd, 0)
        span.setSpan(ForegroundColorSpan(glucColor), glucStart, glucEnd, 0)
        span.setSpan(ForegroundColorSpan(fatColor), lipStart, lipEnd, 0)

        return span
    }

    //TODO: Il serait bon que le NourritureAdapter reçoive directement les recettes à l'avenir plutôt que des versions "affichée"
    fun recetteToAffichee(
        recette: Recette,
        ingredients: List<RecetteAliments>,
        aliments: List<Aliment>
    ): RecetteAffichee {

        var prot = 0f
        var glu = 0f
        var lip = 0f
        var cal = 0
        var poids = 0f

        ingredients.forEach { ing ->
            val alim = aliments.firstOrNull { it.id == ing.idAliment } ?: return@forEach
            val coef = ing.coefAliment

            poids += 100f * coef

            prot += alim.proteines * coef
            glu += alim.glucides * coef
            lip += alim.lipides * coef
            cal += (alim.calories * coef).toInt()
        }


        return RecetteAffichee(
            id = recette.id,
            nom = recette.nom,
            proteines = prot,
            glucides = glu,
            lipides = lip,
            calories = cal,
            quantiteTotale = poids,
            quantitePortion = recette.quantitePortion?.toFloat()
        )
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

    private fun updateMacrosFromCalories(nouvelleValeur: String, poidsUtilisateur: Float?)
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

            updateMacrosObjectivesInFragment(glucidesGr, lipidesGr, proteinesGr)
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

            updateMacrosObjectivesInFragment(0, 0, 0)
        }

        isUpdating = false
    }

    private fun updateMacrosFromEditTextsGrams(editedMacro: String, nouvelleValeur: String, poidsUtilisateur: Float?)
    {
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

        //TODO: Ce code est dupliqué dans UpdateMacrosFromSliders, ça serait bien de l'avoir en qu'un seul exemplaire
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
                        val addToLip = minOf(100 - l, reste)
                        l += addToLip
                        val addToProt = minOf(100 - p, reste - addToLip)
                        p += addToProt
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
        sliderProteines.value = p.coerceIn(sliderProteines.valueFrom.toInt(), sliderProteines.valueTo.toInt()).toFloat()
        sliderGlucides.value = g.coerceIn(sliderGlucides.valueFrom.toInt(), sliderGlucides.valueTo.toInt()).toFloat()
        sliderLipides.value = l.coerceIn(sliderLipides.valueFrom.toInt(), sliderLipides.valueTo.toInt()).toFloat()

        // Mise à jour pourcentages visibles
        tvProteinesPercentage.text = "$p%"
        tvGlucidesPercentage.text = "$g%"
        tvLipidesPercentage.text = "$l%"

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

        updateMacrosObjectivesInFragment(newGlucidesGr, newLipidesGr, newProteinesGr)

        poidsUtilisateur?.let { poids ->
            etProteinesKg.setHint(String.format("%.2f", newProteinesGr / poids))
            etGlucidesKg.setHint(String.format("%.2f", newGlucidesGr / poids))
            etLipidesKg.setHint(String.format("%.2f", newLipidesGr / poids))
        }


        isUpdating = false
    }

    private fun updateMacrosFromSliders(editedSlider: Slider, poidsUtilisateur: Float?) {
        isUpdating = true

        var p = sliderProteines.value.roundToInt()
        var g = sliderGlucides.value.roundToInt()
        var l = sliderLipides.value.roundToInt()

        val total = p + g + l
        val diff = total - 100

        if (diff == 0) {
            isUpdating = false
            return
        }

        //TODO: Ce code est dupliqué dans UpdateMacrosFromGrams, ça serait bien de l'avoir en qu'un seul exemplaire
        when (editedSlider) {
            sliderProteines -> {
                if (diff > 0) {
                    var reste = diff
                    val takeFromLip = minOf(l, reste)
                    l -= takeFromLip
                    reste -= takeFromLip
                    if (reste > 0) {
                        val takeFromGluc = minOf(g, reste)
                        g -= takeFromGluc
                        reste -= takeFromGluc
                    }
                } else if (diff < 0) {
                    var reste = -diff
                    val addToGluc = minOf(100 - g, reste)
                    g += addToGluc
                    reste -= addToGluc
                    if (reste > 0) {
                        val addToLip = minOf(100 - l, reste)
                        l += addToLip
                        reste -= addToLip
                    }
                }
            }
            sliderGlucides -> {
                var reste = diff
                val takeFromLip = minOf(l, reste)
                l -= takeFromLip
                reste -= takeFromLip
                if (reste > 0) {
                    val takeFromProt = minOf(p, reste)
                    p -= takeFromProt
                    reste -= takeFromProt
                }
            }
            sliderLipides -> {
                var reste = diff
                val takeFromGluc = minOf(g, reste)
                g -= takeFromGluc
                reste -= takeFromGluc
                if (reste > 0) {
                    val takeFromProt = minOf(p, reste)
                    p -= takeFromProt
                    reste -= takeFromProt
                }
            }
        }

        // Appliquer les nouvelles valeurs
        sliderProteines.value = p.toFloat()
        sliderGlucides.value = g.toFloat()
        sliderLipides.value = l.toFloat()

        tvProteinesPercentage.text = "$p%"
        tvGlucidesPercentage.text = "$g%"
        tvLipidesPercentage.text = "$l%"

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

        updateMacrosObjectivesInFragment(glucidesGr, lipidesGr, proteinesGr)

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

    private fun updateMacrosObjectivesInFragment(glucidesGr: Int, lipidesGr: Int, proteinesGr: Int)
    {
        objectifProteines = proteinesGr
        objectifGlucides = glucidesGr
        objectifLipides = lipidesGr

        tvCaloriesStatus!!.text = "${usedCalories} / ${totalCalories}\ncalories"
        cpbCalories!!.progressMax = totalCalories.toFloat()
        if(usedCalories < totalCalories)
            cpbCalories!!.progress = usedCalories.toFloat()
        else
            cpbCalories!!.progress = cpbCalories.progressMax

        tvProteines!!.text = "${usedProteines} / ${objectifProteines}g"
        tvGlucides!!.text = "${usedGlucides} / ${objectifGlucides}g"
        tvLipides!!.text = "${usedLipides} / ${objectifLipides}g"

        progressProteines!!.max = objectifProteines
        if(usedProteines < objectifProteines)
            progressProteines!!.progress = usedProteines
        else
            progressProteines!!.progress = progressProteines!!.max

        progressGlucides!!.max = objectifGlucides
        if(usedGlucides < objectifGlucides)
            progressGlucides!!.progress = usedGlucides
        else
            progressGlucides!!.progress = progressGlucides!!.max

        progressLipides!!.max = objectifLipides
        if(usedLipides < objectifLipides)
            progressLipides!!.progress = usedLipides
        else
            progressLipides!!.progress = progressLipides!!.max
    }
}

