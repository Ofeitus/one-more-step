package com.ofeitus.onemorestep.presentation.screen

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ofeitus.onemorestep.R
import com.ofeitus.onemorestep.data.dateTimeFormat
import com.ofeitus.onemorestep.data.tempoFromNanosToHours
import com.ofeitus.onemorestep.data.timeFormat
import com.ofeitus.onemorestep.presentation.dialog.StepsTargetDialog
import com.ofeitus.onemorestep.presentation.dialog.TargetTimeDialog
import com.ofeitus.onemorestep.presentation.viewmodel.StepsViewModel
import com.ofeitus.onemorestep.ui.theme.CustomGreen
import com.ofeitus.onemorestep.ui.theme.CustomRed
import com.patrykandpatrick.vico.compose.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalLine
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
import com.patrykandpatrick.vico.compose.common.Position
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun StepsScreen(viewModel: StepsViewModel) {

    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) {}

    LaunchedEffect(Unit) {
        permissionLauncher.launch(viewModel.permissions)
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatsComponent(viewModel, modifier = Modifier.weight(1f))
                ChartComponent(viewModel, modifier = Modifier.fillMaxSize().weight(1f))
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatsComponent(viewModel, modifier = Modifier.weight(1f))
                ChartComponent(viewModel, modifier = Modifier.fillMaxSize().weight(1f))
            }
        }
    }
}

@Composable
fun StatsComponent(viewModel: StepsViewModel, modifier: Modifier = Modifier) {
    val stepsCount = viewModel.stepsCount
    val stepsTarget = viewModel.stepsTarget
    val targetTime = viewModel.targetTime
    val currentTempo = viewModel.currentTempo
    val avgTempoEstimatedGoalTime = viewModel.avgTempoEstimatedGoalTime

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                Text(
                    "Target steps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    "/${stepsTarget.collectAsStateWithLifecycle().value ?: "-"}",
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
                Text(
                    "Target time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    targetTime.collectAsStateWithLifecycle().value?.format(timeFormat) ?: "-",
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
        Row(
            modifier = Modifier.fillMaxWidth().weight(0.4f),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Current tempo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    if (currentTempo != null) tempoFromNanosToHours(currentTempo).toString() else "-",
                    autoSize = TextAutoSize.StepBased(),
                    maxLines = 1
                )
            }
            Column(modifier = Modifier.padding(12.dp, 0.dp, 0.dp, 0.dp).weight(1f)) {
                Text(
                    "Estimate time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    avgTempoEstimatedGoalTime?.format(
                        if (avgTempoEstimatedGoalTime.toLocalDate().isAfter(viewModel.currentDate))
                            dateTimeFormat
                        else
                            timeFormat
                    ) ?: "-",
                    autoSize = TextAutoSize.StepBased(),
                )
            }
        }
    }
}

@Composable
fun ChartComponent(viewModel: StepsViewModel, modifier: Modifier = Modifier) {
    val targetTempo = viewModel.targetTempo

    val indicatorShape = ShapeComponent(
        Fill(MaterialTheme.colorScheme.background),
        RoundedCornerShape(100),
        Insets.Zero,
        Fill(MaterialTheme.colorScheme.primary),
        2.dp,
        emptyList(),
    )
    val horizontalLine = HorizontalLine(
        y = { tempoFromNanosToHours(targetTempo ?: 0.0).toDouble() },
        line = rememberLineComponent(fill = Fill(CustomGreen), thickness = 2.dp),
        labelComponent = rememberTextComponent(
            margins = Insets(8.dp),
            padding = Insets(8.dp, 2.dp),
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary),
            background = rememberShapeComponent(Fill(CustomGreen), RoundedCornerShape(100)),
        ),
        label = { "Target" },
        verticalLabelPosition = Position.Vertical.Bottom
    )

    Column(modifier = modifier) {
        Text(
            "Average tempo",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
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
                            areaFill =
                                LineCartesianLayer.AreaFill.single(
                                    Fill(
                                        Brush.verticalGradient(listOf(CustomRed.copy(alpha = 0.4f), Color.Transparent))
                                    )
                                ),
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
                getXStep = { _, _, _ -> 5.0 * 60 * 1000 },
                decorations = listOf(horizontalLine)
            ),
            remember { viewModel.chartModelProducer },
            scrollState = rememberVicoScrollState(initialScroll = Scroll.Absolute.End, autoScrollCondition = AutoScrollCondition.OnModelGrowth),
            zoomState = rememberVicoZoomState(initialZoom = Zoom.fixed(0.66f)),
            animationSpec = null,
            animateIn = false,
            modifier = Modifier.fillMaxSize()
        )
    }

}