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
            requestLayout()
            invalidate()
        }

    private val slashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    private val placeholderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF999999.toInt() // gris
        textSize = 72f
        textAlign = Paint.Align.CENTER
    }

    private val slashSize = 50f
    private val spacing = 5f

    // Même hauteur dans tous les cas
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = (slashSize + spacing).toInt() // au moins l'espace du placeholder
        val width = if (couleurs.isEmpty()) {
            minWidth
        } else {
            (couleurs.size * (slashSize + spacing)).toInt()
        }

        val height = slashSize.toInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (couleurs.isEmpty()) {
            // PAS D'ÉLASTIQUE → afficher "/ ?"
            val centerX = width / 2f
            val centerY = (height / 2f) - ((placeholderPaint.descent() + placeholderPaint.ascent()) / 2)
            canvas.drawText("/?", centerX, centerY, placeholderPaint)
            return
        }

        // Sinon → dessiner les slashs
        couleurs.forEachIndexed { index, color ->
            slashPaint.color = color

            val startX = index * (slashSize + spacing)
            val startY = height.toFloat()
            val endX = startX + slashSize
            val endY = 0f

            canvas.drawLine(startX, startY, endX, endY, slashPaint)
        }
    }
}






fun getCouleursForBitmask(elastiques: List<Elastique>, bitmask: Int): List<Int> {
    return elastiques
        .filter { (it.valeurBitmask and bitmask) != 0 }
        .map { it.couleur }
}