package com.df4l.liftaz.manger

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Aliment
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.Diete
import com.df4l.liftaz.data.DieteDao
import com.df4l.liftaz.data.PeriodeRepas
import com.df4l.liftaz.data.Recette
import com.df4l.liftaz.data.RecetteAliments
import com.df4l.liftaz.databinding.FragmentMangerBinding
import com.df4l.liftaz.manger.nourriture.NourritureAdapter
import com.df4l.liftaz.manger.nourriture.RecetteAffichee
import com.df4l.liftaz.manger.suivi.AjouterRepasBottomSheetFragment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MangerFragment : Fragment() {

    private var _binding: FragmentMangerBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var dieteDao: DieteDao

    private var dieteActive: Diete? = null
    private var caloriesConsommees: Int = 0
    private var proteinesConsommees: Int = 0
    private var glucidesConsommees: Int = 0
    private var lipidesConsommees: Int = 0


    private var matinMangerItems = mutableListOf<RecetteAffichee>()
    private var midiMangerItems = mutableListOf<RecetteAffichee>()
    private var apresMidiMangerItems = mutableListOf<RecetteAffichee>()
    private var soirMangerItems = mutableListOf<RecetteAffichee>()

    private lateinit var matinAdapter: NourritureAdapter
    private lateinit var midiAdapter: NourritureAdapter
    private lateinit var apresMidiAdapter: NourritureAdapter
    private lateinit var soirAdapter: NourritureAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMangerBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())
        dieteDao = database.dieteDao()

        lifecycleScope.launch {
            dieteActive = dieteDao.getActiveDiete()

            setupAdapters()
            rafraichirListeRepas()
            updateCaloriesAndMacros()
            defaultAlimentsAndRecettes()
        }

        setupCurrentDate()

        binding.fabManger.setOnClickListener { view ->
            showFabMenu(view)
        }

        childFragmentManager.setFragmentResultListener("repasAjoute", this) { requestKey, bundle ->
            // Ce bloc de code sera exécuté lorsque le BottomSheet enverra un résultat.
            // Le bundle pourrait contenir des données, mais ici on a juste besoin du signal.
            Log.d(AjouterRepasBottomSheetFragment.TAG, "Résultat reçu depuis le BottomSheet, mise à jour de l'UI...")

            // Mettez à jour votre UI ici !
            lifecycleScope.launch {
                updateCaloriesAndMacros()
                rafraichirListeRepas()
            }
        }

        binding.btnAddRepas.setOnClickListener {
            // Crée une instance de notre BottomSheetFragment
            val bottomSheet = AjouterRepasBottomSheetFragment()
            // Affiche le BottomSheet en utilisant le childFragmentManager du fragment
            bottomSheet.show(childFragmentManager, AjouterRepasBottomSheetFragment.TAG)
        }


    }

    suspend private fun setupAdapters()
    {
        matinAdapter = NourritureAdapter(matinMangerItems, onItemClick={}, onDeleteClick={})
        binding.rvMatin.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMatin.adapter = matinAdapter
        binding.rvMatin.isNestedScrollingEnabled = false // Pour un défilement fluide dans le ScrollView

        // --- Adapter pour le Midi ---
        midiAdapter = NourritureAdapter(midiMangerItems, onItemClick={}, onDeleteClick={})
        binding.rvMidi.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMidi.adapter = midiAdapter
        binding.rvMidi.isNestedScrollingEnabled = false

        // --- Adapter pour l'Après-midi ---
        apresMidiAdapter = NourritureAdapter(apresMidiMangerItems, onItemClick={}, onDeleteClick={})
        binding.rvApresMidi.layoutManager = LinearLayoutManager(requireContext())
        binding.rvApresMidi.adapter = apresMidiAdapter
        binding.rvApresMidi.isNestedScrollingEnabled = false

        // --- Adapter pour le Soir ---
        soirAdapter = NourritureAdapter(soirMangerItems, onItemClick={}, onDeleteClick={})
        binding.rvSoir.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSoir.adapter = soirAdapter
        binding.rvSoir.isNestedScrollingEnabled = false
    }

    suspend private fun rafraichirListeRepas()
    {
        val db = AppDatabase.getDatabase(requireContext())
        val listeRepas = db.mangerHistoriqueDao().getHistoriqueForDate(Date())

        matinMangerItems.clear()
        midiMangerItems.clear()
        apresMidiMangerItems.clear()
        soirMangerItems.clear()

        listeRepas.forEach{
            //On crée une "recette affichée" à partir de chaque entrée du Manger Historique car ça permet de recycler NourritureAdapter !

            var toRecetteAffichee = RecetteAffichee(
                id = it.id,
                nom = it.nomElement,
                proteines = it.proteines,
                glucides = it.glucides,
                lipides = it.lipides,
                calories = it.calories,
                quantiteTotale = 0f,
                heureManger = getHeureFromDate(it.date),
                quantiteTexte = it.quantite,
                imageUri = getImageUriParNom(db, it.nomElement)
            )

            when(getPeriodeRepasFromDate(it.date))
            {
                PeriodeRepas.MATIN -> matinMangerItems.add(toRecetteAffichee)
                PeriodeRepas.MIDI -> midiMangerItems.add(toRecetteAffichee)
                PeriodeRepas.APRES_MIDI -> apresMidiMangerItems.add(toRecetteAffichee)
                PeriodeRepas.SOIR -> soirMangerItems.add(toRecetteAffichee)
            }
        }

        matinAdapter.updateData(matinMangerItems)
        midiAdapter.updateData(midiMangerItems)
        apresMidiAdapter.updateData(apresMidiMangerItems)
        soirAdapter.updateData(soirMangerItems)

        if (matinMangerItems.isEmpty()) {
            binding.rvMatin.visibility = View.GONE
            binding.emptyMatin.visibility = View.VISIBLE
        } else {
            binding.rvMatin.visibility = View.VISIBLE
            binding.emptyMatin.visibility = View.GONE
        }

// Met à jour la visibilité pour la section du midi
        if (midiMangerItems.isEmpty()) {
            binding.rvMidi.visibility = View.GONE
            binding.emptyMidi.visibility = View.VISIBLE
        } else {
            binding.rvMidi.visibility = View.VISIBLE
            binding.emptyMidi.visibility = View.GONE
        }

// Met à jour la visibilité pour la section de l'après-midi
        if (apresMidiMangerItems.isEmpty()) {
            binding.rvApresMidi.visibility = View.GONE
            binding.emptyApresMidi.visibility = View.VISIBLE
        } else {
            binding.rvApresMidi.visibility = View.VISIBLE
            binding.emptyApresMidi.visibility = View.GONE
        }

// Met à jour la visibilité pour la section du soir
        if (soirMangerItems.isEmpty()) {
            binding.rvSoir.visibility = View.GONE
            binding.emptySoir.visibility = View.VISIBLE
        } else {
            binding.rvSoir.visibility = View.VISIBLE
            binding.emptySoir.visibility = View.GONE
        }
    }

    suspend fun getImageUriParNom(db: AppDatabase, nom: String): String? {
        // Tente de trouver l'URI dans les recettes d'abord
        val imageUriRecette = db.recetteDao().getImageUriParNom(nom)
        if (imageUriRecette != null) {
            return imageUriRecette
        }

        // Si non trouvé, tente de trouver dans les aliments
        return db.alimentDao().getImageUriParNom(nom)
    }

    private fun getHeureFromDate(date: Date): String {
        // Le pattern "HH:mm" correspond à l'heure sur 24 heures et aux minutes.
        val formatHeure = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatHeure.format(date)
    }

    private fun getPeriodeRepasFromDate(date: Date): PeriodeRepas {
        val calendar = Calendar.getInstance()
        calendar.time = date // Configure le calendrier avec la date fournie
        val heure = calendar.get(Calendar.HOUR_OF_DAY)

        return when (heure) {
            in 4..11 -> PeriodeRepas.MATIN
            in 12..14 -> PeriodeRepas.MIDI
            in 15..18 -> PeriodeRepas.APRES_MIDI
            else -> PeriodeRepas.SOIR // Couvre de 19h à 3h du matin
        }
    }

    suspend private fun defaultAlimentsAndRecettes()
    {
        val db = AppDatabase.getDatabase(requireContext())
        val aliments = db.alimentDao().getAll()
        val recettes = db.recetteDao().getAll()

        if(aliments.isEmpty() && recettes.isEmpty()) {
            //Un aliment avec portion
            var idWhey = db.alimentDao().insert(
                Aliment(nom = "Whey vanille",
                    marque = "XXL Nutrition",
                    calories = 392,
                    proteines = 77f,
                    lipides = 6.7f,
                    glucides = 8.1f,
                    quantiteParDefaut = 30,
                    imageUri = null
                )
            ).toInt()

            //Un aliment sans portion
            var idLait = db.alimentDao().insert(
                Aliment(nom = "Lait demi-ecrémé",
                    marque = "Envia",
                    calories = 47,
                    proteines = 3.3f,
                    lipides = 1.6f,
                    glucides = 4.8f,
                    quantiteParDefaut = null,
                    imageUri = null
                )
            ).toInt()

            //Une recette sans portion
            var idShaker = db.recetteDao().insert(
                Recette(nom = "Shaker protéiné",
                    quantitePortion = null,
                    imageUri = null
                )
            ).toInt()

            db.recetteAlimentsDao().insert(
                RecetteAliments(
                    idRecette = idShaker,
                    idAliment = idWhey,
                    coefAliment = 30 / 100f
                )
            )

            db.recetteAlimentsDao().insert(
                RecetteAliments(
                    idRecette = idShaker,
                    idAliment = idLait,
                    coefAliment = 200 / 100f
                )
            )

            //Une recette avec portion
            var idShakerVerre = db.recetteDao().insert(
                Recette(nom = "Verre de shaker protéiné",
                    quantitePortion = 50,
                    imageUri = null
                )
            ).toInt()

            db.recetteAlimentsDao().insert(
                RecetteAliments(
                    idRecette = idShakerVerre,
                    idAliment = idWhey,
                    coefAliment = 30 / 100f
                )
            )

            db.recetteAlimentsDao().insert(
                RecetteAliments(
                    idRecette = idShakerVerre,
                    idAliment = idLait,
                    coefAliment = 200 / 100f
                )
            )

        }
    }

    suspend private fun updateCaloriesAndMacros() {
        val diete = dieteActive // Crée une copie locale pour la capture par la lambda
        if (diete != null) {
            val caloriesRestantes = diete.objCalories - caloriesConsommees
            binding.tvCaloriesRestantes.text = "$caloriesRestantes\ncalories\nrestantes"
            binding.cpbCalories.progressMax = diete.objCalories.toFloat()

            binding.tvProteinesManger.text = "${proteinesConsommees} / ${diete.objProteines}g"
            binding.tvGlucidesManger.text = "${glucidesConsommees} / ${diete.objGlucides}g"
            binding.tvLipidesManger.text = "${lipidesConsommees} / ${diete.objLipides}g"
        } else {
            binding.tvCaloriesRestantes.text = "${caloriesConsommees}\ncalories\nconsommées"
            binding.cpbCalories.progressMax = 100f // Valeur par défaut si aucune diète n'est active

            // Récupère le dernier poids pour un affichage plus informatif
            val dernierPoids =
                AppDatabase.getDatabase(requireContext()).entreePoidsDao().getLatestWeight()?.poids
            if (dernierPoids != null && dernierPoids > 0) {
                val proteinesParKg = "%.1f".format(proteinesConsommees / dernierPoids)
                val glucidesParKg = "%.1f".format(glucidesConsommees / dernierPoids)
                val lipidesParKg = "%.1f".format(lipidesConsommees / dernierPoids)

                binding.tvProteinesManger.text = "${proteinesConsommees}g (${proteinesParKg}g/kg)"
                binding.tvGlucidesManger.text = "${glucidesConsommees}g (${glucidesParKg}g/kg)"
                binding.tvLipidesManger.text = "${lipidesConsommees}g (${lipidesParKg}g/kg)"
            } else {
                binding.tvProteinesManger.text = "${proteinesConsommees}g"
                binding.tvGlucidesManger.text = "${glucidesConsommees}g"
                binding.tvLipidesManger.text = "${lipidesConsommees}g"
            }
        }

        binding.cpbCalories.progress = caloriesConsommees.toFloat()
    }

    private fun setupCurrentDate() {
        val dateFormat = SimpleDateFormat("EEEE dd LLLL", Locale.getDefault())
        val today = dateFormat.format(Date())

        binding.tvDate.text = today
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            }
    }
    private fun showFabMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_manger_options, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_nourriture -> {
                    goToNourritureView()
                    true
                }
                R.id.action_dietes -> {
                    goToDieteView()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun goToDieteView()
    {
        val navController = findNavController()
        navController.navigate(R.id.action_mangerFragment_to_dieteFragment)
    }

    private fun goToNourritureView()
    {
        val navController = findNavController()
        navController.navigate(R.id.action_mangerFragment_to_nourritureFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
