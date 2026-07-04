package com.ofeitus.onemorestep.presentation.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ofeitus.onemorestep.presentation.viewmodel.StepsViewModel
import com.ofeitus.onemorestep.ui.theme.DarkGray
import com.ofeitus.onemorestep.presentation.component.TimerPicker
import com.ofeitus.onemorestep.presentation.component.rememberTimerPickerState
import java.time.LocalTime

@Composable
fun TargetTimeDialog(viewModel: StepsViewModel) {
    if (viewModel.showTargetTimeDialog) {
        val time = viewModel.targetTime.collectAsStateWithLifecycle().value
        val timerPickerState = if (time == null)
            rememberTimerPickerState()
        else
            rememberTimerPickerState(
                hours = time.hour,
                minutes = time.minute
            )
        val selectedSeconds = timerPickerState.selectedSeconds

        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                background = DarkGray
            )
        ) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissTargetTimeDialog() },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TimerPicker(timerPickerState)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateTargetTime(LocalTime.ofSecondOfDay(selectedSeconds.toLong()))
                            viewModel.dismissTargetTimeDialog()
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.dismissTargetTimeDialog() }
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            )
        }
    }
}