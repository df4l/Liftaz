package com.df4l.liftaz.soulever.fioul

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.key.type
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.FioulType
import com.df4l.liftaz.data.FioulType.*
import com.df4l.liftaz.data.MotivationFioul
import com.df4l.liftaz.data.MotivationFioulDao
import com.df4l.liftaz.data.Muscle
import com.df4l.liftaz.data.MuscleDao
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.io.path.exists

class FioulFragment : Fragment(R.layout.fragment_motivationfioul) {

    private lateinit var database: AppDatabase
    private lateinit var motivationFioulDao: MotivationFioulDao
    private lateinit var adapter: FioulAdapter

    private lateinit var textEmpty: TextView
    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())
        motivationFioulDao = database.motivationFioulDao()

        textEmpty = view.findViewById<TextView>(R.id.text_empty_fioul)
        recyclerView = view.findViewById<RecyclerView>(R.id.rvFiouls)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FioulAdapter(emptyList()) { fuelToDelete ->
            AlertDialog.Builder(requireContext())
                .setTitle("Supprimer ce fioul ?")
                .setMessage("Voulez-vous vraiment supprimer « ${fuelToDelete.title} » ?")
                .setPositiveButton("Supprimer") { _, _ ->
                    lifecycleScope.launch {
                        // ✨ Supprimer le fichier associé avant de supprimer l'entrée DB
                        deleteAssociatedFile(fuelToDelete)
                        motivationFioulDao.deleteFioul(fuelToDelete)
                        loadFiouls()
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
        recyclerView.adapter = adapter

        loadFiouls()

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_addFioulMotivation)
        fab.setOnClickListener {
            val dialog = AddFioulMotivationDialog { newFuel ->
                lifecycleScope.launch {
                    motivationFioulDao.insertFuel(newFuel)
                    loadFiouls()
                }
            }
            dialog.show(parentFragmentManager, "AddFioulDialog")
        }
    }

        private suspend fun deleteAssociatedFile(fuel: MotivationFioul) {
        // S'assurer qu'il s'agit bien d'un fioul avec un média
        if (fuel.type == TEXTE || fuel.contentUri.isNullOrBlank()) {
            return
        }

        withContext(Dispatchers.IO) {
            try {
                val fileUri = Uri.parse(fuel.contentUri)
                // L'URI est maintenant de type "file:///", nous utilisons .path
                val filePath = fileUri.path
                if (filePath != null) {
                    val file = File(filePath)
                    if (file.exists()) {
                        if (file.delete()) {
                            Log.d("FioulFragment", "Fichier associé supprimé : $filePath")
                        } else {
                            Log.w("FioulFragment", "Échec de la suppression du fichier : $filePath")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FioulFragment", "Erreur lors de la suppression du fichier associé.", e)
            }
        }
    }

    private fun loadFiouls() {
        lifecycleScope.launch {
            val fiouls = motivationFioulDao.getAllFioulsOnce() // version suspendue
            adapter.updateData(fiouls)
            Log.d("FioulFragment", "Nombre de fiouls : ${fiouls.size}")

            if (fiouls.isEmpty()) {
                textEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                textEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.releaseAllPlayers()
    }
}

