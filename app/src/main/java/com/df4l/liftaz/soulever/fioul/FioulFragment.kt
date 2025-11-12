package com.df4l.liftaz.soulever.fioul

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.MotivationFioulDao
import com.df4l.liftaz.data.Muscle
import com.df4l.liftaz.data.MuscleDao
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class FioulFragment : Fragment(R.layout.fragment_motivationfioul) {

    private lateinit var database: AppDatabase
    private lateinit var motivationFioulDao: MotivationFioulDao
    private lateinit var adapter: FioulAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())
        motivationFioulDao = database.motivationFioulDao()

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvFiouls)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FioulAdapter(emptyList()) { fuelToDelete ->
            AlertDialog.Builder(requireContext())
                .setTitle("Supprimer ce fioul ?")
                .setMessage("Voulez-vous vraiment supprimer « ${fuelToDelete.title} » ?")
                .setPositiveButton("Supprimer") { _, _ ->
                    lifecycleScope.launch {
                        motivationFioulDao.deleteFioul(fuelToDelete)
                        loadFiouls()
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
        recyclerView.adapter = adapter

        // Charger les fiouls au démarrage
        loadFiouls()

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_addFioulMotivation)
        fab.setOnClickListener {
            val dialog = AddFioulMotivationDialog { newFuel ->
                lifecycleScope.launch {
                    motivationFioulDao.insertFuel(newFuel)
                    loadFiouls() // 🔁 Recharge après insertion
                }
            }
            dialog.show(parentFragmentManager, "AddFioulDialog")
        }
    }

    private fun loadFiouls() {
        lifecycleScope.launch {
            val fiouls = motivationFioulDao.getAllFioulsOnce() // version suspendue
            adapter.updateData(fiouls)
            Log.d("FioulFragment", "Nombre de fiouls : ${fiouls.size}")
        }
    }
}

