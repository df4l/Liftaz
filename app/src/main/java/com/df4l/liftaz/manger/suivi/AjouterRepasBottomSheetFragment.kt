package com.df4l.liftaz.manger.suivi

import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.data.Aliment
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.Diete
import com.df4l.liftaz.data.MangerHistorique
import com.df4l.liftaz.data.PeriodeRepas
import com.df4l.liftaz.data.TypeElement
import com.df4l.liftaz.databinding.BottomSheetMangerBinding
import com.df4l.liftaz.databinding.LayoutTabAjoutRapideBinding
import com.df4l.liftaz.manger.nourriture.NourritureAdapter
import com.df4l.liftaz.manger.nourriture.OpenFoodFactsAPI
import com.df4l.liftaz.manger.nourriture.RecetteAffichee
import com.df4l.liftaz.manger.nourriture.aliments.BarcodeScanner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class AjouterRepasBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMangerBinding? = null
    private var dieteActive: Diete? = null
    private val dieteItems = mutableListOf<Any>()
    private lateinit var dieteItemsAdapter: NourritureAdapter

    private val allNourritureItems = mutableListOf<Any>() // Liste complète pour la recherche
    private lateinit var rechercheNourritureAdapter: NourritureAdapter

    private val favoriteItems = mutableListOf<Any>()
    private val selectedItems = mutableListOf<ItemSelectionne>()
    private lateinit var selectionNourritureAdapter: NourritureSelectionAdapter

    private var ajoutRapideBinding: LayoutTabAjoutRapideBinding? = null
    lateinit var barcodeScanner: BarcodeScanner

    // Cette propriété est valide uniquement entre onCreateView et onDestroyView.
    private val binding get() = _binding!!

    // 2. Créez une instance de l'adaptateur
