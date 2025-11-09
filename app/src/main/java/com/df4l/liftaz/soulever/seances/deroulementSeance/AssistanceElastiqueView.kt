package com.df4l.liftaz.soulever.seances.deroulementSeance

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.df4l.liftaz.data.Elastique

class AssistanceElastiqueView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var couleurs: List<Int> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val bandPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = 0xFF444444.toInt() // Bordure gris foncé
    }
    private val emptyBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFFEFEFEF.toInt() // Fond gris clair quand aucune couleur
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (couleurs.isEmpty()) {
            // Aucun élastique -> fond gris
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), emptyBackgroundPaint)
        } else {
            val bandWidth = width.toFloat() / couleurs.size

            couleurs.forEachIndexed { index, color ->
                bandPaint.color = color
                val left = index * bandWidth
                val right = left + bandWidth
                canvas.drawRect(left, 0f, right, height.toFloat(), bandPaint)
            }
        }

        // Bordure
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), borderPaint)
    }
}



fun getCouleursForBitmask(elastiques: List<Elastique>, bitmask: Int): List<Int> {
    return elastiques
        .filter { (it.valeurBitmask and bitmask) != 0 }
        .map { it.couleur }
}