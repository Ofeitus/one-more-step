package com.example.onemorestep.data

import androidx.health.connect.client.records.StepsRecord
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

fun stepsDurationNanos(record: StepsRecord): Long {
    return Duration.between(record.startTime.atZone(record.startZoneOffset), record.endTime.atZone(record.endZoneOffset)).toNanos()
}

fun tempoFromNanosToHours(tempo: Double): Long {
    return (tempo * 3600000000000).toLong()
}

fun localEndTime(record: StepsRecord): LocalDateTime {
    return record.endTime.atZone(record.endZoneOffset).toLocalDateTime()
}