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
import com.example.onemorestep.data.stepsAvgTime
import com.example.onemorestep.data.stepsDurationNanos
import com.example.onemorestep.data.tempoFromNanosToHours
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.milliseconds

class StepsViewModel(private val healthConnectManager: HealthConnectManager) : ViewModel() {
    private val lastTempoSeconds = 5 * 60

    var stepsCount by mutableStateOf<Long?>(null)
        private set
    var goal by mutableStateOf<Long?>(100000)
        private set
    var avgTempo by mutableStateOf<Double?>(null)
        private set
    var lastTempo by mutableStateOf<Double?>(null)
        private set
    var avgTempoEstimatedGoalTime by mutableStateOf<LocalDateTime?>(null)
        private set
    var lastTempoEstimatedGoalTime by mutableStateOf<LocalDateTime?>(null)
        private set

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND,
        HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY
    )

    val chartModelProducer = CartesianChartModelProducer()

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

    fun checkPermissionsAndReadSteps() {
        viewModelScope.launch {
            if (!healthConnectManager.hasAllPermissions(permissions)) {
                return@launch
            }

            val stepsRecords = healthConnectManager.readSteps(
                LocalDate.now().minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC),
                LocalDateTime.now().toInstant(ZoneOffset.UTC)
            )

            val totalSteppingTime = stepsRecords.stream()
                .mapToLong { record -> stepsDurationNanos(record) }
                .sum()
            var lastStepsCount = 0L
            var lastSteppingTime = 0L
            for (i in stepsRecords.size - 1 downTo 0) {
                val seconds = stepsDurationNanos(stepsRecords[i])
                if (lastSteppingTime < lastTempoSeconds) {
                    lastStepsCount += stepsRecords[i].count
                    lastSteppingTime += seconds
                }
            }

            stepsCount = stepsRecords.sumOf { it.count }
            avgTempo = if (totalSteppingTime > 0) stepsCount!!.toDouble() / totalSteppingTime else 0.0
            lastTempo = if (lastSteppingTime > 0) lastStepsCount.toDouble() / lastSteppingTime else 0.0
            val remaining = goal!! - stepsCount!!
            avgTempoEstimatedGoalTime = if (avgTempo!! > 0) LocalDateTime.now().plusNanos((remaining / avgTempo!!).toLong()) else null
            lastTempoEstimatedGoalTime = if (lastTempo!! > 0) LocalDateTime.now().plusNanos((remaining / lastTempo!!).toLong()) else null

            chartModelProducer.runTransaction {
                lineModel {
                    if (stepsRecords.isEmpty()) {
                        series(0)
                        return@lineModel
                    }
                    var xValues = mutableListOf<Number>()
                    var yValues = mutableListOf<Number>()
                    for (i in stepsRecords.indices) {
                        xValues.add(stepsAvgTime(stepsRecords[i]))
                        yValues.add(tempoFromNanosToHours(stepsRecords[i].count.toDouble() / stepsDurationNanos(stepsRecords[i])))
                        if (i < stepsRecords.size - 1 && stepsRecords[i].endTime != stepsRecords[i + 1].startTime) {
                            series(xValues, yValues)
                            xValues = mutableListOf()
                            yValues = mutableListOf()
                        }
                    }
                }
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