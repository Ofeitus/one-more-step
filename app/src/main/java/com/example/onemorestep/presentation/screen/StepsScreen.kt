package com.example.onemorestep.presentation.screen

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.onemorestep.R
import com.example.onemorestep.data.tempoFromNanosToHours
import com.example.onemorestep.data.timeFormat
import com.example.onemorestep.presentation.dialog.StepsTargetDialog
import com.example.onemorestep.presentation.dialog.TargetTimeDialog
import com.example.onemorestep.presentation.viewmodel.StepsViewModel
import com.example.onemorestep.ui.theme.CustomBlue
import com.example.onemorestep.ui.theme.CustomGreen
import com.example.onemorestep.ui.theme.CustomRed
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
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
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
            .padding(8.dp)
    ) {
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Column(
                modifier = Modifier.fillMaxSize().weight(3f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StepsCountComponent(viewModel, modifier = Modifier.weight(0.5f))
                TempoStatsComponent(viewModel, modifier = Modifier.weight(0.5f))
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize().weight(3f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StepsCountComponent(viewModel, modifier = Modifier.weight(0.5f))
                TempoStatsComponent(viewModel, modifier = Modifier.weight(0.5f))
            }
        }

        TempoChartComponent(viewModel, modifier = Modifier.fillMaxSize().weight(2f))
    }
}

@Composable
fun StepsCountComponent(viewModel: StepsViewModel, modifier: Modifier = Modifier) {
    val stepsCount = viewModel.stepsCount

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(0.6f),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                stepsCount?.toString() ?: "-",
                modifier = Modifier.padding(12.dp, 0.dp),
                textAlign = TextAlign.Left,
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(0.4f),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Target steps", color = MaterialTheme.colorScheme.secondary)
                Text(
                    "/${viewModel.stepsTarget.collectAsStateWithLifecycle().value ?: "-"}",
                    autoSize = TextAutoSize.StepBased(),
                    maxLines = 1
                )
            }
            IconButton(
                onClick = { viewModel.openTargetDialog() },
                content = {
                    Icon(
                        painterResource(R.drawable.vscode_codicons_edit),
                        contentDescription = null
                    )
                }
            )
            Column(modifier = Modifier.padding(12.dp, 0.dp, 0.dp, 0.dp).weight(1f)) {
                Text("Target time", color = MaterialTheme.colorScheme.secondary)
                Text(
                    viewModel.targetTime.collectAsStateWithLifecycle().value?.format(timeFormat) ?: "-",
                    autoSize = TextAutoSize.StepBased(),
                    maxLines = 1
                )
            }
            IconButton(
                onClick = { viewModel.openTargetTimeDialog() },
                content = {
                    Icon(
                        painterResource(R.drawable.vscode_codicons_edit),
                        contentDescription = null
                    )
                }
            )
            StepsTargetDialog(viewModel)
            TargetTimeDialog(viewModel)
        }
    }
}

@Composable
fun TempoStatsComponent(viewModel: StepsViewModel, modifier: Modifier = Modifier) {
    val currentTempo = viewModel.currentTempo
    val targetTempo = viewModel.targetTempo
    val avgTempo = viewModel.avgTempo
    val currentTempoEstimatedGoalTime = viewModel.currentTempoEstimatedGoalTime
    val targetTime = viewModel.targetTime
    val avgTempoEstimatedGoalTime = viewModel.avgTempoEstimatedGoalTime

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.weight(0.6f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("", modifier = Modifier.weight(1f))
            Text(
                "Tempo",
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                "Estimate",
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Current",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(100))
                    .background(CustomBlue)
                    .padding(8.dp, 0.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .weight(1f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
            Text(
                if (currentTempo != null) tempoFromNanosToHours(currentTempo).toString() else "-",
                modifier = Modifier.weight(1f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
            Text(
                currentTempoEstimatedGoalTime?.format(timeFormat) ?: "-",
                modifier = Modifier.weight(1f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Target",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(100))
                    .background(CustomGreen)
                    .padding(8.dp, 0.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .weight(1f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
            Text(
                if (targetTempo != null) tempoFromNanosToHours(targetTempo).toString() else "-",
                modifier = Modifier.weight(1f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
            Text(
                targetTime.collectAsStateWithLifecycle().value?.format(timeFormat) ?: "-",
                modifier = Modifier.weight(1f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Average",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(100))
                    .background(CustomRed)
                    .padding(8.dp, 0.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .weight(1f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
            Text(
                if (avgTempo != null) tempoFromNanosToHours(avgTempo).toString() else "-",
                modifier = Modifier.weight(1f),
                autoSize = TextAutoSize.StepBased(),
                maxLines = 1
            )
            Text(
                avgTempoEstimatedGoalTime?.format(timeFormat) ?: "-",
                modifier = Modifier.weight(1f),
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
        Fill(MaterialTheme.colorScheme.primary),
        2.dp,
        emptyList(),
    )

    CartesianChartHost(
        rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = remember { LineCartesianLayer.LineFill.single(Fill(CustomGreen)) },
                        interpolator = LineCartesianLayer.Interpolator.catmullRom(0f)
                    ),
                    LineCartesianLayer.rememberLine(
                        fill = remember { LineCartesianLayer.LineFill.single(Fill(CustomRed)) },
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
            marker = rememberDefaultCartesianMarker(
                rememberTextComponent(
                    margins = Insets(0.dp, 5.dp)
                ),
                indicator = { _ -> indicatorShape },
                indicatorSize = 10.dp
            ),
            markerController = CartesianMarkerController.rememberToggleOnTap(),
            getXStep = { _, _, _ -> 5.0 * 60 * 1000 }
        ),
        remember { viewModel.chartModelProducer },
        scrollState = rememberVicoScrollState(initialScroll = Scroll.Absolute.End),
        zoomState = rememberVicoZoomState(initialZoom = Zoom.fixed(0.66f)),
        animationSpec = null,
        animateIn = false,
        modifier = modifier
    )
}