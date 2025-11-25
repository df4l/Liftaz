package com.df4l.liftaz.manger.diete.dietesMenu

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.DieteDao
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

private lateinit var recyclerView: RecyclerView
private lateinit var adapter: DieteAdapter
private lateinit var database: AppDatabase
private lateinit var dieteDao: DieteDao
private lateinit var textEmpty: TextView

class DietesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
            return inflater.inflate(R.layout.fragment_dietes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fab_add_diete)

        fabAdd.setOnClickListener {
            goToCreationDieteView()
        }

        recyclerView = view.findViewById(R.id.recyclerViewDietes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        textEmpty = view.findViewById(R.id.text_empty_dietes)

        database = AppDatabase.getDatabase(requireContext())
        dieteDao = database.dieteDao()

        loadDietes()
    }

    private fun loadDietes()
    {
        lifecycleScope.launch {
            val dietes = dieteDao.getAllDietes()

            adapter = DieteAdapter(dietes = dietes,
                onActivate = {

                },
                onDelete = {
                    diete ->
                    AlertDialog.Builder(requireContext())
                        .setTitle("Supprimer la diète ?")
                        .setMessage("Voulez-vous vraiment supprimer « ${diete.nom} » ? ")
                        .setPositiveButton("Oui") { _, _ ->
                            lifecycleScope.launch {
                                // Détacher les séances de ce programme
                                AppDatabase.getDatabase(requireContext()).dieteElementsDao().deleteByIdDiete(diete.id)
                                // Supprimer le programme
                                AppDatabase.getDatabase(requireContext()).dieteDao().delete(diete)
                                loadDietes()
                                Toast.makeText(requireContext(), "Diète supprimée 🗑️", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Non", null)
                        .show()
                })
            recyclerView.adapter = adapter


            if (dietes.isEmpty()) {
                textEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                textEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun goToCreationDieteView() {
        val navController = findNavController()
        navController.navigate(R.id.action_dietesFragment_to_creationDieteFragment)
    }
}