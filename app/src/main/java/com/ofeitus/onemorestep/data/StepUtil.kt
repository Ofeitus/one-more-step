package com.ofeitus.onemorestep.data

import androidx.health.connect.client.records.StepsRecord
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val dateTimeFormat: DateTimeFormatter? = DateTimeFormatter.ofPattern("dd.MM.yyyy'\n'H:mm")
val timeFormat: DateTimeFormatter? = DateTimeFormatter.ofPattern("H:mm")

fun stepsDurationNanos(record: StepsRecord): Long {
    return Duration.between(record.startTime.atZone(record.startZoneOffset), record.endTime.atZone(record.endZoneOffset)).toNanos()
}

fun tempoFromNanosToHours(tempo: Double): Long {
    return (tempo * 3600000000000).toLong()
}

fun localEndTime(record: StepsRecord): LocalDateTime {
    return record.endTime.atZone(record.endZoneOffset).toLocalDateTime()
}