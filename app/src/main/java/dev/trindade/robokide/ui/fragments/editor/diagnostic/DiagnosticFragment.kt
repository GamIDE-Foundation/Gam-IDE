package dev.trindade.robokide.ui.fragments.editor.diagnostic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.material.transition.MaterialSharedAxis

import dev.trindade.robokide.databinding.FragmentDiagnosticBinding
import dev.trindade.robokide.ui.base.RobokFragment

class DiagnosticFragment (private val tansitionAxis : Int = MaterialSharedAxis.X) : RobokFragment(tansitionAxis) {

    private var _binding: FragmentDiagnosticBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDiagnosticBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}