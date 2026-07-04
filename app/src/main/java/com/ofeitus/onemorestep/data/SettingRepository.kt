package com.ofeitus.onemorestep.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class SettingRepository(private val context: Context) {

    companion object {
        val STEPS_TARGET = longPreferencesKey("steps_target")
        val TARGET_TIME = stringPreferencesKey("target_time")
    }

    val stepsTargetFlow: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[STEPS_TARGET]
        }
    val targetTimeFlow: Flow<LocalTime?> = context.dataStore.data
        .map { preferences ->
            val stringTime = preferences[TARGET_TIME]
            if (stringTime == null)
                return@map null
            else
                return@map LocalTime.parse(stringTime)
        }

    suspend fun saveStepsTarget(stepsTarget: Long) {
        context.dataStore.edit { preferences ->
            preferences[STEPS_TARGET] = stepsTarget
        }
    }

    suspend fun saveTargetTime(time: LocalTime) {
        context.dataStore.edit { preferences ->
            preferences[TARGET_TIME] = time.toString()
        }
    }
}