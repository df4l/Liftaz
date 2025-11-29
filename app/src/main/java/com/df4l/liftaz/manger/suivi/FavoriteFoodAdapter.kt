package com.df4l.liftaz.manger.suivi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Aliment
import com.df4l.liftaz.manger.nourriture.RecetteAffichee
import com.google.android.material.imageview.ShapeableImageView

class FavoriteFoodAdapter(
    private val onFavoriteClick: (Any) -> Unit
) : RecyclerView.Adapter<FavoriteFoodAdapter.ViewHolder>() {

    private var items: List<Any> = listOf()

    // Le ViewHolder qui contient les vues de item_favorite_food.xml
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ShapeableImageView = itemView.findViewById(R.id.iv_favorite_food)
        val nameTextView: TextView = itemView.findViewById(R.id.tv_favorite_food_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // On "gonfle" (inflate) le layout item_favorite_food.xml pour chaque élément
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_food, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // On utilise un 'when' pour différencier le type d'objet, comme dans NourritureAdapter
        when (item) {
            is Aliment -> bindAliment(holder, item)
            is RecetteAffichee -> bindRecette(holder, item)
        }

        // On configure le clic sur l'ensemble de l'item pour déclencher l'action
        holder.itemView.setOnClickListener {
            onFavoriteClick(item)
        }
    }

    /**
     * Gère l'affichage pour un objet de type Aliment.
     */
    private fun bindAliment(holder: ViewHolder, aliment: Aliment) {
        holder.nameTextView.text = aliment.nom

        // Utilisation de Glide pour charger l'image
        aliment.imageUri?.let { uriString ->
            Glide.with(holder.itemView.context)
                .load(uriString.toUri())
                .into(holder.imageView)
        } ?: run {
            // Si pas d'image, on peut mettre une image par défaut
            holder.imageView.setImageResource(R.drawable.ic_add_circle) // Remplacez par une image placeholder si vous en avez une
        }
    }

    /**
     * Gère l'affichage pour un objet de type RecetteAffichee.
     */
    private fun bindRecette(holder: ViewHolder, recette: RecetteAffichee) {
        holder.nameTextView.text = recette.nom

        // Utilisation de Glide pour charger l'image
        recette.imageUri?.let { uriString ->
            Glide.with(holder.itemView.context)
                .load(uriString.toUri())
                .into(holder.imageView)
        } ?: run {
            // Si pas d'image, on peut mettre une image par défaut
            holder.imageView.setImageResource(R.drawable.ic_add_circle) // Remplacez par une image placeholder si vous en avez une
        }
    }

    /**
     * Méthode pour mettre à jour la liste d'éléments et rafraîchir le RecyclerView.
     */
    fun updateData(newItems: List<Any>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}