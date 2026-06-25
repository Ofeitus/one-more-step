package com.example.onemorestep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onemorestep.data.HealthConnectManager
import com.example.onemorestep.presentation.screen.StepsScreen
import com.example.onemorestep.presentation.screen.StepsViewModelFactory
import com.example.onemorestep.ui.theme.BWTheme

class StepActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        actionBar?.hide()

        setContent {
            BWTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StepsScreen(viewModel(
                        factory = StepsViewModelFactory(
                            healthConnectManager = HealthConnectManager(this)
                        )
                    ))
                }
            }
        }
    }
}