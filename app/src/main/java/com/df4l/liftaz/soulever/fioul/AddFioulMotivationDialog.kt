package com.df4l.liftaz.soulever.fioul

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.df4l.liftaz.R
import com.df4l.liftaz.data.FioulType
import com.df4l.liftaz.data.MotivationFioul
import com.df4l.liftaz.data.Muscle
import com.df4l.liftaz.soulever.muscles.musclesListe.MuscleListAdapter
import java.time.LocalDate
import java.util.Date

class AddFioulMotivationDialog(
    private val onFioulAdded: (MotivationFioul) -> Unit
) : DialogFragment() {

    private var selectedType: FioulType? = null
    private var selectedUri: Uri? = null
    private var textContent: String = ""
    private var title: String = ""

    private lateinit var btnImage: Button
    private lateinit var btnVideo: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_fioul, null)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etText = view.findViewById<EditText>(R.id.etText)
        btnImage = view.findViewById(R.id.btnImage)
        btnVideo = view.findViewById(R.id.btnVideo)

        // Boutons image/vidéo
        btnImage.setOnClickListener {
            selectedType = FioulType.IMAGE
            selectMedia("image/*")
        }

        btnVideo.setOnClickListener {
            selectedType = FioulType.VIDEO
            selectMedia("video/*")
        }

        builder.setView(view)
            .setTitle("Ajouter du FIOUL de motivation")
            .setPositiveButton("Enregistrer") { _, _ ->
                title = etTitle.text.toString()
                textContent = etText.text.toString()

                val type = selectedType ?: FioulType.TEXTE
                if (title.isNotEmpty()) {
                    val fuel = MotivationFioul(
                        title = title,
                        type = type,
                        contentUri = selectedUri?.toString(),
                        textContent = textContent.ifBlank { null },
                        dateAdded = Date()
                    )
                    onFioulAdded(fuel)
                }
            }
            .setNegativeButton("Annuler") { dialog, _ -> dialog.dismiss() }

        return builder.create()
    }

    private fun selectMedia(type: String) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            this.type = type
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        mediaLauncher.launch(intent)
    }

    private val mediaLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedUri = uri

                    val contentResolver = requireContext().contentResolver
                    val takeFlags = result.data?.flags?.and(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    ) ?: 0
                    try {
                        contentResolver.takePersistableUriPermission(uri, takeFlags)
                    } catch (e: SecurityException) {
                        Log.w("AddFioulDialog", "Permission persistante échouée : ${e.message}")
                    }

                    // ✅ Confirmation visuelle
                    updateButtonColors()
                }
            }
        }

    private fun updateButtonColors() {
        val green = ContextCompat.getColor(requireContext(), R.color.teal_700)
        val gray = ContextCompat.getColor(requireContext(), android.R.color.darker_gray)

        when (selectedType) {
            FioulType.IMAGE -> {
                btnImage.setBackgroundColor(green)
                btnVideo.setBackgroundColor(gray)
            }
            FioulType.VIDEO -> {
                btnVideo.setBackgroundColor(green)
                btnImage.setBackgroundColor(gray)
            }
            else -> {
                btnImage.setBackgroundColor(gray)
                btnVideo.setBackgroundColor(gray)
            }
        }
    }
}

