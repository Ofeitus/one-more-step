package com.ofeitus.onemorestep.presentation.dialog

import androidx.compose.foundation.isSystemInDarkTheme
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
import com.ofeitus.onemorestep.presentation.viewmodel.StepsViewModel
import com.ofeitus.onemorestep.ui.theme.DarkGray
import com.ofeitus.onemorestep.ui.theme.LightGray
import io.github.plovotok.wheelpicker.OverlayConfiguration
import io.github.plovotok.wheelpicker.WheelPicker
import io.github.plovotok.wheelpicker.rememberWheelPickerState

@Composable
fun StepsGoalDialog(viewModel: StepsViewModel) {
    if (viewModel.showStepsGoalDialog) {
        val index = viewModel.stepsGoalList.indexOf(viewModel.stepsGoal.collectAsStateWithLifecycle().value)
        val pickerState = rememberWheelPickerState(
            initialIndex = if (index != -1) index else 0,
            infinite = false
        )
        val selectedIndex = pickerState.selectedItemIndex(viewModel.stepsGoalList.size)

        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                background = if (isSystemInDarkTheme()) DarkGray else LightGray
            )
        ) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissStepsGoalDialog() },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        WheelPicker(
                            data = viewModel.stepsGoalList,
                            state = pickerState,
                            overlay = OverlayConfiguration.create(
                                scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                focusColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                cornerRadius = 24.dp
                            ),
                            itemHeightDp = 34.dp,
                            itemContent = {
                                Text(
                                    text = viewModel.stepsGoalList[it].toString(),
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
                            viewModel.updateStepsGoal(viewModel.stepsGoalList[selectedIndex])
                            viewModel.dismissStepsGoalDialog()
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.dismissStepsGoalDialog() }
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            )
        }
    }
}