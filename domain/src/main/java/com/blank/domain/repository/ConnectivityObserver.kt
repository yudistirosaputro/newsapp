package com.blank.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Observes device connectivity state reactively.
 */
interface ConnectivityObserver {
    val isOnline: Flow<Boolean>
}
