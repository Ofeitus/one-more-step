package com.ofeitus.onemorestep.presentation.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults.iconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.RichTooltipColors
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.ofeitus.onemorestep.R
import com.ofeitus.onemorestep.ui.theme.DarkGray
import com.ofeitus.onemorestep.ui.theme.LightGray
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextWithTooltip(
    text: String,
    style: TextStyle,
    color: Color,
    tooltip: @Composable () -> Unit,
) {
    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text,
            style = style,
            color = color,
            modifier = Modifier.padding(end = 8.dp)
        )
        TooltipBox(
            positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
            tooltip = {
                RichTooltip(
                    colors = RichTooltipColors(
                        containerColor = if (isSystemInDarkTheme()) DarkGray else LightGray,
                        contentColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        actionContentColor = MaterialTheme.colorScheme.primary
                    ),
                    text = tooltip
                )
            },
            state = tooltipState
        ) {
            IconButton(
                modifier = Modifier.size(20.dp),
                onClick = { scope.launch { tooltipState.show() } },
                colors = iconButtonColors(
                    contentColor = color
                ),
                content = {
                    Icon(
                        painterResource(R.drawable.vscode_codicons_info),
                        contentDescription = null
                    )
                }
            )
        }
    }
}