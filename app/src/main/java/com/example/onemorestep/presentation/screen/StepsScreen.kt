package com.example.onemorestep.presentation.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController

@Composable
fun StepsScreen(viewModel: StepsViewModel) {
    val steps = viewModel.stepsCount
    val goal = viewModel.goal
    val percent = viewModel.percent

    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) {}

    LaunchedEffect(Unit) {
        permissionLauncher.launch(viewModel.permissions)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp, 0.dp)
    ) {
        Spacer(modifier = Modifier.weight(0.1f))
        Box(
            modifier = Modifier.fillMaxWidth().weight(0.2f),
            contentAlignment = Alignment.Center
        ) {
            when {
                steps != null -> {
                    Text(
                        "$steps",
                        autoSize = TextAutoSize.StepBased(),
                    )
                }
                else -> {
                    CircularProgressIndicator()
                }
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth().weight(0.15f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "/$goal",
                autoSize = TextAutoSize.StepBased(),
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth().weight(0.25f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                String.format(LocalLocale.current.platformLocale, "%.0f%%", percent),
                autoSize = TextAutoSize.StepBased(),
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth().weight(0.20f),
            contentAlignment = Alignment.Center
        ) {}
        Spacer(modifier = Modifier.weight(0.1f))
    }
}