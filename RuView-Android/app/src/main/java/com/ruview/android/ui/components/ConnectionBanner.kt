package com.ruview.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruview.android.data.models.ConnectionStatus
import com.ruview.android.data.websocket.WsState
import com.ruview.android.ui.theme.StatusGreen
import com.ruview.android.ui.theme.StatusRed
import com.ruview.android.ui.theme.StatusYellow

@Composable
fun ConnectionBanner(
    wsState: WsState,
    isSimulated: Boolean,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusText) = when {
        wsState == WsState.CONNECTED && !isSimulated -> StatusGreen to "LIVE · ESP32"
        wsState == WsState.CONNECTING    -> StatusYellow to "Connecting..."
        wsState == WsState.RECONNECTING  -> StatusYellow to "Reconnecting..."
        wsState == WsState.SIMULATED     -> StatusRed to "Simulated Data"
        else                             -> Color.Gray to "Disconnected"
    }

    val animatedColor by animateColorAsState(
        targetValue = statusColor,
        animationSpec = tween(500),
        label = "statusColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0F1A))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "RuView",
            color = Color(0xFF00FFFF),
            fontSize = 14.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = null,
                tint = animatedColor,
                modifier = Modifier.size(8.dp)
            )
            Text(
                text = statusText,
                color = animatedColor,
                fontSize = 11.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}
