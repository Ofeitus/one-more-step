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
import com.ofeitus.onemorestep.data.paceFromNanosToHours
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
    private val currentPaceSeconds = 5 * 60

    var currentDate by mutableStateOf<LocalDate>(LocalDate.now())
        private set

    private val _stepsRecords = MutableStateFlow<List<StepsRecord>>(emptyList())
    val stepsRecords: StateFlow<List<StepsRecord>> = _stepsRecords.asStateFlow()

    var stepsCount by mutableStateOf<Long?>(null)
        private set

    val stepsGoal: StateFlow<Long?> = settingRepository.stepsGoalFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )
    val stepsGoalList = buildList {
        repeat(100) { add(1000L * (it + 1)) }
    }
    var showStepsGoalDialog by mutableStateOf(false)
        private set

    var timeGoal: StateFlow<LocalTime?> = settingRepository.timeGoalFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )
    var showTimeGoalDialog by mutableStateOf(false)
        private set

    var currentPace by mutableStateOf<Double?>(null)
        private set
    var avgPaceEstimatedGoalTime by mutableStateOf<LocalDateTime?>(null)
        private set

    var targetPace by mutableStateOf<Double?>(null)
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
                if (currentSteppingTime < currentPaceSeconds) {
                    currentStepsCount += stepsRecords.value[i].count
                    currentSteppingTime += seconds
                }
            }

            stepsCount = stepsRecords.value.sumOf { it.count }
            var remainingSteps = stepsCount?.let { stepsGoal.value?.minus(it) }
            remainingSteps?.let { if (it < 0) remainingSteps = 0 }

            val passedTime = Duration.between(atStartOfDay, now).toNanos()
            val remainingTime = if (timeGoal.value == null)
                null
            else
                Duration.between(now, currentDate.atTime(timeGoal.value)).toNanos()

            currentPace = if (currentSteppingTime > 0) currentStepsCount.toDouble() / currentSteppingTime else null
            targetPace = if (remainingTime == null || remainingTime <= 0)
                null
            else
                remainingSteps?.toDouble()?.div(remainingTime)
            targetPace?.let { if (it < 0) targetPace = 0.0 }
            val avgPace = if (passedTime > 0) stepsCount?.toDouble()?.div(passedTime) else null

            avgPaceEstimatedGoalTime = if (remainingSteps == null || remainingSteps == 0L || avgPace == null)
                null
            else
                now.plusNanos((remainingSteps / avgPace).toLong())

            chartModelProducer.runTransaction {
                lineModel {
                    val stack = ArrayDeque(stepsRecords.value)
                    var stepsCount = 0L
                    val x = mutableListOf<Number>()
                    val targetPaceY = mutableListOf<Number>()
                    val avgPaceY = mutableListOf<Number>()

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

                        targetPaceY.add(paceFromNanosToHours(targetPace ?: 0.0))
                        avgPaceY.add(paceFromNanosToHours(if (passedTime > 0) stepsCount.toDouble() / passedTime else 0.0))
                    }

                    series(x, targetPaceY)
                    series(x, avgPaceY)
                }
            }
        }
    }

    fun openStepsGoalDialog() {
        showStepsGoalDialog = true
    }

    fun dismissStepsGoalDialog() {
        showStepsGoalDialog = false
    }

    fun openTimeGoalDialog() {
        showTimeGoalDialog = true
    }

    fun dismissTimeGoalDialog() {
        showTimeGoalDialog = false
    }

    fun updateStepsGoal(newStepsGoal: Long) {
        viewModelScope.launch {
            settingRepository.saveStepsGoal(newStepsGoal)
            calculateData()
        }
    }

    fun updateTimeGoal(newTimeGoal: LocalTime) {
        viewModelScope.launch {
            settingRepository.saveTimeGoal(newTimeGoal)
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