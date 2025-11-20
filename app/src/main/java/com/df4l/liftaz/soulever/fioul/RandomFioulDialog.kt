package com.df4l.liftaz.soulever.fioul

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.browse.MediaBrowser
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.FioulType
import com.df4l.liftaz.data.Muscle
import kotlinx.coroutines.launch

object RandomFioulDialog {

    fun showRandomFioulDialog(
        context: Context,
        scope: LifecycleCoroutineScope,
        muscle: Muscle? = null
    ) {
        val database = AppDatabase.getDatabase(context)
        val dao = database.motivationFioulDao()

        scope.launch {
            val allFiouls = dao.getAllFioulsOnce()
            val fiouls = if (muscle != null) allFiouls.filter { it.muscleId == muscle.id } else allFiouls

            if (fiouls.isEmpty()) {
                Toast.makeText(context, "Aucun fioul disponible !", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val randomFioul = fiouls.random()

            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.item_fioul, null)

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

            lateinit var dialog: AlertDialog

            when (randomFioul.type) {

                /* --------------------------------------------------------------------------
                 *                                IMAGE
                 * -------------------------------------------------------------------------- */
                FioulType.IMAGE -> {
                    ivMedia.visibility = View.VISIBLE

                    val uri = Uri.parse(randomFioul.contentUri)
                    var bitmap: Bitmap? = null

                    try {
                        context.contentResolver.openInputStream(uri)?.use {
                            bitmap = BitmapFactory.decodeStream(it)
                        }
                    } catch (e: Exception) {
                        Log.e("RandomFioulDialog", "Erreur image: ${e.message}")
                    }

                    bitmap?.let { bmp ->
                        ivMedia.setImageBitmap(bmp)

                        // 🔥 Affichage ENTIER SANS CROP (on calcule la hauteur correcte dynamiquement)
                        ivMedia.viewTreeObserver.addOnGlobalLayoutListener(
                            object : ViewTreeObserver.OnGlobalLayoutListener {
                                override fun onGlobalLayout() {
                                    ivMedia.viewTreeObserver.removeOnGlobalLayoutListener(this)

                                    val containerWidth = ivMedia.width
                                    val ratio = bmp.height.toFloat() / bmp.width.toFloat()
                                    val finalHeight = (containerWidth * ratio).toInt()

                                    ivMedia.layoutParams.height = finalHeight
                                    ivMedia.scaleType = ImageView.ScaleType.FIT_CENTER
                                    ivMedia.requestLayout()
                                }
                            }
                        )
                    }

                    // 🔒 Compte à rebours 3 sec + verrouillage fermeture
                    builder.setCancelable(false)

                    builder.setPositiveButton("Fermer", null)

                    dialog = builder.setView(view).setTitle("🔥 HOP HOP HOP 🔥").create()

                    dialog.setOnShowListener {
                        val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        button.isEnabled = false

                        object : CountDownTimer(3000, 1000) {
                            override fun onTick(millis: Long) {
                                button.text = "Fermer (${millis / 1000})"
                            }

                            override fun onFinish() {
                                button.text = "Fermer"
                                button.isEnabled = true
                                dialog.setCancelable(true)
                            }
                        }.start()
                    }

                    dialog.show()
                }

                /* --------------------------------------------------------------------------
                 *                               VIDEO
                 * -------------------------------------------------------------------------- */
                FioulType.VIDEO -> {
                    playerView.visibility = View.VISIBLE

                    // 🔥 Fullscreen en modifiant dynamiquement (sans toucher layout)
                    playerView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

                    val player = ExoPlayer.Builder(context).build()
                    playerView.player = player
                    playerView.useController = false

                    val mediaItem = MediaItem.fromUri(Uri.parse(randomFioul.contentUri))
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.playWhenReady = true

                    // 🔒 Non skippable
                    builder.setCancelable(false)
                    builder.setPositiveButton("Fermer", null)

                    dialog = builder.setView(view).setTitle("🔥 HOP HOP HOP 🔥").create()

                    dialog.setOnShowListener {
                        val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        button.isEnabled = false
                        button.text = "Regardez jusqu'au bout…"
                    }

                    // Attendre fin de la vidéo
                    player.addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            if (state == Player.STATE_ENDED) {
                                val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                button.isEnabled = true
                                dialog.setCancelable(true)
                                button.text = "Fermer"
                            }
                        }
                    })

                    // Release player
                    dialog.setOnDismissListener { player.release() }
                    dialog.show()
                }

                else -> {
                    builder.setPositiveButton("Fermer", null)
                    builder.setView(view).setTitle("🔥 HOP HOP HOP 🔥").show()
                }
            }
        }
    }
}
