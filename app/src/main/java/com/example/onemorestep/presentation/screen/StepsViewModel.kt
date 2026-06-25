package com.example.onemorestep.presentation.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.onemorestep.data.HealthConnectManager
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StepsViewModel(private val healthConnectManager: HealthConnectManager) : ViewModel() {
    var stepsCount by mutableStateOf<Long?>(null)
        private set
    var isGranted by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND,
        HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY
    )
    val permissionsLauncher = healthConnectManager.requestPermissionsActivityContract()

    fun checkPermissionsAndRead() {
        viewModelScope.launch {
            try {
                isGranted = healthConnectManager.hasAllPermissions(permissions)

                val todayStart = LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant()
                val now = Instant.now()
                stepsCount = healthConnectManager.readSteps(todayStart, now).sumOf { it.count }
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Ошибка: ${e.localizedMessage ?: "неизвестная ошибка"}"
                stepsCount = null
            }
        }
    }
}

class StepsViewModelFactory(private val healthConnectManager: HealthConnectManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StepsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StepsViewModel(healthConnectManager = healthConnectManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}