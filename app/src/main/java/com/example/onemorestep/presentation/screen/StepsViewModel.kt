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
    private val currentTempoSeconds = 5 * 60

    var stepsCount by mutableStateOf<Long?>(null)
        private set
    var goal by mutableStateOf<Long?>(100000)
        private set
    var targetTempo by mutableStateOf<Double?>(6500.0 / 3600000000000)
        private set
    var currentTempo by mutableStateOf<Double?>(null)
        private set
    var avgTempo by mutableStateOf<Double?>(null)
        private set
    var targetTempoEstimatedGoalTime by mutableStateOf<LocalDateTime?>(null)
        private set
    var currentTempoEstimatedGoalTime by mutableStateOf<LocalDateTime?>(null)
        private set
    var avgTempoEstimatedGoalTime by mutableStateOf<LocalDateTime?>(null)
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
            var currentStepsCount = 0L
            var currentSteppingTime = 0L
            for (i in stepsRecords.size - 1 downTo 0) {
                val seconds = stepsDurationNanos(stepsRecords[i])
                if (currentSteppingTime < currentTempoSeconds) {
                    currentStepsCount += stepsRecords[i].count
                    currentSteppingTime += seconds
                }
            }

            stepsCount = stepsRecords.sumOf { it.count }

            currentTempo = if (currentSteppingTime > 0) currentStepsCount.toDouble() / currentSteppingTime else 0.0
            avgTempo = if (totalSteppingTime > 0) stepsCount!!.toDouble() / totalSteppingTime else 0.0

            val remaining = goal!! - stepsCount!!
            targetTempoEstimatedGoalTime = if (targetTempo!! > 0) LocalDateTime.now().plusNanos((remaining / targetTempo!!).toLong()) else null
            currentTempoEstimatedGoalTime = if (currentTempo!! > 0) LocalDateTime.now().plusNanos((remaining / currentTempo!!).toLong()) else null
            avgTempoEstimatedGoalTime = if (avgTempo!! > 0) LocalDateTime.now().plusNanos((remaining / avgTempo!!).toLong()) else null

            chartModelProducer.runTransaction {
                lineModel {
                    if (stepsRecords.isEmpty()) {
                        series(0)
                        return@lineModel
                    }

                    val targetTempoX = mutableListOf<Number>()
                    val targetTempoY = mutableListOf<Number>()

                    val currentTempoX = mutableListOf<Number>()
                    val currentTempoY = mutableListOf<Number>()

                    var stepsCountForAvg = 0L
                    var timeForAvg = 0L
                    val avgTempoX = mutableListOf<Number>()
                    val avgTempoY = mutableListOf<Number>()

                    for (i in stepsRecords.indices) {
                        val record = stepsRecords[i]
                        val time = stepsAvgTime(record)

                        targetTempoX.add(time)
                        targetTempoY.add(tempoFromNanosToHours(targetTempo!!))

                        currentTempoX.add(time)
                        currentTempoY.add(tempoFromNanosToHours(record.count.toDouble() / stepsDurationNanos(record)))

                        stepsCountForAvg += record.count
                        timeForAvg += stepsDurationNanos(record)
                        avgTempoX.add(time)
                        avgTempoY.add(tempoFromNanosToHours(stepsCountForAvg.toDouble() / timeForAvg))
                        /*if (i < stepsRecords.size - 1 && record.endTime != stepsRecords[i + 1].startTime) {
                            xValues = mutableListOf()
                            yValues = mutableListOf()
                        }*/
                    }
                    series(targetTempoX, targetTempoY)
                    series(currentTempoX, currentTempoY)
                    series(avgTempoX, avgTempoY)
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