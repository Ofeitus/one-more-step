package com.ofeitus.onemorestep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ofeitus.onemorestep.data.HealthConnectManager
import com.ofeitus.onemorestep.data.SettingRepository
import com.ofeitus.onemorestep.presentation.App

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