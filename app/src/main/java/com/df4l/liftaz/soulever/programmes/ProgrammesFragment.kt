package com.df4l.liftaz.soulever.programmes

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.ProgrammeDao
import com.df4l.liftaz.data.SeanceDao
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch


class ProgrammesFragment : Fragment()  {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProgrammeAdapter
    private lateinit var database: AppDatabase
    private lateinit var programmeDao: ProgrammeDao
    private lateinit var seanceDao: SeanceDao
    private lateinit var textEmpty: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_programmes, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewProgrammes)

        textEmpty = view.findViewById(R.id.text_empty_programmes)

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_add_programme)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        database = AppDatabase.getDatabase(requireContext())
        programmeDao = database.programmeDao()
        seanceDao = database.seanceDao()

        fab.setOnClickListener {
            CreationProgrammeDialog {
                loadProgrammes()
            }.show(parentFragmentManager, "CreationProgrammeDialog")
        }

        loadProgrammes()
        return view
    }

    private fun loadProgrammes() {
        lifecycleScope.launch {
            val programmes = programmeDao.getAllProgrammesAvecSeances()

            adapter = ProgrammeAdapter(
                programmes = programmes,
                onActivate = { programme ->
                    lifecycleScope.launch {
                        programmeDao.desactiverTous()
                        programmeDao.activer(programme.id)
                        loadProgrammes()
                        Toast.makeText(requireContext(), "Programme « ${programme.nom} » activé ✅", Toast.LENGTH_SHORT).show()
                    }
                },
                onDelete = { programme ->
                    AlertDialog.Builder(requireContext())
                        .setTitle("Supprimer le programme ?")
                        .setMessage("Voulez-vous vraiment supprimer « ${programme.nom} » ? Les séances associées seront détachées.")
                        .setPositiveButton("Oui") { _, _ ->
                            lifecycleScope.launch {
                                // Détacher les séances de ce programme
                                seanceDao.clearProgrammeIdForProgramme(programme.id)
                                // Supprimer le programme
                                programmeDao.delete(programme)
                                loadProgrammes()
                                Toast.makeText(requireContext(), "Programme supprimé 🗑️", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Non", null)
                        .show()
                },
                onModify = {
                    loadProgrammes()
                }
            )
            recyclerView.adapter = adapter


            if (programmes.isEmpty()) {
                textEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                textEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }

        }
    }
}


