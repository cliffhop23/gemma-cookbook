package com.ruview.android.ui.screens.mat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruview.android.data.models.AlertPriority
import com.ruview.android.data.models.MatAlert
import com.ruview.android.data.models.MatResponse
import com.ruview.android.ui.components.ConnectionBanner
import com.ruview.android.ui.theme.Background
import com.ruview.android.ui.theme.CardBackground
import com.ruview.android.ui.theme.CardBorder
import com.ruview.android.ui.theme.Primary
import com.ruview.android.ui.theme.TextPrimary
import com.ruview.android.ui.theme.TextSecondary
import com.ruview.android.ui.theme.TriageDelayed
import com.ruview.android.ui.theme.TriageExpectant
import com.ruview.android.ui.theme.TriageImmediate
import com.ruview.android.ui.theme.TriageMinimal
import com.ruview.android.viewmodel.RuViewViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MatScreen(viewModel: RuViewViewModel = hiltViewModel()) {
    val matData by viewModel.matData.collectAsState()
    val wsState by viewModel.wsState.collectAsState()
    val frame by viewModel.sensingFrame.collectAsState()

    LaunchedEffect(Unit) { viewModel.refreshMat() }

    // Use live detected count if REST is empty
    val totalDetected = if (matData.totalDetected > 0) matData.totalDetected
    else frame.persons.size

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = TriageImmediate, modifier = Modifier.size(20.dp))
                Text(
                    "MASS CASUALTY TRIAGE",
                    color = TriageImmediate,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
            IconButton(onClick = { viewModel.refreshMat() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Primary)
            }
        }

        // Total count
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(CardBackground, RoundedCornerShape(12.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$totalDetected",
                    color = TextPrimary,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "TOTAL DETECTED",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // START triage counts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TriageCount("IMMEDIATE", matData.immediateCount, TriageImmediate, modifier = Modifier.weight(1f))
            TriageCount("DELAYED", matData.delayedCount, TriageDelayed, modifier = Modifier.weight(1f))
            TriageCount("MINIMAL", matData.minimalCount, TriageMinimal, modifier = Modifier.weight(1f))
            TriageCount("EXPECT.", matData.expectantCount, TriageExpectant, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("START TRIAGE PROTOCOL", color = TextSecondary, fontSize = 10.sp, letterSpacing = 1.sp, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(8.dp))
            TriageLegendRow(TriageImmediate, "IMMEDIATE", "Life-threatening — act now")
            TriageLegendRow(TriageDelayed, "DELAYED", "Serious but stable")
            TriageLegendRow(TriageMinimal, "MINIMAL", "Minor injuries")
            TriageLegendRow(TriageExpectant, "EXPECTANT", "Critical — expect mortality")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Alerts
        if (matData.alerts.isNotEmpty()) {
            Text(
                "ACTIVE ALERTS",
                color = TextSecondary,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            matData.alerts.forEach { alert ->
                AlertRow(alert = alert, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }
        } else {
            // Demo alert
            val demoAlert = MatAlert(
                id = "demo",
                zone = "Zone A",
                priority = AlertPriority.IMMEDIATE,
                message = "2 persons detected, no movement detected for 30s",
                timestamp = System.currentTimeMillis() - 30_000
            )
            Text(
                "ACTIVE ALERTS",
                color = TextSecondary,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            AlertRow(alert = demoAlert, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TriageCount(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(CardBackground, RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$count",
            color = color,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            color = color.copy(alpha = 0.7f),
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun TriageLegendRow(color: Color, label: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Column {
            Text(label, color = color, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text(description, color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun AlertRow(alert: MatAlert, modifier: Modifier = Modifier) {
    val color = when (alert.priority) {
        AlertPriority.IMMEDIATE -> TriageImmediate
        AlertPriority.DELAYED -> TriageDelayed
        AlertPriority.MINIMAL -> TriageMinimal
        AlertPriority.EXPECTANT -> TriageExpectant
    }
    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        .format(Date(alert.timestamp))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(alert.priority.name, color = color, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("·", color = TextSecondary, fontSize = 10.sp)
                Text(alert.zone, color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Text(alert.message, color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
        Text(timeStr, color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}
