package com.example.onemorestep.presentation.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.onemorestep.presentation.viewmodel.StepsViewModel
import com.example.onemorestep.ui.theme.DarkGray
import io.github.plovotok.wheelpicker.OverlayConfiguration
import io.github.plovotok.wheelpicker.WheelPicker
import io.github.plovotok.wheelpicker.rememberWheelPickerState

@Composable
fun StepsTargetDialog(viewModel: StepsViewModel) {
    if (viewModel.showStepsTargetDialog) {
        val index = viewModel.stepsTargetList.indexOf(viewModel.stepsTarget.collectAsStateWithLifecycle().value)
        val pickerState = rememberWheelPickerState(
            initialIndex = if (index != -1) index else 0,
            infinite = false
        )
        val selectedIndex = pickerState.selectedItemIndex(viewModel.stepsTargetList.size)

        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                background = DarkGray
            )
        ) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissTargetDialog() },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        WheelPicker(
                            data = viewModel.stepsTargetList,
                            state = pickerState,
                            overlay = OverlayConfiguration.create(
                                scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                focusColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                cornerRadius = 24.dp
                            ),
                            itemHeightDp = 34.dp,
                            itemContent = {
                                Text(
                                    text = viewModel.stepsTargetList[it].toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateStepsTarget(viewModel.stepsTargetList[selectedIndex])
                            viewModel.dismissTargetDialog()
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.dismissTargetDialog() }
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            )
        }
    }
}