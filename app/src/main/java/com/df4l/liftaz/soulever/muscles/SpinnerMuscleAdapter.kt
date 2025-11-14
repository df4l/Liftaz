package com.df4l.liftaz.soulever.muscles

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
    muscles: List<Muscle>,
    private val allowNone: Boolean = false
) : ArrayAdapter<Muscle>(
    context,
    0,
    if (allowNone)
        listOf(Muscle(id = -1, nom = "— Aucun muscle —", nomImage = "")) + muscles
    else
        muscles
) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_spinner_muscle_with_icon, parent, false)

        val muscle = getItem(position)!!

        val imageView = view.findViewById<ImageView>(R.id.imageMuscleIcon)
        val textView = view.findViewById<TextView>(R.id.textMuscleName)

        textView.text = muscle.nom

        if (muscle.id == -1 && allowNone) {
            imageView.visibility = View.GONE
        } else {
            imageView.visibility = View.VISIBLE
            val resId = context.resources.getIdentifier(muscle.nomImage, "drawable", context.packageName)
            if (resId != 0) imageView.setImageResource(resId)
        }

        return view
    }
}
