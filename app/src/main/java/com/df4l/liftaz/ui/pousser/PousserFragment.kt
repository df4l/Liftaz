package com.df4l.liftaz.ui.pousser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.df4l.liftaz.databinding.FragmentPousserBinding

class PousserFragment : Fragment() {

    private var _binding: FragmentPousserBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val pousserViewModel =
            ViewModelProvider(this).get(PousserViewModel::class.java)

        _binding = FragmentPousserBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textPousser
        pousserViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}