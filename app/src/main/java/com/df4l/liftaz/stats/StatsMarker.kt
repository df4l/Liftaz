package com.df4l.liftaz.stats

import android.content.Context
import android.widget.TextView
import com.df4l.liftaz.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class StatsMarker(context: Context) : MarkerView(context, R.layout.marker_stats) {

    private val tvValue: TextView = findViewById(R.id.tvMarkerValue)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        tvValue.text = "Valeur : ${e?.y}"
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat() - 15f)
    }
}
