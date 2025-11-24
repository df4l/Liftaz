package com.df4l.liftaz.manger.diete

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
import com.df4l.liftaz.manger.nourriture.RecetteAffichee

class DialogSelectNourriture(
    private val items: List<Any>,   // Mélange aliment + recetteAffichee
    private val onItemSelected: (Any) -> Unit
) : DialogFragment() {

    private lateinit var adapter: NourritureAdapter
    private var filtered: List<Any> = items

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_select_aliment, null)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerAliments)
        val search = view.findViewById<EditText>(R.id.searchAliment)

        adapter = NourritureAdapter(filtered, { item ->
            onItemSelected(item)
            dismiss()
        })

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString().lowercase()

                filtered = if (q.isEmpty()) items else {
                    items.filter {
                        when (it) {
                            is Aliment -> it.nom.lowercase().contains(q)
                            is RecetteAffichee -> it.nom.lowercase().contains(q)
                            else -> false
                        }
                    }
                }

                adapter.updateData(filtered)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        builder.setView(view)
        return builder.create()
    }
}
