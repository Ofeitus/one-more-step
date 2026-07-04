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
        val STEPS_GOAL = longPreferencesKey("steps_goal")
        val TIME_GOAL = stringPreferencesKey("time_goal")
    }

    val stepsGoalFlow: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[STEPS_GOAL]
        }
    val timeGoalFlow: Flow<LocalTime?> = context.dataStore.data
        .map { preferences ->
            val stringTime = preferences[TIME_GOAL]
            if (stringTime == null)
                return@map null
            else
                return@map LocalTime.parse(stringTime)
        }

    suspend fun saveStepsGoal(stepsGoal: Long) {
        context.dataStore.edit { preferences ->
            preferences[STEPS_GOAL] = stepsGoal
        }
    }

    suspend fun saveTimeGoal(timeGoal: LocalTime) {
        context.dataStore.edit { preferences ->
            preferences[TIME_GOAL] = timeGoal.toString()
        }
    }
}