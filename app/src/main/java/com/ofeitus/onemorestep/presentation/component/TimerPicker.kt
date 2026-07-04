package com.ofeitus.onemorestep.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.plovotok.wheelpicker.MultiWheelPicker
import io.github.plovotok.wheelpicker.OverlayConfiguration
import io.github.plovotok.wheelpicker.WheelConfig
import io.github.plovotok.wheelpicker.WheelPickerState

@Stable
class TimerPickerState(
    hours: Int,
    minutes: Int,
    seconds: Int,
) {

    internal val hoursState = WheelPickerState(initiallySelectedItemIndex = hours)
    internal val minutesState = WheelPickerState(initiallySelectedItemIndex = minutes)
    internal val secondsState = WheelPickerState(initiallySelectedItemIndex = seconds)

    val selectedSeconds by derivedStateOf {
        val h = hoursState.selectedItem(Companion.hours.size)
        val m = minutesState.selectedItem(Companion.minutes.size)
        val s = secondsState.selectedItem(Companion.seconds.size)

        h * 3600 + m * 60 + s
    }

    companion object {
        internal val hours = (0..23).map { it.toString() }
        internal val minutes = (0..59).map { it.toString() }
        internal val seconds = (0..59).map { it.toString() }

        fun Saver() = listSaver(
            save = {
                listOf(
                    it.hoursState.selectedItem(hours.size),
                    it.minutesState.selectedItem(minutes.size),
                    it.secondsState.selectedItem(seconds.size)
                )
            },
            restore = {
                TimerPickerState(it[0], it[1], it[2])
            }
        )
    }
}

@Composable
fun rememberTimerPickerState(
    hours: Int = 0,
    minutes: Int = 15,
    seconds: Int = 0,
): TimerPickerState {
    return rememberSaveable(
        saver = TimerPickerState.Saver()
    ) {
        TimerPickerState(hours, minutes, seconds)
    }
}

@Composable
fun TimerPicker(
    state: TimerPickerState,
    modifier: Modifier = Modifier,
) {

    val wheelData = remember {
        listOf(
            TimerPickerState.hours,
            TimerPickerState.minutes,
            TimerPickerState.seconds
        )
    }
    val wheelStates = listOf(state.hoursState, state.minutesState, state.secondsState)

    val units = remember {
        listOf("hours", "min")
    }


    val fontScale = LocalDensity.current.fontScale
    val measurer = rememberTextMeasurer()

    val unitTextStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onBackground
    )

    BoxWithConstraints(
        modifier = Modifier.widthIn(max = 600.dp).then(modifier),
        contentAlignment = Alignment.Center
    ) {
        val textItemWidth = 30.dp * fontScale
        MultiWheelPicker(
            wheelCount = 2,
            overlay = OverlayConfiguration.create(
                scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                focusColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                cornerRadius = 24.dp
            ),
            wheelConfig = { index ->
                WheelConfig(
                    weight = 130.dp.value / maxWidth.value,
                    data = wheelData[index],
                    state = wheelStates[index],
                    contentAlignment = when (index) {
                        0 -> Alignment.Center
                        1 -> Alignment.Center
                        else -> Alignment.Center
                    },
                    contentPaddings = PaddingValues()
                )
            },
            itemContent = { wheelIndex, itemIndex ->
                Row(
                    modifier = Modifier
                        .semantics(mergeDescendants = true) {},
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(textItemWidth),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = wheelData[wheelIndex][itemIndex],
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 23.sp,
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                textMotion = TextMotion.Animated
                            )
                        )
                    }

                }
            },
            itemHeightDp = 34.dp,
            modifier = Modifier.drawWithCache {
                val wheelWidth = size.width / 2

                val unitOffset = units.mapIndexed { index, unit ->
                    val result = measurer.measure(
                        unit,
                        unitTextStyle
                    )

                    val x = when (index) {
                        0 -> wheelWidth * 0.5f + 60.dp.value
                        else -> wheelWidth * 1.5f + 60.dp.value
                    }


                    Offset(
                        x = x,
                        y = size.height / 2 - result.size.height / 2
                    ) to result
                }

                onDrawWithContent {
                    drawContent()

                    unitOffset.forEach {
                        drawText(
                            textLayoutResult = it.second,
                            topLeft = it.first
                        )
                    }
                }
            }
        )
    }
}

@Preview
@Composable
private fun TimerPickerPreview() {
    Box(
        modifier = Modifier.background(Color.White)
    ) {
        TimerPicker(
            state = rememberTimerPickerState()
        )
    }
}