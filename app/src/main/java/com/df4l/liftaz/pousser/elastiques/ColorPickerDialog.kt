package com.df4l.liftaz.pousser.elastiques

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.GridLayout

class ColorPickerDialog(
    context: Context,
    initialColor: Int,
    private val onColorSelected: (Int) -> Unit
) : AlertDialog(context) {

    init {
        val colors = listOf(
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
            Color.CYAN, Color.MAGENTA, Color.GRAY, Color.BLACK
        )

        val grid = GridLayout(context).apply {
            rowCount = 2
            columnCount = 4
            setPadding(16, 16, 16, 16)
        }

        colors.forEach { color ->
            val view = View(context).apply {
                setBackgroundColor(color)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 120
                    height = 120
                    setMargins(16, 16, 16, 16)
                }
                setOnClickListener {
                    onColorSelected(color)
                    dismiss()
                }
            }
            grid.addView(view)
        }

        setTitle("Choisir une couleur")
        setView(grid)
    }
}
