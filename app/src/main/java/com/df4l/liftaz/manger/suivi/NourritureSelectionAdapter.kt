package com.df4l.liftaz.manger.suivi

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Aliment
import com.df4l.liftaz.manger.nourriture.RecetteAffichee

class NourritureSelectionAdapter(
    private var items: List<ItemSelectionne>,
    private val onDeleteClick: ((Any) -> Unit)
) : RecyclerView.Adapter<NourritureSelectionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.itemName)
        val txtSub: TextView = itemView.findViewById(R.id.itemSub)
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        val quantityEditText: EditText = itemView.findViewById(R.id.quantityEditText)

        val quantityPortionPrefix: TextView = itemView.findViewById(R.id.quantityPortionPrefix)
        val gramsSuffix: TextView = itemView.findViewById(R.id.quantityGramsSuffix)
        val btnRemoveSelection: ImageView = itemView.findViewById(R.id.btnRemoveSelection)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nourriture_selection, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemSelectionne = items[position]
        val item = itemSelectionne.item // On récupère l'objet (Aliment ou RecetteAffichee)
        val quantite = itemSelectionne.quantite // Et sa quantité

        when(item) {
            is Aliment -> bindAliment(holder, item)
            is RecetteAffichee -> bindRecette(holder, item)
        }

        if(item is RecetteAffichee || (item is Aliment && item.quantiteParDefaut != null))
        {
            holder.gramsSuffix.visibility = View.GONE
            holder.quantityPortionPrefix.visibility = View.VISIBLE
        }
        else
        {
            holder.gramsSuffix.visibility = View.VISIBLE
            holder.quantityPortionPrefix.visibility = View.GONE
        }

        holder.btnRemoveSelection.setOnClickListener {
            onDeleteClick.invoke(item)
        }

        holder.quantityEditText.setText("${quantite}")
    }

    private fun bindAliment(holder: ViewHolder, a: Aliment) {
        val q = a.quantiteParDefaut ?: 100

        holder.txtName.text = a.nom
        holder.txtSub.text =
            if (a.quantiteParDefaut != null) "${a.marque} - $q g"
            else a.marque
        holder.quantityEditText.setText(q.toString())

        a.imageUri?.let { uriString ->
            Glide.with(holder.itemView.context)
                .load(uriString.toUri())
                .centerCrop()
                .into(holder.itemImage)
        } ?: run {
            holder.itemImage.setImageResource(0)
            holder.itemImage.setBackgroundColor(Color.parseColor("#E0E0E0"))
        }
    }

    private fun bindRecette(holder: ViewHolder, r: RecetteAffichee) {
        holder.txtName.text = r.nom

        val (subText, defaultQuantity) = if (r.quantitePortion != null) {
            "Portion de ${r.quantitePortion.toInt()}g" to r.quantitePortion.toInt()
        } else {
            val totalQuantityText = if (r.quantiteTotale % 1f == 0f) "${r.quantiteTotale.toInt()}g" else "${r.quantiteTotale}"
            totalQuantityText to r.quantiteTotale.toInt()
        }
        holder.txtSub.text = subText
        holder.quantityEditText.setText(defaultQuantity.toString())


        r.imageUri?.let { uriString ->
            Glide.with(holder.itemView.context)
                .load(uriString.toUri())
                .centerCrop()
                .into(holder.itemImage)
        } ?: run {
            holder.itemImage.setImageResource(0)
            holder.itemImage.setBackgroundColor(Color.parseColor("#E0E0E0"))
        }
    }

    fun updateData(newItems: List<ItemSelectionne>) {
        // Implémentation avec DiffUtil (recommandé) ou notifyDataSetChanged
        this.items = newItems
        notifyDataSetChanged() // Méthode simple mais moins performante
    }
}

data class ItemSelectionne(
    val item: Any, // L'objet (Aliment ou RecetteAffichee)
    var quantite: Int = 1 // La quantité, initialisée à 1 par défaut
)
