package com.example.onemorestep.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.onemorestep.data.timeFormat
import com.example.onemorestep.presentation.viewmodel.StepsViewModel
import java.time.Instant
import java.time.ZoneOffset

@Composable
fun HistoryScreen(viewModel: StepsViewModel) {
    val itemList by viewModel.stepsRecords.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
    ) {
        stickyHeader {
            Row (
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Steps count",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        itemsIndexed(
            items = itemList,
            key = { _, item -> item.metadata.id }
        ) { index, item ->
            CardItem(item)
            if (index < itemList.size - 1) {
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun CardItem(record: StepsRecord) {
    Row (
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${
                record.startTime.atZone(record.startZoneOffset).toLocalDateTime().format(timeFormat)
            } - ${
                record.endTime.atZone(record.endZoneOffset).toLocalDateTime().format(timeFormat)
            }",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${record.count}",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
@Preview
fun CardItemPreview() {
    CardItem(StepsRecord(Instant.now(), ZoneOffset.UTC, Instant.now().plusSeconds(1000), ZoneOffset.UTC, 156L,
        Metadata.activelyRecorded(Device(1, "", ""))))
}