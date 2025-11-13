package com.df4l.liftaz.soulever.fioul

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.media.browse.MediaBrowser
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.FioulType
import com.df4l.liftaz.data.Muscle
import kotlinx.coroutines.launch

object RandomFioulDialog {

    /**
     * Affiche un fioul de motivation aléatoire dans une boîte de dialogue.
     *
     * @param context le contexte (Fragment ou Activity)
     * @param scope le LifecycleCoroutineScope du Fragment/Activity appelant
     * @param muscle (facultatif) : un Muscle pour filtrer les fiouls associés
     */
    fun showRandomFioulDialog(
        context: Context,
        scope: LifecycleCoroutineScope,
        muscle: Muscle? = null
    ) {
        val database = AppDatabase.getDatabase(context)
        val dao = database.motivationFioulDao()

        scope.launch {
            // 🔍 Récupère tous les fiouls (plus tard tu pourras filtrer par muscle)
            val allFiouls = dao.getAllFioulsOnce()

            // Ici tu pourras faire : allFiouls.filter { it.muscleId == muscle.id }
            val fiouls = if (muscle != null) {
                allFiouls // TODO: filtrer quand tu auras lié les muscles
            } else allFiouls

            if (fiouls.isEmpty()) {
                Toast.makeText(context, "Aucun fioul disponible !", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val randomFioul = fiouls.random()

            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)
            val view: View = inflater.inflate(R.layout.item_fioul, null)

            val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
            val tvText = view.findViewById<TextView>(R.id.tvText)
            val ivMedia = view.findViewById<ImageView>(R.id.ivMedia)
            val playerView = view.findViewById<PlayerView>(R.id.playerView)

            val btnRemoveFioul = view.findViewById<ImageButton>(R.id.btnRemoveFioul)
            btnRemoveFioul.visibility = View.GONE

            tvTitle.text = randomFioul.title
            tvText.text = randomFioul.textContent ?: ""

            ivMedia.visibility = View.GONE
            playerView.visibility = View.GONE

            when (randomFioul.type) {
                FioulType.IMAGE -> {
                    ivMedia.visibility = View.VISIBLE
                    val uri = Uri.parse(randomFioul.contentUri)
                    try {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            val bitmap = BitmapFactory.decodeStream(input)
                            ivMedia.setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        Log.e("RandomFioulDialog", "Erreur chargement image: ${e.message}")
                    }
                }

                FioulType.VIDEO -> {
                    playerView.visibility = View.VISIBLE

                    playerView.useController = false

                    val player = ExoPlayer.Builder(context).build()
                    val mediaItem = MediaItem.fromUri(Uri.parse(randomFioul.contentUri))
                    playerView.player = player
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.playWhenReady = true

                    playerView.setOnClickListener {
                        player.playWhenReady = !player.isPlaying
                    }

                    builder.setOnDismissListener { player.release() }
                }

                else -> {}
            }

            builder.setView(view)
                .setTitle(
                    "🔥 HOP HOP HOP 🔥"
                )
                .setPositiveButton("Fermer", null)
                .show()
        }
    }
}