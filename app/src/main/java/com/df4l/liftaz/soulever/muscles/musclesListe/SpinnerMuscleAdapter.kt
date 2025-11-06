package com.df4l.liftaz.soulever.muscles.musclesListe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Muscle

class SpinnerMuscleAdapter(
    context: Context,
    private val muscles: List<Muscle>
) : ArrayAdapter<Muscle>(context, 0, muscles) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_spinner_muscle_with_icon, parent, false)

        val muscle = muscles[position]
        val imageView = view.findViewById<ImageView>(R.id.imageMuscleIcon)
        val textView = view.findViewById<TextView>(R.id.textMuscleName)

        textView.text = muscle.nom

        // 🔹 Charger l'image depuis nomImage
        val resId = context.resources.getIdentifier(muscle.nomImage, "drawable", context.packageName)
        if (resId != 0) {
            imageView.setImageResource(resId)
        } else {
            //imageView.setImageResource(R.drawable.ic_default_muscle)
        }

        return view
    }
}
