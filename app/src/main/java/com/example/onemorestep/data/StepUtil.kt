package com.example.onemorestep.data

import androidx.health.connect.client.records.StepsRecord
import java.time.Duration
import java.time.Instant

fun stepsDurationNanos(steps: StepsRecord): Long {
    return Duration.between(steps.startTime.atZone(steps.startZoneOffset), steps.endTime.atZone(steps.endZoneOffset)).toNanos()
}

fun tempoFromNanosToHours(tempo: Double): Long {
    return (tempo * 3600000000000).toLong()
}

fun stepsAvgTime(record: StepsRecord): Long {
    return Instant.ofEpochMilli((record.startTime.toEpochMilli() + record.endTime.toEpochMilli()) / 2).toEpochMilli()
}