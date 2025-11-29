package com.df4l.liftaz.manger.nourriture

import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.animation.with
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Aliment

class NourritureAdapter(
    private var items: List<Any>,
    private val onItemClick: (Any) -> Unit,
    private val onDeleteClick: ((Any) -> Unit)? = null
) : RecyclerView.Adapter<NourritureAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.itemName)
        val txtSub: TextView = itemView.findViewById(R.id.itemSub)
        val txtNutri: TextView = itemView.findViewById(R.id.itemNutri)
        val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemoveNourriture)
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        val itemTimeManger: TextView = itemView.findViewById(R.id.itemTimeManger)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nourriture, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        when (item) {
            is Aliment -> bindAliment(holder, item)
            is RecetteAffichee -> bindRecette(holder, item)
        }

        // --- Gestion du clic sur l'item ---
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        // --- Gestion du bouton supprimer ---
        if (onDeleteClick == null) {
            holder.btnRemove.visibility = View.GONE
        } else {
            holder.btnRemove.visibility = View.VISIBLE
            holder.btnRemove.setOnClickListener { onDeleteClick.invoke(item) }
        }
    }

    private fun bindAliment(holder: ViewHolder, a: Aliment) {
        val q = a.quantiteParDefaut ?: 100

        holder.txtName.text = a.nom
        holder.txtSub.text =
            if (a.quantiteParDefaut != null) "${a.marque} - $q g"
            else "${a.marque} - Pour 100 g"

        a.imageUri?.let { uriString ->
            Glide.with(holder.itemView.context)
                .load(uriString.toUri())
                .centerCrop()
                .into(holder.itemImage)
        } ?: run {
            // S'il n'y a pas d'image, on peut afficher une placeholder ou nettoyer la vue
            holder.itemImage.setImageResource(0) // Efface l'image précédente
            holder.itemImage.setBackgroundColor(Color.parseColor("#E0E0E0"))
        }

        val coef = q / 100f
        val prot = a.proteines * coef
        val glu = a.glucides * coef
        val lip = a.lipides * coef
        val cal = (a.calories * coef).toInt()

        holder.txtNutri.text = nutritionalString(prot, glu, lip, cal)
    }

    private fun bindRecette(holder: ViewHolder, r: RecetteAffichee) {
        holder.txtName.text = r.nom

        val subText = if (r.quantitePortion != null) {
            "Portion de ${r.quantitePortion.toInt()}g"
        } else {
            if (r.quantiteTotale % 1f == 0f) "${r.quantiteTotale.toInt()}g" else "${r.quantiteTotale}"
        }

        r.imageUri?.let { uriString ->
            Glide.with(holder.itemView.context)
                .load(uriString.toUri())
                .centerCrop()
                .into(holder.itemImage)
        } ?: run {
            holder.itemImage.setImageResource(0)
            holder.itemImage.setBackgroundColor(Color.parseColor("#E0E0E0"))
        }

        if(r.heureManger != null) {
            holder.itemTimeManger.visibility = View.VISIBLE
            holder.itemTimeManger.text = r.heureManger
        }

        holder.txtSub.text = subText

        val coef =
            if (r.quantitePortion != null && r.quantiteTotale > 0f)
                r.quantitePortion / r.quantiteTotale
            else
                1f

        val prot = r.proteines * coef
        val glu = r.glucides * coef
        val lip = r.lipides * coef
        val cal = (r.calories * coef).toInt()

        if(r.heureManger == null) {
            holder.txtNutri.text = nutritionalString(prot, glu, lip, cal)
        }
        else
        {
            holder.txtSub.text = nutritionalString(prot, glu, lip, cal)
            holder.txtNutri.text = r.quantiteTexte

        }
    }

    private fun nutritionalString(p: Float, g: Float, l: Float, kcal: Int): SpannableString {
        val protStr = "${"%.1f".format(p)}g"
        val glucStr = "${"%.1f".format(g)}g"
        val lipStr = "${"%.1f".format(l)}g"
        val calStr = "${kcal}kcal"

        val text = "$protStr / $glucStr / $lipStr / $calStr"
        val span = SpannableString(text)

        val protColor = Color.parseColor("#ec99b5")
        val glucColor = Color.parseColor("#86e8cd")
        val fatColor = Color.parseColor("#f2d678")

        val protStart = 0
        val protEnd = protStr.length

        val glucStart = protEnd + 3
        val glucEnd = glucStart + glucStr.length

        val lipStart = glucEnd + 3
        val lipEnd = lipStart + lipStr.length

        span.setSpan(ForegroundColorSpan(protColor), protStart, protEnd, 0)
        span.setSpan(ForegroundColorSpan(glucColor), glucStart, glucEnd, 0)
        span.setSpan(ForegroundColorSpan(fatColor), lipStart, lipEnd, 0)

        return span
    }

    fun updateData(newItems: List<Any>) {
        items = newItems
        notifyDataSetChanged()
    }
}


data class RecetteAffichee(
    val id: Int,
    val nom: String,
    val proteines: Float,
    val glucides: Float,
    val lipides: Float,
    val calories: Int,
    val quantiteTotale: Float,
    val quantitePortion: Float? = null,
    val imageUri: String? = null,
    val heureManger: String? = null,
    val quantiteTexte: String? = null
)
