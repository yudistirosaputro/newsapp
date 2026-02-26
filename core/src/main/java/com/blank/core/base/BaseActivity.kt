package com.blank.core.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding>(
    private val inflate: (LayoutInflater) -> VB,
) : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding: VB get() = requireNotNull(_binding) {
        "ViewBinding is not initialized. Access binding after onCreate."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflate(layoutInflater)
        setContentView(binding.root)
        onViewReady(savedInstanceState)
    }

    abstract fun onViewReady(savedInstanceState: Bundle?)

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
