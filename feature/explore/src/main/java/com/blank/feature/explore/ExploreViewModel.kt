package com.blank.feature.explore

import androidx.lifecycle.viewModelScope
import com.blank.core.base.BaseViewModel
import com.blank.domain.repository.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
) : BaseViewModel() {

    val isOffline: StateFlow<Boolean> = connectivityObserver.isOnline
        .map { online -> !online }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}
