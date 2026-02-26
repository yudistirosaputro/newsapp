package com.blank.core.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> VB,
) : Fragment() {

    private var _binding: VB? = null
    protected val binding: VB get() = requireNotNull(_binding) {
        "ViewBinding is not initialized. Access binding between onCreateView and onDestroyView."
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewReady(savedInstanceState)
        observeState()
    }

    abstract fun onViewReady(savedInstanceState: Bundle?)

    open fun observeState() {}

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
