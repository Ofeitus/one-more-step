package com.example.onemorestep.presentation.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StepsScreen(viewModel: StepsViewModel) {
    val steps = viewModel.stepsCount
    val isGranted = viewModel.isGranted
    val error = viewModel.errorMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("За сегодня", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(24.dp))

        when {
            steps != null -> {
                Text("$steps", style = MaterialTheme.typography.displayLarge)
                Text("шагов", style = MaterialTheme.typography.titleMedium)
            }
            error != null -> {
                Text(error, style = MaterialTheme.typography.titleMedium)
            }
            else -> {
                CircularProgressIndicator()
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val permissions = viewModel.permissions
        val permissionsLauncher = rememberLauncherForActivityResult(viewModel.permissionsLauncher) {
            viewModel.checkPermissionsAndRead()
        }
        Button (
            onClick = {
                permissionsLauncher.launch(permissions)
            }
        ) {
            Text(if (isGranted) "Обновить" else "Дать разрешение", style = MaterialTheme.typography.labelLarge)
        }
    }
}