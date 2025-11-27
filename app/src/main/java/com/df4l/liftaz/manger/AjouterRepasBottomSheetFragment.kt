package com.df4l.liftaz.manger

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.Diete
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

class AjouterRepasBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMangerBinding? = null

    private val favoriteItems = mutableListOf<Any>()
    private var dieteActive: Diete? = null
    private val dieteItems = mutableListOf<Any>()
    private lateinit var dieteItemsAdapter: NourritureAdapter

    // Cette propriété est valide uniquement entre onCreateView et onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetMangerBinding.inflate(inflater, container, false)

        lifecycleScope.launch {
            dieteActive = AppDatabase.getDatabase(requireContext()).dieteDao().getActiveDiete()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        //Peupler la partie "favoris"
        lifecycleScope.launch {

            // Exécute la requête de base de données dans un thread d'arrière-plan
            val listeFavoris = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(requireContext()).mangerHistoriqueDao()
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
                    onItemClick = { Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show() }
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

    private fun getCurrentPeriodeRepas(): PeriodeRepas {
        val calendar = Calendar.getInstance()
        val heure = calendar.get(Calendar.HOUR_OF_DAY)

        return when (heure) {
            in 4..11 -> {
                binding.tvSuggestionsTitle.text = "Manger du matin"
                PeriodeRepas.MATIN
            }
            in 12..15 -> {
                binding.tvSuggestionsTitle.text = "Manger du midi"
                PeriodeRepas.MIDI
            }
            in 16..18 -> {
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
        val db = AppDatabase.getDatabase(requireContext())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // Le Tag est utile pour trouver le fragment si nécessaire
        const val TAG = "AjouterRepasBottomSheetFragment"
    }
}