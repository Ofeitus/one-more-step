package com.example.onemorestep.data

import android.content.Context
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

// The minimum android level that can use Health Connect
const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1

class HealthConnectManager(private val context: Context) {
  private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

  var availability = mutableStateOf(HealthConnectAvailability.NOT_SUPPORTED)
    private set

  init {
    checkAvailability()
  }

  fun checkAvailability() {
    availability.value = when {
      HealthConnectClient.getSdkStatus(context) == SDK_AVAILABLE -> HealthConnectAvailability.INSTALLED
      isSupported() -> HealthConnectAvailability.NOT_INSTALLED
      else -> HealthConnectAvailability.NOT_SUPPORTED
    }
  }

  /**
   * Determines whether all the specified permissions are already granted. It is recommended to
   * call [PermissionController.getGrantedPermissions] first in the permissions flow, as if the
   * permissions are already granted then there is no need to request permissions via
   * [PermissionController.createRequestPermissionResultContract].
   */
  suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
    return healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
  }

  fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
    return PermissionController.createRequestPermissionResultContract()
  }

  suspend fun readSteps(start: Instant, end: Instant): List<StepsRecord> {
    val request = ReadRecordsRequest(
      recordType = StepsRecord::class,
      timeRangeFilter = TimeRangeFilter.between(start, end)
    )
    val response = healthConnectClient.readRecords(request)
    return response.records
  }

  private fun isSupported() = Build.VERSION.SDK_INT >= MIN_SUPPORTED_SDK
}

/**
 * Health Connect requires that the underlying Health Connect APK is installed on the device.
 * [HealthConnectAvailability] represents whether this APK is indeed installed, whether it is not
 * installed but supported on the device, or whether the device is not supported (based on Android
 * version).
 */
enum class HealthConnectAvailability {
  INSTALLED,
  NOT_INSTALLED,
  NOT_SUPPORTED
}
