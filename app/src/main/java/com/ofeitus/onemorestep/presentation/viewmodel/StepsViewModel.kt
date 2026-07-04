package com.ofeitus.onemorestep.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ofeitus.onemorestep.data.HealthConnectManager
import com.ofeitus.onemorestep.data.SettingRepository
import com.ofeitus.onemorestep.data.localEndTime
import com.ofeitus.onemorestep.data.stepsDurationNanos
import com.ofeitus.onemorestep.data.tempoFromNanosToHours
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.milliseconds

class StepsViewModel(
    private val healthConnectManager: HealthConnectManager,
    private val settingRepository: SettingRepository
) : ViewModel() {
    private val currentTempoSeconds = 5 * 60

    var currentDate by mutableStateOf<LocalDate>(LocalDate.now())
        private set

    private val _stepsRecords = MutableStateFlow<List<StepsRecord>>(emptyList())
    val stepsRecords: StateFlow<List<StepsRecord>> = _stepsRecords.asStateFlow()

    var stepsCount by mutableStateOf<Long?>(null)
        private set

    val stepsTarget: StateFlow<Long?> = settingRepository.stepsTargetFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )
    val stepsTargetList = buildList {
        repeat(100) { add(1000L * (it + 1)) }
    }
    var showStepsTargetDialog by mutableStateOf(false)
        private set

    var targetTime: StateFlow<LocalTime?> = settingRepository.targetTimeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )
    var showTargetTimeDialog by mutableStateOf(false)
        private set

    var currentTempo by mutableStateOf<Double?>(null)
        private set
    var avgTempoEstimatedGoalTime by mutableStateOf<LocalDateTime?>(null)
        private set

    var targetTempo by mutableStateOf<Double?>(null)
        private set

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND,
        HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY
    )

    val chartModelProducer = CartesianChartModelProducer()

    init {
        startTask()
    }

    private fun startTask() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                calculateData()
                delay(5000.milliseconds)
            }
        }
    }

    fun calculateData() {
        viewModelScope.launch {
            if (!healthConnectManager.hasAllPermissions(permissions)) {
                return@launch
            }

            currentDate = LocalDate.now()
            val atStartOfDay = currentDate.atStartOfDay()
            val now = LocalDateTime.now()

            _stepsRecords.value = healthConnectManager.readSteps(
                atStartOfDay.toInstant(ZoneOffset.UTC),
                now.toInstant(ZoneOffset.UTC)
            )

            var currentStepsCount = 0L
            var currentSteppingTime = 0L
            for (i in _stepsRecords.value.size - 1 downTo 0) {
                val seconds = stepsDurationNanos(stepsRecords.value[i])
                if (currentSteppingTime < currentTempoSeconds) {
                    currentStepsCount += stepsRecords.value[i].count
                    currentSteppingTime += seconds
                }
            }

            stepsCount = stepsRecords.value.sumOf { it.count }
            var remainingSteps = stepsCount?.let { stepsTarget.value?.minus(it) }
            remainingSteps?.let { if (it < 0) remainingSteps = 0 }

            val passedTime = Duration.between(atStartOfDay, now).toNanos()
            val remainingTime = if (targetTime.value == null)
                null
            else
                Duration.between(now, currentDate.atTime(targetTime.value)).toNanos()

            currentTempo = if (currentSteppingTime > 0) currentStepsCount.toDouble() / currentSteppingTime else null
            targetTempo = if (remainingTime == null || remainingTime <= 0)
                null
            else
                remainingSteps?.toDouble()?.div(remainingTime)
            targetTempo?.let { if (it < 0) targetTempo = 0.0 }
            val avgTempo = if (passedTime > 0) stepsCount?.toDouble()?.div(passedTime) else null

            avgTempoEstimatedGoalTime = if (remainingSteps == null || remainingSteps == 0L || avgTempo == null)
                null
            else
                now.plusNanos((remainingSteps / avgTempo).toLong())

            chartModelProducer.runTransaction {
                lineModel {
                    val stack = ArrayDeque(stepsRecords.value)
                    var stepsCount = 0L
                    val x = mutableListOf<Number>()
                    val targetTempoY = mutableListOf<Number>()
                    val avgTempoY = mutableListOf<Number>()

                    val timeTicks = mutableListOf<LocalDateTime>()
                    var time = atStartOfDay
                    while (time.isBefore(now)) {
                        timeTicks.add(time)
                        time = time.plusMinutes(5)
                    }
                    timeTicks.add(now)

                    timeTicks.forEach { time ->
                        while (!stack.isEmpty() && localEndTime(stack.first()).isBefore(time)) {
                            stepsCount += stack.removeFirst().count
                        }
                        val passedTime = Duration.between(atStartOfDay, time).toNanos()

                        x.add(time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())

                        targetTempoY.add(tempoFromNanosToHours(targetTempo ?: 0.0))
                        avgTempoY.add(tempoFromNanosToHours(if (passedTime > 0) stepsCount.toDouble() / passedTime else 0.0))
                    }

                    series(x, targetTempoY)
                    series(x, avgTempoY)
                }
            }
        }
    }

    fun openTargetDialog() {
        showStepsTargetDialog = true
    }

    fun dismissTargetDialog() {
        showStepsTargetDialog = false
    }

    fun openTargetTimeDialog() {
        showTargetTimeDialog = true
    }

    fun dismissTargetTimeDialog() {
        showTargetTimeDialog = false
    }

    fun updateStepsTarget(newStepsTarget: Long) {
        viewModelScope.launch {
            settingRepository.saveStepsTarget(newStepsTarget)
            calculateData()
        }
    }

    fun updateTargetTime(newTime: LocalTime) {
        viewModelScope.launch {
            settingRepository.saveTargetTime(newTime)
            calculateData()
        }
    }
}

class StepsViewModelFactory(private val healthConnectManager: HealthConnectManager, private val settingRepository: SettingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StepsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StepsViewModel(healthConnectManager, settingRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}