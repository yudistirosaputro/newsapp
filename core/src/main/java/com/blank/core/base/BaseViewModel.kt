package com.blank.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()

    protected fun showLoading() {
        _isLoading.value = true
    }

    protected fun hideLoading() {
        _isLoading.value = false
    }

    protected fun showError(message: String) {
        viewModelScope.launch {
            _error.emit(message)
        }
    }

    protected fun <T> launchWithLoading(
        block: suspend () -> T,
        onSuccess: (T) -> Unit,
        onGeneralError: (Throwable) -> Unit = { showError(it.message ?: "Unknown error") },
    ) {
        viewModelScope.launch {
            try {
                showLoading()
                val result = block()
                onSuccess(result)
            } catch (e: Exception) {
                onGeneralError(e)
            } finally {
                hideLoading()
            }
        }
    }
}
