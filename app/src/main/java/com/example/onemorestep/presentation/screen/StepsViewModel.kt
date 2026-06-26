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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.milliseconds

class StepsViewModel(private val healthConnectManager: HealthConnectManager) : ViewModel() {
    var isGranted by mutableStateOf(false)
        private set
    var stepsCount by mutableStateOf<Long?>(null)
        private set
    var goal by mutableStateOf<Long?>(100000)
        private set
    var percent by mutableStateOf<Double?>(null)
        private set

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND,
        HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY
    )

    init {
        startReadingTask()
    }

    private fun startReadingTask() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                checkPermissionsAndReadSteps()
                delay(5000.milliseconds)
            }
        }
    }

    private fun checkPermissionsAndReadSteps() {
        viewModelScope.launch {
            isGranted = healthConnectManager.hasAllPermissions(permissions)
            if (!isGranted) {
                return@launch
            }

            val todayStart = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC)
            val now = LocalDateTime.now().toInstant(ZoneOffset.UTC)
            stepsCount = healthConnectManager.readSteps(todayStart, now).sumOf { it.count }
            percent = stepsCount!!.toDouble() * 100 / goal!!
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