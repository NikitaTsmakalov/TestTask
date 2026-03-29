package com.example.testtask.presentation.util

import kotlinx.coroutines.flow.Flow

interface NetworkStatusProvider {
    val isOnline: Flow<Boolean>
}

