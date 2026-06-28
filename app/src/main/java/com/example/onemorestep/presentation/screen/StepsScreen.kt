package com.example.onemorestep.presentation.screen

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import com.example.onemorestep.R
import com.example.onemorestep.data.tempoFromNanosToHours
import com.example.onemorestep.ui.theme.CustomBlue
import com.example.onemorestep.ui.theme.CustomBlueTransparent
import com.example.onemorestep.ui.theme.CustomGreenTransparent
import com.example.onemorestep.ui.theme.CustomRedTransparent
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val timeFormat: DateTimeFormatter? = DateTimeFormatter.ofPattern("H:mm")

@Composable
fun StepsScreen(viewModel: StepsViewModel) {

    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) {}

    LaunchedEffect(Unit) {
        permissionLauncher.launch(viewModel.permissions)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(12.dp)
    ) {
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Column(modifier = Modifier.fillMaxSize().weight(0.6f)) {
                StepsCountComponent(viewModel, modifier = Modifier.weight(0.5f))
                TempoStatsComponent(viewModel, modifier = Modifier.weight(0.5f))
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize().weight(0.6f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StepsCountComponent(viewModel, modifier = Modifier.weight(0.5f))
                TempoStatsComponent(viewModel, modifier = Modifier.weight(0.5f))
            }
        }

        TempoChartComponent(viewModel, modifier = Modifier.fillMaxSize().weight(0.4f))
    }
}

@Composable
fun StepsCountComponent(viewModel: StepsViewModel, modifier: Modifier = Modifier) {
    val stepsCount = viewModel.stepsCount
    val goal = viewModel.goal

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(0.6f),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Icon(
                painterResource(R.drawable.material_symbols_steps),
                modifier = Modifier.fillMaxSize().weight(0.15f),
                contentDescription = null
            )
            Text(
                stepsCount?.toString() ?: "-",
                modifier = Modifier.padding(12.dp, 0.dp, 0.dp, 0.dp).weight(0.85f),
                textAlign = TextAlign.Center,
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
        }
        Text(
            "/$goal",
            modifier = Modifier.weight(0.4f),
            autoSize = TextAutoSize.StepBased(),
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1
        )
    }
}

@Composable
fun TempoStatsComponent(viewModel: StepsViewModel, modifier: Modifier = Modifier) {
    val targetTempo = viewModel.targetTempo
    val lastTempo = viewModel.lastTempo
    val avgTempo = viewModel.avgTempo
    val targetTempoEstimatedGoalTime = viewModel.targetTempoEstimatedGoalTime
    val lastTempoEstimatedGoalTime = viewModel.lastTempoEstimatedGoalTime
    val avgTempoEstimatedGoalTime = viewModel.avgTempoEstimatedGoalTime

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().weight(1f),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(R.drawable.material_icons_speed),
                modifier = Modifier.fillMaxSize().weight(0.1f),
                contentDescription = null
            )
            Text(
                "Tempo",
                modifier = Modifier.padding(6.dp, 0.dp).weight(0.4f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
            Icon(
                painterResource(R.drawable.lucide_timer),
                modifier = Modifier.fillMaxSize().weight(0.1f),
                contentDescription = null
            )
            Text(
                "Estimated time to reach goal",
                modifier = Modifier.padding(6.dp, 0.dp).weight(0.4f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 2
            )
        }
        Row(modifier = Modifier.fillMaxSize().weight(0.4f)) {
            Text(
                "Target",
                modifier = Modifier.padding(6.dp, 0.dp),
                color = MaterialTheme.colorScheme.secondary,
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(CustomGreenTransparent)
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (targetTempo != null) tempoFromNanosToHours(targetTempo).toString() else "-",
                modifier = Modifier.padding(6.dp, 0.dp).weight(0.5f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
            Text(
                targetTempoEstimatedGoalTime?.format(timeFormat) ?: "∞",
                modifier = Modifier.padding(6.dp, 0.dp).weight(0.5f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
        }
        Row(modifier = Modifier.fillMaxSize().weight(0.4f)) {
            Text(
                "Current",
                modifier = Modifier.padding(6.dp, 0.dp),
                color = MaterialTheme.colorScheme.secondary,
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(CustomBlueTransparent)
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (lastTempo != null) tempoFromNanosToHours(lastTempo).toString() else "-",
                modifier = Modifier.padding(6.dp, 0.dp).weight(0.5f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
            Text(
                lastTempoEstimatedGoalTime?.format(timeFormat) ?: "∞",
                modifier = Modifier.padding(6.dp, 0.dp).weight(0.5f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
        }
        Row(modifier = Modifier.fillMaxSize().weight(0.4f)) {
            Text(
                "Average",
                modifier = Modifier.padding(6.dp, 0.dp),
                color = MaterialTheme.colorScheme.secondary,
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(CustomRedTransparent)
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (avgTempo != null) tempoFromNanosToHours(avgTempo).toString() else "-",
                modifier = Modifier.padding(6.dp, 0.dp).weight(0.5f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
            Text(
                avgTempoEstimatedGoalTime?.format(timeFormat) ?: "∞",
                modifier = Modifier.padding(6.dp, 0.dp).weight(0.5f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
        }
    }
}

@Composable
fun TempoChartComponent(viewModel: StepsViewModel, modifier: Modifier = Modifier) {
    val indicatorShape = ShapeComponent(
        Fill(MaterialTheme.colorScheme.background),
        RoundedCornerShape(100),
        Insets.Zero,
        Fill(CustomBlue),
        2.dp,
        emptyList(),
    )

    CartesianChartHost(
        rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = remember { LineCartesianLayer.LineFill.single(Fill(CustomBlue)) },
                        interpolator = LineCartesianLayer.Interpolator.catmullRom(0f)
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = CartesianValueFormatter { _, x, _ ->
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(x.toLong()), ZoneId.systemDefault())
                        .format(timeFormat)
                }
            ),
            markerController = CartesianMarkerController.rememberToggleOnTap(),
            marker = rememberDefaultCartesianMarker(
                rememberTextComponent(
                    margins = Insets(0.dp, 5.dp)
                ),
                indicator = { _ -> indicatorShape },
                indicatorSize = 10.dp
            )
        ),
        remember { viewModel.chartModelProducer },
        zoomState = rememberVicoZoomState(initialZoom = Zoom.Content),
        modifier = modifier
    )
}