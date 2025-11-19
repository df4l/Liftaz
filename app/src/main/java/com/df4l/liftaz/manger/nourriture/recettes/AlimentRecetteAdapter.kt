package com.df4l.liftaz.manger.nourriture.recettes

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Aliment

class AlimentRecetteAdapter(
    private val aliments: MutableList<AlimentRecetteItem>,
    private val onQuantityChanged: () -> Unit,
    private val onRemove: (AlimentRecetteItem) -> Unit
) : RecyclerView.Adapter<AlimentRecetteAdapter.AlimentRecetteViewHolder>() {

    data class AlimentRecetteItem(
        val aliment: Aliment,
        var quantite: Float = 0f
    )

    inner class AlimentRecetteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.textAlimentName)
        val marque = view.findViewById<TextView>(R.id.textAlimentMarque)
        val quantityInput = view.findViewById<EditText>(R.id.editQuantiteIngredient)
        val btnRemove = view.findViewById<ImageButton>(R.id.btnRemoveAliment)

        val tvP = view.findViewById<TextView>(R.id.tvProteinesIngredient)
        val tvG = view.findViewById<TextView>(R.id.tvGlucidesIngredient)
        val tvL = view.findViewById<TextView>(R.id.tvLipidesIngredient)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlimentRecetteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_aliment_recette, parent, false)
        return AlimentRecetteViewHolder(view)
    }

    override fun getItemCount(): Int = aliments.size

    override fun onBindViewHolder(holder: AlimentRecetteViewHolder, position: Int) {
        val item = aliments[position]
        val aliment = item.aliment

        holder.name.text = aliment.nom
        holder.marque.text = aliment.marque

        holder.quantityInput.setText(
            if (item.quantite == 0f) "" else item.quantite.toString()
        )

        // --- CALCUL MACROS (quantité en grammes vs per 100g) ---
        fun updateMacros() {
            val coef = item.quantite / 100f

            holder.tvP.text = "${"%.1f".format(aliment.proteines * coef)} g"
            holder.tvG.text = "${"%.1f".format(aliment.glucides * coef)} g"
            holder.tvL.text = "${"%.1f".format(aliment.lipides * coef)} g"

            onQuantityChanged()
        }

        updateMacros()

        // --- LISTEN QUANTITÉ ---
        holder.quantityInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString().toFloatOrNull() ?: 0f
                item.quantite = q
                updateMacros()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // --- REMOVE ---
        holder.btnRemove.setOnClickListener {
            onRemove(item)
        }
    }

    fun removeItem(item: AlimentRecetteItem) {
        val index = aliments.indexOf(item)
        if (index >= 0) {
            aliments.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
