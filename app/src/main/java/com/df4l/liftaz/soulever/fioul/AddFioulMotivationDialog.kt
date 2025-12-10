package com.df4l.liftaz.soulever.fioul

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
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
import androidx.compose.ui.graphics.vector.path
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.android.identity.util.UUID
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.FioulType
import com.df4l.liftaz.data.MotivationFioul
import com.df4l.liftaz.data.Muscle
import com.df4l.liftaz.soulever.muscles.MuscleListAdapter
import com.df4l.liftaz.soulever.muscles.SpinnerMuscleAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.Date
import kotlin.io.path.copyTo
import kotlin.io.path.exists

class AddFioulMotivationDialog(
    private val onFioulAdded: (MotivationFioul) -> Unit
) : DialogFragment() {

    private var selectedType: FioulType? = null
    // L'URI du fichier copié en interne
    private var internalFileUri: Uri? = null
    private var textContent: String = ""
    private var title: String = ""

    private lateinit var btnImage: Button
    private lateinit var btnVideo: Button
    private lateinit var spinnerMuscle: Spinner

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_add_fioul, null)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etText = view.findViewById<EditText>(R.id.etText)
        btnImage = view.findViewById(R.id.btnImage)
        btnVideo = view.findViewById(R.id.btnVideo)
        spinnerMuscle = view.findViewById(R.id.spinnerMuscle)

        loadMusclesIntoSpinner()

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
                    val selectedMuscle = spinnerMuscle.selectedItem as Muscle
                    val muscleId = if (selectedMuscle.id == -1) null else selectedMuscle.id

                    val fuel = MotivationFioul(
                        title = title,
                        type = type,
                        // ✨ Utiliser l'URI du fichier interne
                        contentUri = internalFileUri?.toString(),
                        textContent = textContent.ifBlank { null },
                        dateAdded = Date(),
                        muscleId = muscleId
                    )
                    onFioulAdded(fuel)
                }
            }
            .setNegativeButton("Annuler") { dialog, _ ->
                // ✨ Si l'utilisateur annule, supprimer le fichier qui a pu être copié
                internalFileUri?.let {
                    lifecycleScope.launch {
                        deleteFileFromInternalStorage(it)
                    }
                }
                dialog.dismiss()
            }

        return builder.create()
    }

    private fun loadMusclesIntoSpinner() {
        val database = AppDatabase.getDatabase(requireContext())
        val dao = database.muscleDao()

        lifecycleScope.launch {
            val muscles = dao.getAllMuscles()
            val adapter = SpinnerMuscleAdapter(requireContext(), muscles, allowNone = true)
            spinnerMuscle.adapter = adapter
        }
    }

    private fun selectMedia(type: String) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            this.type = type
        }
        mediaLauncher.launch(intent)
    }

    private val mediaLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    // ✨ Lancer la copie du fichier dans le stockage interne
                    lifecycleScope.launch {
                        internalFileUri = copyFileToInternalStorage(uri, requireContext())
                        updateButtonColors()
                    }
                }
            }
        }

    // ✨ NOUVELLE FONCTION : Copie le fichier vers le stockage interne
    private suspend fun copyFileToInternalStorage(sourceUri: Uri, context: Context): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                // Crée un nom de fichier unique pour éviter les conflits
                val fileName = "fioul_${UUID.randomUUID()}"
                val destinationFile = File(context.filesDir, fileName)

                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    FileOutputStream(destinationFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Log.d("AddFioulDialog", "Fichier copié vers : ${destinationFile.absolutePath}")
                Uri.fromFile(destinationFile) // Retourne l'URI du nouveau fichier
            } catch (e: Exception) {
                Log.e("AddFioulDialog", "Échec de la copie du fichier", e)
                null
            }
        }
    }

    // ✨ NOUVELLE FONCTION : Supprime un fichier à partir de son URI
    private suspend fun deleteFileFromInternalStorage(fileUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(fileUri.path!!)
                if (file.exists()) {
                    if (file.delete()) {
                        Log.d("AddFioulDialog", "Fichier temporaire supprimé : ${file.path}")
                    } else {
                        Log.w("AddFioulDialog", "Échec de la suppression du fichier temporaire : ${file.path}")
                    }
                }
            } catch (e: Exception) {
                Log.e("AddFioulDialog", "Erreur lors de la suppression du fichier temporaire", e)
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

