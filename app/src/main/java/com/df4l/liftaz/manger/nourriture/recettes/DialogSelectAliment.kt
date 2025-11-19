package com.df4l.liftaz.manger.nourriture.recettes

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Aliment
import com.df4l.liftaz.manger.nourriture.NourritureAdapter

class DialogSelectAliment(
    private val aliments: List<Aliment>,
    private val onAlimentSelected: (Aliment) -> Unit
) : DialogFragment() {

    private lateinit var adapter: NourritureAdapter
    private var filteredAliments: List<Aliment> = aliments

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_select_aliment, null)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerAliments)
        val search = view.findViewById<EditText>(R.id.searchAliment)

        adapter = NourritureAdapter(
            items = filteredAliments,
            onItemClick = { item ->
                // Uniquement selection → pas de suppression ici
                if (item is Aliment) {
                    onAlimentSelected(item)
                    dismiss()
                }
            },
            onDeleteClick = null
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // --- Filtre search ---
        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()

                filteredAliments =
                    if (query.isEmpty()) aliments
                    else aliments.filter { it.nom.lowercase().contains(query) }

                adapter.updateData(filteredAliments)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        builder.setView(view)
        return builder.create()
    }
}
