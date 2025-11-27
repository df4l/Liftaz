package com.df4l.liftaz.manger.suivi

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.df4l.liftaz.data.Aliment
import com.df4l.liftaz.databinding.DialogQuantiteAlimentBinding

class QuantiteAlimentDialogFragment(
    private val aliment: Aliment,
    private val onConfirm: (Int) -> Unit
) : DialogFragment() {

    private var _binding: DialogQuantiteAlimentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogQuantiteAlimentBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
            .setTitle("Quantité pour ${aliment.nom}")
            .setPositiveButton("Ajouter") { _, _ ->
                val quantiteStr = binding.etQuantite.text.toString()
                val quantite = quantiteStr.toIntOrNull()
                if (quantite != null && quantite > 0) {
                    onConfirm(quantite)
                }
            }
            .setNegativeButton("Annuler") { dialog, _ ->
                dialog.cancel()
            }

        // Pré-remplir la quantité par défaut si elle existe
        aliment.quantiteParDefaut?.let {
            binding.etQuantite.setText(it.toString())
        }

        return builder.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Éviter les fuites de mémoire
    }
}