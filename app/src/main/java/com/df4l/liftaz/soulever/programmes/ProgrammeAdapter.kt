package com.df4l.liftaz.soulever.programmes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Programme
import com.df4l.liftaz.data.ProgrammeAvecSeances

class ProgrammeAdapter(
    private var programmes: List<ProgrammeAvecSeances>,
    private val onActivate: (Programme) -> Unit,
    private val onDelete: (Programme) -> Unit,
    private val onModify: () -> Unit
) : RecyclerView.Adapter<ProgrammeAdapter.ProgrammeViewHolder>() {

    inner class ProgrammeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nom: TextView = view.findViewById(R.id.textNomProgramme)
        val description: TextView = view.findViewById(R.id.textDescriptionProgramme)
        val nbSeances: TextView = view.findViewById(R.id.textNbSeances)
        val badge: TextView = view.findViewById(R.id.textActifBadge)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteProgramme)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgrammeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_programme, parent, false)
        return ProgrammeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProgrammeViewHolder, position: Int) {
        val programmeAvecSeances = programmes[position]
        val programme = programmeAvecSeances.programme

        holder.nom.text = programme.nom
        holder.description.text = programme.description ?: ""
        holder.nbSeances.text = "${programmeAvecSeances.seances.size} séance(s)"
        holder.badge.visibility = if (programme.actif) View.VISIBLE else View.GONE

        // Activer au clic sur la carte
        holder.itemView.setOnClickListener { onActivate(programme) }

        // Suppression via le bouton
        holder.btnDelete.setOnClickListener {
            onDelete(programme)
        }

        holder.itemView.setOnLongClickListener {
            ModifierProgrammeDialog(programme) {
                // Rafraîchir la liste après modification
                onModify()
            }.show((holder.itemView.context as androidx.fragment.app.FragmentActivity).supportFragmentManager, "ModifierProgrammeDialog")
            true
        }

    }

    override fun getItemCount() = programmes.size

    fun updateData(newList: List<ProgrammeAvecSeances>) {
        programmes = newList
        notifyDataSetChanged()
    }
}

