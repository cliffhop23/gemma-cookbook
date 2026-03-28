package com.ruview.android.ui.screens.zones

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruview.android.data.models.Zone
import com.ruview.android.ui.components.ConnectionBanner
import com.ruview.android.ui.theme.Background
import com.ruview.android.ui.theme.CardBackground
import com.ruview.android.ui.theme.CardBorder
import com.ruview.android.ui.theme.Primary
import com.ruview.android.ui.theme.StatusGreen
import com.ruview.android.ui.theme.TextPrimary
import com.ruview.android.ui.theme.TextSecondary
import com.ruview.android.viewmodel.RuViewViewModel

@Composable
fun ZonesScreen(viewModel: RuViewViewModel = hiltViewModel()) {
    val zones by viewModel.zones.collectAsState()
    val wsState by viewModel.wsState.collectAsState()
    val frame by viewModel.sensingFrame.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshZones()
    }

    // Use simulated zones if REST returns empty
    val displayZones = zones.ifEmpty { defaultZones() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        ConnectionBanner(wsState = wsState, isSimulated = frame.simulated)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ZONE DETECTION",
                color = Primary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            IconButton(onClick = { viewModel.refreshZones() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Primary)
            }
        }

        // Floor plan SVG-style view
        FloorPlanView(
            zones = displayZones,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(1.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Zone list
        Text(
            text = "ZONE STATUS",
            color = TextSecondary,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        displayZones.forEach { zone ->
            ZoneRow(zone = zone, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendItem(color = StatusGreen, label = "Occupied")
            LegendItem(color = CardBorder, label = "Empty")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FloorPlanView(zones: List<Zone>, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
    ) {
        // Render each zone as a colored rectangle
        zones.forEach { zone ->
            val color = if (zone.occupied) StatusGreen else Color(0xFF1F2937)
            val alpha = if (zone.occupied) {
                (0.2f + 0.5f * (zone.personCount.toFloat() / 5f)).coerceIn(0.2f, 0.7f)
            } else 0.3f

            Box(
                modifier = Modifier
                    .fillMaxSize(zone.width)
                    .aspectRatio(zone.width / zone.height.coerceAtLeast(0.01f))
                    .align(Alignment.TopStart)
                    .padding(
                        start = (zone.x * 100).dp,
                        top = (zone.y * 100).dp
                    )
                    .background(color.copy(alpha = alpha), RoundedCornerShape(4.dp))
                    .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            )
        }

        // Label for each zone
        zones.forEach { zone ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = (zone.x * 200).dp,
                        top = (zone.y * 150).dp
                    )
            ) {
                Column {
                    Text(
                        text = zone.name,
                        color = if (zone.occupied) StatusGreen else TextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (zone.occupied) {
                        Text(
                            text = "${zone.personCount}p",
                            color = StatusGreen,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoneRow(zone: Zone, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(8.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (zone.occupied) StatusGreen else CardBorder, CircleShape)
            )
            Text(zone.name, color = TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (zone.occupied) {
                Text(
                    "${zone.personCount} person${if (zone.personCount != 1) "s" else ""}",
                    color = StatusGreen,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                if (zone.occupied) "OCCUPIED" else "EMPTY",
                color = if (zone.occupied) StatusGreen else TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Text(label, color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}

private fun defaultZones(): List<Zone> = listOf(
    Zone("zone_a", "Zone A", occupied = true, personCount = 2, x = 0.05f, y = 0.05f, width = 0.4f, height = 0.4f),
    Zone("zone_b", "Zone B", occupied = false, personCount = 0, x = 0.55f, y = 0.05f, width = 0.4f, height = 0.4f),
    Zone("zone_c", "Zone C", occupied = true, personCount = 1, x = 0.05f, y = 0.55f, width = 0.4f, height = 0.4f),
    Zone("zone_d", "Zone D", occupied = false, personCount = 0, x = 0.55f, y = 0.55f, width = 0.4f, height = 0.4f)
)
