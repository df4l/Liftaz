package com.df4l.liftaz.manger.diete

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.df4l.liftaz.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CreationDieteFragment : Fragment() {

    private lateinit var topSheetBehavior: TopSheetBehavior<MaterialCardView>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_creationdiete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val topSheet: MaterialCardView = view.findViewById(R.id.topSheetLayout)
        topSheetBehavior = TopSheetBehavior.from(topSheet)

        // Rendre le top sheet complètement invisible au démarrage
        topSheetBehavior.setHideable(true)
        topSheetBehavior.setPeekHeight(0)
        topSheetBehavior.state = TopSheetBehavior.STATE_EXPANDED

        // Bouton pour ouvrir/fermer
        val btnShowTopSheet = view.findViewById<FloatingActionButton>(R.id.btnShowTopSheet)
        btnShowTopSheet.setOnClickListener {
            toggleTopSheet()
        }

        // FAB secondaire
        val fabToggle = view.findViewById<FloatingActionButton>(R.id.fabToggleTopSheet)
        fabToggle.setOnClickListener { toggleTopSheet() }

        // Récupérer layoutObjectif pour appliquer la marge
        val layoutObjectif = topSheet.findViewById<LinearLayout>(R.id.layoutObjectif)

        ViewCompat.setOnApplyWindowInsetsListener(topSheet) { _, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            layoutObjectif.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBarsInsets.top
            }
            insets
        }

    }

    private fun toggleTopSheet() {
        topSheetBehavior.state = if (topSheetBehavior.state == TopSheetBehavior.STATE_EXPANDED) {
            TopSheetBehavior.STATE_COLLAPSED // au lieu de STATE_HIDDEN
        } else {
            TopSheetBehavior.STATE_EXPANDED
        }
    }
}

