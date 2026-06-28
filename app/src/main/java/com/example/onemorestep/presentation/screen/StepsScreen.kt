package com.example.onemorestep.presentation.screen

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import com.example.onemorestep.R
import com.example.onemorestep.data.tempoFromNanosToHours
import com.example.onemorestep.ui.theme.CustomGreen
import com.example.onemorestep.ui.theme.PureWhite
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
                painterResource(R.drawable.lucide_footprints),
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
    val avgTempo = viewModel.avgTempo
    val lastTempo = viewModel.lastTempo
    val avgTempoEstimatedGoalTime = viewModel.avgTempoEstimatedGoalTime
    val lastTempoEstimatedGoalTime = viewModel.lastTempoEstimatedGoalTime

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxSize().weight(0.5f),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(R.drawable.lucide_sport_shoe),
                modifier = Modifier.weight(0.1f),
                contentDescription = null
            )
            Text(
                if (avgTempo != null) tempoFromNanosToHours(avgTempo).toString() else "-",
                modifier = Modifier.padding(12.dp, 0.dp).weight(0.4f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
            Icon(
                painterResource(R.drawable.lucide_timer),
                modifier = Modifier.weight(0.1f),
                contentDescription = null
            )
            Text(
                avgTempoEstimatedGoalTime?.format(timeFormat) ?: "∞",
                modifier = Modifier.padding(12.dp, 0.dp).weight(0.4f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
        }
        Row(
            modifier = Modifier.fillMaxSize().weight(0.5f),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(R.drawable.lucide_sport_shoe),
                modifier = Modifier.weight(0.1f),
                contentDescription = null
            )
            Text(
                if (lastTempo != null) tempoFromNanosToHours(lastTempo).toString() else "-",
                modifier = Modifier.padding(12.dp, 0.dp).weight(0.4f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
            Icon(
                painterResource(R.drawable.lucide_timer),
                modifier = Modifier.weight(0.1f),
                contentDescription = null
            )
            Text(
                lastTempoEstimatedGoalTime?.format(timeFormat) ?: "∞",
                modifier = Modifier.padding(12.dp, 0.dp).weight(0.4f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
        }
    }
}

@Composable
fun TempoChartComponent(viewModel: StepsViewModel, modifier: Modifier = Modifier) {
    CartesianChartHost(
        rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = remember { LineCartesianLayer.LineFill.single(Fill(CustomGreen)) },
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
                indicator = { _ -> ShapeComponent(
                    Fill(PureWhite),
                    RoundedCornerShape(100),
                    Insets.Zero,
                    Fill(CustomGreen),
                    2.dp,
                    emptyList(),
                ) },
                indicatorSize = 10.dp
            )
        ),
        remember { viewModel.chartModelProducer },
        zoomState = rememberVicoZoomState(initialZoom = Zoom.Content),
        modifier = modifier
    )
}