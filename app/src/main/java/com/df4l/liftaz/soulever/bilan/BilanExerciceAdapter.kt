package com.df4l.liftaz.soulever.bilan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.data.Elastique
import com.df4l.liftaz.databinding.ItemBilanExerciceBinding

class BilanExerciceAdapter(
    private val exercices: List<ExerciceBilan>,
    private val allElastiques: List<Elastique>       // <-- Nouvel argument
) : RecyclerView.Adapter<BilanExerciceAdapter.ExViewHolder>() {

    class ExViewHolder(val binding: ItemBilanExerciceBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBilanExerciceBinding.inflate(inflater, parent, false)
        return ExViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExViewHolder, position: Int) {
        val ex = exercices[position]
        val b = holder.binding

        b.textExerciceName.text = ex.nom
        b.textExerciceMuscle.text = ex.muscle

        b.recyclerSeriesBilan.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = BilanSerieAdapter(ex.series, ex.poidsDuCorps, allElastiques)
        }
    }

    override fun getItemCount(): Int = exercices.size
}