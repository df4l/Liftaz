package com.df4l.liftaz.manger.suivi

import com.df4l.liftaz.manger.suivi.QuantiteAlimentDialogFragment
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.df4l.liftaz.data.Aliment
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.Diete
import com.df4l.liftaz.data.MangerHistorique
import com.df4l.liftaz.data.PeriodeRepas
import com.df4l.liftaz.data.Recette
import com.df4l.liftaz.data.TypeElement
import com.df4l.liftaz.databinding.BottomSheetMangerBinding
import com.df4l.liftaz.manger.nourriture.NourritureAdapter
import com.df4l.liftaz.manger.nourriture.RecetteAffichee
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class AjouterRepasBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMangerBinding? = null

    private val favoriteItems = mutableListOf<Any>()
    private var dieteActive: Diete? = null
    private val dieteItems = mutableListOf<Any>()
    private lateinit var dieteItemsAdapter: NourritureAdapter

    private val selectedItems = mutableListOf<ItemSelectionne>()
    private lateinit var selectionNourritureAdapter: NourritureSelectionAdapter

    // Cette propriété est valide uniquement entre onCreateView et onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetMangerBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectionNourritureAdapter = NourritureSelectionAdapter(selectedItems)
        binding.rvSelectionManger.adapter = selectionNourritureAdapter
        binding.rvSelectionManger.layoutManager = LinearLayoutManager(requireContext())

        binding.btnSaveMeal.setOnClickListener {
            sauvegarderSelection()
        }

        getCurrentPeriodeRepas()

        //Peupler la partie "favoris"
        lifecycleScope.launch {

            dieteActive = AppDatabase.Companion.getDatabase(requireContext()).dieteDao().getActiveDiete()

            // Exécute la requête de base de données dans un thread d'arrière-plan
            val listeFavoris = withContext(Dispatchers.IO) {
                AppDatabase.Companion.getDatabase(requireContext()).mangerHistoriqueDao()
                    .getTopTenFavoriteFoods()
            }
            if (listeFavoris.isEmpty()) {
                binding.emptyFavorites.visibility = View.VISIBLE
                binding.rvFavorites.visibility = View.GONE
            } else {
                binding.emptyFavorites.visibility = View.GONE
                binding.rvFavorites.visibility = View.VISIBLE

                //Sert à rien de le faire tant que je sauvegarde rien dans MangerHistorique
                //TODO: Récupérer les item favoris et les mettre dans favoriteItems


            }

            if(dieteActive != null)
                getItemsFromDieteForCurrentPeriode(dieteActive!!.id)

            if(dieteActive != null && dieteItems.isNotEmpty())
            {
                binding.emptyDiete.visibility = View.GONE
                binding.rvDiete.visibility = View.VISIBLE

                dieteItemsAdapter = NourritureAdapter(
                    dieteItems,
                    onItemClick = { item -> addToSelectedItems(item) }
                )
                binding.rvDiete.layoutManager = LinearLayoutManager(requireContext())
                binding.rvDiete.adapter = dieteItemsAdapter
            }
            else
            {
                binding.emptyDiete.visibility = View.VISIBLE
                binding.rvDiete.visibility = View.GONE
            }
        }
    }

    private fun addToSelectedItems(item: Any) {
        if (item is Aliment || item is RecetteAffichee) {
            val existingItem = selectedItems.find {
                when (item) {
                    is Aliment -> it.item is Aliment && it.item.id == item.id
                    is RecetteAffichee -> it.item is RecetteAffichee && it.item.id == item.id
                    else -> it.item == item
                }
            }

            if (existingItem != null && (item is RecetteAffichee || (item is Aliment && item.quantiteParDefaut != null))) {
                // Si la recette existe déjà, on incrémente la quantité de portions
                existingItem.quantite++
                selectionNourritureAdapter.updateData(selectedItems)
            } else if (item is RecetteAffichee || (item is Aliment && item.quantiteParDefaut != null)) {
                // Si c'est une nouvelle recette, on l'ajoute avec une quantité de 1
                selectedItems.add(ItemSelectionne(item = item, quantite = 1))
                selectionNourritureAdapter.updateData(selectedItems)
            } else if (item is Aliment && item.quantiteParDefaut == null) {
                // Pour un aliment (nouveau ou existant), on ouvre le dialogue pour demander la quantité
                val dialog = QuantiteAlimentDialogFragment(item) { quantiteSaisie ->
                    // Ce code est exécuté quand l'utilisateur confirme la quantité
                    val alimentAvecQuantite = ItemSelectionne(item = item, quantite = quantiteSaisie)
                    selectedItems.add(alimentAvecQuantite)
                    selectionNourritureAdapter.updateData(selectedItems)
                }
                // Affiche le dialogue en utilisant le FragmentManager du BottomSheet
                dialog.show(childFragmentManager, "QuantiteAlimentDialog")
            }
        }
    }

    private fun getCurrentPeriodeRepas(): PeriodeRepas {
        val calendar = Calendar.getInstance()
        val heure = calendar.get(Calendar.HOUR_OF_DAY)

        return when (heure) {
            in 4..11 -> {
                binding.tvSuggestionsTitle.text = "Manger du matin"
                PeriodeRepas.MATIN
            }
            in 12..14 -> {
                binding.tvSuggestionsTitle.text = "Manger du midi"
                PeriodeRepas.MIDI
            }
            in 15..18 -> {
                binding.tvSuggestionsTitle.text = "Manger de l'après-midi"
                PeriodeRepas.APRES_MIDI
            }
            else -> { // Couvre de 19h à 3h du matin
                binding.tvSuggestionsTitle.text = "Manger du soir"
                PeriodeRepas.SOIR
            }
        }
    }

    suspend private fun getItemsFromDieteForCurrentPeriode(idDiete: Int)
    {
        val currentPeriode = getCurrentPeriodeRepas()
        val db = AppDatabase.Companion.getDatabase(requireContext())
        val dieteElements = db.dieteElementsDao().getAllForDieteAndPeriode(idDiete, currentPeriode)

        dieteItems.clear()
        for (element in dieteElements) {
            val itemToAdd = when (element.typeElement) {
                TypeElement.ALIMENT -> db.alimentDao().getById(element.idElement)
                TypeElement.RECETTE -> getRecetteAsRecetteAffichee(db, element.idElement)
            }

            if (itemToAdd != null && !dieteItems.contains(itemToAdd)) {
                dieteItems.add(itemToAdd)
            }
        }

        withContext(Dispatchers.Main) {
            if (::dieteItemsAdapter.isInitialized) {
                dieteItemsAdapter.notifyDataSetChanged()
            }
        }
    }

    suspend private fun getRecetteAsRecetteAffichee(db: AppDatabase, recetteId: Int): RecetteAffichee
    {
        val recAliments = db.recetteAlimentsDao().getAllForRecette(recetteId)
        val recette = db.recetteDao().getById(recetteId)

        var totalProteines = 0f
        var totalGlucides = 0f
        var totalLipides = 0f
        var totalCalories = 0
        var quantiteTotale = 0f

        for (ra in recAliments) {
            val aliment = db.alimentDao().getById(ra.idAliment) ?: continue
            val coef = ra.coefAliment
            totalProteines += aliment.proteines * coef
            totalGlucides += aliment.glucides * coef
            totalLipides += aliment.lipides * coef
            totalCalories += (aliment.calories * coef).toInt()
            quantiteTotale += 100f * coef
        }

        return RecetteAffichee(
            id = recetteId,
            nom = recette!!.nom,
            proteines = totalProteines,
            glucides = totalGlucides,
            lipides = totalLipides,
            calories = totalCalories,
            quantiteTotale = quantiteTotale,
            quantitePortion = recette.quantitePortion?.toFloat(),
            imageUri = recette.imageUri
        )
    }

    fun sauvegarderSelection()
    {
        if(selectedItems.isEmpty())
            return

        var db = AppDatabase.getDatabase(requireContext())
        var mangerHistoriqueDao = db.mangerHistoriqueDao()

        lifecycleScope.launch {
            selectedItems.forEach {
                var date: Date = Calendar.getInstance().time
                var nomElement: String = ""
                var calories: Int = 0
                var proteines: Float = 0f
                var glucides: Float = 0f
                var lipides: Float = 0f
                var quantite: String = ""

                if(it.item is RecetteAffichee || (it.item is Aliment && it.item.quantiteParDefaut != null))
                {
                    when(val item = it.item)
                    {
                        is Aliment -> {
                            Log.d(TAG, "Aliment avec quantité par défaut: ${item.nom}, " +
                                    "ID: ${item.id}, " +
                                    "Calories: ${item.calories}, " +
                                    "Protéines: ${item.proteines}, " +
                                    "Glucides: ${item.glucides}, " +
                                    "Lipides: ${item.lipides}, " +
                                    "Quantité par défaut: ${item.quantiteParDefaut}, " +
                                    "Quantité sélectionnée (nombre de portions): ${it.quantite}")
                            nomElement = item.nom
                            calories = ((item.calories * item.quantiteParDefaut!!) / 100) * it.quantite
                            proteines = ((item.proteines * item.quantiteParDefaut!!) / 100) * it.quantite
                            glucides = ((item.glucides * item.quantiteParDefaut!!) / 100) * it.quantite
                            lipides = ((item.lipides * item.quantiteParDefaut!!) / 100) * it.quantite
                            quantite = "x ${it.quantite}"
                        }
                        is RecetteAffichee -> {
                            Log.d(TAG, "Recette: ${item.nom}, " +
                                    "ID: ${item.id}, " +
                                    "Calories: ${item.calories}, " +
                                    "Protéines: ${item.proteines}, " +
                                    "Glucides: ${item.glucides}, " +
                                    "Lipides: ${item.lipides}, " +
                                    "Quantité totale recette: ${item.quantiteTotale}, " +
                                    "Quantité portion: ${item.quantitePortion}, " +
                                    "Quantité sélectionnée (nombre de portions): ${it.quantite}")
                            nomElement = item.nom
                            val multiplication = if(item.quantitePortion != null) item.quantitePortion else item.quantiteTotale
                            calories = (((item.calories * multiplication) / item.quantiteTotale) * it.quantite).toInt()
                            proteines = (((item.proteines * multiplication) / item.quantiteTotale) * it.quantite)
                            glucides = (((item.glucides * multiplication) / item.quantiteTotale) * it.quantite)
                            lipides = (((item.lipides * multiplication) / item.quantiteTotale) * it.quantite)
                            quantite = "x ${it.quantite}"
                        }
                    }
                }
                else
                {
                    val item = it.item as Aliment
                    Log.d(TAG, "Aliment sans quantité par défaut: ${item.nom}, " +
                            "ID: ${item.id}, " +
                            "Calories: ${item.calories}, " +
                            "Protéines: ${item.proteines}, " +
                            "Glucides: ${item.glucides}, " +
                            "Lipides: ${item.lipides}, " +
                            "Quantité saisie (en grammes): ${it.quantite}")
                    nomElement = item.nom
                    calories = item.calories * (it.quantite / 100)
                    proteines = item.proteines * (it.quantite / 100)
                    glucides = item.glucides * (it.quantite / 100)
                    lipides = item.lipides * (it.quantite / 100)
                    quantite = "${it.quantite}g"
                }

                mangerHistoriqueDao.insert(
                    MangerHistorique(
                        date = date,
                        nomElement = nomElement,
                        calories = calories,
                        proteines = proteines,
                        glucides = glucides,
                        lipides = lipides,
                        quantite = quantite
                    )
                )
            }

            val result = Bundle()
            parentFragmentManager.setFragmentResult("repasAjoute", result)

            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // Le Tag est utile pour trouver le fragment si nécessaire
        const val TAG = "AjouterRepasBottomSheetFragment"
    }
}