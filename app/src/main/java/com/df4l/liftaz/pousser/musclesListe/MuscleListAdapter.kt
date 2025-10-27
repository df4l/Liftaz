package com.df4l.liftaz.pousser.musclesListe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Muscle

class MuscleListAdapter(
    context: Context,
    private val muscles: List<Muscle>
) : ArrayAdapter<Muscle>(context, 0, muscles) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_muscle_with_icon, parent, false)

        val muscle = muscles[position]
        val textView = view.findViewById<TextView>(R.id.textMuscleName)
        val imageView = view.findViewById<ImageView>(R.id.imageMuscleIcon)

        textView.text = muscle.nom

        // ✅ On utilise directement le champ nomImage de la BDD
        val resId = context.resources.getIdentifier(muscle.nomImage, "drawable", context.packageName)
        if (resId != 0) {
            imageView.setImageResource(resId)
        } else {
            //imageView.setImageResource(R.drawable.ic_default_muscle)
        }

        return view
    }
}
