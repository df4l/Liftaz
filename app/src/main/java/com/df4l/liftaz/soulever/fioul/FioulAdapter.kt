package com.df4l.liftaz.soulever.fioul

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.FioulType
import com.df4l.liftaz.data.MotivationFioul
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class FioulAdapter(
    private var fiouls: List<MotivationFioul>,
    private val onDeleteFioul: (MotivationFioul) -> Unit
) : RecyclerView.Adapter<FioulAdapter.FioulViewHolder>() {

    private val players = mutableListOf<ExoPlayer>()

    inner class FioulViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvText: TextView = view.findViewById(R.id.tvText)
        val ivMedia: ImageView = view.findViewById(R.id.ivMedia)
        val playerView: PlayerView = view.findViewById(R.id.playerView)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemoveFioul)
        var player: ExoPlayer? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FioulViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fioul, parent, false)
        return FioulViewHolder(view)
    }

    override fun getItemCount(): Int = fiouls.size

    override fun onBindViewHolder(holder: FioulViewHolder, position: Int) {
        val fuel = fiouls[position]
        holder.tvTitle.text = fuel.title
        holder.tvText.text = fuel.textContent ?: ""

        // 🔴 Bouton supprimer
        holder.btnRemove.setOnClickListener {
            onDeleteFioul(fuel)
        }

        // Nettoyage avant réutilisation
        holder.ivMedia.visibility = View.GONE
        holder.playerView.visibility = View.GONE
        holder.player?.release()
        holder.player = null

        when (fuel.type) {
            FioulType.IMAGE -> {
                if (fuel.contentUri != null) {
                    holder.ivMedia.visibility = View.VISIBLE
                    val uri = Uri.parse(fuel.contentUri)
                    try {
                        holder.itemView.context.contentResolver.openInputStream(uri)?.use { input ->
                            val bitmap = BitmapFactory.decodeStream(input)
                            holder.ivMedia.setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        Log.e("FioulAdapter", "Erreur lecture image: ${e.message}")
                    }
                }
            }

            FioulType.VIDEO -> {
                val context = holder.itemView.context
                holder.playerView.visibility = View.VISIBLE

                val player = ExoPlayer.Builder(context).build()
                players.add(player)
                holder.player = player
                holder.playerView.player = player

                try {
                    val mediaItem = MediaItem.fromUri(Uri.parse(fuel.contentUri))
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.playWhenReady = false
                } catch (e: Exception) {
                    Log.e("FioulAdapter", "Erreur lecture vidéo: ${e.message}")
                }

                holder.playerView.setOnClickListener {
                    player.playWhenReady = !player.isPlaying
                }
            }

            else -> {
                holder.ivMedia.visibility = View.GONE
                holder.playerView.visibility = View.GONE
            }
        }
    }

    fun releaseAllPlayers() {
        for (p in players) {
            try { p.release() } catch (_: Exception) {}
        }
        players.clear()
    }

    override fun onViewRecycled(holder: FioulViewHolder) {
        super.onViewRecycled(holder)
        holder.player?.release()
        holder.player = null
    }

    fun updateData(newFiouls: List<MotivationFioul>) {
        fiouls = newFiouls
        notifyDataSetChanged()
    }
}
