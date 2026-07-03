package com.example.onemorestep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.onemorestep.data.HealthConnectManager
import com.example.onemorestep.data.SettingRepository
import com.example.onemorestep.presentation.App

class StepActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        actionBar?.hide()

        setContent {
            App(HealthConnectManager(this), SettingRepository(this))
        }
    }
}