// La lambda passée en paramètre sera exécutée quand un item est cliqué.
    private val favoriteFoodAdapter = FavoriteFoodAdapter { clickedItem ->
        addToSelectedItems(clickedItem)
    }

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

        // Initialisation de l'adapter pour la sélection
        selectionNourritureAdapter = NourritureSelectionAdapter(selectedItems, onDeleteClick = { itemToDelete ->
            selectedItems.removeIf { it.item == itemToDelete }
            selectionNourritureAdapter.updateData(selectedItems)
        })
        binding.rvSelectionManger.adapter = selectionNourritureAdapter
        binding.rvSelectionManger.layoutManager = LinearLayoutManager(requireContext())

        // Initialisation de l'adapter pour les favoris
        binding.rvFavorites.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.rvFavorites.adapter = favoriteFoodAdapter

        // Initialisation pour la recherche
        setupRecherche()

        setupTabs()

        barcodeScanner = BarcodeScanner(requireContext())

        binding.btnSaveMeal.setOnClickListener {
            sauvegarderSelection()
        }

        getCurrentPeriodeRepas()

        lifecycleScope.launch {
            // Charger toutes les données nécessaires
            loadAllData()

            // Mise à jour des vues après le chargement
            updateFavoritesUI()
            updateDieteUI()
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showRechercheView() // Onglet "Rechercher"
                    1 -> showAjoutRapideView() // Onglet "Ajout rapide"
                    2 -> scanCode()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Rien à faire ici pour le moment
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Optionnel : peut être utilisé pour remonter en haut d'une liste par exemple
            }
        })

        // Affiche la vue par défaut (Rechercher) au démarrage
        showRechercheView()
    }

    private fun scanCode()
    {
            lifecycleScope.launch {
                try {
                    val scannedCode = barcodeScanner.startScan()
                    if (!scannedCode.isNullOrEmpty()) {
                        Toast.makeText(requireContext(), "Code scannée : $scannedCode (FONCTIONNALITE PAS TERMINEE ENCORE)", Toast.LENGTH_SHORT).show()
                        //TODO: Je ne peux PLUS supporter les choses de la diète pour l'instant, je suis CRAMÉ ! La fonctionnalité n'est pas la plus indispensable, je verrais après avoir terminé le fragment des stats
                        binding.tabLayout.getTabAt(0)?.select()
                    } else {
                        Toast.makeText(requireContext(), "Scan annulé ou code vide", Toast.LENGTH_SHORT).show()
                        binding.tabLayout.getTabAt(0)?.select()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Erreur lors du scan ou de la récupération : ${e.message}", Toast.LENGTH_LONG).show()
                    binding.tabLayout.getTabAt(0)?.select()
                }
            }
    }

    private fun showRechercheView() {
        // Affiche les vues de recherche/suggestions et cache le conteneur des autres onglets
        binding.nestedScrollView.visibility = View.VISIBLE
        binding.searchBarLayout.visibility = View.VISIBLE
        binding.tabContentContainer.visibility = View.GONE
        binding.tabContentContainer.removeAllViews() // Nettoyer le conteneur
        ajoutRapideBinding = null // Libérer la référence du binding
    }

    private fun showAjoutRapideView() {
        // Cache les vues de recherche/suggestions et affiche le conteneur des onglets
        binding.nestedScrollView.visibility = View.GONE
        binding.searchBarLayout.visibility = View.GONE
        binding.recyclerRechercheNourriture.visibility = View.GONE
        binding.tabContentContainer.visibility = View.VISIBLE
        binding.tabContentContainer.removeAllViews() // Nettoyer avant d'ajouter

        // Inflater le layout d'ajout rapide
        ajoutRapideBinding = LayoutTabAjoutRapideBinding.inflate(layoutInflater, binding.tabContentContainer, true)

        // Logique pour le bouton "Ajouter à la sélection"
        ajoutRapideBinding?.btnQuickAddToSelection?.setOnClickListener {
            val nom = ajoutRapideBinding?.etQuickAddNom?.text.toString()
            val calories = ajoutRapideBinding?.etQuickAddCalories?.text.toString().toIntOrNull() ?: 0
            val proteines = ajoutRapideBinding?.etQuickAddProteines?.text.toString().toFloatOrNull() ?: 0f
            val glucides = ajoutRapideBinding?.etQuickAddGlucides?.text.toString().toFloatOrNull() ?: 0f
            val lipides = ajoutRapideBinding?.etQuickAddLipides?.text.toString().toFloatOrNull() ?: 0f
            val quantite = ajoutRapideBinding?.etQuickAddQuantite?.text.toString().toFloatOrNull() ?: 0f

            if (nom.isNotBlank() && calories > 0 && proteines >= 0 && glucides >= 0 && lipides >= 0 && quantite > 0) {

                //TODO: Créer un recette affichée et l'ajouter dans le bazar
                var item = RecetteAffichee(-1, nom = nom,
                    calories = calories,
                    proteines = proteines,
                    glucides = glucides,
                    lipides = lipides,
                    quantiteTotale = quantite)

                selectedItems.add(ItemSelectionne(item = item, quantite = 1, isQuickAdd = true))
                selectionNourritureAdapter.updateData(selectedItems)

                // Optionnel : vider les champs après l'ajout
                ajoutRapideBinding?.etQuickAddNom?.text?.clear()
                ajoutRapideBinding?.etQuickAddCalories?.text?.clear()
                ajoutRapideBinding?.etQuickAddProteines?.text?.clear()
                ajoutRapideBinding?.etQuickAddGlucides?.text?.clear()
                ajoutRapideBinding?.etQuickAddLipides?.text?.clear()
                ajoutRapideBinding?.etQuickAddQuantite?.text?.clear()

            } else {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupRecherche() {
        // 1. Initialiser l'adapter pour les résultats de recherche
        rechercheNourritureAdapter = NourritureAdapter(emptyList(), { item ->
            addToSelectedItems(item)
        })
        binding.recyclerRechercheNourriture.adapter = rechercheNourritureAdapter
        binding.recyclerRechercheNourriture.layoutManager = LinearLayoutManager(requireContext())

        // 2. Ajouter le TextWatcher sur le champ de recherche
        binding.etSearchFood.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase().trim()
                if (query.isNotEmpty()) {
                    // Afficher la liste de recherche et masquer le contenu principal
                    binding.recyclerRechercheNourriture.visibility = View.VISIBLE
                    binding.nestedScrollView.visibility = View.GONE

                    // Filtrer les données
                    val filteredList = allNourritureItems.filter {
                        when (it) {
                            is Aliment -> it.nom.lowercase().contains(query)
                            is RecetteAffichee -> it.nom.lowercase().contains(query)
                            else -> false
                        }
                    }
                    rechercheNourritureAdapter.updateData(filteredList)
                } else {
                    // Masquer la liste de recherche et afficher le contenu principal
                    binding.recyclerRechercheNourriture.visibility = View.GONE
                    binding.nestedScrollView.visibility = View.VISIBLE
                    rechercheNourritureAdapter.updateData(emptyList()) // Vider la liste
                }
            }
        })
    }

    private suspend fun loadAllData() {
        val db = AppDatabase.getDatabase(requireContext())
        dieteActive = db.dieteDao().getActiveDiete()

        // Charger tous les aliments et recettes pour la recherche
        val allAliments = db.alimentDao().getAll()
        val allRecettes = db.recetteDao().getAll()
        allNourritureItems.clear()
        allNourritureItems.addAll(allAliments)
        allNourritureItems.addAll(allRecettes.map { getRecetteAsRecetteAffichee(db, it.id) })

        // Charger les favoris
        val listeFavoris = db.mangerHistoriqueDao().getTopTenFavoriteFoods()
        favoriteItems.clear()
        listeFavoris.forEach { nom ->
            getNourritureParNom(db, nom)?.let { favoriteItems.add(it) }
        }

        // Charger les suggestions de la diète
        dieteActive?.let {
            getItemsFromDieteForCurrentPeriode(it.id)
        }
    }

    private fun updateFavoritesUI() {
        if (favoriteItems.isEmpty()) {
            binding.emptyFavorites.visibility = View.VISIBLE
            binding.rvFavorites.visibility = View.GONE
        } else {
            binding.emptyFavorites.visibility = View.GONE
            binding.rvFavorites.visibility = View.VISIBLE
            favoriteFoodAdapter.updateData(favoriteItems)
        }
    }

    private fun updateDieteUI() {
        if (dieteActive != null && dieteItems.isNotEmpty()) {
            binding.emptyDiete.visibility = View.GONE
            binding.rvDiete.visibility = View.VISIBLE

            dieteItemsAdapter = NourritureAdapter(dieteItems, { item ->
                addToSelectedItems(item)
            })
            binding.rvDiete.layoutManager = LinearLayoutManager(requireContext())
            binding.rvDiete.adapter = dieteItemsAdapter
        } else {
            binding.emptyDiete.visibility = View.VISIBLE
            binding.rvDiete.visibility = View.GONE
        }
    }

    suspend fun getNourritureParNom(db: AppDatabase, nom: String): Any? {
        // Tente de trouver l'URI dans les recettes d'abord
        val recette = db.recetteDao().getByNom(nom)
        if (recette != null) {
            return getRecetteAsRecetteAffichee(db, recette.id)
        }

        // Si non trouvé, tente de trouver dans les aliments
        return db.alimentDao().getByNom(nom)
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
                    val ratio = it.quantite.toFloat() / 100f
                    calories = (item.calories * ratio).toInt()
                    proteines = item.proteines * ratio
                    glucides = item.glucides * ratio
                    lipides = item.lipides * ratio
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